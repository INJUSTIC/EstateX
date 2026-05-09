package com.estatex.application.acceptance;

import com.estatex.application.acceptance.fakes.TestingBackendSetup;
import com.estatex.application.listing.ListingResult;
import com.estatex.application.listing.ListingService;
import com.estatex.application.user.UserResult;
import com.estatex.application.user.UserService;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.listing.ListingSearchCriteria;
import com.estatex.domain.listing.ListingStatus;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingOperationsAcceptanceTest {

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

    @Test
    void uc3_1_Creation_UserCanCreateListing() {
        UUID ownerId = createTestUser();

        ListingService.CreateListingCommand cmd = new ListingService.CreateListingCommand(
                ownerId, "Beautiful Apartment", "Spacious", "Main St 1", "Warsaw", "Mazowieckie", "00-001", "Poland",
                52.2297, 21.0122, PropertyType.APARTMENT, ListingTransactionType.RENT,
                BigDecimal.valueOf(3000), 50.0, 2, null
        );

        ListingResult result = listingService.createListing(cmd);

        assertNotNull(result.id());
        assertEquals("Beautiful Apartment", result.title());
        assertEquals(ListingStatus.ACTIVE, result.status());
    }

    @Test
    void uc3_3_Updating_OnlyOwnerCanUpdateActiveListing() {
        UUID ownerId = createTestUser();
        UUID otherUserId = userService.register(new UserService.RegisterCommand("other@example.com", "Other")).id();

        ListingResult listing = listingService.createListing(new ListingService.CreateListingCommand(
                ownerId, "Apartment", "Desc", "St 1", "City", "Voi", "00-0", "Poland", null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE, BigDecimal.valueOf(100000), 100, 4, null
        ));

        // Owner updates successfully
        ListingService.UpdateListingCommand updateCmd = new ListingService.UpdateListingCommand(
                listing.id(), ownerId, "Updated House", "Desc", "St 1", "City", "Voi", "00-0", "Poland", null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE, BigDecimal.valueOf(120000), 100, 4, null
        );
        ListingResult updated = listingService.updateListing(updateCmd);
        assertEquals("Updated House", updated.title());

        // Other user attempts update
        ListingService.UpdateListingCommand otherUpdateCmd = new ListingService.UpdateListingCommand(
                listing.id(), otherUserId, "Hacked House", "Desc", "St 1", "City", "Voi", "00-0", "Poland", null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE, BigDecimal.valueOf(10), 100, 4, null
        );

        assertThrows(AccessDeniedException.class, () -> listingService.updateListing(otherUpdateCmd));
    }

    @Test
    void uc3_4_UC3_5_DeletionAndLifecycle_OwnerCanChangeStatusAndDelete() {
        UUID ownerId = createTestUser();
        ListingResult listing = listingService.createListing(new ListingService.CreateListingCommand(
                ownerId, "Apartment", "Desc", "St", "City", "V", "0", "Pol", null, null, PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.TEN, 10, 1, null
        ));

        // Lifecycle change to ARCHIVED
        ListingResult archived = listingService.changeStatus(listing.id(), ownerId, ListingStatus.ARCHIVED);
        assertEquals(ListingStatus.ARCHIVED, archived.status());

        // Owner deletes
        listingService.deleteListing(listing.id(), ownerId, false);

        // Fetching deleted listing fails
        assertThrows(RuntimeException.class, () -> listingService.getListingDetail(listing.id()));
    }

    @Test
    void uc4_UC4_6_DiscoveryAndSearch_AndViewsCalculation() {
        UUID ownerId = createTestUser();
        for (int i = 0; i < 5; i++) {
            listingService.createListing(new ListingService.CreateListingCommand(
                    ownerId, "Apartment " + i, "Desc", "St", "Krakow", "V", "0", "Pol", null, null, PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.valueOf(1000 * (i + 1)), 10, 1, null
            ));
        }

        // Search in Krakow under 3500
        ListingSearchCriteria criteria = new ListingSearchCriteria("Apartment", "Krakow", null, null, BigDecimal.valueOf(3500), null, null, null, null, null, null, ListingSearchCriteria.SortBy.PRICE, ListingSearchCriteria.SortDirection.ASC, null, null);
        ListingService.ListingPage page = listingService.searchListings(criteria, 0, 10);
        
        // 1000, 2000, 3000 -> 3 apartments
        assertEquals(3, page.totalElements());

        // View one listing detail twice
        UUID listingId = page.items().get(0).id();
        listingService.getListingDetail(listingId);
        ListingResult detail = listingService.getListingDetail(listingId);

        // Assert view count incremented
        assertEquals(2, detail.viewCount());
    }
}
