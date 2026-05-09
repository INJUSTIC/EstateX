package com.estatex.adapter.persistence.chat;

import com.estatex.adapter.persistence.listing.ListingJpaEntity;
import com.estatex.adapter.persistence.listing.ListingJpaRepository;
import com.estatex.adapter.persistence.user.UserJpaEntity;
import com.estatex.adapter.persistence.user.UserJpaRepository;
import com.estatex.domain.chat.Conversation;
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
class ConversationRepositoryAdapterTest {

    @Autowired
    private ConversationJpaRepository jpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ListingJpaRepository listingJpaRepository;

    private ConversationRepositoryAdapter adapter;

    private UUID ownerId;
    private UUID initiatorId;
    private UUID listingId;

    @BeforeEach
    void setUp() {
        adapter = new ConversationRepositoryAdapter(jpaRepository);

        // Bootstrap Users
        UserJpaEntity owner = UserJpaEntity.builder().id(UUID.randomUUID()).email("owner@example.com").displayName("Owner").createdAt(LocalDateTime.now()).active(true).build();
        UserJpaEntity initiator = UserJpaEntity.builder().id(UUID.randomUUID()).email("initiator@example.com").displayName("Init").createdAt(LocalDateTime.now()).active(true).build();
        userJpaRepository.save(owner);
        userJpaRepository.save(initiator);
        ownerId = owner.getId();
        initiatorId = initiator.getId();

        // Bootstrap Listing
        ListingJpaEntity listing = ListingJpaEntity.builder()
                .id(UUID.randomUUID()).title("A").description("D").street("S").city("C").voivodeship("V").postalCode("0").country("P")
                .propertyType("APARTMENT").transactionType("RENT").price(BigDecimal.TEN).currency("PLN").areaSqMeters(10.0).numberOfRooms(1)
                .status("ACTIVE").ownerId(ownerId).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        listingJpaRepository.save(listing);
        listingId = listing.getId();
    }

    @Test
    void shouldSaveAndFindById() {
        Conversation conv = Conversation.create(listingId, initiatorId, ownerId);
        Conversation saved = adapter.save(conv);

        assertNotNull(saved.getId());

        Optional<Conversation> found = adapter.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(initiatorId, found.get().getInitiatorId());
        assertEquals(listingId, found.get().getListingId());
    }

    @Test
    void shouldFindByParticipantId() {
        Conversation conv = Conversation.create(listingId, initiatorId, ownerId);
        adapter.save(conv);

        List<Conversation> byInitiator = adapter.findByParticipantId(initiatorId);
        assertEquals(1, byInitiator.size());

        List<Conversation> byOwner = adapter.findByParticipantId(ownerId);
        assertEquals(1, byOwner.size());
    }

    @Test
    void shouldCheckIfExistsByListingAndInitiator() {
        Conversation conv = Conversation.create(listingId, initiatorId, ownerId);
        adapter.save(conv);

        assertTrue(adapter.existsByListingIdAndInitiatorId(listingId, initiatorId));
        assertFalse(adapter.existsByListingIdAndInitiatorId(listingId, ownerId));
    }

    @Test
    void shouldFindByListingAndInitiator() {
        Conversation conv = Conversation.create(listingId, initiatorId, ownerId);
        adapter.save(conv);

        Optional<Conversation> found = adapter.findByListingIdAndInitiatorId(listingId, initiatorId);
        assertTrue(found.isPresent());
    }
}
