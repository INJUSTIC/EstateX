package com.estatex.adapter.persistence.user;

import com.estatex.domain.user.User;
import com.estatex.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        return toDomain(jpa.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public long countActiveListings(UUID userId) {
        return jpa.countActiveListings(userId);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private UserJpaEntity toEntity(User u) {
        return UserJpaEntity.builder()
                .id(u.getId())
                .email(u.getEmail())
                .displayName(u.getDisplayName())
                .phone(u.getPhone())
                .createdAt(u.getCreatedAt())
                .active(u.isActive())
                .build();
    }

    private User toDomain(UserJpaEntity e) {
        return new User(e.getId(), e.getEmail(), e.getDisplayName(),
                e.getPhone(), e.getCreatedAt(), e.isActive());
    }
}
