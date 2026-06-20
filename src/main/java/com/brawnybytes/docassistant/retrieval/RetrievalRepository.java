package com.brawnybytes.docassistant.retrieval;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RetrievalRepository {

    private final JdbcTemplate jdbcTemplate;

    // <=> is pgvector's cosine distance operator: 0 means pointing the same
    // direction (very similar), 2 means opposite. We order by it ascending
    // (closest match first) and convert it to a similarity score for display.
    public List<RetrievedChunk> findSimilarChunks(long documentId, float[] queryEmbedding, int topK) {
        PGvector vector = new PGvector(queryEmbedding);
        String sql = """
                SELECT page_number, content, embedding <=> ? AS distance
                FROM chunks
                WHERE document_id = ? AND embedding IS NOT NULL
                ORDER BY embedding <=> ?
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new RetrievedChunk(
                        rs.getInt("page_number"),
                        rs.getString("content"),
                        1.0 - rs.getDouble("distance")
                ),
                vector, documentId, vector, topK);
    }
}