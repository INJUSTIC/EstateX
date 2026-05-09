package com.estatex.domain.listing;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Photo entity — part of the Listing aggregate.
 */
public class Photo {

    private final UUID id;
    private final UUID listingId;
    private String url;
    private boolean cover;
    private final LocalDateTime uploadedAt;

    public Photo(UUID id, UUID listingId, String url, boolean cover, LocalDateTime uploadedAt) {
        this.id = id;
        this.listingId = listingId;
        this.url = url;
        this.cover = cover;
        this.uploadedAt = uploadedAt;
    }

    public static Photo create(UUID listingId, String url, boolean cover) {
        return new Photo(UUID.randomUUID(), listingId, url, cover, LocalDateTime.now());
    }

    public void markAsCover() { this.cover = true; }
    public void unmarkCover() { this.cover = false; }

    public UUID getId() { return id; }
    public UUID getListingId() { return listingId; }
    public String getUrl() { return url; }
    public boolean isCover() { return cover; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
