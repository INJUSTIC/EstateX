package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.user.UserResult;
import com.estatex.application.user.UserService;
import com.estatex.domain.exception.DomainException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class AuthControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Register endpoint ─────────────────────────────────────────────────────

    @Test
    void shouldReturnFullUserContractWhenRegisterSucceeds() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        when(userService.register(any())).thenReturn(
                new UserResult(userId, "alice@example.com", "Alice", null, now, true, 0L));

        /// when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"alice@example.com","displayName":"Alice"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.displayName").value("Alice"))
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.activeListingsCount").value(0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnJsonContentTypeOnRegister() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.register(any())).thenReturn(
                new UserResult(userId, "a@b.com", "AB", null, LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"a@b.com","displayName":"AB"}
                            """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnUserIdInRegisterResponse() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.register(any())).thenReturn(
                new UserResult(userId, "new@mail.com", "NewUser", null, LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"new@mail.com","displayName":"NewUser"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldReturnActiveUserOnRegister() throws Exception {
        /// given
        when(userService.register(any())).thenReturn(
                new UserResult(UUID.randomUUID(), "a@b.com", "Test", null, LocalDateTime.now(), true, 0L));

        /// when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"a@b.com","displayName":"Test"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.activeListingsCount").value(0));
    }

    @Test
    void shouldReturn400WhenDuplicateEmailOnRegister() throws Exception {
        /// given
        when(userService.register(any())).thenThrow(new DomainException("Email is already registered"));

        /// when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"taken@example.com","displayName":"Someone"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void shouldReturnErrorResponseBodyOnDuplicateEmail() throws Exception {
        /// given
        when(userService.register(any())).thenThrow(new DomainException("Email is already registered"));

        /// when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"taken@a.com","displayName":"Someone"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already registered"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── Login endpoint ────────────────────────────────────────────────────────

    @Test
    void shouldReturnFullUserContractWhenLoginSucceeds() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(userService.login(eq("john@example.com"))).thenReturn(
                new UserResult(userId, "john@example.com", "John Doe", "+48123456789",
                        LocalDateTime.now(), true, 3L));

        /// when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"john@example.com"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.displayName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("+48123456789"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.activeListingsCount").value(3));
    }

    @Test
    void shouldReturn400WhenLoginEmailNotFound() throws Exception {
        /// given
        when(userService.login(eq("ghost@example.com")))
                .thenThrow(new DomainException("No user found with this email"));

        /// when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"ghost@example.com"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No user found with this email"));
    }

    @Test
    void shouldReturnLoginErrorResponseWithTimestamp() throws Exception {
        /// given
        when(userService.login(eq("ghost@a.com")))
                .thenThrow(new DomainException("No user found with this email"));

        /// when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"ghost@a.com"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
