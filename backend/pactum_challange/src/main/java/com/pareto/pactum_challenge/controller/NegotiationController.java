package com.pareto.pactum_challenge.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class NegotiationController {

    @MessageMapping("/session/{sessionId}/offer")
    @SendTo("/topic/session/{sessionId}")
    public Map<String, Object> handleOffer(@DestinationVariable Long sessionId, Map<String, Object> offer) {
        // TODO: wire to NegotiationService
        return offer;
    }

    @MessageMapping("/session/{sessionId}/message")
    @SendTo("/topic/session/{sessionId}")
    public Map<String, Object> handleMessage(@DestinationVariable Long sessionId, Map<String, Object> message) {
        // TODO: wire to chat service
        return message;
    }
}
