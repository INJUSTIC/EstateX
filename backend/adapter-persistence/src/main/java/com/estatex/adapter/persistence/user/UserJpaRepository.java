package com.estatex.adapter.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(l) FROM ListingJpaEntity l WHERE l.ownerId = :ownerId AND l.status = 'ACTIVE'")
    long countActiveListings(UUID ownerId);
}
