package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.user.UserResult;
import com.estatex.application.user.UserService;
import com.estatex.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = UserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class UserControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/users/me ─────────────────────────────────────────────────────

    @Test
    void shouldReturnFullUserContractForGetMe() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.getCurrentUser(userId)).thenReturn(
                new UserResult(userId, "user@example.com", "User One", "+48111222333",
                        LocalDateTime.of(2025, 1, 1, 12, 0), true, 5L));

        /// when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.displayName").value("User One"))
                .andExpect(jsonPath("$.phone").value("+48111222333"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.activeListingsCount").value(5))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnNullPhoneAsNullInContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.getCurrentUser(userId)).thenReturn(
                new UserResult(userId, "u@b.com", "User", null, LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").isEmpty());
    }

    @Test
    void shouldReturnErrorWithMessageWhenUserNotFound() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.getCurrentUser(userId))
                .thenThrow(new UserNotFoundException(userId));

        /// when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnCreatedAtFieldForProfile() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.getCurrentUser(userId)).thenReturn(
                new UserResult(userId, "a@b.com", "User", null,
                        LocalDateTime.of(2025, 3, 15, 10, 30), true, 1L));

        /// when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    // ── PUT /api/users/me ─────────────────────────────────────────────────────

    @Test
    void shouldReturnUpdatedProfileWithAllFields() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.updateProfile(any())).thenReturn(
                new UserResult(userId, "user@example.com", "New Name", "+48999888777",
                        LocalDateTime.now(), true, 2L));

        /// when & then
        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"displayName":"New Name","phone":"+48999888777"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Name"))
                .andExpect(jsonPath("$.phone").value("+48999888777"))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void shouldReturnUpdatedProfileWithNullPhone() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.updateProfile(any())).thenReturn(
                new UserResult(userId, "user@example.com", "Updated", null,
                        LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"displayName":"Updated"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").isEmpty());
    }

    @Test
    void shouldAcceptUpdateWithNullPhone() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.updateProfile(any())).thenReturn(
                new UserResult(userId, "user@example.com", "Updated", null,
                        LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"displayName":"Updated"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").isEmpty());
    }

    // ── GET /api/users/{userId}/profile ───────────────────────────────────────

    @Test
    void shouldReturnPublicProfileContract() throws Exception {
        /// given
        UUID publicId = UUID.randomUUID();
        when(userService.getPublicProfile(publicId)).thenReturn(
                new UserResult(publicId, "public@example.com", "PublicUser", null,
                        LocalDateTime.now(), true, 10L));

        /// when & then
        mockMvc.perform(get("/api/users/{userId}/profile", publicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(publicId.toString()))
                .andExpect(jsonPath("$.displayName").value("PublicUser"))
                .andExpect(jsonPath("$.activeListingsCount").value(10));
    }

    @Test
    void shouldReturn404WhenPublicProfileNotFound() throws Exception {
        /// given
        UUID unknownId = UUID.randomUUID();
        when(userService.getPublicProfile(unknownId))
                .thenThrow(new UserNotFoundException(unknownId));

        /// when & then
        mockMvc.perform(get("/api/users/{userId}/profile", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnJsonContentTypeForProfile() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.getCurrentUser(userId)).thenReturn(
                new UserResult(userId, "a@b.com", "A", null, LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
