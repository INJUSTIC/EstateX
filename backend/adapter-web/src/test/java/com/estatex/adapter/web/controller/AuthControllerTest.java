package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.user.UserResult;
import com.estatex.application.user.UserService;
import com.estatex.domain.exception.DomainException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterNewUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResult mockResult = new UserResult(userId, "john@example.com", "John Doe", null, LocalDateTime.now(), true, 0L);
        when(userService.register(any(UserService.RegisterCommand.class))).thenReturn(mockResult);

        var request = new AuthController.RegisterRequest("john@example.com", "John Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.displayName").value("John Doe"));
    }

    @Test
    void shouldFailToRegisterWithDuplicateEmail() throws Exception {
        when(userService.register(any(UserService.RegisterCommand.class)))
                .thenThrow(new DomainException("Email is already registered"));

        var request = new AuthController.RegisterRequest("taken@example.com", "Someone");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginExistingUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResult mockResult = new UserResult(userId, "john@example.com", "John Doe", null, LocalDateTime.now(), true, 2L);
        when(userService.login(eq("john@example.com"))).thenReturn(mockResult);

        var request = new AuthController.LoginRequest("john@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.displayName").value("John Doe"))
                .andExpect(jsonPath("$.activeListingsCount").value(2));
    }

    @Test
    void shouldFailToLoginWithNonExistentEmail() throws Exception {
        when(userService.login(eq("nonexistent@example.com")))
                .thenThrow(new DomainException("No user found with this email"));

        var request = new AuthController.LoginRequest("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
