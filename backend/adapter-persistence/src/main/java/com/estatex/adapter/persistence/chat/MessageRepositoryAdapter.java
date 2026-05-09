package com.estatex.adapter.persistence.chat;

import com.estatex.domain.chat.Message;
import com.estatex.domain.chat.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository jpa;

    public MessageRepositoryAdapter(MessageJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Message save(Message m) {
        var e = MessageJpaEntity.builder()
                .id(m.getId()).conversationId(m.getConversationId())
                .senderId(m.getSenderId()).content(m.getContent())
                .attachmentUrl(m.getAttachmentUrl()).sentAt(m.getSentAt())
                .read(m.isRead())
                .build();
        var saved = jpa.save(e);
        return toDomain(saved);
    }

    @Override
    public List<Message> findByConversationId(UUID conversationId, int page, int size) {
        return jpa.findByConversationIdOrderBySentAtAsc(conversationId, PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByConversationId(UUID conversationId) {
        return jpa.countByConversationId(conversationId);
    }

    @Override
    public long countUnread(UUID conversationId, UUID userId) {
        return jpa.countUnread(conversationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID conversationId, UUID recipientId) {
        jpa.markAllAsRead(conversationId, recipientId);
    }

    private Message toDomain(MessageJpaEntity e) {
        return new Message(e.getId(), e.getConversationId(), e.getSenderId(),
                e.getContent(), e.getAttachmentUrl(), e.getSentAt(), e.isRead());
    }
}
