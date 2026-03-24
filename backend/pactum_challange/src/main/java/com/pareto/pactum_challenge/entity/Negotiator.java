package com.pareto.pactum_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Negotiator extends Participant {
    @Column(nullable = false)
    private double acceptanceThreshold;

    @Column(nullable = false)
    private double walkawayThreshold;

    @Column(nullable = false)
    private double concessionRate;

    @Column(nullable = false)
    private int maxOffersCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Strategy strategy = Strategy.BALANCED;

    @Column(columnDefinition = "TEXT")
    private String marketContext;
}
