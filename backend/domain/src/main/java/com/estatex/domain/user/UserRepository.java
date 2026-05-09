package com.estatex.domain.user;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port (driven port) — defines persistence contract for User.
 * Implemented in adapter-persistence.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Count only ACTIVE listings owned by this user. */
    long countActiveListings(UUID userId);
}
