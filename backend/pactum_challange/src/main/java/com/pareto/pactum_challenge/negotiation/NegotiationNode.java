package com.pareto.pactum_challenge.negotiation;

import com.pareto.pactum_challenge.dto.NegotiationContext;
import com.pareto.pactum_challenge.dto.NegotiationResult;
import com.pareto.pactum_challenge.entity.Offer;

public interface NegotiationNode {
    NegotiationResult evaluate(Offer offer, NegotiationContext context);
}
