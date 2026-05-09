package com.estatex.adapter.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatex.application.chat.ChatService;
import com.estatex.application.chat.MessageResult;
import com.estatex.domain.listing.ListingStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
    value = ConversationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.estatex.adapter.web.config.ModulesConfig.class}
    )
)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;
    
    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldStartConversation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ChatService.ConversationResult mockResult = new ChatService.ConversationResult(conversationId, listingId, "Listing", userId, UUID.randomUUID(), LocalDateTime.now());

        Mockito.when(chatService.startConversation(eq(listingId), eq(userId))).thenReturn(mockResult);

        ConversationController.StartConversationRequest request = new ConversationController.StartConversationRequest(listingId);

        mockMvc.perform(post("/api/conversations")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conversationId.toString()))
                .andExpect(jsonPath("$.listingTitle").value("Listing"));
    }

    @Test
    void shouldGetConversations() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ChatService.ConversationSummaryResult mockResult = new ChatService.ConversationSummaryResult(conversationId, UUID.randomUUID(), "Listing", userId, UUID.randomUUID(), LocalDateTime.now(), 1, ListingStatus.ACTIVE, "Initiator", "Owner");

        Mockito.when(chatService.getConversations(userId)).thenReturn(List.of(mockResult));

        mockMvc.perform(get("/api/conversations")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(conversationId.toString()))
                .andExpect(jsonPath("$[0].unreadCount").value(1));
    }

    @Test
    void shouldSendMessage() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        MessageResult mockResult = new MessageResult(UUID.randomUUID(), conversationId, userId, "Hello back!", null, LocalDateTime.now(), false);

        Mockito.when(chatService.sendMessage(any(ChatService.SendMessageCommand.class))).thenReturn(mockResult);

        ConversationController.SendMessageRequest request = new ConversationController.SendMessageRequest("Hello back!");

        mockMvc.perform(post("/api/conversations/{id}/messages", conversationId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello back!"))
                .andExpect(jsonPath("$.senderId").value(userId.toString()));
    }
}
