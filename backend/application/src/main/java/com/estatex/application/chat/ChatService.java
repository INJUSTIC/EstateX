package com.estatex.application.chat;

import com.estatex.application.port.out.FileStoragePort;
import com.estatex.domain.chat.Conversation;
import com.estatex.domain.chat.ConversationRepository;
import com.estatex.domain.chat.Message;
import com.estatex.domain.chat.MessageRepository;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.exception.ConversationNotFoundException;
import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.listing.ListingRepository;
import com.estatex.domain.listing.ListingStatus;
import com.estatex.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final FileStoragePort fileStorage;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       ListingRepository listingRepository,
                       UserRepository userRepository,
                       FileStoragePort fileStorage) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
    }

    // ── UC-5.1  Start conversation ────────────────────────────────────────────

    public ConversationResult startConversation(UUID listingId, UUID initiatorId) {
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));
        var existing = conversationRepository.findByListingIdAndInitiatorId(listingId, initiatorId);
        if (existing.isPresent()) {
            return toResult(existing.get(), listing.getTitle());
        }
        var conversation = Conversation.create(listingId, initiatorId, listing.getOwnerId());
        conversation = conversationRepository.save(conversation);
        return toResult(conversation, listing.getTitle());
    }

    // ── UC-5.2  Send message ──────────────────────────────────────────────────

    public record SendMessageCommand(UUID conversationId, UUID senderId,
                                     String content, String filename,
                                     InputStream attachmentData, String attachmentContentType) {}

    public MessageResult sendMessage(SendMessageCommand cmd) {
        var conversation = conversationRepository.findById(cmd.conversationId())
                .orElseThrow(() -> new ConversationNotFoundException(cmd.conversationId()));
        if (!conversation.isParticipant(cmd.senderId())) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }
        String attachmentUrl = null;
        if (cmd.attachmentData() != null && cmd.filename() != null) {
            attachmentUrl = fileStorage.store(cmd.filename(), cmd.attachmentData(), cmd.attachmentContentType());
        }
        var message = Message.create(cmd.conversationId(), cmd.senderId(), cmd.content(), attachmentUrl);
        message = messageRepository.save(message);
        return MessageResult.from(message);
    }

    // ── UC-5.3  Get conversations ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConversationSummaryResult> getConversations(UUID userId) {
        return conversationRepository.findByParticipantId(userId).stream()
                .map(c -> {
                    var listing = c.getListingId() != null
                            ? listingRepository.findById(c.getListingId()).orElse(null)
                            : null;
                    var title = listing != null ? listing.getTitle() : "(deleted listing)";
                    var listingStatus = listing != null ? listing.getStatus() : null;
                    var unread = messageRepository.countUnread(c.getId(), userId);
                    var initiatorName = userRepository.findById(c.getInitiatorId())
                            .map(u -> u.getDisplayName()).orElse("Unknown");
                    var ownerName = userRepository.findById(c.getListingOwnerId())
                            .map(u -> u.getDisplayName()).orElse("Unknown");
                    return new ConversationSummaryResult(c.getId(), c.getListingId(), title,
                            c.getInitiatorId(), c.getListingOwnerId(), c.getStartedAt(), unread,
                            listingStatus, initiatorName, ownerName);
                })
                .collect(Collectors.toList());
    }

    // ── UC-5.2  Get messages ──────────────────────────────────────────────────

    @Transactional
    public MessagePage getMessages(UUID conversationId, UUID userId, int page, int size) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        if (!conversation.isParticipant(userId)) {
            throw new AccessDeniedException();
        }
        messageRepository.markAllAsRead(conversationId, userId);
        var messages = messageRepository.findByConversationId(conversationId, page, size);
        var total = messageRepository.countByConversationId(conversationId);
        var totalPages = (int) Math.ceil((double) total / size);
        var items = messages.stream().map(MessageResult::from).collect(Collectors.toList());
        return new MessagePage(items, total, totalPages, page);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ConversationResult toResult(Conversation c, String listingTitle) {
        return new ConversationResult(c.getId(), c.getListingId(), listingTitle,
                c.getInitiatorId(), c.getListingOwnerId(), c.getStartedAt());
    }

    // ── Result types ──────────────────────────────────────────────────────────

    public record ConversationResult(UUID id, UUID listingId, String listingTitle,
                                     UUID initiatorId, UUID listingOwnerId, LocalDateTime startedAt) {}

    public record ConversationSummaryResult(UUID id, UUID listingId, String listingTitle,
                                            UUID initiatorId, UUID listingOwnerId,
                                            LocalDateTime startedAt, long unreadCount,
                                            ListingStatus listingStatus,
                                            String initiatorName, String ownerName) {}

    public record MessagePage(List<MessageResult> items, long totalElements, int totalPages, int page) {}
}
