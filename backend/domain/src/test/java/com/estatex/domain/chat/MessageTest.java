package com.estatex.domain.chat;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID SENDER_ID = UUID.randomUUID();

    @Test
    void shouldCreateMessageWithUnreadStatusWhenCreated() {
        //when
        Message message = Message.create(CONVERSATION_ID, SENDER_ID, "Hej!", null);

        //then
        assertFalse(message.isRead());
    }

    @Test
    void shouldCreateMessageWithContentWhenCreated() {
        //given
        String content = "Jestem zainteresowany";

        //when
        Message message = Message.create(CONVERSATION_ID, SENDER_ID, content, null);

        //then
        assertEquals(content, message.getContent());
    }

    @Test
    void shouldCreateMessageWithAttachmentUrlWhenProvided() {
        //given
        String attachmentUrl = "http://example.com/file.pdf";

        //when
        Message message = Message.create(CONVERSATION_ID, SENDER_ID, null, attachmentUrl);

        //then
        assertEquals(attachmentUrl, message.getAttachmentUrl());
    }

    @Test
    void shouldThrowWhenBothContentAndAttachmentAreNull() {
        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> Message.create(CONVERSATION_ID, SENDER_ID, null, null));
    }

    @Test
    void shouldThrowWhenContentIsBlankAndNoAttachment() {
        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> Message.create(CONVERSATION_ID, SENDER_ID, "   ", null));
    }

    @Test
    void shouldMarkMessageAsReadWhenMarkAsReadCalled() {
        //given
        Message message = Message.create(CONVERSATION_ID, SENDER_ID, "Hej!", null);

        //when
        message.markAsRead();

        //then
        assertTrue(message.isRead());
    }

    @Test
    void shouldHaveNonNullIdWhenCreated() {
        //when
        Message message = Message.create(CONVERSATION_ID, SENDER_ID, "Hej!", null);

        //then
        assertNotNull(message.getId());
        assertEquals(CONVERSATION_ID, message.getConversationId());
        assertEquals(SENDER_ID, message.getSenderId());
    }

    @Test
    void shouldHaveNonNullSentAtWhenCreated() {
        //when
        Message message = Message.create(CONVERSATION_ID, SENDER_ID, "Hej!", null);

        //then
        assertNotNull(message.getSentAt());
    }
}
