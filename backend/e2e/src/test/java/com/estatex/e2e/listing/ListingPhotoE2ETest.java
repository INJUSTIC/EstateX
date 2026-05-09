package com.estatex.e2e.listing;

import com.estatex.e2e.E2ETestBase;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingPhotoE2ETest extends E2ETestBase {

    @SuppressWarnings("unchecked")
    private Map<String, Object> uploadPhoto(UUID ownerId, UUID listingId, String filename) {
        var headers = new HttpHeaders();
        headers.set("X-User-Id", ownerId.toString());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource("img".getBytes()) {
            @Override
            public String getFilename() { return filename; }
        });
        return http.exchange("/api/listings/" + listingId + "/photos",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class).getBody();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUploadPhotoAndSetAsCoverWhenFirstPhoto() {
        //given
        UUID ownerId = registerUser("photo1@test.com", "Owner");
        UUID listingId = createListing(ownerId);

        //when
        uploadPhoto(ownerId, listingId, "photo.jpg");
        var listing = http.getForObject("/api/listings/" + listingId, Map.class);

        //then
        var photos = (List<Map<String, Object>>) listing.get("photos");
        assertEquals(1, photos.size());
        assertTrue((Boolean) photos.get(0).get("cover"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPromoteNextPhotoToCoverWhenCoverDeleted() {
        //given
        UUID ownerId = registerUser("photo2@test.com", "Owner");
        UUID listingId = createListing(ownerId);
        uploadPhoto(ownerId, listingId, "first.jpg");
        uploadPhoto(ownerId, listingId, "second.jpg");
        var photos = (List<Map<String, Object>>) http.getForObject(
                "/api/listings/" + listingId, Map.class).get("photos");
        var coverId = photos.stream()
                .filter(p -> (Boolean) p.get("cover")).findFirst().orElseThrow().get("id");

        //when
        http.exchange("/api/listings/" + listingId + "/photos/" + coverId,
                HttpMethod.DELETE, new HttpEntity<>(userHeaders(ownerId)), Map.class);
        var remaining = (List<Map<String, Object>>) http.getForObject(
                "/api/listings/" + listingId, Map.class).get("photos");

        //then
        assertEquals(1, remaining.size());
        assertTrue((Boolean) remaining.get(0).get("cover"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldManuallySetCoverPhoto() {
        //given
        UUID ownerId = registerUser("photo3@test.com", "Owner");
        UUID listingId = createListing(ownerId);
        uploadPhoto(ownerId, listingId, "a.jpg");
        uploadPhoto(ownerId, listingId, "b.jpg");
        var photos = (List<Map<String, Object>>) http.getForObject(
                "/api/listings/" + listingId, Map.class).get("photos");
        var nonCoverId = photos.stream()
                .filter(p -> !(Boolean) p.get("cover")).findFirst().orElseThrow().get("id");

        //when
        var response = http.exchange(
                "/api/listings/" + listingId + "/photos/" + nonCoverId + "/cover",
                HttpMethod.PATCH, new HttpEntity<>(userHeaders(ownerId)), Map.class);

        //then
        assertEquals(200, response.getStatusCode().value());
        var updated = (List<Map<String, Object>>) response.getBody().get("photos");
        var targetPhoto = updated.stream()
                .filter(p -> p.get("id").equals(nonCoverId)).findFirst().orElseThrow();
        assertTrue((Boolean) targetPhoto.get("cover"));
    }

    @Test
    void shouldReturn400WhenUploadingMoreThan20Photos() {
        //given
        UUID ownerId = registerUser("photo4@test.com", "Owner");
        UUID listingId = createListing(ownerId);
        for (int i = 0; i < 20; i++) {
            uploadPhoto(ownerId, listingId, "photo" + i + ".jpg");
        }

        //when
        var headers = new HttpHeaders();
        headers.set("X-User-Id", ownerId.toString());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource("img".getBytes()) {
            @Override
            public String getFilename() { return "extra.jpg"; }
        });
        var response = http.exchange("/api/listings/" + listingId + "/photos",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        //then
        assertEquals(400, response.getStatusCode().value());
    }
}
