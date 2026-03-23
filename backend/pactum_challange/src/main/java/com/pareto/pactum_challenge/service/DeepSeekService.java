package com.pareto.pactum_challenge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeepSeekService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekService(@Value("${deepseek.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.deepseek.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public Flux<String> sendStreaming(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", messages,
                "stream", true
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> !chunk.equals("[DONE]"))
                .mapNotNull(this::extractContent);
    }

    public String send(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", messages,
                "stream", false
        );

        String response = webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractFullContent(response);
    }

    private String extractContent(String chunk) {
        try {
            return objectMapper.readTree(chunk)
                    .path("choices").path(0)
                    .path("delta").path("content")
                    .asText(null);
        } catch (Exception e) {
            log.debug("Could not parse chunk: {}", chunk);
            return null;
        }
    }

    private String extractFullContent(String response) {
        try {
            return objectMapper.readTree(response)
                    .path("choices").path(0)
                    .path("message").path("content")
                    .asText("I'm having trouble responding right now.");
        } catch (Exception e) {
            log.error("Could not parse DeepSeek response: {}", response, e);
            return "I'm having trouble responding right now.";
        }
    }
}
