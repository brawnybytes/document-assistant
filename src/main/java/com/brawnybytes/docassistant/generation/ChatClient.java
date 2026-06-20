package com.brawnybytes.docassistant.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ChatClient {

    private final RestClient restClient;
    private final String model;

    public ChatClient(@Value("${ollama.base-url}") String baseUrl,
                      @Value("${ollama.chat-model}") String model) {
        this.restClient = RestClient.create(baseUrl);
        this.model = model;
    }

    // think=false skips qwen3's chain-of-thought output - we only want the
    // final answer text, and skipping the reasoning step is also faster.
    public String chat(String systemPrompt, String userMessage) {
        ChatRequest request = new ChatRequest(
                model,
                List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userMessage)
                ),
                false,
                false
        );

        ChatResponse response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        return response.message().content();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatMessage(String role, String content) {
    }

    record ChatRequest(String model, List<ChatMessage> messages, boolean stream, boolean think) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(ChatMessage message) {
    }
}