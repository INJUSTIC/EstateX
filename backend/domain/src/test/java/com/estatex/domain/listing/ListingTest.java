package com.estatex.domain.listing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingTest {

    private static final UUID OWNER_ID = UUID.randomUUID();

    private Listing buildListing() {
        return Listing.create("Kawalerka w centrum", "Opis",
                Address.of(null, "Warszawa", null, null, "Poland", null, null),
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new Money(new BigDecimal("2000.00")), 25.0, 1, OWNER_ID, null);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void shouldCreateListingWithActiveStatusWhenCreated() {
        //when
        Listing listing = buildListing();

        //then
        assertEquals(ListingStatus.ACTIVE, listing.getStatus());
    }

    @Test
    void shouldCreateListingWithGeneratedIdWhenCreated() {
        //when
        Listing listing = buildListing();

        //then
        assertNotNull(listing.getId());
    }

    @Test
    void shouldCreateListingWithZeroViewCountWhenCreated() {
        //when
        Listing listing = buildListing();

        //then
        assertEquals(0, listing.getViewCount());
    }

    @Test
    void shouldCreateListingWithEmptyPhotosWhenCreated() {
        //when
        Listing listing = buildListing();

        //then
        assertTrue(listing.getPhotos().isEmpty());
    }

    @Test
    void shouldCreateListingWithCorrectTransactionTypeWhenCreated() {
        //when
        Listing listing = buildListing();

        //then
        assertEquals(ListingTransactionType.RENT, listing.getTransactionType());
    }

    @Test
    void shouldCreateListingWithAllFieldsWhenCreated() {
        //when
        Listing listing = buildListing();

        //then
        assertNotNull(listing.getAddress());
        assertEquals(25.0, listing.getAreaSqMeters());
        assertEquals("Opis", listing.getDescription());
        assertEquals(1, listing.getNumberOfRooms());
        assertEquals(OWNER_ID, listing.getOwnerId());
        assertEquals(PropertyType.APARTMENT, listing.getPropertyType());
        assertNotNull(listing.getCreatedAt());
        assertNotNull(listing.getUpdatedAt());
        assertNotNull(listing.getTitle());
        assertNotNull(listing.getPrice());
        assertEquals("2000.00", listing.getPrice().amount().toPlainString());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateTitleWhenUpdated() {
        //given
        Listing listing = buildListing();

        //when
        listing.update("Nowy tytuł", "Nowy opis",
                Address.of(null, "Kraków", null, null, "Poland", null, null),
                PropertyType.HOUSE, ListingTransactionType.SALE,
                new Money(new BigDecimal("500000.00")), 80.0, 4, null);

        //then
        assertEquals("Nowy tytuł", listing.getTitle());
    }

    @Test
    void shouldUpdateTransactionTypeWhenUpdated() {
        //given
        Listing listing = buildListing();

        //when
        listing.update("Tytuł", "Opis",
                Address.of(null, "Kraków", null, null, "Poland", null, null),
                PropertyType.HOUSE, ListingTransactionType.SALE,
                new Money(new BigDecimal("500000.00")), 80.0, 4, null);

        //then
        assertEquals(ListingTransactionType.SALE, listing.getTransactionType());
    }

    @Test
    void shouldSetUpdatedAtWhenUpdated() {
        //given
        Listing listing = buildListing();
        var before = listing.getUpdatedAt();

        //when
        listing.update("Tytuł", "Opis",
                listing.getAddress(), listing.getPropertyType(), listing.getTransactionType(),
                listing.getPrice(), listing.getAreaSqMeters(), listing.getNumberOfRooms(), null);

        //then
        assertNotNull(listing.getUpdatedAt());
    }

    // ── changeStatus ──────────────────────────────────────────────────────────

    @Test
    void shouldChangeStatusToArchivedWhenStatusChanged() {
        //given
        Listing listing = buildListing();

        //when
        listing.changeStatus(ListingStatus.ARCHIVED);

        //then
        assertEquals(ListingStatus.ARCHIVED, listing.getStatus());
    }

    @Test
    void shouldChangeStatusToRentedWhenStatusChanged() {
        //given
        Listing listing = buildListing();

        //when
        listing.changeStatus(ListingStatus.RENTED);

        //then
        assertEquals(ListingStatus.RENTED, listing.getStatus());
    }

    // ── addPhoto ──────────────────────────────────────────────────────────────

    @Test
    void shouldAddPhotoAndSetAsCoverWhenFirstPhotoAdded() {
        //given
        Listing listing = buildListing();

        //when
        Photo photo = listing.addPhoto("http://example.com/img.jpg");

        //then
        assertTrue(photo.isCover());
    }

    @Test
    void shouldAddSecondPhotoAsNonCoverWhenSecondPhotoAdded() {
        //given
        Listing listing = buildListing();
        listing.addPhoto("http://example.com/img1.jpg");

        //when
        Photo photo = listing.addPhoto("http://example.com/img2.jpg");

        //then
        assertFalse(photo.isCover());
    }

    @Test
    void shouldHaveTwoPhotosWhenTwoPhotosAdded() {
        //given
        Listing listing = buildListing();

        //when
        listing.addPhoto("http://example.com/img1.jpg");
        listing.addPhoto("http://example.com/img2.jpg");

        //then
        assertEquals(2, listing.getPhotos().size());
    }

    @Test
    void shouldThrowWhenMaxPhotosExceeded() {
        //given
        Listing listing = buildListing();
        for (int i = 0; i < 20; i++) {
            listing.addPhoto("http://example.com/img" + i + ".jpg");
        }

        //when / then
        assertThrows(IllegalStateException.class,
                () -> listing.addPhoto("http://example.com/img21.jpg"));
    }

    // ── removePhoto ───────────────────────────────────────────────────────────

    @Test
    void shouldRemovePhotoWhenValidPhotoIdGiven() {
        //given
        Listing listing = buildListing();
        Photo photo = listing.addPhoto("http://example.com/img.jpg");

        //when
        listing.removePhoto(photo.getId());

        //then
        assertTrue(listing.getPhotos().isEmpty());
    }

    @Test
    void shouldPromoteSecondPhotoToCoverWhenCoverPhotoRemoved() {
        //given
        Listing listing = buildListing();
        Photo cover = listing.addPhoto("http://example.com/img1.jpg");
        listing.addPhoto("http://example.com/img2.jpg");

        //when
        listing.removePhoto(cover.getId());

        //then
        assertTrue(listing.getPhotos().get(0).isCover());
        assertNotEquals(cover.getId(), listing.getPhotos().get(0).getId());
    }

    @Test
    void shouldThrowWhenRemovingNonExistentPhoto() {
        //given
        Listing listing = buildListing();
        listing.addPhoto("http://example.com/img1.jpg");

        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> listing.removePhoto(UUID.randomUUID()));
    }

    // ── setCoverPhoto ─────────────────────────────────────────────────────────

    @Test
    void shouldSetGivenPhotoAsCoverWhenSetCoverPhotoCalled() {
        //given
        Listing listing = buildListing();
        listing.addPhoto("http://example.com/img1.jpg");
        Photo second = listing.addPhoto("http://example.com/img2.jpg");

        //when
        listing.setCoverPhoto(second.getId());

        //then
        assertTrue(listing.getPhotos().stream()
                .filter(p -> p.getId().equals(second.getId()))
                .findFirst().get().isCover());
    }

    @Test
    void shouldUnmarkPreviousCoverWhenNewCoverPhotoSet() {
        //given
        Listing listing = buildListing();
        Photo first = listing.addPhoto("http://example.com/img1.jpg");
        Photo second = listing.addPhoto("http://example.com/img2.jpg");

        //when
        listing.setCoverPhoto(second.getId());

        //then
        assertFalse(listing.getPhotos().stream()
                .filter(p -> p.getId().equals(first.getId()))
                .findFirst().get().isCover());
    }

    @Test
    void shouldThrowWhenSettingCoverForNonExistentPhoto() {
        //given
        Listing listing = buildListing();

        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> listing.setCoverPhoto(UUID.randomUUID()));
    }

    // ── incrementViewCount ────────────────────────────────────────────────────

    @Test
    void shouldIncrementViewCountByOneWhenCalled() {
        //given
        Listing listing = buildListing();

        //when
        listing.incrementViewCount();

        //then
        assertEquals(1, listing.getViewCount());
    }

    // ── isOwnedBy ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenOwnerIdMatches() {
        //given
        Listing listing = buildListing();

        //when
        boolean result = listing.isOwnedBy(OWNER_ID);

        //then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenOwnerIdDoesNotMatch() {
        //given
        Listing listing = buildListing();

        //when
        boolean result = listing.isOwnedBy(UUID.randomUUID());

        //then
        assertFalse(result);
    }

    // ── photos are unmodifiable ───────────────────────────────────────────────

    @Test
    void shouldThrowWhenTryingToModifyPhotosDirectly() {
        //given
        Listing listing = buildListing();

        //when / then
        assertThrows(UnsupportedOperationException.class,
                () -> listing.getPhotos().add(null));
    }
}
