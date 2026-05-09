package com.estatex.domain.favourite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteRepository {

    Favourite save(Favourite favourite);

    List<Favourite> findByUserId(UUID userId);

    Optional<Favourite> findByUserIdAndListingId(UUID userId, UUID listingId);

    void deleteByUserIdAndListingId(UUID userId, UUID listingId);

    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);
}
