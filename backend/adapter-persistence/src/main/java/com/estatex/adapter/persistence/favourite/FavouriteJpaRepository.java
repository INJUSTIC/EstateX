package com.estatex.adapter.persistence.favourite;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteJpaRepository extends JpaRepository<FavouriteJpaEntity, UUID> {
    List<FavouriteJpaEntity> findByUserId(UUID userId);
    Optional<FavouriteJpaEntity> findByUserIdAndListingId(UUID userId, UUID listingId);
    void deleteByUserIdAndListingId(UUID userId, UUID listingId);
    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);
}
