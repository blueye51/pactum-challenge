package com.pareto.pactum_challenge.dto;

import com.pareto.pactum_challenge.entity.Direction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddTermPreferenceRequest(
        Long termId,
        String termName,
        String termUnit,
        String termDescription,
        Double termMin,
        Double termMax,
        Boolean termWholeNumber,
        @NotNull Direction direction,
        @NotNull Double idealValue,
        @NotNull Double limitValue,
        @NotNull @Min(0) @Max(1) Double weight,
        @NotNull @Min(0) @Max(1) Double strictness,
        String reasoning
) {
}
