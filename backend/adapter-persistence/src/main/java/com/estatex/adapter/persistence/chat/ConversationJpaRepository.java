package com.estatex.adapter.persistence.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, UUID> {
    List<ConversationJpaEntity> findByInitiatorIdOrListingOwnerId(UUID initiatorId, UUID listingOwnerId);
    boolean existsByListingIdAndInitiatorId(UUID listingId, UUID initiatorId);
    Optional<ConversationJpaEntity> findByListingIdAndInitiatorId(UUID listingId, UUID initiatorId);
}
