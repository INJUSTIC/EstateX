
CREATE TABLE users
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    phone        VARCHAR(30),
    avatar_url   VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_users_email ON users (email);
