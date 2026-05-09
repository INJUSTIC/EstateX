package com.estatex.domain.listing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(UUID id);

    List<Listing> findByOwnerId(UUID ownerId);

    ListingPage search(ListingSearchCriteria criteria, int page, int size);

    void delete(UUID id);

    void incrementViewCount(UUID id);

    record ListingPage(List<Listing> items, long totalElements, int totalPages, int page) {}
}
