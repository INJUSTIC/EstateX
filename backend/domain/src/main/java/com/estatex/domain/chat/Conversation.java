package com.estatex.domain.chat;

import java.time.LocalDateTime;
import java.util.UUID;
import com.estatex.domain.exception.DomainException;

/**
 * Conversation aggregate root. Ties one listing to exactly two participants.
 */
public class Conversation {

    private final UUID id;
    private final UUID listingId;  // null if listing was deleted
    private final UUID initiatorId;
    private final UUID listingOwnerId;
    private final LocalDateTime startedAt;

    public Conversation(UUID id, UUID listingId, UUID initiatorId,
                        UUID listingOwnerId, LocalDateTime startedAt) {
        this.id = id;
        this.listingId = listingId;
        this.initiatorId = initiatorId;
        this.listingOwnerId = listingOwnerId;
        this.startedAt = startedAt;
    }

    public static Conversation create(UUID listingId, UUID initiatorId, UUID listingOwnerId) {
        if (initiatorId.equals(listingOwnerId)) {
            throw new DomainException("Cannot start a conversation about your own listing");
        }
        return new Conversation(UUID.randomUUID(), listingId, initiatorId,
                listingOwnerId, LocalDateTime.now());
    }

    public boolean isParticipant(UUID userId) {
        return initiatorId.equals(userId) || listingOwnerId.equals(userId);
    }

    public UUID otherParticipant(UUID userId) {
        if (initiatorId.equals(userId)) return listingOwnerId;
        if (listingOwnerId.equals(userId)) return initiatorId;
        throw new IllegalArgumentException("User is not a participant: " + userId);
    }

    public UUID getId() { return id; }
    public UUID getListingId() { return listingId; }
    public UUID getInitiatorId() { return initiatorId; }
    public UUID getListingOwnerId() { return listingOwnerId; }
    public LocalDateTime getStartedAt() { return startedAt; }
}
