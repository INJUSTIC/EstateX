package com.estatex.domain.chat;

import com.estatex.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {

    private static final UUID LISTING_ID = UUID.randomUUID();
    private static final UUID INITIATOR_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();

    @Test
    void shouldCreateConversationWhenValidParticipantsGiven() {
        //when
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //then
        assertNotNull(conversation.getId());
        assertEquals(LISTING_ID, conversation.getListingId());
        assertEquals(INITIATOR_ID, conversation.getInitiatorId());
        assertEquals(OWNER_ID, conversation.getListingOwnerId());
    }

    @Test
    void shouldThrowWhenInitiatorIsSameAsOwner() {
        //given
        UUID sameUser = UUID.randomUUID();

        //when / then
        assertThrows(DomainException.class,
                () -> Conversation.create(LISTING_ID, sameUser, sameUser));
    }

    @Test
    void shouldReturnTrueWhenInitiatorCheckedAsParticipant() {
        //given
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //when
        boolean result = conversation.isParticipant(INITIATOR_ID);

        //then
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenOwnerCheckedAsParticipant() {
        //given
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //when
        boolean result = conversation.isParticipant(OWNER_ID);

        //then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenStrangerCheckedAsParticipant() {
        //given
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //when
        boolean result = conversation.isParticipant(UUID.randomUUID());

        //then
        assertFalse(result);
    }

    @Test
    void shouldReturnOwnerAsOtherParticipantWhenInitiatorAsked() {
        //given
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //when
        UUID other = conversation.otherParticipant(INITIATOR_ID);

        //then
        assertEquals(OWNER_ID, other);
    }

    @Test
    void shouldReturnInitiatorAsOtherParticipantWhenOwnerAsked() {
        //given
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //when
        UUID other = conversation.otherParticipant(OWNER_ID);

        //then
        assertEquals(INITIATOR_ID, other);
    }

    @Test
    void shouldHaveNonNullStartedAtWhenCreated() {
        //when
        Conversation conversation = Conversation.create(LISTING_ID, INITIATOR_ID, OWNER_ID);

        //then
        assertNotNull(conversation.getStartedAt());
    }
}
