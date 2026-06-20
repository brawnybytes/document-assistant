package com.brawnybytes.docassistant.generation;

import com.brawnybytes.docassistant.retrieval.RetrievalService;
import com.brawnybytes.docassistant.retrieval.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private static final int SNIPPET_LENGTH = 150;

    private static final String SYSTEM_PROMPT = """
            You are a document assistant. Answer the user's question using ONLY
            the context provided below, which was extracted from a single document.
            Every claim you make must be grounded in that context - do not use
            outside knowledge and do not guess.
            When you use information from the context, cite the page number it
            came from like this: (page 3).
            If the answer is not contained in the context, say plainly that the
            document does not cover it. Do not make anything up.
            """;

    private final RetrievalService retrievalService;
    private final ChatClient chatClient;

    public AnswerResponse answer(long documentId, String question) {
        List<RetrievedChunk> chunks = retrievalService.retrieve(documentId, question);

        if (chunks.isEmpty()) {
            return new AnswerResponse(
                    "This document doesn't have any indexed content to search yet.",
                    List.of());
        }

        String userMessage = buildUserMessage(chunks, question);
        String rawAnswer = chatClient.chat(SYSTEM_PROMPT, userMessage);

        return new AnswerResponse(rawAnswer.trim(), buildSources(chunks));
    }

    private String buildUserMessage(List<RetrievedChunk> chunks, String question) {
        StringBuilder context = new StringBuilder();
        for (RetrievedChunk chunk : chunks) {
            context.append("[Page ").append(chunk.pageNumber()).append("]\n")
                    .append(chunk.content()).append("\n\n");
        }
        return "Context:\n" + context + "\nQuestion: " + question;
    }

    // Multiple retrieved chunks can land on the same page - only list each
    // page once in the sources, not once per chunk.
    private List<SourcePage> buildSources(List<RetrievedChunk> chunks) {
        Map<Integer, String> byPage = new LinkedHashMap<>();
        for (RetrievedChunk chunk : chunks) {
            byPage.putIfAbsent(chunk.pageNumber(), snippet(chunk.content()));
        }
        return byPage.entrySet().stream()
                .map(e -> new SourcePage(e.getKey(), e.getValue()))
                .toList();
    }

    private String snippet(String text) {
        String collapsed = text.replaceAll("\\s+", " ").trim();
        if (collapsed.length() <= SNIPPET_LENGTH) {
            return collapsed;
        }
        return collapsed.substring(0, SNIPPET_LENGTH) + "...";
    }
}