package com.pareto.pactum_challenge.negotiation.nodes;

import com.pareto.pactum_challenge.dto.Action;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.*;
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
            // Count previous user offers (exclude the current one being evaluated)
            long previousUserOffers = context.offers().stream()
                    .filter(o -> o.getMadeBy() instanceof User)
                    .filter(o -> !o.getId().equals(offer.getId()))
                    .count();

            // First user offer — give them another chance instead of rejecting
            if (previousUserOffers == 0) {
                String worstTermFeedback = findWorstTerm(offer, preferences);
                String reason = "This offer is quite far from what we can work with. "
                        + worstTermFeedback
                        + " Please revise your offer — we'd like to keep negotiating.";
                return new NegotiationResult(Action.CONTINUE, null, reason);
            }

            return new NegotiationResult(Action.REJECT, null, "We can't make this work. The offer is too far from what's acceptable for us.");
        }

        return new NegotiationResult(Action.CONTINUE, null, null);
    }

    private String findWorstTerm(Offer offer, List<NegotiatorTermPreference> preferences) {
        String worstTermName = null;
        double worstScore = Double.MAX_VALUE;

        for (OfferTerm offerTerm : offer.getOfferTerms()) {
            NegotiatorTermPreference pref = preferences.stream()
                    .filter(p -> p.getNegotiationTerm().getId().equals(offerTerm.getNegotiationTerm().getId()))
                    .findFirst()
                    .orElse(null);

            if (pref == null) continue;

            double value = offerTerm.getValue();
            double ideal = pref.getIdealValue();
            double limit = pref.getLimitValue();

            double normalized;
            if (ideal == limit) {
                normalized = value == ideal ? 1.0 : 0.0;
            } else {
                normalized = (value - limit) / (ideal - limit);
            }

            // Weight the score so high-importance terms that are bad stand out more
            double weightedScore = normalized * pref.getWeight();

            if (weightedScore < worstScore) {
                worstScore = weightedScore;
                worstTermName = offerTerm.getNegotiationTerm().getName();
            }
        }

        if (worstTermName != null) {
            return "In particular, the %s is way off from what we can accept.".formatted(worstTermName);
        }
        return "Several terms are far from our range.";
    }
}
