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

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CounterOfferNode implements NegotiationNode {

    private final ScoringService scoringService;
    private final NegotiationDataService dataService;

    @Override
    public NegotiationResult evaluate(Offer offer, NegotiationContext context) {
        Negotiator negotiator = context.negotiator();
        List<NegotiatorTermPreference> preferences = dataService.getAllPreferencesByNegotiator(negotiator);
        int round = context.offers().size();

        Offer previousBotOffer = context.offers().stream()
                .filter(o -> o.getMadeBy() instanceof Negotiator)
                .reduce((first, second) -> second)
                .orElse(null);

        List<OfferTerm> counterTerms = new ArrayList<>();

        Offer counterOffer = new Offer(context.session(), negotiator);

        for (NegotiatorTermPreference pref : preferences) {
            OfferTerm supplierTerm = dataService.getAllOfferTermsByOffer(offer).stream()
                    .filter(t -> t.getNegotiationTerm().getId().equals(pref.getNegotiationTerm().getId()))
                    .findFirst()
                    .orElseThrow();

            double botPrevious = previousBotOffer != null
                    ? dataService.getAllOfferTermsByOffer(previousBotOffer).stream()
                    .filter(t -> t.getNegotiationTerm().getId().equals(pref.getNegotiationTerm().getId()))
                    .findFirst().orElseThrow().getValue()
                    : pref.getIdealValue();

            double supplierValue = supplierTerm.getValue();

            double flexibility = (1 - pref.getWeight()) * (1 - pref.getStrictness());
            double concessionAmount = (supplierValue - botPrevious) * flexibility * negotiator.getConcessionRate() * round;

            double newValue = botPrevious + concessionAmount;

            if (pref.getIdealValue() < pref.getLimitValue()) {
                newValue = Math.min(newValue, pref.getLimitValue());
            } else {
                newValue = Math.max(newValue, pref.getLimitValue());
            }

            counterTerms.add(new OfferTerm(counterOffer, pref.getNegotiationTerm(), newValue));
        }

        counterOffer.setOfferTerms(counterTerms);

        return new NegotiationResult(Action.COUNTER, counterOffer, "Here's our counteroffer");
    }
}
