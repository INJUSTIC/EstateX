package com.estatex.application.acceptance.fakes;

import com.estatex.domain.favourite.Favourite;
import com.estatex.domain.favourite.FavouriteRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryFavouriteRepository implements FavouriteRepository {
    private final Map<UUID, Favourite> database = new HashMap<>();

    @Override
    public Favourite save(Favourite favourite) {
        database.put(favourite.getId(), favourite);
        return favourite;
    }

    @Override
    public List<Favourite> findByUserId(UUID userId) {
        return database.values().stream()
                .filter(f -> f.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Favourite> findByUserIdAndListingId(UUID userId, UUID listingId) {
        return database.values().stream()
                .filter(f -> f.getUserId().equals(userId) && f.getListingId().equals(listingId))
                .findFirst();
    }

    @Override
    public void deleteByUserIdAndListingId(UUID userId, UUID listingId) {
        findByUserIdAndListingId(userId, listingId).ifPresent(f -> database.remove(f.getId()));
    }

    @Override
    public boolean existsByUserIdAndListingId(UUID userId, UUID listingId) {
        return findByUserIdAndListingId(userId, listingId).isPresent();
    }
}
