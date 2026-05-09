package com.estatex.domain.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

    Message save(Message message);

    List<Message> findByConversationId(UUID conversationId, int page, int size);

    long countByConversationId(UUID conversationId);

    /** Number of unread messages in this conversation that were NOT sent by the given user. */
    long countUnread(UUID conversationId, UUID userId);

    /** Mark all messages in a conversation as read for a specific recipient. */
    void markAllAsRead(UUID conversationId, UUID recipientId);
}
