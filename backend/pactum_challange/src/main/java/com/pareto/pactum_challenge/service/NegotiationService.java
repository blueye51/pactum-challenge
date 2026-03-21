package com.pareto.pactum_challenge.service;

import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.Offer;
import com.pareto.pactum_challenge.negotiation.NegotiationStrategyFactory;
import com.pareto.pactum_challenge.negotiation.NegotiationTree;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NegotiationService {
    private final NegotiationStrategyFactory strategyFactory;

    public NegotiationResult handleOffer(Offer offer, NegotiationContext context) {
        NegotiationTree tree = strategyFactory.create(context.negotiator().getStrategy());
        return tree.evaluate(offer, context);
    }
}
