package com.estatex.e2e.chat;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConversationE2ETest extends E2ETestBase {

    private UUID startConversation(UUID buyerId, UUID listingId) {
        var response = http.exchange("/api/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of("listingId", listingId.toString()), userHeaders(buyerId)), Map.class);
        return UUID.fromString((String) response.getBody().get("id"));
    }

    @Test
    void shouldOpenConversationWhenBuyerContactsOwner() {
        //given
        UUID ownerId = registerUser("chat1o@test.com", "Owner");
        UUID buyerId = registerUser("chat1b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);

        //when
        var response = http.exchange("/api/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of("listingId", listingId.toString()), userHeaders(buyerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody().get("id"));
        assertEquals(listingId.toString(), response.getBody().get("listingId").toString());
    }

    @Test
    void shouldReturnExistingConversationWhenDuplicateOpenAttempt() {
        //given
        UUID ownerId = registerUser("chat2o@test.com", "Owner");
        UUID buyerId = registerUser("chat2b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        UUID firstId = startConversation(buyerId, listingId);

        //when
        UUID secondId = startConversation(buyerId, listingId);

        //then
        assertEquals(firstId, secondId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendTextMessageAndRetrieveIt() {
        //given
        UUID ownerId = registerUser("chat3o@test.com", "Owner");
        UUID buyerId = registerUser("chat3b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        UUID convId = startConversation(buyerId, listingId);

        //when
        http.exchange("/api/conversations/" + convId + "/messages", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", "Hello!"), userHeaders(buyerId)), Map.class);
        var response = http.exchange("/api/conversations/" + convId + "/messages",
                HttpMethod.GET, new HttpEntity<>(userHeaders(buyerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        var items = (List<Map<String, Object>>) response.getBody().get("items");
        assertEquals(1, items.size());
        assertEquals("Hello!", items.get(0).get("content"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMarkMessagesAsReadWhenConversationFetchedByOtherParticipant() {
        //given
        UUID ownerId = registerUser("chat4o@test.com", "Owner");
        UUID buyerId = registerUser("chat4b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        UUID convId = startConversation(buyerId, listingId);
        http.exchange("/api/conversations/" + convId + "/messages", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", "Hi"), userHeaders(buyerId)), Map.class);

        //when — owner fetches messages, marking them as read
        var response = http.exchange("/api/conversations/" + convId + "/messages",
                HttpMethod.GET, new HttpEntity<>(userHeaders(ownerId)), Map.class);

        //then
        var items = (List<Map<String, Object>>) response.getBody().get("items");
        assertTrue(items.stream().allMatch(m -> (Boolean) m.get("read")));
    }

    @Test
    void shouldReturn403WhenStrangerReadsConversation() {
        //given
        UUID ownerId = registerUser("chat5o@test.com", "Owner");
        UUID buyerId = registerUser("chat5b@test.com", "Buyer");
        UUID strangerId = registerUser("chat5s@test.com", "Stranger");
        UUID listingId = createListing(ownerId);
        UUID convId = startConversation(buyerId, listingId);

        //when
        var response = http.exchange("/api/conversations/" + convId + "/messages",
                HttpMethod.GET, new HttpEntity<>(userHeaders(strangerId)), Map.class);

        //then
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void shouldReturn400WhenSendingEmptyMessage() {
        //given
        UUID ownerId = registerUser("chat6o@test.com", "Owner");
        UUID buyerId = registerUser("chat6b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        UUID convId = startConversation(buyerId, listingId);

        //when
        var response = http.exchange("/api/conversations/" + convId + "/messages", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", ""), userHeaders(buyerId)), Map.class);

        //then
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnInboxWithUnreadBadgeWhenMessagesUnread() {
        //given
        UUID ownerId = registerUser("chat7o@test.com", "Owner");
        UUID buyerId = registerUser("chat7b@test.com", "Buyer");
        UUID listingId = createListing(ownerId);
        UUID convId = startConversation(buyerId, listingId);
        http.exchange("/api/conversations/" + convId + "/messages", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", "Unread msg"), userHeaders(buyerId)), Map.class);

        //when — owner checks inbox, message not yet read
        var response = http.exchange("/api/conversations", HttpMethod.GET,
                new HttpEntity<>(userHeaders(ownerId)), List.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        var conv = (Map<String, Object>) response.getBody().get(0);
        assertTrue(((Number) conv.get("unreadCount")).longValue() > 0);
    }
}
