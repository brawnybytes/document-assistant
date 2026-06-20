package com.brawnybytes.docassistant.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class EmbeddingClient {

    private final RestClient restClient;
    private final String model;

    public EmbeddingClient(@Value("${ollama.base-url}") String baseUrl,
                           @Value("${ollama.embedding-model}") String model) {
        this.restClient = RestClient.create(baseUrl);
        this.model = model;
    }

    // Sends every chunk's text in ONE request to Ollama's batch embedding
    // endpoint, instead of one HTTP call per chunk. Order of the returned
    // vectors matches the order of the input list.
    public List<float[]> embed(List<String> texts) {
        EmbedResponse response = restClient.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new EmbedRequest(model, texts))
                .retrieve()
                .body(EmbedResponse.class);

        return response.embeddings();
    }

    record EmbedRequest(String model, List<String> input) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EmbedResponse(List<float[]> embeddings) {
    }
}