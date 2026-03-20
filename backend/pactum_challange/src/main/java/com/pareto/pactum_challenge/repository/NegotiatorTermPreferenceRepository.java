package com.pareto.pactum_challenge.repository;

import com.pareto.pactum_challenge.entity.NegotiatorTermPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiatorTermPreferenceRepository extends JpaRepository<NegotiatorTermPreference, Long> {
    List<NegotiatorTermPreference> findAllByNegotiatorId(Long negotiatorId);
}
