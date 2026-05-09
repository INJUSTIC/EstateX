-- Fix contradiction: listing_id is NOT NULL but the FK was ON DELETE SET NULL.
-- When a listing is deleted its conversations should be deleted too (CASCADE).
ALTER TABLE conversations
    DROP CONSTRAINT conversations_listing_id_fkey;

ALTER TABLE conversations
    ADD CONSTRAINT conversations_listing_id_fkey
        FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE;
