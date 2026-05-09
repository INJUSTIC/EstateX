package com.estatex.application.chat;

import com.estatex.application.port.out.FileStoragePort;
import com.estatex.domain.chat.Conversation;
import com.estatex.domain.chat.ConversationRepository;
import com.estatex.domain.chat.Message;
import com.estatex.domain.chat.MessageRepository;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.exception.ConversationNotFoundException;
import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.listing.Listing;
import com.estatex.domain.listing.ListingRepository;
import com.estatex.domain.listing.ListingTransactionType;
import com.estatex.domain.listing.*;
import com.estatex.domain.user.User;
import com.estatex.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStoragePort fileStorage;

    @InjectMocks private ChatService chatService;

    private static final UUID INITIATOR_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID LISTING_ID = UUID.randomUUID();

    private Listing buildListing() {
        return Listing.create("Kawalerka", "Opis",
                Address.of(null, "Warszawa", null, null, "Poland", null, null),
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new Money(new BigDecimal("2000.00")), 25.0, 1, OWNER_ID, null);
    }

    private Conversation buildConversation() {
        return Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);
    }

    // ── startConversation ─────────────────────────────────────────────────────

    @Test
    void shouldCreateNewConversationWhenNoneExists() {
        //given
        var listing = buildListing();
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(conversationRepository.findByListingIdAndInitiatorId(LISTING_ID, INITIATOR_ID))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        var result = chatService.startConversation(LISTING_ID, INITIATOR_ID);

        //then
        assertEquals(LISTING_ID, result.listingId());
    }

    @Test
    void shouldReturnExistingConversationWhenAlreadyStarted() {
        //given
        var listing = buildListing();
        var existing = buildConversation();
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(conversationRepository.findByListingIdAndInitiatorId(LISTING_ID, INITIATOR_ID))
                .thenReturn(Optional.of(existing));

        //when
        var result = chatService.startConversation(LISTING_ID, INITIATOR_ID);

        //then
        verify(conversationRepository, never()).save(any());
        assertEquals(existing.getId(), result.id());
    }

    @Test
    void shouldThrowWhenListingNotFoundOnStartConversation() {
        //given
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        //when / then
        assertThrows(ListingNotFoundException.class,
                () -> chatService.startConversation(LISTING_ID, INITIATOR_ID));
    }

    // ── sendMessage ───────────────────────────────────────────────────────────

    @Test
    void shouldSendMessageWhenSenderIsParticipant() {
        //given
        var conversation = buildConversation();
        when(conversationRepository.findById(conversation.getId()))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var cmd = new ChatService.SendMessageCommand(
                conversation.getId(), INITIATOR_ID, "Hej!", null, null, null);

        //when
        var result = chatService.sendMessage(cmd);

        //then
        assertEquals("Hej!", result.content());
    }

    @Test
    void shouldThrowWhenNonParticipantSendsMessage() {
        //given
        var conversation = buildConversation();
        when(conversationRepository.findById(conversation.getId()))
                .thenReturn(Optional.of(conversation));
        var cmd = new ChatService.SendMessageCommand(
                conversation.getId(), UUID.randomUUID(), "Hej!", null, null, null);

        //when / then
        assertThrows(AccessDeniedException.class, () -> chatService.sendMessage(cmd));
    }

    @Test
    void shouldThrowWhenConversationNotFoundOnSend() {
        //given
        UUID convId = UUID.randomUUID();
        when(conversationRepository.findById(convId)).thenReturn(Optional.empty());
        var cmd = new ChatService.SendMessageCommand(convId, INITIATOR_ID, "Hej!", null, null, null);

        //when / then
        assertThrows(ConversationNotFoundException.class, () -> chatService.sendMessage(cmd));
    }

    @Test
    void shouldStoreAttachmentWhenAttachmentProvided() {
        //given
        var conversation = buildConversation();
        when(conversationRepository.findById(conversation.getId()))
                .thenReturn(Optional.of(conversation));
        when(fileStorage.store(any(), any(), any())).thenReturn("http://example.com/file.pdf");
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var cmd = new ChatService.SendMessageCommand(
                conversation.getId(), INITIATOR_ID, null, "file.pdf",
                new ByteArrayInputStream(new byte[0]), "application/pdf");

        //when
        var result = chatService.sendMessage(cmd);

        //then
        verify(fileStorage).store(any(), any(), any());
        assertEquals("http://example.com/file.pdf", result.attachmentUrl());
    }

    @Test
    void shouldNotStoreAttachmentWhenNoAttachmentProvided() {
        //given
        var conversation = buildConversation();
        when(conversationRepository.findById(conversation.getId()))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var cmd = new ChatService.SendMessageCommand(
                conversation.getId(), INITIATOR_ID, "Hej!", null, null, null);

        //when
        chatService.sendMessage(cmd);

        //then
        verify(fileStorage, never()).store(any(), any(), any());
    }

    // ── getConversations ──────────────────────────────────────────────────────

    @Test
    void shouldReturnAllConversationsForUser() {
        //given
        var conv = buildConversation();
        var listing = buildListing();
        when(conversationRepository.findByParticipantId(INITIATOR_ID)).thenReturn(List.of(conv));
        when(listingRepository.findById(conv.getListingId())).thenReturn(Optional.of(listing));
        when(messageRepository.countUnread(conv.getId(), INITIATOR_ID)).thenReturn(2L);

        //when
        var result = chatService.getConversations(INITIATOR_ID);

        //then
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).unreadCount());
        assertEquals(listing.getTitle(), result.get(0).listingTitle());
    }

    @Test
    void shouldUsePlaceholderTitleWhenListingDeleted() {
        //given
        var conv = buildConversation();
        when(conversationRepository.findByParticipantId(INITIATOR_ID)).thenReturn(List.of(conv));
        when(listingRepository.findById(conv.getListingId())).thenReturn(Optional.empty());
        when(messageRepository.countUnread(any(), any())).thenReturn(0L);

        //when
        var result = chatService.getConversations(INITIATOR_ID);

        //then
        assertEquals("(deleted listing)", result.get(0).listingTitle());
    }

    // ── getMessages ───────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenNonParticipantGetMessages() {
        //given
        var conversation = buildConversation();
        when(conversationRepository.findById(conversation.getId()))
                .thenReturn(Optional.of(conversation));

        //when / then
        assertThrows(AccessDeniedException.class,
                () -> chatService.getMessages(conversation.getId(), UUID.randomUUID(), 0, 50));
    }

    @Test
    void shouldMarkMessagesAsReadWhenParticipantGetsMessages() {
        //given
        var conversation = buildConversation();
        var msg = Message.create(conversation.getId(), OWNER_ID, "Hej!", null);
        when(conversationRepository.findById(conversation.getId()))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationId(conversation.getId(), 0, 2))
                .thenReturn(List.of(msg));
        when(messageRepository.countByConversationId(conversation.getId())).thenReturn(3L);

        //when
        var page = chatService.getMessages(conversation.getId(), INITIATOR_ID, 0, 2);

        //then
        verify(messageRepository).markAllAsRead(conversation.getId(), INITIATOR_ID);
        assertNotNull(page);
        assertEquals(1, page.items().size());
        assertEquals(3L, page.totalElements());
        assertEquals(2, page.totalPages()); // ceil(3/2) = 2
    }

    @Test
    void shouldThrowWhenConversationNotFoundOnGetMessages() {
        //given
        UUID convId = UUID.randomUUID();
        when(conversationRepository.findById(convId)).thenReturn(Optional.empty());

        //when / then
        assertThrows(ConversationNotFoundException.class,
                () -> chatService.getMessages(convId, INITIATOR_ID, 0, 50));
    }
}
