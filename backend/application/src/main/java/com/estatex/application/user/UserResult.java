package com.estatex.application.user;

import com.estatex.domain.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResult(
        UUID id,
        String email,
        String displayName,
        String phone,
        LocalDateTime createdAt,
        boolean active,
        long activeListingsCount
) {
    public static UserResult from(User user, long activeListingsCount) {
        return new UserResult(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhone(),
                user.getCreatedAt(),
                user.isActive(),
                activeListingsCount
        );
    }
}
