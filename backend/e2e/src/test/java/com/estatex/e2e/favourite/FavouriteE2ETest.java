package com.estatex.e2e.favourite;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FavouriteE2ETest extends E2ETestBase {

    @Test
    void shouldAddListingToFavouritesWhenNotAlreadySaved() {
        //given
        UUID ownerId = registerUser("fav1o@test.com", "Owner");
        UUID buyerId = registerUser("fav1b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);

        //when
        var response = http.exchange("/api/favourites/" + listingId, HttpMethod.POST,
                new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(listingId.toString(), response.getBody().get("listingId").toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBeIdempotentWhenAddingSameFavouriteTwice() {
        //given
        UUID ownerId = registerUser("fav2o@test.com", "Owner");
        UUID buyerId = registerUser("fav2b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        http.exchange("/api/favourites/" + listingId, HttpMethod.POST,
                new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //when
        var response = http.exchange("/api/favourites/" + listingId, HttpMethod.POST,
                new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        var favs = http.exchange("/api/favourites", HttpMethod.GET,
                new HttpEntity<>(userHeaders(buyerId)), List.class);
        assertEquals(1, favs.getBody().size());
    }

    @Test
    void shouldRemoveFavouriteWhenExists() {
        //given
        UUID ownerId = registerUser("fav3o@test.com", "Owner");
        UUID buyerId = registerUser("fav3b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        http.exchange("/api/favourites/" + listingId, HttpMethod.POST,
                new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //when
        http.exchange("/api/favourites/" + listingId, HttpMethod.DELETE,
                new HttpEntity<>(userHeaders(buyerId)), Void.class);
        var statusResponse = http.exchange("/api/favourites/" + listingId + "/status",
                HttpMethod.GET, new HttpEntity<>(userHeaders(buyerId)), Boolean.class);

        //then
        assertFalse(statusResponse.getBody());
    }

    @Test
    void shouldReturnFavouriteStatusTrueAfterSaving() {
        //given
        UUID ownerId = registerUser("fav4o@test.com", "Owner");
        UUID buyerId = registerUser("fav4b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        http.exchange("/api/favourites/" + listingId, HttpMethod.POST,
                new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //when
        var response = http.exchange("/api/favourites/" + listingId + "/status",
                HttpMethod.GET, new HttpEntity<>(userHeaders(buyerId)), Boolean.class);

        //then
        assertTrue(response.getBody());
    }

    @Test
    void shouldReturnFavouriteStatusFalseBeforeSaving() {
        //given
        UUID ownerId = registerUser("fav5o@test.com", "Owner");
        UUID buyerId = registerUser("fav5b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);

        //when
        var response = http.exchange("/api/favourites/" + listingId + "/status",
                HttpMethod.GET, new HttpEntity<>(userHeaders(buyerId)), Boolean.class);

        //then
        assertFalse(response.getBody());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnAllSavedFavourites() {
        //given
        UUID ownerId = registerUser("fav6o@test.com", "Owner");
        UUID buyerId = registerUser("fav6b@test.com", "Buyer");
        UUID l1 = createListing(ownerId);
        UUID l2 = createListing(ownerId);
        http.exchange("/api/favourites/" + l1, HttpMethod.POST, new HttpEntity<>(userHeaders(buyerId)), Map.class);
        http.exchange("/api/favourites/" + l2, HttpMethod.POST, new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //when
        var response = http.exchange("/api/favourites", HttpMethod.GET,
                new HttpEntity<>(userHeaders(buyerId)), List.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
    }
}
