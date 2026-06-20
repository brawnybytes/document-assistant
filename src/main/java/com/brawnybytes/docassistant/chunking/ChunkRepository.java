package com.brawnybytes.docassistant.chunking;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertAll(long documentId, List<Chunk> chunks) {
        String sql = "INSERT INTO chunks (document_id, page_number, chunk_index, content) VALUES (?, ?, ?, ?)";
        List<Object[]> batchArgs = chunks.stream()
                .map(c -> new Object[]{documentId, c.pageNumber(), c.chunkIndex(), c.content()})
                .toList();
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    // Finds chunks that still need a vector. Querying by "embedding IS NULL"
    // rather than just reusing the in-memory chunk list means this step
    // could be retried later on its own if the embedding call ever failed
    // partway through.
    public List<ChunkRow> findContentNeedingEmbedding(long documentId) {
        String sql = "SELECT id, content FROM chunks WHERE document_id = ? AND embedding IS NULL ORDER BY chunk_index";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new ChunkRow(rs.getLong("id"), rs.getString("content")),
                documentId);
    }

    public void updateEmbeddings(List<ChunkEmbedding> updates) {
        String sql = "UPDATE chunks SET embedding = ? WHERE id = ?";
        List<Object[]> batchArgs = new ArrayList<>();
        for (ChunkEmbedding u : updates) {
            batchArgs.add(new Object[]{new PGvector(u.embedding()), u.chunkId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}