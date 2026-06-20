package com.brawnybytes.docassistant.retrieval;

public record RetrievedChunk(int pageNumber, String content, double similarityScore) {
}