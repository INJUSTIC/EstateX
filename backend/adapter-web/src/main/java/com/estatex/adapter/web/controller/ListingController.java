package com.estatex.adapter.web.controller;

import com.estatex.application.listing.ListingService;
import com.estatex.domain.listing.ListingSearchCriteria;
import com.estatex.domain.listing.ListingStatus;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    record CreateListingRequest(
            @NotBlank @Size(max = 255) String title,
            String description,
            String street, @NotBlank String city,
            String voivodeship, String postalCode, String country,
            Double latitude, Double longitude,
            @NotNull PropertyType propertyType,
            @NotNull ListingTransactionType transactionType,
            @NotNull @Positive BigDecimal price,
            String currency,
            @Positive double areaSqMeters,
            @Positive @Max(50) int numberOfRooms,
            LocalDate availableFrom
    ) {}

    record ChangeStatusRequest(@NotNull ListingStatus status) {}

    @GetMapping
    @Operation(summary = "Search listings")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String voivodeship,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) ListingTransactionType transactionType,
            @RequestParam(required = false) Integer minRooms,
            @RequestParam(required = false) Integer maxRooms,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(required = false) LocalDate availableEarliest,
            @RequestParam(required = false) LocalDate availableLatest,
            @RequestParam(defaultValue = "CREATED_AT") ListingSearchCriteria.SortBy sortBy,
            @RequestParam(defaultValue = "DESC") ListingSearchCriteria.SortDirection sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new ListingSearchCriteria(keyword, city, voivodeship, minPrice, maxPrice,
                propertyType, transactionType, minRooms, maxRooms, minArea, maxArea, sortBy, sortDirection,
                availableEarliest, availableLatest);
        return ResponseEntity.ok(listingService.searchListings(criteria, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing detail")
    public ResponseEntity<?> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(listingService.getListingDetail(id));
    }

    @PostMapping
    @Operation(summary = "Create listing")
    public ResponseEntity<?> create(@RequestHeader("X-User-Id") UUID userId,
                                    @Valid @RequestBody CreateListingRequest req) {
        return ResponseEntity.ok(listingService.createListing(new ListingService.CreateListingCommand(
                userId, req.title(), req.description(), req.street(), req.city(),
                req.voivodeship(), req.postalCode(), req.country(), req.latitude(), req.longitude(),
                req.propertyType(), req.transactionType(), req.price(),
                req.areaSqMeters(), req.numberOfRooms(), req.availableFrom())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update listing")
    public ResponseEntity<?> update(@RequestHeader("X-User-Id") UUID userId,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody CreateListingRequest req) {
        return ResponseEntity.ok(listingService.updateListing(new ListingService.UpdateListingCommand(
                id, userId, req.title(), req.description(), req.street(), req.city(),
                req.voivodeship(), req.postalCode(), req.country(), req.latitude(), req.longitude(),
                req.propertyType(), req.transactionType(), req.price(),
                req.areaSqMeters(), req.numberOfRooms(), req.availableFrom())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete your listing")
    public ResponseEntity<?> delete(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        listingService.deleteListing(id, userId, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change listing status")
    public ResponseEntity<?> changeStatus(@RequestHeader("X-User-Id") UUID userId,
                                          @PathVariable UUID id,
                                          @Valid @RequestBody ChangeStatusRequest req) {
        return ResponseEntity.ok(listingService.changeStatus(id, userId, req.status()));
    }

    @PostMapping("/{id}/photos")
    @Operation(summary = "Upload a photo")
    public ResponseEntity<?> uploadPhoto(@RequestHeader("X-User-Id") UUID userId,
                                         @PathVariable UUID id,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(listingService.uploadPhoto(new ListingService.UploadPhotoCommand(
                id, userId, file.getOriginalFilename(), file.getInputStream(), file.getContentType())));
    }

    @PatchMapping("/{id}/photos/{photoId}/cover")
    @Operation(summary = "Set cover photo")
    public ResponseEntity<?> setCover(@RequestHeader("X-User-Id") UUID userId,
                                      @PathVariable UUID id, @PathVariable UUID photoId) {
        return ResponseEntity.ok(listingService.setCoverPhoto(id, userId, photoId));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @Operation(summary = "Delete a photo")
    public ResponseEntity<?> deletePhoto(@RequestHeader("X-User-Id") UUID userId,
                                         @PathVariable UUID id, @PathVariable UUID photoId) {
        return ResponseEntity.ok(listingService.deletePhoto(id, userId, photoId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my listings")
    public ResponseEntity<?> myListings(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(listingService.getMyListings(userId));
    }

    @GetMapping("/my/analytics")
    @Operation(summary = "View analytics for my listings")
    public ResponseEntity<?> analytics(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(listingService.getAnalytics(userId));
    }
}
