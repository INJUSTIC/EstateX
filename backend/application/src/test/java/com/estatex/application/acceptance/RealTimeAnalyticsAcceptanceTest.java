package com.estatex.application.acceptance;

import com.estatex.application.acceptance.fakes.TestingBackendSetup;
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

class RealTimeAnalyticsAcceptanceTest {

    private UserService userService;
    private ListingService listingService;

    @BeforeEach
    void setUp() {
        TestingBackendSetup backend = new TestingBackendSetup();
        this.userService = backend.userService;
        this.listingService = backend.listingService;
    }

    private UUID createTestUser(String email) {
        return userService.register(new UserService.RegisterCommand(email, "User")).id();
    }

    private ListingResult createTestListing(UUID ownerId, String title) {
        return listingService.createListing(new ListingService.CreateListingCommand(
                ownerId, title, "Desc", "St", "City", "V", "0", "Pol", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.TEN, 10, 1, null
        ));
    }

    @Test
    void uc11_Analytics_ViewCountsAggregateToOwnerDashboard() {
        UUID ownerId = createTestUser("owner@example.com");
        ListingResult listing1 = createTestListing(ownerId, "Prop 1");
        ListingResult listing2 = createTestListing(ownerId, "Prop 2");

        // Users view listings
        listingService.getListingDetail(listing1.id());
        listingService.getListingDetail(listing1.id());
        listingService.getListingDetail(listing1.id());

        listingService.getListingDetail(listing2.id());

        // Owner gets analytics
        List<ListingService.ListingAnalyticsResult> analytics = listingService.getAnalytics(ownerId);
        
        assertEquals(2, analytics.size());
        
        ListingService.ListingAnalyticsResult a1 = analytics.stream().filter(a -> a.listingId().equals(listing1.id())).findFirst().get();
        assertEquals(3, a1.viewCount());

        ListingService.ListingAnalyticsResult a2 = analytics.stream().filter(a -> a.listingId().equals(listing2.id())).findFirst().get();
        assertEquals(1, a2.viewCount());
    }
}
