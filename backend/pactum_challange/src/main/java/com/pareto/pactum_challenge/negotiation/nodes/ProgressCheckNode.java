package com.pareto.pactum_challenge.negotiation.nodes;

import com.pareto.pactum_challenge.dto.Action;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.*;
import com.pareto.pactum_challenge.negotiation.NegotiationNode;
import com.pareto.pactum_challenge.service.NegotiationDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProgressCheckNode implements NegotiationNode {

    private final NegotiationDataService dataService;

    @Override
    public NegotiationResult evaluate(Offer offer, NegotiationContext context) {
        Negotiator negotiator = context.negotiator();

        // Find the previous user offer (excluding the current one)
        List<Offer> previousUserOffers = context.offers().stream()
                .filter(o -> o.getMadeBy() instanceof User)
                .filter(o -> !o.getId().equals(offer.getId()))
                .toList();

        // No previous user offer to compare against — skip
        if (previousUserOffers.isEmpty()) {
            return new NegotiationResult(Action.CONTINUE, null, null);
        }

        Offer previousOffer = previousUserOffers.getLast();

        boolean anyProgress = false;

        for (OfferTerm current : dataService.getAllOfferTermsByOffer(offer)) {
            OfferTerm previous = dataService.getAllOfferTermsByOffer(previousOffer).stream()
                    .filter(t -> t.getNegotiationTerm().getId().equals(current.getNegotiationTerm().getId()))
                    .findFirst()
                    .orElseThrow();

            NegotiatorTermPreference pref = dataService.getAllPreferencesByNegotiator(negotiator).stream()
                    .filter(p -> p.getNegotiationTerm().getId().equals(current.getNegotiationTerm().getId()))
                    .findFirst()
                    .orElseThrow();

            if (Direction.MINIMIZE.equals(pref.getDirection())) {
                if (current.getValue() < previous.getValue()) anyProgress = true;
            } else {
                if (current.getValue() > previous.getValue()) anyProgress = true;
            }
        }

        if (!anyProgress) {
            return new NegotiationResult(Action.REJECT, null, "No progress from your previous offer — you haven't moved on any term.");
        }

        return new NegotiationResult(Action.CONTINUE, null, null);
    }
}
