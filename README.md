# Document Assistant

RAG-based document Q&A. Upload a PDF, ask questions about it, get answers
grounded in the document with page-level citations.

## Why this exists

Built to replace a resume line that was listed before it was actually built.
Every layer below was built and tested manually, end to end.

## Architecture

Upload -> PDFBox text extraction (page-aware) -> chunking (overlap) ->
embeddings (Ollama, nomic-embed-text) -> Postgres/pgvector storage ->
retrieval (cosine similarity, HNSW index) -> LLM answer generation
(Ollama, qwen3:8b) with page citations.

## Stack

- Spring Boot 3 / Java 17
- Plain JDBC (JdbcTemplate), no JPA - pgvector's similarity operators
  aren't something JPQL/Hibernate understands natively
- Postgres + pgvector for storage and similarity search
- Apache PDFBox for page-aware text extraction
- Ollama, fully local and free - nomic-embed-text (768-dim) for embeddings,
  qwen3:8b for answer generation. No API key, no external dependency.

## Endpoints

- `POST /api/documents` - upload a PDF, runs the full ingest pipeline
- `POST /api/documents/{id}/retrieve` - top-5 most similar chunks for a
  question (debug endpoint, no LLM call)
- `POST /api/documents/{id}/ask` - full RAG: retrieve + generate + cite

## Run locally

1. `docker compose up -d`
2. `ollama pull nomic-embed-text` (qwen3:8b assumed already installed)
3. `mvn spring-boot:run`

## Known limitations (Tier 1 scope, by design)

- One document at a time, no cross-document search yet
- No auth, no UI, no streaming
- Fixed-size chunking with overlap, not structure-aware
- Plain vector similarity only - no reranking or hybrid search