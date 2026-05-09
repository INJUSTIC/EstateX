package com.estatex.application.acceptance;

import com.estatex.application.acceptance.fakes.TestingBackendSetup;
import com.estatex.application.chat.ChatService;
import com.estatex.application.chat.MessageResult;
import com.estatex.application.listing.ListingResult;
import com.estatex.application.listing.ListingService;
import com.estatex.application.user.UserService;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessagingAcceptanceTest {

    private UserService userService;
    private ListingService listingService;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        TestingBackendSetup backend = new TestingBackendSetup();
        this.userService = backend.userService;
        this.listingService = backend.listingService;
        this.chatService = backend.chatService;
    }

    private UUID createTestUser(String email) {
        return userService.register(new UserService.RegisterCommand(email, "User")).id();
    }

    private ListingResult createTestListing(UUID ownerId) {
        return listingService.createListing(new ListingService.CreateListingCommand(
                ownerId, "Title", "Desc", "St", "City", "V", "0", "Pol", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT, BigDecimal.TEN, 10, 1, null
        ));
    }

    @Test
    void uc5_1_Conversations_InitiatorCanStartContextBoundThread() {
        UUID ownerId = createTestUser("owner@example.com");
        UUID buyerId = createTestUser("buyer@example.com");
        ListingResult listing = createTestListing(ownerId);

        // Buyer starts conversation
        ChatService.ConversationResult conv1 = chatService.startConversation(listing.id(), buyerId);
        assertNotNull(conv1.id());
        assertEquals(listing.id(), conv1.listingId());

        // Repeated action yields existing thread
        ChatService.ConversationResult conv2 = chatService.startConversation(listing.id(), buyerId);
        assertEquals(conv1.id(), conv2.id());
    }

    @Test
    void uc5_2_Messaging_CannotSendEmptyMessage() {
        UUID ownerId = createTestUser("owner@example.com");
        UUID buyerId = createTestUser("buyer@example.com");
        ListingResult listing = createTestListing(ownerId);

        ChatService.ConversationResult conv = chatService.startConversation(listing.id(), buyerId);

        ChatService.SendMessageCommand emptyCmd = new ChatService.SendMessageCommand(
                conv.id(), buyerId, "   ", null, null, null
        );

        // Domain aggregate rejects blank text with no attachment
        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(emptyCmd));
    }

    @Test
    void uc5_3_InboxAnalytics_FetchingMessagesMarksAsRead() {
        UUID ownerId = createTestUser("owner@example.com");
        UUID buyerId = createTestUser("buyer@example.com");
        ListingResult listing = createTestListing(ownerId);

        ChatService.ConversationResult conv = chatService.startConversation(listing.id(), buyerId);

        // Buyer sends message
        chatService.sendMessage(new ChatService.SendMessageCommand(
                conv.id(), buyerId, "Hello Owner!", null, null, null
        ));

        // Owner gets Inbox (Conversation summary) -> Should have 1 unread message
        List<ChatService.ConversationSummaryResult> ownerInbox = chatService.getConversations(ownerId);
        assertEquals(1, ownerInbox.size());
        assertEquals(1L, ownerInbox.get(0).unreadCount());

        // Owner fetches the actual messages thread -> Flags to read
        ChatService.MessagePage messages = chatService.getMessages(conv.id(), ownerId, 0, 10);
        assertEquals(1, messages.items().size());
        assertEquals("Hello Owner!", messages.items().get(0).content());

        // Inbox unread count now 0
        List<ChatService.ConversationSummaryResult> ownerInboxCheck = chatService.getConversations(ownerId);
        assertEquals(0L, ownerInboxCheck.get(0).unreadCount());
    }
}
