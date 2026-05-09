package com.estatex.adapter.persistence.listing;

import com.estatex.adapter.persistence.user.UserJpaEntity;
import com.estatex.adapter.persistence.user.UserJpaRepository;
import com.estatex.domain.listing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ListingRepositoryAdapterTest {

    @Autowired
    private ListingJpaRepository jpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private ListingRepositoryAdapter adapter;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        adapter = new ListingRepositoryAdapter(jpaRepository);

        // Pre-create User to satisfy FK constraint
        UserJpaEntity user = UserJpaEntity.builder()
                .id(UUID.randomUUID())
                .email("owner@example.com")
                .displayName("Owner")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        userJpaRepository.save(user);
        ownerId = user.getId();
    }

    private Listing createTestListing(String title, BigDecimal price) {
        Address address = Address.of("Street", "Krakow", "Malopolskie", "30-001", "Poland", null, null);
        Money money = new Money(price);
        return Listing.create(title, "Description", address, PropertyType.APARTMENT,
                ListingTransactionType.RENT, money, 50.0, 2, ownerId, null);
    }

    @Test
    void shouldSaveAndFindById() {
        Listing listing = createTestListing("Fancy Apartment", BigDecimal.valueOf(3000));
        listing.addPhoto("http://example.com/photo1.jpg");

        Listing saved = adapter.save(listing);
        assertNotNull(saved.getId());

        Optional<Listing> found = adapter.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Fancy Apartment", found.get().getTitle());
        assertEquals(BigDecimal.valueOf(3000), found.get().getPrice().amount());
        assertEquals("Krakow", found.get().getAddress().city());
        assertEquals(1, found.get().getPhotos().size());
        assertEquals("http://example.com/photo1.jpg", found.get().getPhotos().get(0).getUrl());
    }

    @Test
    void shouldFindByOwnerId() {
        adapter.save(createTestListing("Apartment 1", BigDecimal.valueOf(1000)));
        adapter.save(createTestListing("Apartment 2", BigDecimal.valueOf(2000)));

        List<Listing> listings = adapter.findByOwnerId(ownerId);
        assertEquals(2, listings.size());
    }

    @Test
    void shouldIncrementViewCount() {
        Listing saved = adapter.save(createTestListing("Apartment View", BigDecimal.TEN));
        
        adapter.incrementViewCount(saved.getId());
        // Since increment is done via @Modifying query, we have to reload from DB and bypass cache
        jpaRepository.flush();
        
        Optional<Listing> found = adapter.findById(saved.getId());
        assertEquals(1, found.get().getViewCount());
    }

    @Test
    void shouldDeleteListing() {
        Listing saved = adapter.save(createTestListing("To Delete", BigDecimal.TEN));
        adapter.delete(saved.getId());

        Optional<Listing> found = adapter.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void shouldSearchListingsWithSpecifications() {
        adapter.save(createTestListing("Krakow Apartment", BigDecimal.valueOf(2500)));
        adapter.save(createTestListing("Warsaw Apartment", BigDecimal.valueOf(4000)));
        
        // Search by keyword and maxPrice
        ListingSearchCriteria criteria = new ListingSearchCriteria("Apartment", null, null, null, BigDecimal.valueOf(3000), null, null, null, null, null, null, ListingSearchCriteria.SortBy.PRICE, ListingSearchCriteria.SortDirection.ASC, null, null);
        
        ListingRepository.ListingPage page = adapter.search(criteria, 0, 10);
        
        assertEquals(1, page.items().size());
        assertEquals("Krakow Apartment", page.items().get(0).getTitle());
    }
}
