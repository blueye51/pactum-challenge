package com.pareto.pactum_challenge.negotiation.nodes;

import com.pareto.pactum_challenge.dto.Action;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.negotiation.NegotiationNode;
import org.springframework.stereotype.Component;

@Component
public class MaxRoundsNode implements NegotiationNode {
    @Override
    public NegotiationResult evaluate(Offer offer, NegotiationContext context) {
        int offerAmount = context.offers().size();
        int maxOfferAmount = context.negotiator().getMaxOffersCount();

        if (offerAmount >= maxOfferAmount) {
            return new NegotiationResult(Action.REJECT, null,
                    "Maximum number of negotiation rounds reached");
        }

        return new NegotiationResult(Action.CONTINUE, null, null);
    }
}
