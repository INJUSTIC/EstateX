package com.estatex.application.listing;

import com.estatex.application.port.out.FileStoragePort;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.listing.*;
import com.estatex.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final FileStoragePort fileStorage;

    public ListingService(ListingRepository listingRepository,
                          UserRepository userRepository,
                          FileStoragePort fileStorage) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
    }

    // ── UC-3.1  Create listing ────────────────────────────────────────────────

    public record CreateListingCommand(
            UUID ownerId, String title, String description,
            String street, String city, String voivodeship,
            String postalCode, String country, Double latitude, Double longitude,
            PropertyType propertyType, ListingTransactionType transactionType,
            BigDecimal price,
            double areaSqMeters, int numberOfRooms,
            LocalDate availableFrom
    ) {}

    public ListingResult createListing(CreateListingCommand cmd) {
        var address = Address.of(cmd.street(), cmd.city(), cmd.voivodeship(),
                cmd.postalCode(), cmd.country(),
                cmd.latitude(), cmd.longitude());
        var money = new Money(cmd.price());
        var listing = Listing.create(cmd.title(), cmd.description(), address,
                cmd.propertyType(), cmd.transactionType(), money,
                cmd.areaSqMeters(), cmd.numberOfRooms(), cmd.ownerId(), cmd.availableFrom());
        listing = listingRepository.save(listing);
        return ListingResult.from(listing, ownerName(cmd.ownerId()));
    }

    // ── UC-3.3  Update listing ────────────────────────────────────────────────

    public record UpdateListingCommand(
            UUID listingId, UUID requesterId,
            String title, String description,
            String street, String city, String voivodeship,
            String postalCode, String country, Double latitude, Double longitude,
            PropertyType propertyType, ListingTransactionType transactionType,
            BigDecimal price,
            double areaSqMeters, int numberOfRooms,
            LocalDate availableFrom
    ) {}

    public ListingResult updateListing(UpdateListingCommand cmd) {
        var listing = findAndVerifyOwner(cmd.listingId(), cmd.requesterId());
        var address = Address.of(cmd.street(), cmd.city(), cmd.voivodeship(),
                cmd.postalCode(), cmd.country(),
                cmd.latitude(), cmd.longitude());
        var money = new Money(cmd.price());
        listing.update(cmd.title(), cmd.description(), address, cmd.propertyType(),
                cmd.transactionType(), money, cmd.areaSqMeters(), cmd.numberOfRooms(),
                cmd.availableFrom());
        listing = listingRepository.save(listing);
        return ListingResult.from(listing, ownerName(cmd.requesterId()));
    }

    // ── UC-3.4  Delete listing ────────────────────────────────────────────────

    public void deleteListing(UUID listingId, UUID requesterId, boolean isAdmin) {
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        if (!isAdmin && !listing.isOwnedBy(requesterId)) {
            throw new AccessDeniedException();
        }
        listing.getPhotos().forEach(p -> fileStorage.delete(p.getUrl()));
        listingRepository.delete(listingId);
    }

    // ── UC-3.5  Change status ─────────────────────────────────────────────────

    public ListingResult changeStatus(UUID listingId, UUID ownerId, ListingStatus newStatus) {
        var listing = findAndVerifyOwner(listingId, ownerId);
        listing.changeStatus(newStatus);
        listing = listingRepository.save(listing);
        return ListingResult.from(listing, ownerName(ownerId));
    }

    // ── UC-3.2  Upload photo ──────────────────────────────────────────────────

    public record UploadPhotoCommand(UUID listingId, UUID requesterId,
                                     String filename, InputStream data, String contentType) {}

    public ListingResult uploadPhoto(UploadPhotoCommand cmd) {
        var listing = findAndVerifyOwner(cmd.listingId(), cmd.requesterId());
        var url = fileStorage.store(cmd.filename(), cmd.data(), cmd.contentType());
        listing.addPhoto(url);
        listing = listingRepository.save(listing);
        return ListingResult.from(listing, ownerName(cmd.requesterId()));
    }

    // ── UC-3.2  Set cover photo ───────────────────────────────────────────────

    public ListingResult setCoverPhoto(UUID listingId, UUID ownerId, UUID photoId) {
        var listing = findAndVerifyOwner(listingId, ownerId);
        listing.setCoverPhoto(photoId);
        listing = listingRepository.save(listing);
        return ListingResult.from(listing, ownerName(ownerId));
    }

    // ── UC-3.2  Delete photo ──────────────────────────────────────────────────

    public ListingResult deletePhoto(UUID listingId, UUID ownerId, UUID photoId) {
        var listing = findAndVerifyOwner(listingId, ownerId);
        var photo = listing.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId));
        fileStorage.delete(photo.getUrl());
        listing.removePhoto(photoId);
        listing = listingRepository.save(listing);
        return ListingResult.from(listing, ownerName(ownerId));
    }

    // ── UC-4  Search listings ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ListingPage searchListings(ListingSearchCriteria criteria, int page, int size) {
        var result = listingRepository.search(criteria, page, size);
        var items = result.items().stream()
                .map(l -> ListingResult.from(l, ownerName(l.getOwnerId())))
                .collect(Collectors.toList());
        return new ListingPage(items, result.totalElements(), result.totalPages(), result.page());
    }

    // ── UC-4.6  Get listing detail ────────────────────────────────────────────

    @Transactional
    public ListingResult getListingDetail(UUID listingId) {
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        listingRepository.incrementViewCount(listingId);
        listing.incrementViewCount();
        return ListingResult.from(listing, ownerName(listing.getOwnerId()));
    }

    // ── UC-3.6  My listings ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ListingResult> getMyListings(UUID ownerId) {
        return listingRepository.findByOwnerId(ownerId).stream()
                .map(l -> ListingResult.from(l, ownerName(ownerId)))
                .collect(Collectors.toList());
    }

    // ── UC-11  Analytics ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ListingAnalyticsResult> getAnalytics(UUID ownerId) {
        return listingRepository.findByOwnerId(ownerId).stream()
                .map(l -> new ListingAnalyticsResult(l.getId(), l.getTitle(), l.getViewCount()))
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Listing findAndVerifyOwner(UUID listingId, UUID requesterId) {
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        if (!listing.isOwnedBy(requesterId)) {
            throw new AccessDeniedException();
        }
        return listing;
    }

    private String ownerName(UUID ownerId) {
        return userRepository.findById(ownerId)
                .map(User -> User.getDisplayName())
                .orElse("Unknown");
    }

    public record ListingPage(List<ListingResult> items, long totalElements, int totalPages, int page) {}
    public record ListingAnalyticsResult(UUID listingId, String title, int viewCount) {}
}
