package com.pareto.pactum_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Offer {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private NegotiationSession session;

    @ManyToOne(optional = false)
    private Participant madeBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferTerm> offerTerms;

    public Offer(NegotiationSession session, Participant madeBy) {
        this.session = session;
        this.madeBy = madeBy;
    }
}
