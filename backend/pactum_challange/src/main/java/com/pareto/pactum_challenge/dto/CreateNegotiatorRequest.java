package com.pareto.pactum_challenge.dto;

import com.pareto.pactum_challenge.entity.Strategy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNegotiatorRequest(
        @NotBlank String name,
        @NotNull @Min(0) @Max(1) Double acceptanceThreshold,
        @NotNull @Min(0) @Max(1) Double walkawayThreshold,
        @NotNull @Min(0) @Max(1) Double concessionRate,
        @NotNull @Min(1) Integer maxOffersCount,
        @NotNull Strategy strategy,
        String marketContext
) {
}
