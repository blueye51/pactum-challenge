package com.pareto.pactum_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne(optional = false)
    private NegotiationSession session;

    @ManyToOne(optional = false)
    private Participant sender;

    @ManyToOne(optional = false)
    private Participant recipient;

    @ManyToOne
    private Offer offer;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
