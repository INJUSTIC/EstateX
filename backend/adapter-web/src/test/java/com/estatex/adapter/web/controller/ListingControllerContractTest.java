package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.listing.ListingResult;
import com.estatex.application.listing.ListingService;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.listing.ListingStatus;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = ListingController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class ListingControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    @Autowired
    private ObjectMapper objectMapper;

    private ListingResult sampleListing(UUID id, UUID ownerId) {
        return new ListingResult(id, "Sunny Apartment", "Great view", "ul. Marszalkowska 1",
                "Warsaw", "Mazowieckie", "00-001", "Poland", 52.23, 21.01,
                PropertyType.APARTMENT, ListingTransactionType.SALE, BigDecimal.valueOf(450000),
                65.0, 3, ListingStatus.ACTIVE, ownerId, "Owner Name",
                List.of(new ListingResult.PhotoResult(UUID.randomUUID(), "/files/photo.jpg", true)),
                42, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    // ── GET /api/listings (search) ────────────────────────────────────────────

    @Test
    void shouldReturnSearchResultsWithPaginationContract() throws Exception {
        /// given
        UUID ownerId = UUID.randomUUID();
        var page = new ListingService.ListingPage(
                List.of(sampleListing(UUID.randomUUID(), ownerId)), 1L, 1, 0);
        when(listingService.searchListings(any(), eq(0), eq(20))).thenReturn(page);

        /// when & then
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].title").value("Sunny Apartment"))
                .andExpect(jsonPath("$.items[0].price").value(450000))
                .andExpect(jsonPath("$.items[0].photos").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void shouldAcceptSearchFiltersAsQueryParams() throws Exception {
        /// given
        when(listingService.searchListings(any(), eq(0), eq(10)))
                .thenReturn(new ListingService.ListingPage(List.of(), 0L, 0, 0));

        /// when & then
        mockMvc.perform(get("/api/listings")
                        .param("city", "Krakow")
                        .param("propertyType", "HOUSE")
                        .param("transactionType", "RENT")
                        .param("minPrice", "1000")
                        .param("maxPrice", "5000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void shouldReturnEmptyListWhenNoListingsMatch() throws Exception {
        /// given
        when(listingService.searchListings(any(), eq(0), eq(20)))
                .thenReturn(new ListingService.ListingPage(List.of(), 0L, 0, 0));

        /// when & then
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ── GET /api/listings/{id} ────────────────────────────────────────────────

    @Test
    void shouldReturnFullListingDetailContract() throws Exception {
        /// given
        UUID listingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(listingService.getListingDetail(listingId)).thenReturn(sampleListing(listingId, ownerId));

        /// when & then
        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId.toString()))
                .andExpect(jsonPath("$.title").value("Sunny Apartment"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.street").value("ul. Marszalkowska 1"))
                .andExpect(jsonPath("$.city").value("Warsaw"))
                .andExpect(jsonPath("$.voivodeship").value("Mazowieckie"))
                .andExpect(jsonPath("$.propertyType").value("APARTMENT"))
                .andExpect(jsonPath("$.transactionType").value("SALE"))
                .andExpect(jsonPath("$.price").value(450000))
                .andExpect(jsonPath("$.areaSqMeters").value(65.0))
                .andExpect(jsonPath("$.numberOfRooms").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.ownerName").value("Owner Name"))
                .andExpect(jsonPath("$.photos[0].url").value("/files/photo.jpg"))
                .andExpect(jsonPath("$.photos[0].cover").value(true))
                .andExpect(jsonPath("$.viewCount").value(42));
    }

    @Test
    void shouldReturn404WhenListingNotFound() throws Exception {
        /// given
        UUID listingId = UUID.randomUUID();
        when(listingService.getListingDetail(listingId))
                .thenThrow(new ListingNotFoundException(listingId));

        /// when & then
        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── POST /api/listings ────────────────────────────────────────────────────

    @Test
    void shouldReturnCreatedListingContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(listingService.createListing(any())).thenReturn(sampleListing(listingId, userId));

        /// when & then
        mockMvc.perform(post("/api/listings")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Sunny Apartment",
                              "description": "Great view",
                              "street": "ul. Marszalkowska 1",
                              "city": "Warsaw",
                              "voivodeship": "Mazowieckie",
                              "postalCode": "00-001",
                              "propertyType": "APARTMENT",
                              "transactionType": "SALE",
                              "price": 450000,
                              "areaSqMeters": 65.0,
                              "numberOfRooms": 3
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnListingWithPhotosArray() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        var listing = sampleListing(listingId, userId);
        when(listingService.createListing(any())).thenReturn(listing);

        /// when & then
        mockMvc.perform(post("/api/listings")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Test",
                              "city": "Warsaw",
                              "propertyType": "APARTMENT",
                              "transactionType": "SALE",
                              "price": 100000,
                              "areaSqMeters": 50.0,
                              "numberOfRooms": 2
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photos").isArray())
                .andExpect(jsonPath("$.photos[0].id").exists())
                .andExpect(jsonPath("$.photos[0].url").exists())
                .andExpect(jsonPath("$.photos[0].cover").exists());
    }

    @Test
    void shouldReturnEnumValuesAsStrings() throws Exception {
        /// given
        UUID listingId = UUID.randomUUID();
        when(listingService.getListingDetail(listingId)).thenReturn(sampleListing(listingId, UUID.randomUUID()));

        /// when & then
        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyType").value("APARTMENT"))
                .andExpect(jsonPath("$.transactionType").value("SALE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ── DELETE /api/listings/{id} ─────────────────────────────────────────────

    @Test
    void shouldReturn204OnSuccessfulDelete() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        doNothing().when(listingService).deleteListing(listingId, userId, false);

        /// when & then
        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn403WhenNonOwnerDeletes() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        doThrow(new AccessDeniedException("Not the listing owner"))
                .when(listingService).deleteListing(listingId, userId, false);

        /// when & then
        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not the listing owner"));
    }

    // ── GET /api/listings/my ──────────────────────────────────────────────────

    @Test
    void shouldReturnMyListingsContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(listingService.getMyListings(userId))
                .thenReturn(List.of(sampleListing(UUID.randomUUID(), userId)));

        /// when & then
        mockMvc.perform(get("/api/listings/my")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Sunny Apartment"))
                .andExpect(jsonPath("$[0].ownerId").value(userId.toString()));
    }

    // ── GET /api/listings/my/analytics ────────────────────────────────────────

    @Test
    void shouldReturnAnalyticsContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(listingService.getAnalytics(userId))
                .thenReturn(List.of(new ListingService.ListingAnalyticsResult(listingId, "My House", 99)));

        /// when & then
        mockMvc.perform(get("/api/listings/my/analytics")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].listingId").value(listingId.toString()))
                .andExpect(jsonPath("$[0].title").value("My House"))
                .andExpect(jsonPath("$[0].viewCount").value(99));
    }
}
