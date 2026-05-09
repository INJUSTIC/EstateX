package com.estatex.adapter.persistence.favourite;

import com.estatex.domain.favourite.Favourite;
import com.estatex.domain.favourite.FavouriteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FavouriteRepositoryAdapter implements FavouriteRepository {
    private final FavouriteJpaRepository jpa;
    public FavouriteRepositoryAdapter(FavouriteJpaRepository jpa) { this.jpa = jpa; }

    @Override public Favourite save(Favourite f) {
        var e = FavouriteJpaEntity.builder().id(f.getId()).userId(f.getUserId())
                .listingId(f.getListingId()).savedAt(f.getSavedAt()).build();
        var s = jpa.save(e);
        return new Favourite(s.getId(), s.getUserId(), s.getListingId(), s.getSavedAt());
    }
    @Override public List<Favourite> findByUserId(UUID userId) {
        return jpa.findByUserId(userId).stream()
                .map(e -> new Favourite(e.getId(), e.getUserId(), e.getListingId(), e.getSavedAt()))
                .collect(Collectors.toList());
    }
    @Override public Optional<Favourite> findByUserIdAndListingId(UUID userId, UUID listingId) {
        return jpa.findByUserIdAndListingId(userId, listingId)
                .map(e -> new Favourite(e.getId(), e.getUserId(), e.getListingId(), e.getSavedAt()));
    }
    @Override @Transactional public void deleteByUserIdAndListingId(UUID userId, UUID listingId) {
        jpa.deleteByUserIdAndListingId(userId, listingId);
    }
    @Override public boolean existsByUserIdAndListingId(UUID userId, UUID listingId) {
        return jpa.existsByUserIdAndListingId(userId, listingId);
    }
}
