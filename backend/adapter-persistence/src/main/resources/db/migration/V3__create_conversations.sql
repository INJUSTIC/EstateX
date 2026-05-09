CREATE TABLE conversations
(
    id               UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_id       UUID      NOT NULL REFERENCES listings (id) ON DELETE SET NULL,
    initiator_id     UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    listing_owner_id UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    started_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_conv_listing_initiator UNIQUE (listing_id, initiator_id)
);

CREATE INDEX idx_conv_initiator      ON conversations (initiator_id);
CREATE INDEX idx_conv_listing_owner  ON conversations (listing_owner_id);

CREATE TABLE messages
(
    id              UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    conversation_id UUID      NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    sender_id       UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content         TEXT,
    attachment_url  VARCHAR(500),
    sent_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    read            BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id);
CREATE INDEX idx_messages_read         ON messages (conversation_id, read);
