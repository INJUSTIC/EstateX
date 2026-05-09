package com.estatex.domain.listing;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    @Test
    void shouldCreatePhotoWithAllProperties() {
        UUID listingId = UUID.randomUUID();
        String url = "http://example.com/photo.jpg";
        boolean isCover = true;
        
        Photo photo = Photo.create(listingId, url, isCover);
        
        assertNotNull(photo.getId());
        assertEquals(listingId, photo.getListingId());
        assertEquals(url, photo.getUrl());
        assertEquals(isCover, photo.isCover());
        assertNotNull(photo.getUploadedAt());
    }
}
