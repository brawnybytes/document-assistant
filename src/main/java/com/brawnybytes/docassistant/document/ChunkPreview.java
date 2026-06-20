package com.brawnybytes.docassistant.document;

public record ChunkPreview(int pageNumber, int chunkIndex, int charCount, String preview) {
}