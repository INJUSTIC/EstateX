package com.estatex.domain.listing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Listing aggregate root.
 */
public class Listing {

    private static final int MAX_PHOTOS = 20;

    private final UUID id;
    private String title;
    private String description;
    private Address address;
    private PropertyType propertyType;
    private ListingTransactionType transactionType;
    private Money price;
    private double areaSqMeters;
    private int numberOfRooms;
    private ListingStatus status;
    private final UUID ownerId;
    private final List<Photo> photos;
    private int viewCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate availableFrom;

    public Listing(UUID id, String title, String description, Address address,
                   PropertyType propertyType, ListingTransactionType transactionType,
                   Money price, double areaSqMeters, int numberOfRooms,
                   ListingStatus status, UUID ownerId, List<Photo> photos,
                   int viewCount, LocalDateTime createdAt, LocalDateTime updatedAt,
                   LocalDate availableFrom) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
        this.propertyType = propertyType;
        this.transactionType = transactionType;
        this.price = price;
        this.areaSqMeters = areaSqMeters;
        this.numberOfRooms = numberOfRooms;
        this.status = status;
        this.ownerId = ownerId;
        this.photos = new ArrayList<>(photos);
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.availableFrom = availableFrom;
    }

    public static Listing create(String title, String description, Address address,
                                  PropertyType propertyType, ListingTransactionType transactionType,
                                  Money price, double areaSqMeters, int numberOfRooms, UUID ownerId,
                                  LocalDate availableFrom) {
        var now = LocalDateTime.now();
        return new Listing(UUID.randomUUID(), title, description, address,
                propertyType, transactionType, price, areaSqMeters, numberOfRooms,
                ListingStatus.ACTIVE, ownerId, new ArrayList<>(), 0, now, now, availableFrom);
    }

    // ── Business behaviour ────────────────────────────────────────────────────

    public void update(String title, String description, Address address,
                       PropertyType propertyType, ListingTransactionType transactionType,
                       Money price, double areaSqMeters, int numberOfRooms,
                       LocalDate availableFrom) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.propertyType = propertyType;
        this.transactionType = transactionType;
        this.price = price;
        this.areaSqMeters = areaSqMeters;
        this.numberOfRooms = numberOfRooms;
        this.availableFrom = availableFrom;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(ListingStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public Photo addPhoto(String url) {
        if (photos.size() >= MAX_PHOTOS) {
            throw new IllegalStateException("Maximum " + MAX_PHOTOS + " photos allowed per listing");
        }
        boolean isCover = photos.isEmpty();
        var photo = Photo.create(id, url, isCover);
        photos.add(photo);
        this.updatedAt = LocalDateTime.now();
        return photo;
    }

    public void removePhoto(UUID photoId) {
        var photo = photos.stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId));
        photos.remove(photo);
        if (photo.isCover() && !photos.isEmpty()) {
            photos.get(0).markAsCover();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void setCoverPhoto(UUID photoId) {
        photos.forEach(Photo::unmarkCover);
        photos.stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId))
                .markAsCover();
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Address getAddress() { return address; }
    public PropertyType getPropertyType() { return propertyType; }
    public ListingTransactionType getTransactionType() { return transactionType; }
    public Money getPrice() { return price; }
    public double getAreaSqMeters() { return areaSqMeters; }
    public int getNumberOfRooms() { return numberOfRooms; }
    public ListingStatus getStatus() { return status; }
    public UUID getOwnerId() { return ownerId; }
    public List<Photo> getPhotos() { return Collections.unmodifiableList(photos); }
    public int getViewCount() { return viewCount; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
