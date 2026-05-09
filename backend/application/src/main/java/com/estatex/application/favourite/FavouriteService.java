package com.estatex.application.favourite;

import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.favourite.Favourite;
import com.estatex.domain.favourite.FavouriteRepository;
import com.estatex.domain.listing.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final ListingRepository listingRepository;

    public FavouriteService(FavouriteRepository favouriteRepository,
                            ListingRepository listingRepository) {
        this.favouriteRepository = favouriteRepository;
        this.listingRepository = listingRepository;
    }

    // ── UC-6.1  Save favourite ────────────────────────────────────────────────

    public FavouriteResult saveFavourite(UUID userId, UUID listingId) {
        listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        if (favouriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            // Idempotent: return existing
            return favouriteRepository.findByUserIdAndListingId(userId, listingId)
                    .map(FavouriteResult::from)
                    .orElseThrow();
        }

        var fav = Favourite.create(userId, listingId);
        fav = favouriteRepository.save(fav);
        return FavouriteResult.from(fav);
    }

    // ── UC-6.2  Remove favourite ──────────────────────────────────────────────

    public void removeFavourite(UUID userId, UUID listingId) {
        favouriteRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    // ── UC-6.3  Get all favourites ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FavouriteResult> getFavourites(UUID userId) {
        return favouriteRepository.findByUserId(userId).stream()
                .map(FavouriteResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isFavourite(UUID userId, UUID listingId) {
        return favouriteRepository.existsByUserIdAndListingId(userId, listingId);
    }
}
