package com.estatex.adapter.persistence.chat;

import com.estatex.domain.chat.Conversation;
import com.estatex.domain.chat.ConversationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository jpa;

    public ConversationRepositoryAdapter(ConversationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Conversation save(Conversation c) {
        var e = ConversationJpaEntity.builder()
                .id(c.getId()).listingId(c.getListingId())
                .initiatorId(c.getInitiatorId())
                .listingOwnerId(c.getListingOwnerId())
                .startedAt(c.getStartedAt())
                .build();
        return toDomain(jpa.save(e));
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Conversation> findByParticipantId(UUID userId) {
        return jpa.findByInitiatorIdOrListingOwnerId(userId, userId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByListingIdAndInitiatorId(UUID listingId, UUID initiatorId) {
        return jpa.existsByListingIdAndInitiatorId(listingId, initiatorId);
    }

    @Override
    public Optional<Conversation> findByListingIdAndInitiatorId(UUID listingId, UUID initiatorId) {
        return jpa.findByListingIdAndInitiatorId(listingId, initiatorId).map(this::toDomain);
    }

    private Conversation toDomain(ConversationJpaEntity e) {
        return new Conversation(e.getId(), e.getListingId(), e.getInitiatorId(),
                e.getListingOwnerId(), e.getStartedAt());
    }
}
