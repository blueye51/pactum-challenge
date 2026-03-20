package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.entity.OfferTerm;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoringService {
    @Transactional
    public double score(Offer offer, List<NegotiatorTermPreference> preferences) {
        double totalScore = 0.0;

        for (OfferTerm offerTerm : offer.getOfferTerms()) {
            NegotiatorTermPreference pref = preferences.stream()
                    .filter(p -> p.getNegotiationTerm().getId().equals(offerTerm.getNegotiationTerm().getId()))
                    .findFirst()
                    .orElseThrow();

            double value = offerTerm.getValue();
            double ideal = pref.getIdealValue();
            double limit = pref.getLimitValue();

            double normalized;
            if (ideal == limit) {
                normalized = value == ideal ? 1.0 : 0.0;
            } else {
                normalized = (value - limit) / (ideal - limit);
            }

            normalized = Math.max(0.0, Math.min(1.0, normalized));

            totalScore += normalized * pref.getWeight();
        }

        return totalScore;
    }

    public double adjustedThreshold(Negotiator negotiator, int currentRound) {
        double threshold = negotiator.getAcceptanceThreshold()
                - (negotiator.getConcessionRate() * currentRound);

        return Math.max(threshold, negotiator.getWalkawayThreshold());
    }
}

