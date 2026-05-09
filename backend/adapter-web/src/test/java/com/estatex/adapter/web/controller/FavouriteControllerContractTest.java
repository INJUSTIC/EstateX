package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.favourite.FavouriteResult;
import com.estatex.application.favourite.FavouriteService;
import com.estatex.domain.exception.ListingNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = FavouriteController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class FavouriteControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavouriteService favouriteService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/favourites ───────────────────────────────────────────────────

    @Test
    void shouldReturnFavouritesListContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID favId = UUID.randomUUID();
        LocalDateTime savedAt = LocalDateTime.of(2025, 6, 1, 12, 0);
        when(favouriteService.getFavourites(userId))
                .thenReturn(List.of(new FavouriteResult(favId, userId, listingId, savedAt)));

        /// when & then
        mockMvc.perform(get("/api/favourites")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(favId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].listingId").value(listingId.toString()))
                .andExpect(jsonPath("$[0].savedAt").exists());
    }

    @Test
    void shouldReturnEmptyArrayWhenNoFavourites() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(favouriteService.getFavourites(userId)).thenReturn(List.of());

        /// when & then
        mockMvc.perform(get("/api/favourites")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnErrorWithTimestampForMissingListing() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(favouriteService.saveFavourite(userId, listingId))
                .thenThrow(new ListingNotFoundException(listingId));

        /// when & then
        mockMvc.perform(post("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── POST /api/favourites/{listingId} ──────────────────────────────────────

    @Test
    void shouldReturnSavedFavouriteContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID favId = UUID.randomUUID();
        when(favouriteService.saveFavourite(userId, listingId))
                .thenReturn(new FavouriteResult(favId, userId, listingId, LocalDateTime.now()));

        /// when & then
        mockMvc.perform(post("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(favId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.listingId").value(listingId.toString()))
                .andExpect(jsonPath("$.savedAt").exists());
    }

    @Test
    void shouldReturnExistingFavouriteOnDuplicateSave() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID favId = UUID.randomUUID();
        when(favouriteService.saveFavourite(userId, listingId))
                .thenReturn(new FavouriteResult(favId, userId, listingId, LocalDateTime.now()));

        /// when & then
        mockMvc.perform(post("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(favId.toString()));
    }

    @Test
    void shouldReturn404WhenFavouritingNonExistentListing() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(favouriteService.saveFavourite(userId, listingId))
                .thenThrow(new ListingNotFoundException(listingId));

        /// when & then
        mockMvc.perform(post("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── DELETE /api/favourites/{listingId} ────────────────────────────────────

    @Test
    void shouldReturn204OnFavouriteRemoval() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        doNothing().when(favouriteService).removeFavourite(userId, listingId);

        /// when & then
        mockMvc.perform(delete("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        verify(favouriteService).removeFavourite(userId, listingId);
    }

    // ── GET /api/favourites/{listingId}/status ────────────────────────────────

    @Test
    void shouldReturnTrueWhenListingIsFavourited() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(favouriteService.isFavourite(userId, listingId)).thenReturn(true);

        /// when & then
        mockMvc.perform(get("/api/favourites/{listingId}/status", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalseWhenListingIsNotFavourited() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(favouriteService.isFavourite(userId, listingId)).thenReturn(false);

        /// when & then
        mockMvc.perform(get("/api/favourites/{listingId}/status", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldReturnJsonContentTypeForFavourites() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(favouriteService.getFavourites(userId)).thenReturn(List.of());

        /// when & then
        mockMvc.perform(get("/api/favourites")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
