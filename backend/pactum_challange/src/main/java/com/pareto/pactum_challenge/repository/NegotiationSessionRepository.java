package com.pareto.pactum_challenge.repository;

import com.pareto.pactum_challenge.entity.NegotiationSession;
import com.pareto.pactum_challenge.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NegotiationSessionRepository extends JpaRepository<NegotiationSession, Long> {
    Optional<NegotiationSession> findByUserIdAndNegotiatorId(Long userId, Long negotiatorId);
    Optional<NegotiationSession> findByUserIdAndNegotiatorIdAndSessionStatus(Long userId, Long negotiatorId, SessionStatus status);
}
