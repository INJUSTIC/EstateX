package com.estatex.adapter.web.controller;

import com.estatex.application.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Chat")
public class ConversationController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    record StartConversationRequest(UUID listingId) {}
    record SendMessageRequest(String content) {}

    @PostMapping
    @Operation(summary = "Start a conversation about a listing")
    public ResponseEntity<?> start(@RequestHeader("X-User-Id") UUID userId,
                                   @RequestBody StartConversationRequest req) {
        return ResponseEntity.ok(chatService.startConversation(req.listingId(), userId));
    }

    @GetMapping
    @Operation(summary = "Get my conversations")
    public ResponseEntity<?> list(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(chatService.getConversations(userId));
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get messages (marks them as read)")
    public ResponseEntity<?> getMessages(@RequestHeader("X-User-Id") UUID userId,
                                         @PathVariable UUID conversationId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, userId, page, size));
    }

    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Send a text message")
    public ResponseEntity<?> sendMessage(@RequestHeader("X-User-Id") UUID userId,
                                         @PathVariable UUID conversationId,
                                         @RequestBody SendMessageRequest req) {
        var cmd = new ChatService.SendMessageCommand(conversationId, userId, req.content(), null, null, null);
        var message = chatService.sendMessage(cmd);
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, message);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/{conversationId}/messages/attachment")
    @Operation(summary = "Send a message with file attachment")
    public ResponseEntity<?> sendAttachment(@RequestHeader("X-User-Id") UUID userId,
                                            @PathVariable UUID conversationId,
                                            @RequestParam(required = false) String content,
                                            @RequestParam("file") MultipartFile file) throws IOException {
        var cmd = new ChatService.SendMessageCommand(conversationId, userId, content,
                file.getOriginalFilename(), file.getInputStream(), file.getContentType());
        var message = chatService.sendMessage(cmd);
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, message);
        return ResponseEntity.ok(message);
    }
}
