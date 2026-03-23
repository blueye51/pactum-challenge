package com.pareto.pactum_challenge.dto;

import java.util.List;

public record IncomingOffer(
        List<OfferTermDto> terms
) {
}
