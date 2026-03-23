package com.pareto.pactum_challenge.controller;

import com.pareto.pactum_challenge.dto.ChatResponse;
import com.pareto.pactum_challenge.dto.CreateSessionRequest;
import com.pareto.pactum_challenge.dto.OfferTermDto;
import com.pareto.pactum_challenge.entity.*;
import com.pareto.pactum_challenge.repository.ChatMessageRepository;
import com.pareto.pactum_challenge.repository.NegotiationSessionRepository;
import com.pareto.pactum_challenge.repository.NegotiatorRepository;
import com.pareto.pactum_challenge.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {

    private final UserService userService;
    private final NegotiatorRepository negotiatorRepository;
    private final NegotiationSessionRepository sessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/me")
    public User getOrCreateUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            User existing = userService.findById(userId);
            if (existing != null) {
                return existing;
            }
        }

        User user = userService.createGuest();
        session.setAttribute("userId", user.getId());
        return user;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public NegotiationSession createSession(@Valid @RequestBody CreateSessionRequest request, HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("No guest user found. Call /api/me first.");
        }

        User user = userService.findById(userId);
        Negotiator negotiator = negotiatorRepository.findById(request.negotiatorId())
                .orElseThrow(() -> new EntityNotFoundException("Negotiator not found"));

        // Reuse existing session if one exists for this user + negotiator
        return sessionRepository.findByUserIdAndNegotiatorId(user.getId(), negotiator.getId())
                .orElseGet(() -> {
                    NegotiationSession session = new NegotiationSession();
                    session.setUser(user);
                    session.setNegotiator(negotiator);
                    return sessionRepository.save(session);
                });
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatResponse> getMessages(@PathVariable Long sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);

        return messages.stream().map(msg -> {
            String sender = msg.getSender() instanceof Negotiator ? "bot" : "user";

            ChatResponse.OfferResponse offerResponse = null;
            if (msg.getOffer() != null && msg.getOffer().getOfferTerms() != null) {
                List<OfferTermDto> terms = msg.getOffer().getOfferTerms().stream()
                        .map(ot -> new OfferTermDto(
                                ot.getNegotiationTerm().getId(),
                                ot.getNegotiationTerm().getName(),
                                ot.getNegotiationTerm().getUnit(),
                                ot.getValue()
                        ))
                        .toList();
                offerResponse = new ChatResponse.OfferResponse(terms, msg.getOffer().getStatus());
            }

            return new ChatResponse(
                    "message",
                    sender,
                    msg.getMessage(),
                    msg.getCreatedAt().toEpochMilli(),
                    offerResponse
            );
        }).toList();
    }
}
