package com.pareto.pactum_challenge.dto;

import com.pareto.pactum_challenge.entity.Offer;

public record NegotiationResult(
        Action action,
        Offer counterOffer,
        String reasoning
) {

}