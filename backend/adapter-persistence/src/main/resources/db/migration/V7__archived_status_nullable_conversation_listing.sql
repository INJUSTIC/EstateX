-- Rename PAUSED status to ARCHIVED
UPDATE listings SET status = 'ARCHIVED' WHERE status = 'PAUSED';

-- Make conversations.listing_id nullable so conversations survive listing deletion
ALTER TABLE conversations ALTER COLUMN listing_id DROP NOT NULL;

-- Change FK from ON DELETE CASCADE to ON DELETE SET NULL
ALTER TABLE conversations DROP CONSTRAINT conversations_listing_id_fkey;
ALTER TABLE conversations
    ADD CONSTRAINT conversations_listing_id_fkey
        FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE SET NULL;
