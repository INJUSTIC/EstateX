package com.estatex.e2e.listing;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingSearchE2ETest extends E2ETestBase {

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnAllActiveListingsWhenNoFiltersApplied() {
        //given
        UUID ownerId = registerUser("s1@test.com", "Owner");
        createListing(ownerId, "L1", "Warsaw", "APARTMENT", "RENT", 1000);
        createListing(ownerId, "L2", "Krakow", "HOUSE", "SALE", 500000);

        //when
        var response = http.getForEntity("/api/listings", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, ((List<?>) response.getBody().get("items")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterListingsByCityAndPropertyType() {
        //given
        UUID ownerId = registerUser("s2@test.com", "Owner");
        createListing(ownerId, "KA", "Krakow", "APARTMENT", "RENT", 1500);
        createListing(ownerId, "WA", "Warsaw", "APARTMENT", "RENT", 2000);
        createListing(ownerId, "KH", "Krakow", "HOUSE", "SALE", 500000);

        //when
        var response = http.getForEntity("/api/listings?city=Krakow&propertyType=APARTMENT", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, ((List<?>) response.getBody().get("items")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterListingsByTransactionType() {
        //given
        UUID ownerId = registerUser("s3@test.com", "Owner");
        createListing(ownerId, "Rent", "Wroclaw", "APARTMENT", "RENT", 1500);
        createListing(ownerId, "Sale", "Wroclaw", "APARTMENT", "SALE", 300000);

        //when
        var response = http.getForEntity("/api/listings?transactionType=RENT", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, ((List<?>) response.getBody().get("items")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterListingsByPriceRange() {
        //given
        UUID ownerId = registerUser("s4@test.com", "Owner");
        createListing(ownerId, "Cheap", "Gdansk", "APARTMENT", "RENT", 700);
        createListing(ownerId, "Expensive", "Gdansk", "APARTMENT", "RENT", 1500);

        //when
        var response = http.getForEntity("/api/listings?minPrice=600&maxPrice=1000", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, ((List<?>) response.getBody().get("items")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPaginateListings() {
        //given
        UUID ownerId = registerUser("s5@test.com", "Owner");
        for (int i = 0; i < 3; i++) {
            createListing(ownerId, "L" + i, "Poznan", "APARTMENT", "RENT", 1000 + i * 100);
        }

        //when
        var response = http.getForEntity("/api/listings?page=0&size=2", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, ((List<?>) response.getBody().get("items")).size());
        assertEquals(3L, ((Number) response.getBody().get("totalElements")).longValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterListingsByKeyword() {
        //given
        UUID ownerId = registerUser("s6@test.com", "Owner");
        createListing(ownerId, "Sunny Apartment", "Warsaw", "APARTMENT", "RENT", 2000);
        createListing(ownerId, "Dark Basement", "Warsaw", "APARTMENT", "RENT", 800);

        //when
        var response = http.getForEntity("/api/listings?keyword=sunny", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, ((List<?>) response.getBody().get("items")).size());
    }

}
