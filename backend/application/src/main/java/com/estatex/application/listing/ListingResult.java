package com.estatex.application.listing;

import com.estatex.domain.listing.Listing;
import com.estatex.domain.listing.ListingStatus;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ListingResult(
        UUID id,
        String title,
        String description,
        String street,
        String city,
        String voivodeship,
        String postalCode,
        String country,
        Double latitude,
        Double longitude,
        PropertyType propertyType,
        ListingTransactionType transactionType,
        BigDecimal price,
        double areaSqMeters,
        int numberOfRooms,
        ListingStatus status,
        UUID ownerId,
        String ownerName,
        List<PhotoResult> photos,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDate availableFrom
) {
    public record PhotoResult(UUID id, String url, boolean cover) {}

    public static ListingResult from(Listing listing, String ownerName) {
        var photos = listing.getPhotos().stream()
                .map(p -> new PhotoResult(p.getId(), p.getUrl(), p.isCover()))
                .toList();
        return new ListingResult(
                listing.getId(), listing.getTitle(), listing.getDescription(),
                listing.getAddress().street(), listing.getAddress().city(),
                listing.getAddress().voivodeship(), listing.getAddress().postalCode(),
                listing.getAddress().country(), listing.getAddress().latitude(),
                listing.getAddress().longitude(), listing.getPropertyType(),
                listing.getTransactionType(), listing.getPrice().amount(),
                listing.getAreaSqMeters(),
                listing.getNumberOfRooms(), listing.getStatus(), listing.getOwnerId(),
                ownerName, photos, listing.getViewCount(),
                listing.getCreatedAt(), listing.getUpdatedAt(), listing.getAvailableFrom()
        );
    }
}
