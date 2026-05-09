package com.estatex.e2e.auth;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthE2ETest extends E2ETestBase {

    @Test
    void shouldRegisterNewUserWhenEmailIsUnique() {
        //given
        var body = Map.of("email", "user@test.com", "displayName", "Test User");

        //when
        var response = http.postForEntity("/api/auth/register", body, Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("id"));
        assertEquals("user@test.com", response.getBody().get("email"));
        assertEquals("Test User", response.getBody().get("displayName"));
    }

    @Test
    void shouldReturn400WhenRegisteringDuplicateEmail() {
        //given
        var body = Map.of("email", "dup@test.com", "displayName", "User");
        http.postForEntity("/api/auth/register", body, Map.class);

        //when
        var response = http.postForEntity("/api/auth/register", body, Map.class);

        //then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldLoginExistingUserByEmail() {
        //given
        var registered = http.postForObject("/api/auth/register",
                Map.of("email", "login@test.com", "displayName", "Login User"), Map.class);
        String expectedId = (String) registered.get("id");

        //when
        var response = http.postForEntity("/api/auth/login",
                Map.of("email", "login@test.com"), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedId, response.getBody().get("id"));
    }

    @Test
    void shouldReturn400WhenLoginEmailDoesNotExist() {
        //given / when
        var response = http.postForEntity("/api/auth/login",
                Map.of("email", "ghost@test.com"), Map.class);

        //then
        assertEquals(400, response.getStatusCode().value());
    }
}
