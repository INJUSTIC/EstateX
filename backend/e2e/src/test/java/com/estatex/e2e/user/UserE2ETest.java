package com.estatex.e2e.user;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserE2ETest extends E2ETestBase {

    @Test
    void shouldReturnCurrentUserProfileWhenAuthenticated() {
        //given
        UUID userId = registerUser("me@test.com", "Me User");

        //when
        var response = http.exchange("/api/users/me", HttpMethod.GET,
                new HttpEntity<>(userHeaders(userId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("me@test.com", response.getBody().get("email"));
        assertEquals("Me User", response.getBody().get("displayName"));
    }

    @Test
    void shouldUpdateCurrentUserDisplayNameAndPhone() {
        //given
        UUID userId = registerUser("update@test.com", "Old Name");
        var body = Map.of("displayName", "New Name", "phone", "+48123456789");

        //when
        var response = http.exchange("/api/users/me", HttpMethod.PUT,
                new HttpEntity<>(body, userHeaders(userId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("New Name", response.getBody().get("displayName"));
        assertEquals("+48123456789", response.getBody().get("phone"));
    }

    @Test
    void shouldReturnPublicProfileWithActiveListingsCount() {
        //given
        UUID ownerId = registerUser("owner@test.com", "Owner");
        createListing(ownerId);

        //when
        var response = http.getForEntity("/api/users/" + ownerId + "/profile", Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, ((Number) response.getBody().get("activeListingsCount")).intValue());
    }
}
