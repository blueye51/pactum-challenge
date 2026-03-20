package com.pareto.pactum_challenge.repository;

import com.pareto.pactum_challenge.entity.OfferTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferTermRepository extends JpaRepository<OfferTerm, Long> {
    List<OfferTerm> findAllByOfferId(Long offerId);
}