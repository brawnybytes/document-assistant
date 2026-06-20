package com.brawnybytes.docassistant.document;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public long insert(String filename) {
        String sql = "INSERT INTO documents (filename) VALUES (?) RETURNING id";
        return jdbcTemplate.queryForObject(sql, Long.class, filename);
    }

    public void markExtracted(long documentId, int pageCount) {
        String sql = "UPDATE documents SET page_count = ?, status = 'EXTRACTED' WHERE id = ?";
        jdbcTemplate.update(sql, pageCount, documentId);
    }

    public void markFailed(long documentId) {
        jdbcTemplate.update("UPDATE documents SET status = 'FAILED' WHERE id = ?", documentId);
    }

    public void markChunked(long documentId) {
        jdbcTemplate.update("UPDATE documents SET status = 'CHUNKED' WHERE id = ?", documentId);
    }

    public void markEmbedded(long documentId) {
        jdbcTemplate.update("UPDATE documents SET status = 'READY' WHERE id = ?", documentId);
    }
}