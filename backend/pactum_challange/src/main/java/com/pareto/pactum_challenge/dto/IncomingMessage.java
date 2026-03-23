package com.pareto.pactum_challenge.dto;

public record IncomingMessage(
        String content,
        IncomingOffer offer
) {
}
