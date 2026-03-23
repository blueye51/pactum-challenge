package com.pareto.pactum_challenge.controller;

import com.pareto.pactum_challenge.dto.*;
import com.pareto.pactum_challenge.entity.*;
import com.pareto.pactum_challenge.repository.ChatMessageRepository;
import com.pareto.pactum_challenge.repository.NegotiationSessionRepository;
import com.pareto.pactum_challenge.repository.NegotiationTermRepository;
import com.pareto.pactum_challenge.repository.OfferRepository;
import com.pareto.pactum_challenge.service.DeepSeekMessageBuilder;
import com.pareto.pactum_challenge.service.DeepSeekService;
import com.pareto.pactum_challenge.service.NegotiationService;
import com.pareto.pactum_challenge.service.PromptMode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NegotiationController {

    private final NegotiationSessionRepository sessionRepository;
    private final NegotiationTermRepository termRepository;
    private final OfferRepository offerRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NegotiationService negotiationService;
    private final DeepSeekService deepSeekService;
    private final DeepSeekMessageBuilder messageBuilder;

    @MessageMapping("/session/{sessionId}/offer")
    @SendTo("/topic/session/{sessionId}")
    public ChatResponse handleOffer(@DestinationVariable Long sessionId, IncomingMessage incoming) {
        NegotiationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        // Build and save the user's offer
        Offer userOffer = new Offer(session, session.getUser());
        List<OfferTerm> offerTerms = new ArrayList<>();

        for (OfferTermDto termDto : incoming.offer().terms()) {
            NegotiationTerm term = termRepository.findById(termDto.termId())
                    .orElseThrow(() -> new EntityNotFoundException("Term not found: " + termDto.termId()));
            offerTerms.add(new OfferTerm(userOffer, term, termDto.value()));
        }

        userOffer.setOfferTerms(offerTerms);
        offerRepository.save(userOffer);

        // Save the user's chat message (with offer attached)
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender(session.getUser());
        userMsg.setRecipient(session.getNegotiator());
        userMsg.setMessage(incoming.content());
        userMsg.setOffer(userOffer);
        chatMessageRepository.save(userMsg);

        // Build context with full offer history
        List<Offer> offerHistory = offerRepository.findAllBySessionIdOrderByIdAsc(sessionId);

        NegotiationContext context = new NegotiationContext(
                session.getNegotiator(),
                offerHistory,
                session
        );

        // Run through negotiation tree
        NegotiationResult result = negotiationService.handleOffer(userOffer, context);

        // Map the action to an offer status for the user's offer
        Status userOfferStatus = switch (result.action()) {
            case ACCEPT -> Status.ACCEPTED;
            case REJECT -> Status.REJECTED;
            case COUNTER -> Status.COUNTERED;
            case CONTINUE -> Status.PENDING;
        };
        userOffer.setStatus(userOfferStatus);
        offerRepository.save(userOffer);

        // Build the bot's response
        String botMessage = result.reasoning();
        ChatResponse.OfferResponse responseOffer = null;

        if (result.action() == Action.COUNTER && result.counterOffer() != null) {
            // Persist the counter offer
            Offer counterOffer = result.counterOffer();
            counterOffer.setStatus(Status.PENDING);
            offerRepository.save(counterOffer);

            // Build response terms from the counter offer
            List<OfferTermDto> responseTerms = counterOffer.getOfferTerms().stream()
                    .map(ot -> new OfferTermDto(
                            ot.getNegotiationTerm().getId(),
                            ot.getNegotiationTerm().getName(),
                            ot.getNegotiationTerm().getUnit(),
                            ot.getValue()
                    ))
                    .toList();

            responseOffer = new ChatResponse.OfferResponse(responseTerms, Status.PENDING);
        } else if (result.action() == Action.ACCEPT) {
            // Echo back accepted terms
            List<OfferTermDto> acceptedTerms = userOffer.getOfferTerms().stream()
                    .map(ot -> new OfferTermDto(
                            ot.getNegotiationTerm().getId(),
                            ot.getNegotiationTerm().getName(),
                            ot.getNegotiationTerm().getUnit(),
                            ot.getValue()
                    ))
                    .toList();

            responseOffer = new ChatResponse.OfferResponse(acceptedTerms, Status.ACCEPTED);
        }

        // Save bot's chat message
        ChatMessage botMsg = new ChatMessage();
        botMsg.setSession(session);
        botMsg.setSender(session.getNegotiator());
        botMsg.setRecipient(session.getUser());
        botMsg.setMessage(botMessage);
        if (result.action() == Action.COUNTER && result.counterOffer() != null) {
            botMsg.setOffer(result.counterOffer());
        }
        chatMessageRepository.save(botMsg);

        return new ChatResponse(
                "message",
                "bot",
                botMessage,
                System.currentTimeMillis(),
                responseOffer
        );
    }

    @MessageMapping("/session/{sessionId}/message")
    @SendTo("/topic/session/{sessionId}")
    public ChatResponse handleMessage(@DestinationVariable Long sessionId, IncomingMessage incoming) {
        NegotiationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        // Persist user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender(session.getUser());
        userMsg.setRecipient(session.getNegotiator());
        userMsg.setMessage(incoming.content());
        chatMessageRepository.save(userMsg);

        // Fetch chat history and build DeepSeek prompt
        List<ChatMessage> chatHistory = chatMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);

        List<Map<String, String>> llmMessages = messageBuilder.buildMessages(session, PromptMode.CHAT, chatHistory, incoming.content());
        String botReply = deepSeekService.send(llmMessages);

        ChatMessage botMsg = new ChatMessage();
        botMsg.setSession(session);
        botMsg.setSender(session.getNegotiator());
        botMsg.setRecipient(session.getUser());
        botMsg.setMessage(botReply);
        chatMessageRepository.save(botMsg);

        return new ChatResponse(
                "message",
                "bot",
                botReply,
                System.currentTimeMillis(),
                null
        );
    }
}
