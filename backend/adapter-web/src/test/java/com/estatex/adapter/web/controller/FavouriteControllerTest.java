package com.estatex.adapter.web.controller;

import com.estatex.application.favourite.FavouriteResult;
import com.estatex.application.favourite.FavouriteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
    value = FavouriteController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class FavouriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavouriteService favouriteService;

    @Test
    void shouldGetFavourites() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        FavouriteResult mockResult = new FavouriteResult(UUID.randomUUID(), userId, listingId, LocalDateTime.now());

        Mockito.when(favouriteService.getFavourites(userId)).thenReturn(List.of(mockResult));

        mockMvc.perform(get("/api/favourites")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].listingId").value(listingId.toString()));
    }

    @Test
    void shouldSaveFavourite() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        FavouriteResult mockResult = new FavouriteResult(UUID.randomUUID(), userId, listingId, LocalDateTime.now());

        Mockito.when(favouriteService.saveFavourite(userId, listingId)).thenReturn(mockResult);

        mockMvc.perform(post("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listingId").value(listingId.toString()));
    }

    @Test
    void shouldRemoveFavourite() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();

        Mockito.doNothing().when(favouriteService).removeFavourite(userId, listingId);

        mockMvc.perform(delete("/api/favourites/{listingId}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        Mockito.verify(favouriteService).removeFavourite(userId, listingId);
    }
}
