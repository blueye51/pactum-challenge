package com.pareto.pactum_challenge.negotiation;

import com.pareto.pactum_challenge.dto.Action;
import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.Offer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NegotiationTree implements NegotiationNode {
    private final List<NegotiationNode> nodes;

    public NegotiationResult evaluate(Offer offer, NegotiationContext context) {
        for (NegotiationNode node : nodes) {
            NegotiationResult result = node.evaluate(offer, context);
            if (result.action() != Action.CONTINUE) {
                return result;
            }
        }
        throw new IllegalStateException("No node made a decision");
    }
}
