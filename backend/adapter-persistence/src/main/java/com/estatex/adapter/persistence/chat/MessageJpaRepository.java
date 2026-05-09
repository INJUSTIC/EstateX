package com.estatex.adapter.persistence.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

    List<MessageJpaEntity> findByConversationIdOrderBySentAtAsc(UUID conversationId, Pageable pageable);
    long countByConversationId(UUID conversationId);

    @Query("SELECT COUNT(m) FROM MessageJpaEntity m WHERE m.conversationId = :conversationId AND m.senderId <> :userId AND m.read = false")
    long countUnread(@org.springframework.data.repository.query.Param("conversationId") UUID conversationId, @org.springframework.data.repository.query.Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MessageJpaEntity m SET m.read = true WHERE m.conversationId = :conversationId AND m.senderId <> :userId")
    void markAllAsRead(@org.springframework.data.repository.query.Param("conversationId") UUID conversationId, @org.springframework.data.repository.query.Param("userId") UUID userId);
}
