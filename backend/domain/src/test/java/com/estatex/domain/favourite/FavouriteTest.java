package com.estatex.domain.favourite;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FavouriteTest {

    @Test
    void shouldCreateFavouriteWithNonNullIdWhenCreated() {
        //given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();

        //when
        Favourite favourite = Favourite.create(userId, listingId);

        //then
        assertNotNull(favourite.getId());
    }

    @Test
    void shouldCreateFavouriteWithCorrectUserIdWhenCreated() {
        //given
        UUID userId = UUID.randomUUID();

        //when
        Favourite favourite = Favourite.create(userId, UUID.randomUUID());

        //then
        assertEquals(userId, favourite.getUserId());
    }

    @Test
    void shouldCreateFavouriteWithCorrectListingIdWhenCreated() {
        //given
        UUID listingId = UUID.randomUUID();

        //when
        Favourite favourite = Favourite.create(UUID.randomUUID(), listingId);

        //then
        assertEquals(listingId, favourite.getListingId());
    }

    @Test
    void shouldCreateFavouriteWithNonNullSavedAtWhenCreated() {
        //when
        Favourite favourite = Favourite.create(UUID.randomUUID(), UUID.randomUUID());

        //then
        assertNotNull(favourite.getSavedAt());
    }
}
