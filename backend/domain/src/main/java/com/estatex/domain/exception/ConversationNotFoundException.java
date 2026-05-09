package com.estatex.domain.exception;

import java.util.UUID;

public class ConversationNotFoundException extends DomainException {
    public ConversationNotFoundException(UUID id) {
        super("Conversation not found: " + id);
    }
}
