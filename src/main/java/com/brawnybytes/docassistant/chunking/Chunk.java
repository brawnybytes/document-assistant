package com.brawnybytes.docassistant.chunking;

public record Chunk(int pageNumber, int chunkIndex, String content) {
}