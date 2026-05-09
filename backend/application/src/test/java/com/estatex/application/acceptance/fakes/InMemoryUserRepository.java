package com.estatex.application.acceptance.fakes;

import com.estatex.domain.user.User;
import com.estatex.domain.user.UserRepository;
import com.estatex.domain.listing.ListingStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> database = new HashMap<>();
    private InMemoryListingRepository listingRepository;

    public void setListingRepository(InMemoryListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public User save(User user) {
        database.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return database.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public long countActiveListings(UUID userId) {
        if (listingRepository == null) return 0;
        return listingRepository.findByOwnerId(userId).stream()
                .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
                .count();
    }
}
