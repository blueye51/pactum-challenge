package com.pareto.pactum_challenge.controller;

import com.pareto.pactum_challenge.dto.ChatResponse;
import com.pareto.pactum_challenge.dto.CreateSessionRequest;
import com.pareto.pactum_challenge.dto.OfferTermDto;
import com.pareto.pactum_challenge.entity.*;
import com.pareto.pactum_challenge.repository.ChatMessageRepository;
import com.pareto.pactum_challenge.repository.NegotiationSessionRepository;
import com.pareto.pactum_challenge.repository.NegotiatorRepository;
import com.pareto.pactum_challenge.repository.OfferRepository;
import com.pareto.pactum_challenge.service.NegotiationDataService;
import com.pareto.pactum_challenge.service.ScoringService;
import com.pareto.pactum_challenge.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
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
    private final OfferRepository offerRepository;
    private final ScoringService scoringService;
    private final NegotiationDataService dataService;

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

        // Reuse existing ACTIVE session if one exists for this user + negotiator
        return sessionRepository.findByUserIdAndNegotiatorIdAndSessionStatus(user.getId(), negotiator.getId(), SessionStatus.ACTIVE)
                .orElseGet(() -> {
                    NegotiationSession session = new NegotiationSession();
                    session.setUser(user);
                    session.setNegotiator(negotiator);
                    NegotiationSession saved = sessionRepository.save(session);

                    // Save opening message with market context
                    if (negotiator.getMarketContext() != null && !negotiator.getMarketContext().isBlank()) {
                        String opening = "Hi! I'm %s. %s\n\nFeel free to ask me anything or make your first offer when you're ready."
                                .formatted(negotiator.getName(), negotiator.getMarketContext());

                        ChatMessage openingMsg = new ChatMessage();
                        openingMsg.setSession(saved);
                        openingMsg.setSender(negotiator);
                        openingMsg.setRecipient(user);
                        openingMsg.setMessage(opening);
                        chatMessageRepository.save(openingMsg);
                    }

                    return saved;
                });
    }

    @PostMapping("/sessions/{sessionId}/reset")
    @Transactional
    public NegotiationSession resetSession(@PathVariable Long sessionId) {
        NegotiationSession old = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        chatMessageRepository.deleteAllBySessionId(sessionId);
        offerRepository.deleteAllBySessionId(sessionId);
        sessionRepository.delete(old);

        Negotiator negotiator = old.getNegotiator();
        User user = old.getUser();

        NegotiationSession fresh = new NegotiationSession();
        fresh.setUser(user);
        fresh.setNegotiator(negotiator);
        NegotiationSession saved = sessionRepository.save(fresh);

        if (negotiator.getMarketContext() != null && !negotiator.getMarketContext().isBlank()) {
            String opening = "Hi! I'm %s. %s\n\nFeel free to ask me anything or make your first offer when you're ready."
                    .formatted(negotiator.getName(), negotiator.getMarketContext());
            ChatMessage openingMsg = new ChatMessage();
            openingMsg.setSession(saved);
            openingMsg.setSender(negotiator);
            openingMsg.setRecipient(user);
            openingMsg.setMessage(opening);
            chatMessageRepository.save(openingMsg);
        }

        return saved;
    }

    @PostMapping("/sessions/{sessionId}/accept")
    @Transactional
    public NegotiationSession acceptOffer(@PathVariable Long sessionId) {
        NegotiationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        if (session.getSessionStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }

        Offer latestBotOffer = offerRepository.findAllBySessionIdOrderByIdAsc(sessionId).stream()
                .filter(o -> o.getMadeBy() instanceof Negotiator)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No bot offer to accept"));

        latestBotOffer.setStatus(Status.ACCEPTED);
        offerRepository.save(latestBotOffer);

        List<NegotiatorTermPreference> prefs = dataService.getAllPreferencesByNegotiator(session.getNegotiator());
        double finalScore = scoringService.score(latestBotOffer, prefs);
        session.setScore(finalScore);
        session.setSessionStatus(SessionStatus.ACCEPTED);
        return sessionRepository.save(session);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatResponse> getMessages(@PathVariable Long sessionId) {
        NegotiationSession negotiationSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

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
                    offerResponse,
                    negotiationSession.getSessionStatus()
            );
        }).toList();
    }
}
