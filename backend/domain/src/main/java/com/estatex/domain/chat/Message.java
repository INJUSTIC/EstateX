package com.estatex.domain.chat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message entity — part of the Conversation aggregate.
 */
public class Message {

    private final UUID id;
    private final UUID conversationId;
    private final UUID senderId;
    private final String content;
    private final String attachmentUrl;
    private final LocalDateTime sentAt;
    private boolean read;

    public Message(UUID id, UUID conversationId, UUID senderId,
                   String content, String attachmentUrl,
                   LocalDateTime sentAt, boolean read) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.attachmentUrl = attachmentUrl;
        this.sentAt = sentAt;
        this.read = read;
    }

    public static Message create(UUID conversationId, UUID senderId,
                                  String content, String attachmentUrl) {
        if ((content == null || content.isBlank()) && attachmentUrl == null) {
            throw new IllegalArgumentException("Message must have content or an attachment");
        }
        return new Message(UUID.randomUUID(), conversationId, senderId,
                content, attachmentUrl, LocalDateTime.now(), false);
    }

    public void markAsRead() {
        this.read = true;
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public UUID getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public LocalDateTime getSentAt() { return sentAt; }
    public boolean isRead() { return read; }
}
