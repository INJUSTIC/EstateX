CREATE TABLE listings
(
    id               UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    title            VARCHAR(255)   NOT NULL,
    description      TEXT,
    street           VARCHAR(255),
    city             VARCHAR(100)   NOT NULL,
    voivodeship      VARCHAR(100),
    postal_code      VARCHAR(20),
    country          VARCHAR(100)   NOT NULL DEFAULT 'Poland',
    latitude         DOUBLE PRECISION,
    longitude        DOUBLE PRECISION,
    property_type    VARCHAR(50)    NOT NULL,
    transaction_type VARCHAR(50)    NOT NULL DEFAULT 'RENT',
    price            NUMERIC(12, 2) NOT NULL,
    currency         VARCHAR(10)    NOT NULL DEFAULT 'PLN',
    area_sq_meters   DOUBLE PRECISION NOT NULL,
    number_of_rooms  INTEGER        NOT NULL,
    status           VARCHAR(50)    NOT NULL DEFAULT 'ACTIVE',
    owner_id         UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    view_count       INTEGER        NOT NULL DEFAULT 0,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE INDEX idx_listings_owner            ON listings (owner_id);
CREATE INDEX idx_listings_status           ON listings (status);
CREATE INDEX idx_listings_city             ON listings (city);
CREATE INDEX idx_listings_price            ON listings (price);
CREATE INDEX idx_listings_transaction_type ON listings (transaction_type);

CREATE TABLE photos
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    listing_id  UUID         NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    cover       BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_photos_listing ON photos (listing_id);
