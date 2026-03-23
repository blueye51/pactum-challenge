package com.pareto.pactum_challenge.dto;

import com.pareto.pactum_challenge.entity.Status;

import java.util.List;

public record ChatResponse(
        String type,
        String sender,
        String content,
        long timestamp,
        OfferResponse offer
) {
    public record OfferResponse(
            List<OfferTermDto> terms,
            Status status
    ) {
    }
}
