package com.brawnybytes.docassistant.document;

import com.brawnybytes.docassistant.chunking.Chunk;
import com.brawnybytes.docassistant.chunking.ChunkEmbedding;
import com.brawnybytes.docassistant.chunking.ChunkRepository;
import com.brawnybytes.docassistant.chunking.ChunkRow;
import com.brawnybytes.docassistant.chunking.ChunkingService;
import com.brawnybytes.docassistant.embedding.EmbeddingClient;
import com.brawnybytes.docassistant.extraction.ExtractedPage;
import com.brawnybytes.docassistant.extraction.TextExtractionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final int PREVIEW_LENGTH = 150;

    private final DocumentRepository documentRepository;
    private final TextExtractionService textExtractionService;
    private final ChunkingService chunkingService;
    private final ChunkRepository chunkRepository;
    private final EmbeddingClient embeddingClient;

    public UploadResponse ingest(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();
        long documentId = documentRepository.insert(filename);
        log.info("Created document {} for file '{}'", documentId, filename);

        List<ExtractedPage> pages;
        try {
            pages = textExtractionService.extractPages(file.getBytes());
        } catch (IOException e) {
            documentRepository.markFailed(documentId);
            throw new DocumentProcessingException(
                    "Failed to extract text from '" + filename + "'", e);
        }

        documentRepository.markExtracted(documentId, pages.size());
        log.info("Extracted {} pages from document {}", pages.size(), documentId);

        List<Chunk> chunks = chunkingService.chunkDocument(pages);
        chunkRepository.insertAll(documentId, chunks);
        documentRepository.markChunked(documentId);
        log.info("Stored {} chunks for document {}", chunks.size(), documentId);

        embedChunks(documentId);
        documentRepository.markEmbedded(documentId);
        log.info("Embedded chunks for document {}", documentId);

        List<ChunkPreview> previews = chunks.stream()
                .map(c -> new ChunkPreview(c.pageNumber(), c.chunkIndex(), c.content().length(), preview(c.content())))
                .toList();

        return new UploadResponse(documentId, filename, "READY", pages.size(), chunks.size(), previews);
    }

    private void embedChunks(long documentId) {
        List<ChunkRow> rows = chunkRepository.findContentNeedingEmbedding(documentId);
        if (rows.isEmpty()) {
            return;
        }

        List<String> texts = rows.stream().map(ChunkRow::content).toList();
        List<float[]> vectors = embeddingClient.embed(texts);

        List<ChunkEmbedding> updates = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            updates.add(new ChunkEmbedding(rows.get(i).id(), vectors.get(i)));
        }
        chunkRepository.updateEmbeddings(updates);
    }

    private String preview(String text) {
        String collapsed = text.replaceAll("\\s+", " ").trim();
        if (collapsed.length() <= PREVIEW_LENGTH) {
            return collapsed;
        }
        return collapsed.substring(0, PREVIEW_LENGTH) + "...";
    }
}