package com.brawnybytes.docassistant.document;

import java.util.List;

public record UploadResponse(
        long documentId,
        String filename,
        String status,
        int pageCount,
        int chunkCount,
        List<ChunkPreview> chunks
) {
}