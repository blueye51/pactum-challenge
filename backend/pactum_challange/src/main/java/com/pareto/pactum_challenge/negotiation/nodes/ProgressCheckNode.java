package com.pareto.pactum_challenge.negotiation.nodes;

import com.pareto.pactum_challenge.dto.Action;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.*;
import com.pareto.pactum_challenge.negotiation.NegotiationNode;
import com.pareto.pactum_challenge.service.NegotiationService;
import com.pareto.pactum_challenge.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProgressCheckNode implements NegotiationNode {

    private final NegotiationService negotiationService;

    @Override
    public NegotiationResult evaluate(Offer offer, NegotiationContext context) {
        Negotiator negotiator = context.negotiator();

        Offer previousOffer = context.offers().stream()
                .filter(o -> o.getMadeBy() instanceof User)
                .reduce((first, second) -> second)
                .orElse(null);

        if (previousOffer == null) {
            return new NegotiationResult(Action.CONTINUE, null, null);
        }

        boolean anyProgress = false;

        for (OfferTerm current : negotiationService.getAllOfferTermsByOffer(offer)) {
            OfferTerm previous = negotiationService.getAllOfferTermsByOffer(previousOffer).stream()
                    .filter(t -> t.getNegotiationTerm().getId().equals(current.getNegotiationTerm().getId()))
                    .findFirst()
                    .orElseThrow();

            NegotiatorTermPreference pref = negotiationService.getAllPreferencesByNegotiator(negotiator).stream()
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
            return new NegotiationResult(Action.REJECT, null, "The User didn't even try to negotiate for a more favourable price");
        }

        return new NegotiationResult(Action.CONTINUE, null, null);
    }
}
