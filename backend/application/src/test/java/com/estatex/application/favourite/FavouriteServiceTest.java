package com.estatex.application.favourite;

import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.favourite.Favourite;
import com.estatex.domain.favourite.FavouriteRepository;
import com.estatex.domain.listing.ListingRepository;
import com.estatex.domain.listing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceTest {

    @Mock private FavouriteRepository favouriteRepository;
    @Mock private ListingRepository listingRepository;
    @InjectMocks private FavouriteService favouriteService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();

    private Listing buildListing() {
        return Listing.create("Kawalerka", "Opis",
                Address.of(null, "Warszawa", null, null, "Poland", null, null),
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new Money(new BigDecimal("2000.00")), 25.0, 1, UUID.randomUUID(), null);
    }

    // ── saveFavourite ─────────────────────────────────────────────────────────

    @Test
    void shouldSaveFavouriteWhenListingExistsAndNotAlreadySaved() {
        //given
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(buildListing()));
        when(favouriteRepository.existsByUserIdAndListingId(USER_ID, LISTING_ID)).thenReturn(false);
        when(favouriteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        FavouriteResult result = favouriteService.saveFavourite(USER_ID, LISTING_ID);

        //then
        assertEquals(LISTING_ID, result.listingId());
    }

    @Test
    void shouldThrowWhenSavingFavouriteForNonExistentListing() {
        //given
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        //when / then
        assertThrows(ListingNotFoundException.class,
                () -> favouriteService.saveFavourite(USER_ID, LISTING_ID));
    }

    @Test
    void shouldReturnExistingFavouriteWhenAlreadySaved() {
        //given
        var existing = Favourite.create(USER_ID, LISTING_ID);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(buildListing()));
        when(favouriteRepository.existsByUserIdAndListingId(USER_ID, LISTING_ID)).thenReturn(true);
        when(favouriteRepository.findByUserIdAndListingId(USER_ID, LISTING_ID))
                .thenReturn(Optional.of(existing));

        //when
        FavouriteResult result = favouriteService.saveFavourite(USER_ID, LISTING_ID);

        //then
        verify(favouriteRepository, never()).save(any());
        assertEquals(existing.getId(), result.id());
    }

    // ── removeFavourite ───────────────────────────────────────────────────────

    @Test
    void shouldDeleteFavouriteWhenRemoveCalled() {
        //when
        favouriteService.removeFavourite(USER_ID, LISTING_ID);

        //then
        verify(favouriteRepository).deleteByUserIdAndListingId(USER_ID, LISTING_ID);
    }

    // ── getFavourites ─────────────────────────────────────────────────────────

    @Test
    void shouldReturnAllFavouritesForUser() {
        //given
        var fav = Favourite.create(USER_ID, LISTING_ID);
        when(favouriteRepository.findByUserId(USER_ID)).thenReturn(List.of(fav));

        //when
        var result = favouriteService.getFavourites(USER_ID);

        //then
        assertEquals(1, result.size());
        assertEquals(LISTING_ID, result.get(0).listingId());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFavourites() {
        //given
        when(favouriteRepository.findByUserId(USER_ID)).thenReturn(List.of());

        //when
        var result = favouriteService.getFavourites(USER_ID);

        //then
        assertTrue(result.isEmpty());
    }

    // ── isFavourite ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrueWhenListingIsFavourite() {
        //given
        when(favouriteRepository.existsByUserIdAndListingId(USER_ID, LISTING_ID)).thenReturn(true);

        //when
        boolean result = favouriteService.isFavourite(USER_ID, LISTING_ID);

        //then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenListingIsNotFavourite() {
        //given
        when(favouriteRepository.existsByUserIdAndListingId(USER_ID, LISTING_ID)).thenReturn(false);

        //when
        boolean result = favouriteService.isFavourite(USER_ID, LISTING_ID);

        //then
        assertFalse(result);
    }
}
