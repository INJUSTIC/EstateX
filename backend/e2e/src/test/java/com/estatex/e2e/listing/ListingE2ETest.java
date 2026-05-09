package com.estatex.e2e.listing;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingE2ETest extends E2ETestBase {

    @Test
    void shouldCreateListingWhenAllFieldsValid() {
        //given
        UUID ownerId = registerUser("creator@test.com", "Creator");
        var body = Map.of("title", "Nice Flat", "city", "Warsaw", "country", "Poland",
                "propertyType", "APARTMENT", "transactionType", "SALE",
                "price", 500000, "areaSqMeters", 75.0, "numberOfRooms", 3);

        //when
        var response = http.exchange("/api/listings", HttpMethod.POST,
                new HttpEntity<>(body, userHeaders(ownerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("id"));
        assertEquals("Nice Flat", response.getBody().get("title"));
    }

    @Test
    void shouldReturnListingDetailAndIncrementViewCount() {
        //given
        UUID ownerId = registerUser("viewer@test.com", "Owner");
        UUID listingId = createListing(ownerId);

        //when
        http.getForEntity("/api/listings/" + listingId, Map.class);
        var response = http.getForEntity("/api/listings/" + listingId, Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, ((Number) response.getBody().get("viewCount")).intValue());
    }

    @Test
    void shouldUpdateListingWhenCalledByOwner() {
        //given
        UUID ownerId = registerUser("updater@test.com", "Owner");
        UUID listingId = createListing(ownerId);
        var body = Map.of("title", "Updated Title", "city", "Krakow", "country", "Poland",
                "propertyType", "APARTMENT", "transactionType", "RENT",
                "price", 2000, "areaSqMeters", 50.0, "numberOfRooms", 2);

        //when
        var response = http.exchange("/api/listings/" + listingId, HttpMethod.PUT,
                new HttpEntity<>(body, userHeaders(ownerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Updated Title", response.getBody().get("title"));
    }

    @Test
    void shouldReturn403WhenNonOwnerUpdatesListing() {
        //given
        UUID ownerId = registerUser("own3@test.com", "Owner");
        UUID strangerId = registerUser("str3@test.com", "Stranger");
        UUID listingId = createListing(ownerId);
        var body = Map.of("title", "Hijack", "city", "X", "country", "Poland",
                "propertyType", "APARTMENT", "transactionType", "RENT",
                "price", 1, "areaSqMeters", 10.0, "numberOfRooms", 1);

        //when
        var response = http.exchange("/api/listings/" + listingId, HttpMethod.PUT,
                new HttpEntity<>(body, userHeaders(strangerId)), Map.class);

        //then
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void shouldDeleteListingWhenCalledByOwner() {
        //given
        UUID ownerId = registerUser("deleter@test.com", "Owner");
        UUID listingId = createListing(ownerId);

        //when
        var deleteResponse = http.exchange("/api/listings/" + listingId, HttpMethod.DELETE,
                new HttpEntity<>(userHeaders(ownerId)), Void.class);
        var getResponse = http.getForEntity("/api/listings/" + listingId, Map.class);

        //then
        assertEquals(204, deleteResponse.getStatusCode().value());
        assertEquals(404, getResponse.getStatusCode().value());
    }

    @Test
    void shouldReturn403WhenNonOwnerDeletesListing() {
        //given
        UUID ownerId = registerUser("own4@test.com", "Owner");
        UUID strangerId = registerUser("str4@test.com", "Stranger");
        UUID listingId = createListing(ownerId);

        //when
        var response = http.exchange("/api/listings/" + listingId, HttpMethod.DELETE,
                new HttpEntity<>(userHeaders(strangerId)), Map.class);

        //then
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void shouldArchiveListingWhenOwnerChangesStatus() {
        //given
        UUID ownerId = registerUser("archiver@test.com", "Owner");
        UUID listingId = createListing(ownerId);

        //when
        var response = http.exchange("/api/listings/" + listingId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "ARCHIVED"), userHeaders(ownerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("ARCHIVED", response.getBody().get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnOwnerListingsOnMyEndpoint() {
        //given
        UUID ownerId = registerUser("my@test.com", "Owner");
        UUID otherId = registerUser("other@test.com", "Other");
        createListing(ownerId);
        createListing(otherId);

        //when
        var response = http.exchange("/api/listings/my", HttpMethod.GET,
                new HttpEntity<>(userHeaders(ownerId)), List.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldDeleteListingWithExistingConversationWhenCalledByOwner() {
        //given
        UUID ownerId = registerUser("del-conv-owner@test.com", "Owner");
        UUID buyerId = registerUser("del-conv-buyer@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        // buyer starts a conversation about this listing
        http.exchange("/api/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of("listingId", listingId.toString()), userHeaders(buyerId)), Map.class);

        //when
        var deleteResponse = http.exchange("/api/listings/" + listingId, HttpMethod.DELETE,
                new HttpEntity<>(userHeaders(ownerId)), Void.class);

        //then
        assertEquals(204, deleteResponse.getStatusCode().value());
    }
}
