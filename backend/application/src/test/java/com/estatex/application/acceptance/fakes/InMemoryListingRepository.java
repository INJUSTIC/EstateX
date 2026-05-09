package com.estatex.application.acceptance.fakes;

import com.estatex.domain.listing.Listing;
import com.estatex.domain.listing.ListingRepository;
import com.estatex.domain.listing.ListingSearchCriteria;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryListingRepository implements ListingRepository {
    private final Map<UUID, Listing> database = new HashMap<>();

    @Override
    public Listing save(Listing listing) {
        database.put(listing.getId(), listing);
        return listing;
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Listing> findByOwnerId(UUID ownerId) {
        return database.values().stream()
                .filter(l -> l.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public ListingPage search(ListingSearchCriteria criteria, int page, int size) {
        List<Listing> filtered = database.values().stream()
                .filter(l -> criteria.keyword() == null || l.getTitle().toLowerCase().contains(criteria.keyword().toLowerCase()))
                .filter(l -> criteria.city() == null || l.getAddress().city().equalsIgnoreCase(criteria.city()))
                .filter(l -> criteria.minPrice() == null || l.getPrice().amount().compareTo(criteria.minPrice()) >= 0)
                .filter(l -> criteria.maxPrice() == null || l.getPrice().amount().compareTo(criteria.maxPrice()) <= 0)
                .filter(l -> criteria.transactionType() == null || l.getTransactionType() == criteria.transactionType())
                .filter(l -> criteria.propertyType() == null || l.getPropertyType() == criteria.propertyType())
                .collect(Collectors.toList());

        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        int totalPages = (int) Math.ceil((double) filtered.size() / size);

        return new ListingPage(filtered.subList(start, end), filtered.size(), totalPages, page);
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
    }

    @Override
    public void incrementViewCount(UUID id) {
        // Do nothing in-memory since the service also calls listing.incrementViewCount() 
        // which modifies the shared reference.
    }
}
