package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.entity.ChatMessage;
import com.pareto.pactum_challenge.entity.NegotiationSession;
import com.pareto.pactum_challenge.entity.Negotiator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeepSeekMessageBuilder {

    private final PromptRegistry promptRegistry;

    public List<Map<String, String>> buildMessages(
            NegotiationSession session,
            PromptMode mode,
            List<ChatMessage> chatHistory,
            String latestUserMessage
    ) {
        List<Map<String, String>> messages = new ArrayList<>();

        String systemPrompt = promptRegistry.buildSystemPrompt(session, mode);
        messages.add(Map.of("role", "system", "content", systemPrompt));

        for (ChatMessage msg : chatHistory) {
            String role = msg.getSender() instanceof Negotiator ? "assistant" : "user";
            if (msg.getMessage() != null && !msg.getMessage().isBlank()) {
                messages.add(Map.of("role", role, "content", msg.getMessage()));
            }
        }

        messages.add(Map.of("role", "user", "content", latestUserMessage));

        return messages;
    }
}
