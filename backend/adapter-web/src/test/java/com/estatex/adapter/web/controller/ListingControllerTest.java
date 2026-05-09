package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.listing.ListingResult;
import com.estatex.application.listing.ListingService;
import com.estatex.domain.listing.ListingSearchCriteria;
import com.estatex.domain.listing.ListingStatus;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
    value = ListingController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateListing() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        ListingResult mockResult = new ListingResult(listingId, "Apartment1", "Desc", "St", "City", "Voiv", "Zip", "PL", null, null, PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.valueOf(2000), 50.0, 2, ListingStatus.ACTIVE, userId, "Owner", List.of(), 0, LocalDateTime.now(), LocalDateTime.now(), null);

        Mockito.when(listingService.createListing(any(ListingService.CreateListingCommand.class))).thenReturn(mockResult);

        ListingController.CreateListingRequest request = new ListingController.CreateListingRequest(
                "Apartment1", "Desc", "Street", "Krakow", "Malopolskie", "30-001", "Poland", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.valueOf(2000), null, 50.0, 2, null
        );

        mockMvc.perform(post("/api/listings")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId.toString()))
                .andExpect(jsonPath("$.title").value("Apartment1"))
                .andExpect(jsonPath("$.price").value(2000));
    }

    @Test
    void shouldGetListingDetail() throws Exception {
        UUID listingId = UUID.randomUUID();
        ListingResult mockResult = new ListingResult(listingId, "Detail", "Desc", "St", "City", "Voiv", "Zip", "PL", null, null, PropertyType.HOUSE, ListingTransactionType.SALE, BigDecimal.valueOf(500000), 150.0, 5, ListingStatus.ACTIVE, UUID.randomUUID(), "Owner", List.of(), 10, LocalDateTime.now(), LocalDateTime.now(), null);

        Mockito.when(listingService.getListingDetail(listingId)).thenReturn(mockResult);

        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail"))
                .andExpect(jsonPath("$.viewCount").value(10));
    }

    @Test
    void shouldDeleteListing() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();

        Mockito.doNothing().when(listingService).deleteListing(listingId, userId, false);

        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        Mockito.verify(listingService).deleteListing(listingId, userId, false);
    }
}
