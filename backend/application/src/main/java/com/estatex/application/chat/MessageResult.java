package com.estatex.application.chat;

import com.estatex.domain.chat.Message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResult(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String content,
        String attachmentUrl,
        LocalDateTime sentAt,
        boolean read
) {
    public static MessageResult from(Message message) {
        return new MessageResult(message.getId(), message.getConversationId(),
                message.getSenderId(), message.getContent(),
                message.getAttachmentUrl(), message.getSentAt(), message.isRead());
    }
}
