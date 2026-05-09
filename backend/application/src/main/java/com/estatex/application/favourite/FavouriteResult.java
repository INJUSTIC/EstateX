package com.estatex.application.favourite;

import com.estatex.domain.favourite.Favourite;

import java.time.LocalDateTime;
import java.util.UUID;

public record FavouriteResult(UUID id, UUID userId, UUID listingId, LocalDateTime savedAt) {
    public static FavouriteResult from(Favourite f) {
        return new FavouriteResult(f.getId(), f.getUserId(), f.getListingId(), f.getSavedAt());
    }
}
