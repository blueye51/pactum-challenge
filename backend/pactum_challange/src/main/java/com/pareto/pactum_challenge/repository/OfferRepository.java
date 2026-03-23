package com.pareto.pactum_challenge.repository;

import com.pareto.pactum_challenge.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findAllBySessionIdOrderByIdAsc(Long sessionId);
}
