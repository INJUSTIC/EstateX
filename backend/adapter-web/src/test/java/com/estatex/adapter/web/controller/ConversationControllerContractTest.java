package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.chat.ChatService;
import com.estatex.application.chat.MessageResult;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.exception.ConversationNotFoundException;
import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.listing.ListingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = ConversationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class ConversationControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ── POST /api/conversations ───────────────────────────────────────────────

    @Test
    void shouldReturnConversationContractOnCreate() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(chatService.startConversation(eq(listingId), eq(userId)))
                .thenReturn(new ChatService.ConversationResult(
                        convId, listingId, "Nice Apartment", userId, ownerId, LocalDateTime.now()));

        /// when & then
        mockMvc.perform(post("/api/conversations")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"listingId":"%s"}
                            """.formatted(listingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(convId.toString()))
                .andExpect(jsonPath("$.listingId").value(listingId.toString()))
                .andExpect(jsonPath("$.listingTitle").value("Nice Apartment"))
                .andExpect(jsonPath("$.initiatorId").value(userId.toString()))
                .andExpect(jsonPath("$.listingOwnerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.startedAt").exists());
    }

    @Test
    void shouldReturn404WhenStartingConversationForMissingListing() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(chatService.startConversation(eq(listingId), eq(userId)))
                .thenThrow(new ListingNotFoundException(listingId));

        /// when & then
        mockMvc.perform(post("/api/conversations")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"listingId":"%s"}
                            """.formatted(listingId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnExistingConversationWhenDuplicate() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID existingConvId = UUID.randomUUID();
        when(chatService.startConversation(eq(listingId), eq(userId)))
                .thenReturn(new ChatService.ConversationResult(
                        existingConvId, listingId, "Listing", userId, UUID.randomUUID(), LocalDateTime.now()));

        /// when & then
        mockMvc.perform(post("/api/conversations")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"listingId":"%s"}
                            """.formatted(listingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingConvId.toString()));
    }

    // ── GET /api/conversations ────────────────────────────────────────────────

    @Test
    void shouldReturnConversationSummariesWithUnreadCount() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        when(chatService.getConversations(userId))
                .thenReturn(List.of(new ChatService.ConversationSummaryResult(
                        convId, UUID.randomUUID(), "Apartment", userId,
                        UUID.randomUUID(), LocalDateTime.now(), 3, ListingStatus.ACTIVE, "Initiator", "Owner")));

        /// when & then
        mockMvc.perform(get("/api/conversations")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(convId.toString()))
                .andExpect(jsonPath("$[0].listingTitle").value("Apartment"))
                .andExpect(jsonPath("$[0].unreadCount").value(3));
    }

    @Test
    void shouldReturnEmptyConversationsListForNewUser() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        when(chatService.getConversations(userId)).thenReturn(List.of());

        /// when & then
        mockMvc.perform(get("/api/conversations")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/conversations/{id}/messages ──────────────────────────────────

    @Test
    void shouldReturnMessagePageContract() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();
        when(chatService.getMessages(eq(convId), eq(userId), eq(0), eq(50)))
                .thenReturn(new ChatService.MessagePage(
                        List.of(new MessageResult(msgId, convId, userId, "Hello!", null,
                                LocalDateTime.now(), true)),
                        1L, 1, 0));

        /// when & then
        mockMvc.perform(get("/api/conversations/{id}/messages", convId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(msgId.toString()))
                .andExpect(jsonPath("$.items[0].conversationId").value(convId.toString()))
                .andExpect(jsonPath("$.items[0].senderId").value(userId.toString()))
                .andExpect(jsonPath("$.items[0].content").value("Hello!"))
                .andExpect(jsonPath("$.items[0].attachmentUrl").isEmpty())
                .andExpect(jsonPath("$.items[0].read").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void shouldReturn403WhenNonParticipantReadsMessages() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        when(chatService.getMessages(eq(convId), eq(userId), eq(0), eq(50)))
                .thenThrow(new AccessDeniedException());

        /// when & then
        mockMvc.perform(get("/api/conversations/{id}/messages", convId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404WhenConversationNotFoundForMessages() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        when(chatService.getMessages(eq(convId), eq(userId), eq(0), eq(50)))
                .thenThrow(new ConversationNotFoundException(convId));

        /// when & then
        mockMvc.perform(get("/api/conversations/{id}/messages", convId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/conversations/{id}/messages ─────────────────────────────────

    @Test
    void shouldReturnMessageContractOnSend() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();
        when(chatService.sendMessage(any())).thenReturn(
                new MessageResult(msgId, convId, userId, "Hi there", null,
                        LocalDateTime.now(), false));

        /// when & then
        mockMvc.perform(post("/api/conversations/{id}/messages", convId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"content":"Hi there"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(msgId.toString()))
                .andExpect(jsonPath("$.content").value("Hi there"))
                .andExpect(jsonPath("$.senderId").value(userId.toString()))
                .andExpect(jsonPath("$.read").value(false))
                .andExpect(jsonPath("$.sentAt").exists());
    }

    @Test
    void shouldReturn403WhenNonParticipantSendsMessage() throws Exception {
        /// given
        UUID userId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();
        when(chatService.sendMessage(any()))
                .thenThrow(new AccessDeniedException("Not a participant"));

        /// when & then
        mockMvc.perform(post("/api/conversations/{id}/messages", convId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"content":"test"}
                            """))
                .andExpect(status().isForbidden());
    }
}
