package com.estatex.adapter.web.controller;

import com.estatex.application.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    record UpdateProfileRequest(@NotBlank String displayName, String phone) {}

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<?> getMe(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<?> updateMe(@RequestHeader("X-User-Id") UUID userId,
                                      @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(
                new UserService.UpdateProfileCommand(userId, req.displayName(), req.phone())));
    }


    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get public profile of any user")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getPublicProfile(userId));
    }
}
