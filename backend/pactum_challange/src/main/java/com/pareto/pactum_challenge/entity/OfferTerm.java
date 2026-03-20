package com.pareto.pactum_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OfferTerm {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Offer offer;

    @ManyToOne(optional = false)
    private NegotiationTerm negotiationTerm;

    @Column(nullable = false)
    private double value;
}
