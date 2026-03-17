package com.pareto.pactum_challenge.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NegotiationSession {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private NegotiationTerm term;

    @ManyToOne(optional = false)
    private Negotiator negotiator;

    @ManyToOne(optional = false)
    private User user;

    private double score; // 0-1

}
