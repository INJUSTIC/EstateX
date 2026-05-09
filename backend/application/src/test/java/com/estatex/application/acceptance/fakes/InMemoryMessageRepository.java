package com.estatex.application.acceptance.fakes;

import com.estatex.domain.chat.Message;
import com.estatex.domain.chat.MessageRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryMessageRepository implements MessageRepository {
    private final Map<UUID, Message> database = new HashMap<>();

    @Override
    public Message save(Message message) {
        database.put(message.getId(), message);
        return message;
    }

    @Override
    public List<Message> findByConversationId(UUID conversationId, int page, int size) {
        List<Message> filtered = database.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId))
                .sorted(Comparator.comparing(Message::getSentAt).reversed())
                .collect(Collectors.toList());

        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());

        return filtered.subList(start, end);
    }

    @Override
    public long countByConversationId(UUID conversationId) {
        return database.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId))
                .count();
    }

    @Override
    public long countUnread(UUID conversationId, UUID userId) {
        return database.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId) &&
                        !m.getSenderId().equals(userId) &&
                        !m.isRead())
                .count();
    }

    @Override
    public void markAllAsRead(UUID conversationId, UUID recipientId) {
        database.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId) &&
                        !m.getSenderId().equals(recipientId) &&
                        !m.isRead())
                .forEach(Message::markAsRead);
    }
}
