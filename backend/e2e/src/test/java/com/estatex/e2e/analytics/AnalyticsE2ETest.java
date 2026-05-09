package com.estatex.e2e.analytics;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsE2ETest extends E2ETestBase {

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnAnalyticsWithCumulativeViewCountsForOwner() {
        //given
        UUID ownerId = registerUser("analytics1@test.com", "Owner");
        UUID listingId = createListing(ownerId);
        http.getForEntity("/api/listings/" + listingId, Map.class);
        http.getForEntity("/api/listings/" + listingId, Map.class);

        //when
        var response = http.exchange("/api/listings/my/analytics", HttpMethod.GET,
                new HttpEntity<>(userHeaders(ownerId)), List.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        var entry = (Map<String, Object>) response.getBody().get(0);
        assertEquals(listingId.toString(), entry.get("listingId").toString());
        assertEquals(2, ((Number) entry.get("viewCount")).intValue());
    }

    @Test
    void shouldReturnEmptyAnalyticsWhenOwnerHasNoListings() {
        //given
        UUID ownerId = registerUser("analytics2@test.com", "Owner");

        //when
        var response = http.exchange("/api/listings/my/analytics", HttpMethod.GET,
                new HttpEntity<>(userHeaders(ownerId)), List.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }
}
