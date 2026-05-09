package com.estatex.adapter.persistence.chat;

import com.estatex.adapter.persistence.listing.ListingJpaEntity;
import com.estatex.adapter.persistence.listing.ListingJpaRepository;
import com.estatex.adapter.persistence.user.UserJpaEntity;
import com.estatex.adapter.persistence.user.UserJpaRepository;
import com.estatex.domain.chat.Conversation;
import com.estatex.domain.chat.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryAdapterTest {

    @Autowired
    private MessageJpaRepository messageJpaRepository;

    @Autowired
    private ConversationJpaRepository conversationJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ListingJpaRepository listingJpaRepository;

    private MessageRepositoryAdapter adapter;

    private UUID ownerId;
    private UUID initiatorId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        adapter = new MessageRepositoryAdapter(messageJpaRepository);

        // Bootstrap Users
        UserJpaEntity owner = UserJpaEntity.builder().id(UUID.randomUUID()).email("o2@ex.com").displayName("O").createdAt(LocalDateTime.now()).active(true).build();
        UserJpaEntity initiator = UserJpaEntity.builder().id(UUID.randomUUID()).email("i2@ex.com").displayName("I").createdAt(LocalDateTime.now()).active(true).build();
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

        // Bootstrap Conversation
        ConversationJpaEntity conv = ConversationJpaEntity.builder()
                .id(UUID.randomUUID()).listingId(listing.getId()).initiatorId(initiatorId).listingOwnerId(ownerId).startedAt(LocalDateTime.now()).build();
        conversationJpaRepository.save(conv);
        conversationId = conv.getId();
    }

    @Test
    void shouldSaveAndFindByConversationIdPaginated() {
        Message msg1 = Message.create(conversationId, initiatorId, "Hello", null);
        Message msg2 = Message.create(conversationId, ownerId, "Hi", null);
        adapter.save(msg1);
        adapter.save(msg2);

        List<Message> messages = adapter.findByConversationId(conversationId, 0, 10);
        assertEquals(2, messages.size());
        // Since standard sort is usually reversed or linear, we assert elements exist
        assertTrue(messages.stream().anyMatch(m -> m.getContent().equals("Hello")));
    }

    @Test
    void shouldCountByConversationId() {
        adapter.save(Message.create(conversationId, initiatorId, "A", null));
        long count = adapter.countByConversationId(conversationId);
        assertEquals(1, count);
    }

    @Test
    void shouldCountUnreadMessagesNotSentByUser() {
        adapter.save(Message.create(conversationId, initiatorId, "From Initiator", null));
        
        long unreadForOwner = adapter.countUnread(conversationId, ownerId);
        long unreadForInitiator = adapter.countUnread(conversationId, initiatorId);
        
        assertEquals(1, unreadForOwner);
        assertEquals(0, unreadForInitiator); // Because initiator sent it
    }

    @Test
    void shouldMarkAllAsRead() {
        adapter.save(Message.create(conversationId, initiatorId, "From Initiator", null));
        
        adapter.markAllAsRead(conversationId, ownerId);
        messageJpaRepository.flush(); // Flush Modifying Query
        
        long unreadForOwner = adapter.countUnread(conversationId, ownerId);
        assertEquals(0, unreadForOwner);
    }
}
