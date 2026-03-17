package com.pareto.pactum_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NegotiatorTermPreference {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private NegotiationTerm term;

    @ManyToOne(optional = false)
    private Negotiator negotiator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private double strictness;

    private String reasoning; // The "Why" in some of these fields
}
