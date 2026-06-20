package com.brawnybytes.docassistant.retrieval;

import com.brawnybytes.docassistant.embedding.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalService {

    private static final int TOP_K = 5;

    private final EmbeddingClient embeddingClient;
    private final RetrievalRepository retrievalRepository;

    // Embeds the question with the SAME model used for the chunks. This has
    // to match - if the question and the chunks were embedded by different
    // models, "closeness" between their vectors wouldn't mean anything.
    public List<RetrievedChunk> retrieve(long documentId, String question) {
        List<float[]> vectors = embeddingClient.embed(List.of(question));
        float[] queryEmbedding = vectors.get(0);
        return retrievalRepository.findSimilarChunks(documentId, queryEmbedding, TOP_K);
    }
}