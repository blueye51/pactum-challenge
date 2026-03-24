package com.pareto.pactum_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NegotiationTerm {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String unit; // What the value represents

    @Column(nullable = false)
    private double min;

    @Column(nullable = false)
    private double max;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean wholeNumber;
}
