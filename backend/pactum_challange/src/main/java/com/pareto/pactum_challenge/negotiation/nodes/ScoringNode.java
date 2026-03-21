package com.pareto.pactum_challenge.negotiation.nodes;

import com.pareto.pactum_challenge.dto.Action;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.negotiation.NegotiationNode;
import com.pareto.pactum_challenge.service.NegotiationDataService;
import com.pareto.pactum_challenge.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScoringNode implements NegotiationNode {

    private final ScoringService scoringService;
    private final NegotiationDataService dataService;

    @Override
    public NegotiationResult evaluate(Offer offer, NegotiationContext context) {
        Negotiator negotiator = context.negotiator();
        List<NegotiatorTermPreference> preferences = dataService.getAllPreferencesByNegotiator(negotiator);

        double score = scoringService.score(offer, preferences);
        double threshold = scoringService.adjustedThreshold(negotiator, context.offers().size());

        if (score >= threshold) {
            return new NegotiationResult(Action.ACCEPT, null, "Offer meets our requirements");
        }

        if (score < negotiator.getWalkawayThreshold()) {
            return new NegotiationResult(Action.REJECT, null, "Offer is too far from acceptable");
        }

        return new NegotiationResult(Action.CONTINUE, null, null);
    }
}
