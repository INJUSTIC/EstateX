package com.estatex.application.acceptance;

import com.estatex.application.acceptance.fakes.TestingBackendSetup;
import com.estatex.application.listing.ListingResult;
import com.estatex.application.listing.ListingService;
import com.estatex.application.user.UserService;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MediaPhotographyAcceptanceTest {

    private UserService userService;
    private ListingService listingService;

    @BeforeEach
    void setUp() {
        TestingBackendSetup backend = new TestingBackendSetup();
        this.userService = backend.userService;
        this.listingService = backend.listingService;
    }

    private UUID createTestUser() {
        return userService.register(new UserService.RegisterCommand("owner@example.com", "Owner")).id();
    }

    private ListingResult createTestListing(UUID ownerId) {
        return listingService.createListing(new ListingService.CreateListingCommand(
                ownerId, "Title", "Desc", "St", "City", "V", "0", "Pol", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.TEN, 10, 1, null
        ));
    }

    @Test
    void uc3_2_CoverMechanics_FirstPhotoIsCoverAndDeletionPromotesNext() {
        UUID ownerId = createTestUser();
        ListingResult listing = createTestListing(ownerId);

        // Upload first photo -> becomes cover
        ListingService.UploadPhotoCommand upload1 = new ListingService.UploadPhotoCommand(
                listing.id(), ownerId, "img1.png", new ByteArrayInputStream(new byte[]{1}), "image/png"
        );
        listing = listingService.uploadPhoto(upload1);

        assertEquals(1, listing.photos().size());
        assertTrue(listing.photos().get(0).cover());
        UUID photo1Id = listing.photos().get(0).id();

        // Upload second photo
        ListingService.UploadPhotoCommand upload2 = new ListingService.UploadPhotoCommand(
                listing.id(), ownerId, "img2.png", new ByteArrayInputStream(new byte[]{2}), "image/png"
        );
        listing = listingService.uploadPhoto(upload2);
        
        // Second photo is NOT cover
        assertFalse(listing.photos().stream().filter(p -> p.url().contains("img2")).findFirst().get().cover());
        UUID photo2Id = listing.photos().stream().filter(p -> p.url().contains("img2")).findFirst().get().id();

        // Delete first (cover) photo
        listing = listingService.deletePhoto(listing.id(), ownerId, photo1Id);

        // Assert second photo is now cover
        assertEquals(1, listing.photos().size());
        assertTrue(listing.photos().get(0).cover());
        assertEquals(photo2Id, listing.photos().get(0).id());
    }

    @Test
    void uc3_2_PhotoManagement_Max20PhotosAllowed() {
        UUID ownerId = createTestUser();
        ListingResult listing = createTestListing(ownerId);

        // Upload 20 photos
        for (int i = 0; i < 20; i++) {
            ListingService.UploadPhotoCommand upload = new ListingService.UploadPhotoCommand(
                    listing.id(), ownerId, "img" + i + ".png", new ByteArrayInputStream(new byte[]{1}), "image/png"
            );
            listing = listingService.uploadPhoto(upload);
        }

        assertEquals(20, listing.photos().size());

        // Attempting to upload 21st photo throws IllegalArgumentException (Domain rule)
        ListingService.UploadPhotoCommand upload21 = new ListingService.UploadPhotoCommand(
                listing.id(), ownerId, "img21.png", new ByteArrayInputStream(new byte[]{1}), "image/png"
        );

        assertThrows(IllegalStateException.class, () -> listingService.uploadPhoto(upload21));
    }
}
