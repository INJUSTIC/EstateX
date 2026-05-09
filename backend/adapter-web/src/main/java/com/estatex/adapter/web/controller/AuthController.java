package com.estatex.adapter.web.controller;

import com.estatex.application.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 2, max = 100) String displayName
    ) {}

    record LoginRequest(
            @Email @NotBlank String email
    ) {}

    @PostMapping("/register")
    @Operation(summary = "Register — returns the new user's UUID to use as X-User-Id")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.register(
                new UserService.RegisterCommand(req.email(), req.displayName())));
    }

    @PostMapping("/login")
    @Operation(summary = "Login — returns the existing user's UUID to use as X-User-Id")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(req.email()));
    }
}
