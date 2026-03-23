package com.pareto.pactum_challenge.dto;

public record OfferTermDto(
        Long termId,
        String termName,
        String termUnit,
        double value
) {
}
