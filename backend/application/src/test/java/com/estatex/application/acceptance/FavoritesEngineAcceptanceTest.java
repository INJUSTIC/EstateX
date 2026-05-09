package com.estatex.application.acceptance;

import com.estatex.application.acceptance.fakes.TestingBackendSetup;
import com.estatex.application.favourite.FavouriteResult;
import com.estatex.application.favourite.FavouriteService;
import com.estatex.application.listing.ListingResult;
import com.estatex.application.listing.ListingService;
import com.estatex.application.user.UserService;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FavoritesEngineAcceptanceTest {

    private UserService userService;
    private ListingService listingService;
    private FavouriteService favouriteService;

    @BeforeEach
    void setUp() {
        TestingBackendSetup backend = new TestingBackendSetup();
        this.userService = backend.userService;
        this.listingService = backend.listingService;
        this.favouriteService = backend.favouriteService;
    }

    private UUID createTestUser(String email) {
        return userService.register(new UserService.RegisterCommand(email, "User")).id();
    }

    private ListingResult createTestListing(UUID ownerId) {
        return listingService.createListing(new ListingService.CreateListingCommand(
                ownerId, "Title", "Desc", "St", "City", "V", "0", "Pol", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.TEN, 10, 1, null
        ));
    }

    @Test
    void uc6_Favorites_SaveRemoveAndList() {
        UUID ownerId = createTestUser("owner@example.com");
        UUID buyerId = createTestUser("buyer@example.com");
        ListingResult listing1 = createTestListing(ownerId);
        ListingResult listing2 = createTestListing(ownerId);

        // Buyer saves to favorites
        FavouriteResult fav1 = favouriteService.saveFavourite(buyerId, listing1.id());
        assertNotNull(fav1.id());
        
        // Idempotent constraint (Save again)
        FavouriteResult duplicateFav = favouriteService.saveFavourite(buyerId, listing1.id());
        assertEquals(fav1.id(), duplicateFav.id());

        // Library check
        favouriteService.saveFavourite(buyerId, listing2.id());
        List<FavouriteResult> favs = favouriteService.getFavourites(buyerId);
        assertEquals(2, favs.size());

        // Removal
        favouriteService.removeFavourite(buyerId, listing1.id());
        List<FavouriteResult> updatedFavs = favouriteService.getFavourites(buyerId);
        assertEquals(1, updatedFavs.size());
        assertEquals(listing2.id(), updatedFavs.get(0).listingId());
    }
}
