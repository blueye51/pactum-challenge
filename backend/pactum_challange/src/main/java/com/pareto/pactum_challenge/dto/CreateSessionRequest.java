package com.pareto.pactum_challenge.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSessionRequest(
        @NotNull Long negotiatorId
) {
}
