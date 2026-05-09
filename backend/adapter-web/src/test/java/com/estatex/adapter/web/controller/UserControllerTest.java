package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.user.UserResult;
import com.estatex.application.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
    value = UserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCurrentUserProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResult mockResult = new UserResult(userId, "john@example.com", "JohnDoe", "123456789", LocalDateTime.now(), true, 0L);
        Mockito.when(userService.getCurrentUser(userId)).thenReturn(mockResult);

        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("JohnDoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldUpdateCurrentUserProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResult mockResult = new UserResult(userId, "john@example.com", "NewName", "987654321", LocalDateTime.now(), true, 0L);
        Mockito.when(userService.updateProfile(any(UserService.UpdateProfileCommand.class))).thenReturn(mockResult);

        UserController.UpdateProfileRequest req = new UserController.UpdateProfileRequest("NewName", "987654321");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("NewName"))
                .andExpect(jsonPath("$.phone").value("987654321"));
    }

    @Test
    void shouldReturnPublicProfile() throws Exception {
        UUID publicId = UUID.randomUUID();
        UserResult mockResult = new UserResult(publicId, "public@example.com", "PublicUser", null, LocalDateTime.now(), true, 5L);
        Mockito.when(userService.getPublicProfile(publicId)).thenReturn(mockResult);

        mockMvc.perform(get("/api/users/{userId}/profile", publicId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("PublicUser"))
                .andExpect(jsonPath("$.activeListingsCount").value(5));
    }
}
