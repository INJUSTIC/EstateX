package com.estatex.domain.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(UUID id);

    List<Conversation> findByParticipantId(UUID userId);

    boolean existsByListingIdAndInitiatorId(UUID listingId, UUID initiatorId);

    Optional<Conversation> findByListingIdAndInitiatorId(UUID listingId, UUID initiatorId);
}
