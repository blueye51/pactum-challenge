package com.pareto.pactum_challenge.repository;

import com.pareto.pactum_challenge.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
