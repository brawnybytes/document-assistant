package com.brawnybytes.docassistant.document;

import com.brawnybytes.docassistant.generation.AnswerResponse;
import com.brawnybytes.docassistant.generation.AnswerService;
import com.brawnybytes.docassistant.retrieval.RetrievalService;
import com.brawnybytes.docassistant.retrieval.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final RetrievalService retrievalService;
    private final AnswerService answerService;

    @PostMapping("/api/documents")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = documentService.ingest(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/documents/{id}/retrieve")
    public ResponseEntity<List<RetrievedChunk>> retrieve(@PathVariable long id, @RequestBody AskRequest request) {
        List<RetrievedChunk> results = retrievalService.retrieve(id, request.question());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/api/documents/{id}/ask")
    public ResponseEntity<AnswerResponse> ask(@PathVariable long id, @RequestBody AskRequest request) {
        AnswerResponse response = answerService.answer(id, request.question());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadInput(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<Map<String, String>> handleProcessingError(DocumentProcessingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
    }
}