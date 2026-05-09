package com.estatex.application.acceptance.fakes;

import com.estatex.domain.chat.Conversation;
import com.estatex.domain.chat.ConversationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryConversationRepository implements ConversationRepository {
    private final Map<UUID, Conversation> database = new HashMap<>();

    @Override
    public Conversation save(Conversation conversation) {
        database.put(conversation.getId(), conversation);
        return conversation;
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Conversation> findByParticipantId(UUID userId) {
        return database.values().stream()
                .filter(c -> c.getInitiatorId().equals(userId) || c.getListingOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByListingIdAndInitiatorId(UUID listingId, UUID initiatorId) {
        return findByListingIdAndInitiatorId(listingId, initiatorId).isPresent();
    }

    @Override
    public Optional<Conversation> findByListingIdAndInitiatorId(UUID listingId, UUID initiatorId) {
        return database.values().stream()
                .filter(c -> c.getListingId().equals(listingId) && c.getInitiatorId().equals(initiatorId))
                .findFirst();
    }
}
