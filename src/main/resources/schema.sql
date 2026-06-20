CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS documents (
                                          id          BIGSERIAL PRIMARY KEY,
                                          filename    VARCHAR(255) NOT NULL,
                                          uploaded_at TIMESTAMP NOT NULL DEFAULT now(),
                                          page_count  INT,
                                          status      VARCHAR(20) NOT NULL DEFAULT 'PROCESSING'
                                    );

CREATE TABLE IF NOT EXISTS chunks  (
                                        id          BIGSERIAL PRIMARY KEY,
                                        document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                                        page_number INT NOT NULL,
                                        chunk_index INT NOT NULL,
                                        content     TEXT NOT NULL,
                                        embedding   VECTOR(768),
                                        created_at  TIMESTAMP NOT NULL DEFAULT now()
                                  );

CREATE INDEX IF NOT EXISTS chunks_embedding_idx ON chunks USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS chunks_document_id_idx ON chunks (document_id);

