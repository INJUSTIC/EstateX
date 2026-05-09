CREATE TABLE favourites
(
    id         UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id    UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    listing_id UUID      NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    saved_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fav_user_listing UNIQUE (user_id, listing_id)
);

CREATE TABLE reviews
(
    id               UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    reviewer_id      UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reviewed_user_id UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    conversation_id  UUID      NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    rating           SMALLINT  NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text             TEXT,
    reply            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_review_conversation UNIQUE (conversation_id)
);

CREATE INDEX idx_reviews_reviewed_user ON reviews (reviewed_user_id);

CREATE TABLE reports
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    reporter_id UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type VARCHAR(50)  NOT NULL,
    target_id   UUID         NOT NULL,
    reason      TEXT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_status ON reports (status);
