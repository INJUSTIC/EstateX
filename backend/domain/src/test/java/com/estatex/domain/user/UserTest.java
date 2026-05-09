package com.estatex.domain.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Test
    void shouldCreateUserWithActiveStatusWhenCreated() {
        //given
        String email = "jan@example.com";
        String displayName = "Jan Kowalski";

        //when
        User user = User.create(email, displayName);

        //then
        assertTrue(user.isActive());
    }

    @Test
    void shouldCreateUserWithGeneratedIdWhenCreated() {
        //given
        String email = "jan@example.com";

        //when
        User user = User.create(email, "Jan");

        //then
        assertNotNull(user.getId());
    }

    @Test
    void shouldCreateUserWithCorrectEmailWhenCreated() {
        //given
        String email = "jan@example.com";

        //when
        User user = User.create(email, "Jan");

        //then
        assertEquals(email, user.getEmail());
    }

    @Test
    void shouldCreateUserWithCorrectDisplayNameWhenCreated() {
        //given
        String displayName = "Jan Kowalski";

        //when
        User user = User.create("jan@example.com", displayName);

        //then
        assertEquals(displayName, user.getDisplayName());
    }

    @Test
    void shouldCreateUserWithNullPhoneWhenCreated() {
        //when
        User user = User.create("jan@example.com", "Jan");

        //then
        assertNull(user.getPhone());
    }

    @Test
    void shouldCreateUserWithNonNullCreatedAtWhenCreated() {
        //when
        User user = User.create("jan@example.com", "Jan");

        //then
        assertNotNull(user.getCreatedAt());
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void shouldUpdateDisplayNameWhenProfileUpdated() {
        //given
        User user = User.create("jan@example.com", "Jan");

        //when
        user.updateProfile("Jan Kowalski", "+48123456789");

        //then
        assertEquals("Jan Kowalski", user.getDisplayName());
    }

    @Test
    void shouldUpdatePhoneWhenProfileUpdated() {
        //given
        User user = User.create("jan@example.com", "Jan");

        //when
        user.updateProfile("Jan", "+48123456789");

        //then
        assertEquals("+48123456789", user.getPhone());
    }

    @Test
    void shouldKeepExistingDisplayNameWhenBlankNameGiven() {
        //given
        User user = User.create("jan@example.com", "Jan Kowalski");

        //when
        user.updateProfile("  ", "+48123456789");

        //then
        assertEquals("Jan Kowalski", user.getDisplayName());
    }

    @Test
    void shouldClearPhoneWhenNullPhoneGiven() {
        //given
        User user = User.create("jan@example.com", "Jan");
        user.updateProfile("Jan", "+48123456789");

        //when
        user.updateProfile("Jan", null);

        //then
        assertNull(user.getPhone());
    }

    // ── deactivate ────────────────────────────────────────────────────────────

    @Test
    void shouldBeInactiveWhenDeactivated() {
        //given
        User user = User.create("jan@example.com", "Jan");

        //when
        user.deactivate();

        //then
        assertFalse(user.isActive());
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    @Test
    void shouldInitialiseAllFieldsWhenConstructedDirectly() {
        //given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        //when
        User user = new User(id, "jan@example.com", "Jan", "+48111", now, true);

        //then
        assertAll(
                () -> assertEquals(id, user.getId()),
                () -> assertEquals("jan@example.com", user.getEmail()),
                () -> assertEquals("Jan", user.getDisplayName()),
                () -> assertEquals("+48111", user.getPhone()),
                () -> assertEquals(now, user.getCreatedAt()),
                () -> assertTrue(user.isActive())
        );
    }
}
