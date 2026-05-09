package com.estatex.domain.user;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User aggregate root. No framework annotations — pure domain object.
 * Authentication is handled externally; no password stored here.
 */
public class User {

    private final UUID id;
    private String email;
    private String displayName;
    private String phone;
    private final LocalDateTime createdAt;
    private boolean active;

    public User(UUID id, String email, String displayName, String phone,
                LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
        this.createdAt = createdAt;
        this.active = active;
    }

    public static User create(String email, String displayName) {
        return new User(UUID.randomUUID(), email, displayName,
                null, LocalDateTime.now(), true);
    }

    // ── Business behaviour ────────────────────────────────────────────────────

    public void updateProfile(String displayName, String phone) {
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName;
        }
        this.phone = phone;
    }

    public void deactivate() {
        this.active = false;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPhone() { return phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
}
