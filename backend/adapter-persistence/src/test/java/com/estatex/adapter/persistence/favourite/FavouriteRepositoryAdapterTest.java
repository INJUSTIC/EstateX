package com.estatex.adapter.persistence.favourite;

import com.estatex.adapter.persistence.listing.ListingJpaEntity;
import com.estatex.adapter.persistence.listing.ListingJpaRepository;
import com.estatex.adapter.persistence.user.UserJpaEntity;
import com.estatex.adapter.persistence.user.UserJpaRepository;
import com.estatex.domain.favourite.Favourite;
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
class FavouriteRepositoryAdapterTest {

    @Autowired
    private FavouriteJpaRepository favouriteJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ListingJpaRepository listingJpaRepository;

    private FavouriteRepositoryAdapter adapter;

    private UUID userId;
    private UUID listingId;

    @BeforeEach
    void setUp() {
        adapter = new FavouriteRepositoryAdapter(favouriteJpaRepository);

        // Bootstrap Users
        UserJpaEntity user = UserJpaEntity.builder().id(UUID.randomUUID()).email("user@example.com").displayName("User").createdAt(LocalDateTime.now()).active(true).build();
        userJpaRepository.save(user);
        userId = user.getId();

        // Bootstrap Listing
        ListingJpaEntity listing = ListingJpaEntity.builder()
                .id(UUID.randomUUID()).title("A").description("D").street("S").city("C").voivodeship("V").postalCode("0").country("P")
                .propertyType("APARTMENT").transactionType("RENT").price(BigDecimal.TEN).currency("PLN").areaSqMeters(10.0).numberOfRooms(1)
                .status("ACTIVE").ownerId(userId).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        listingJpaRepository.save(listing);
        listingId = listing.getId();
    }

    @Test
    void shouldSaveAndFindByUserId() {
        Favourite fav = Favourite.create(userId, listingId);
        adapter.save(fav);

        List<Favourite> favourites = adapter.findByUserId(userId);
        assertEquals(1, favourites.size());
        assertEquals(listingId, favourites.get(0).getListingId());
    }

    @Test
    void shouldFindByUserIdAndListingId() {
        Favourite fav = Favourite.create(userId, listingId);
        adapter.save(fav);

        Optional<Favourite> found = adapter.findByUserIdAndListingId(userId, listingId);
        assertTrue(found.isPresent());
    }

    @Test
    void shouldDeleteByUserIdAndListingId() {
        Favourite fav = Favourite.create(userId, listingId);
        adapter.save(fav);

        adapter.deleteByUserIdAndListingId(userId, listingId);
        favouriteJpaRepository.flush();

        Optional<Favourite> found = adapter.findByUserIdAndListingId(userId, listingId);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnTrueIfExists() {
        Favourite fav = Favourite.create(userId, listingId);
        adapter.save(fav);

        assertTrue(adapter.existsByUserIdAndListingId(userId, listingId));
        assertFalse(adapter.existsByUserIdAndListingId(UUID.randomUUID(), listingId));
    }
}
