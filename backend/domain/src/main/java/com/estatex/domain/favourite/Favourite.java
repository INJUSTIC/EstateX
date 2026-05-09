package com.estatex.domain.favourite;

import java.time.LocalDateTime;
import java.util.UUID;

public class Favourite {

    private final UUID id;
    private final UUID userId;
    private final UUID listingId;
    private final LocalDateTime savedAt;

    public Favourite(UUID id, UUID userId, UUID listingId, LocalDateTime savedAt) {
        this.id = id;
        this.userId = userId;
        this.listingId = listingId;
        this.savedAt = savedAt;
    }

    public static Favourite create(UUID userId, UUID listingId) {
        return new Favourite(UUID.randomUUID(), userId, listingId, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getListingId() { return listingId; }
    public LocalDateTime getSavedAt() { return savedAt; }
}
