package com.pareto.pactum_challenge.dto;

import com.pareto.pactum_challenge.entity.NegotiationSession;
import com.pareto.pactum_challenge.entity.Negotiator;
import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import com.pareto.pactum_challenge.entity.Offer;

import java.util.List;

public record NegotiationContext(
        Negotiator negotiator,
        List<Offer> offers,
        NegotiationSession session
) {
}
