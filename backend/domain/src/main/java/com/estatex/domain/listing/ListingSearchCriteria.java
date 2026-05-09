package com.estatex.domain.listing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Criteria value object for listing searches.
 */
public record ListingSearchCriteria(
        String keyword,
        String city,
        String voivodeship,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        PropertyType propertyType,
        ListingTransactionType transactionType,
        Integer minRooms,
        Integer maxRooms,
        Double minArea,
        Double maxArea,
        SortBy sortBy,
        SortDirection sortDirection,
        LocalDate availableEarliest,
        LocalDate availableLatest
) {
    public enum SortBy {
        CREATED_AT, PRICE, AREA
    }

    public enum SortDirection {
        ASC, DESC
    }

    public static ListingSearchCriteria empty() {
        return new ListingSearchCriteria(null, null, null, null, null, null,
                null, null, null, null, null, SortBy.CREATED_AT, SortDirection.DESC, null, null);
    }
}
