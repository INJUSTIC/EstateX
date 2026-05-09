package com.estatex.application.listing;

import com.estatex.application.port.out.FileStoragePort;
import com.estatex.domain.exception.AccessDeniedException;
import com.estatex.domain.exception.ListingNotFoundException;
import com.estatex.domain.listing.*;
import com.estatex.domain.user.User;
import com.estatex.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock private ListingRepository listingRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStoragePort fileStorage;
    @InjectMocks private ListingService listingService;

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID STRANGER_ID = UUID.randomUUID();

    private ListingService.CreateListingCommand buildCreateCmd() {
        return new ListingService.CreateListingCommand(
                OWNER_ID, "Kawalerka", "Opis",
                null, "Warszawa", null, null, "Poland", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new BigDecimal("2000.00"), 25.0, 1, null
        );
    }

    private Listing buildListing(UUID id) {
        var listing = Listing.create("Kawalerka", "Opis",
                Address.of(null, "Warszawa", null, null, "Poland", null, null),
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new Money(new BigDecimal("2000.00")), 25.0, 1, OWNER_ID, null);
        return listing;
    }

    private User buildOwner() {
        return User.create("jan@example.com", "Jan Kowalski");
    }

    // ── createListing ─────────────────────────────────────────────────────────

    @Test
    void shouldCreateListingAndReturnResultWhenCommandIsValid() {
        //given
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        ListingResult result = listingService.createListing(buildCreateCmd());

        //then
        assertEquals("Kawalerka", result.title());
    }

    @Test
    void shouldSaveListingWhenCreated() {
        //given
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        listingService.createListing(buildCreateCmd());

        //then
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    void shouldSetTransactionTypeWhenCreated() {
        //given
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        ListingResult result = listingService.createListing(buildCreateCmd());

        //then
        assertEquals(ListingTransactionType.RENT, result.transactionType());
    }

    @Test
    void shouldDefaultCountryToPolandWhenNullCountryGivenOnCreate() {
        //given
        var cmd = new ListingService.CreateListingCommand(
                OWNER_ID, "Kawalerka", "Opis",
                null, "Warszawa", null, null, null /* null country */, null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new BigDecimal("2000.00"), 25.0, 1, null);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        ListingResult result = listingService.createListing(cmd);

        //then
        assertEquals("Poland", result.country());
    }

    @Test
    void shouldPreserveExplicitCountryWhenNonNullCountryGivenOnCreate() {
        //given
        var cmd = new ListingService.CreateListingCommand(
                OWNER_ID, "Kawalerka", "Opis",
                null, "Berlin", null, null, "Germany", null, null,
                PropertyType.APARTMENT, ListingTransactionType.RENT,
                new BigDecimal("2000.00"), 25.0, 1, null);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        ListingResult result = listingService.createListing(cmd);

        //then
        assertEquals("Germany", result.country());
    }

    // ── getListingDetail ──────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenListingNotFoundById() {
        //given
        UUID id = UUID.randomUUID();
        when(listingRepository.findById(id)).thenReturn(Optional.empty());

        //when / then
        assertThrows(ListingNotFoundException.class, () -> listingService.getListingDetail(id));
    }

    @Test
    void shouldIncrementViewCountWhenListingDetailRetrieved() {
        //given
        UUID id = UUID.randomUUID();
        var listing = buildListing(id);
        when(listingRepository.findById(id)).thenReturn(Optional.of(listing));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        var result = listingService.getListingDetail(id);

        //then
        verify(listingRepository).incrementViewCount(id);
        assertNotNull(result);
        assertEquals("Kawalerka", result.title());
        assertEquals(1, result.viewCount()); // incrementViewCount was called on the domain object
    }

    // ── updateListing ─────────────────────────────────────────────────────────

    @Test
    void shouldThrowAccessDeniedWhenNonOwnerUpdatesListing() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        var cmd = new ListingService.UpdateListingCommand(
                listing.getId(), STRANGER_ID, "New", "Desc",
                null, "Kraków", null, null, "Poland", null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE,
                new BigDecimal("100.00"), 50.0, 2, null);

        //when / then
        assertThrows(AccessDeniedException.class, () -> listingService.updateListing(cmd));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentListing() {
        //given
        UUID listingId = UUID.randomUUID();
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());
        var cmd = new ListingService.UpdateListingCommand(
                listingId, OWNER_ID, "New", "Desc",
                null, "Kraków", null, null, "Poland", null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE,
                new BigDecimal("100.00"), 50.0, 2, null);

        //when / then
        assertThrows(ListingNotFoundException.class, () -> listingService.updateListing(cmd));
    }

    @Test
    void shouldUpdateListingAndReturnResultWhenOwnerUpdates() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        var cmd = new ListingService.UpdateListingCommand(
                listing.getId(), OWNER_ID, "Updated Title", "Updated Desc",
                null, "Kraków", null, null, "Poland", null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE,
                new BigDecimal("300000.00"), 60.0, 3, null);

        //when
        ListingResult result = listingService.updateListing(cmd);

        //then
        assertNotNull(result);
        assertEquals("Updated Title", result.title());
    }

    @Test
    void shouldDefaultCountryToPolandWhenNullCountryGivenOnUpdate() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));
        var cmd = new ListingService.UpdateListingCommand(
                listing.getId(), OWNER_ID, "T", "D",
                null, "Kraków", null, null, null /* null country */, null, null,
                PropertyType.HOUSE, ListingTransactionType.SALE,
                new BigDecimal("100.00"), 50.0, 2, null);

        //when
        var result = listingService.updateListing(cmd);

        //then
        assertNotNull(result);
        assertEquals("Poland", result.country());
    }

    // ── deleteListing ─────────────────────────────────────────────────────────

    @Test
    void shouldDeleteListingWhenOwnerRequests() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        //when
        listingService.deleteListing(listing.getId(), OWNER_ID, false);

        //then
        verify(listingRepository).delete(listing.getId());
    }

    @Test
    void shouldThrowAccessDeniedWhenNonOwnerDeletesListing() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        //when / then
        assertThrows(AccessDeniedException.class,
                () -> listingService.deleteListing(listing.getId(), STRANGER_ID, false));
    }

    @Test
    void shouldDeleteListingWhenAdminRequests() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        //when
        listingService.deleteListing(listing.getId(), STRANGER_ID, true);

        //then
        verify(listingRepository).delete(listing.getId());
    }

    @Test
    void shouldDeletePhotoFilesWhenListingDeleted() {
        //given
        var listing = buildListing(UUID.randomUUID());
        listing.addPhoto("http://example.com/img1.jpg");
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        //when
        listingService.deleteListing(listing.getId(), OWNER_ID, false);

        //then
        verify(fileStorage).delete("http://example.com/img1.jpg");
    }

    @Test
    void shouldThrowWhenDeletingNonExistentListing() {
        //given
        UUID id = UUID.randomUUID();
        when(listingRepository.findById(id)).thenReturn(Optional.empty());

        //when / then
        assertThrows(ListingNotFoundException.class,
                () -> listingService.deleteListing(id, OWNER_ID, false));
    }

    // ── changeStatus ──────────────────────────────────────────────────────────

    @Test
    void shouldChangeStatusWhenOwnerRequests() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        ListingResult result = listingService.changeStatus(listing.getId(), OWNER_ID, ListingStatus.ARCHIVED);

        //then
        assertEquals(ListingStatus.ARCHIVED, result.status());
    }

    @Test
    void shouldThrowWhenNonOwnerChangesStatus() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        //when / then
        assertThrows(AccessDeniedException.class,
                () -> listingService.changeStatus(listing.getId(), STRANGER_ID, ListingStatus.ARCHIVED));
    }

    // ── uploadPhoto ───────────────────────────────────────────────────────────

    @Test
    void shouldUploadPhotoWhenOwnerUploads() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(fileStorage.store(any(), any(), any())).thenReturn("http://example.com/photo.jpg");
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        ListingResult result = listingService.uploadPhoto(new ListingService.UploadPhotoCommand(
                listing.getId(), OWNER_ID, "photo.jpg",
                new ByteArrayInputStream(new byte[0]), "image/jpeg"));

        //then
        assertEquals(1, result.photos().size());
    }

    @Test
    void shouldThrowWhenNonOwnerUploadsPhoto() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));

        //when / then
        assertThrows(AccessDeniedException.class,
                () -> listingService.uploadPhoto(new ListingService.UploadPhotoCommand(
                        listing.getId(), STRANGER_ID, "photo.jpg",
                        new ByteArrayInputStream(new byte[0]), "image/jpeg")));
    }

    // ── searchListings ────────────────────────────────────────────────────────

    @Test
    void shouldReturnListingPageWhenSearchCalled() {
        //given
        var criteria = ListingSearchCriteria.empty();
        var page = new ListingRepository.ListingPage(List.of(), 0L, 0, 0);
        when(listingRepository.search(criteria, 0, 20)).thenReturn(page);

        //when
        var result = listingService.searchListings(criteria, 0, 20);

        //then
        assertEquals(0L, result.totalElements());
    }

    @Test
    void shouldReturnMappedListingResultsWhenSearchHasResults() {
        //given
        var listing = buildListing(UUID.randomUUID());
        var criteria = ListingSearchCriteria.empty();
        var page = new ListingRepository.ListingPage(List.of(listing), 1L, 1, 0);
        when(listingRepository.search(criteria, 0, 20)).thenReturn(page);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        var result = listingService.searchListings(criteria, 0, 20);

        //then
        assertEquals(1L, result.totalElements());
        assertEquals("Kawalerka", result.items().get(0).title());
    }

    // ── getMyListings ─────────────────────────────────────────────────────────

    @Test
    void shouldReturnOwnersListingsWhenCalled() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(listing));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        var result = listingService.getMyListings(OWNER_ID);

        //then
        assertEquals(1, result.size());
        assertEquals("Jan Kowalski", result.get(0).ownerName());
    }

    // ── getAnalytics ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnAnalyticsWithViewCountWhenCalled() {
        //given
        var listing = buildListing(UUID.randomUUID());
        when(listingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(listing));

        //when
        var result = listingService.getAnalytics(OWNER_ID);

        //then
        assertEquals(0, result.get(0).viewCount());
    }

    // ── deletePhoto ───────────────────────────────────────────────────────────

    @Test
    void shouldDeletePhotoFileWhenPhotoDeleted() {
        //given
        var listing = buildListing(UUID.randomUUID());
        var photo = listing.addPhoto("http://example.com/img.jpg");
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        var result = listingService.deletePhoto(listing.getId(), OWNER_ID, photo.getId());

        //then
        verify(fileStorage).delete("http://example.com/img.jpg");
        assertNotNull(result);
        assertTrue(result.photos().isEmpty());
    }

    @Test
    void shouldThrowWhenDeletingNonExistentPhoto() {
        //given
        var listing = buildListing(UUID.randomUUID());
        listing.addPhoto("http://example.com/img.jpg");
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        UUID nonExistentPhotoId = UUID.randomUUID();

        //when
        var ex = assertThrows(IllegalArgumentException.class,
                () -> listingService.deletePhoto(listing.getId(), OWNER_ID, nonExistentPhotoId));

        //then
        assertTrue(ex.getMessage().contains("Photo not found"));
    }

    @Test
    void shouldDeleteCorrectPhotoFileWhenMultiplePhotosExist() {
        //given
        var listing = buildListing(UUID.randomUUID());
        listing.addPhoto("http://example.com/img1.jpg");
        var photo2 = listing.addPhoto("http://example.com/img2.jpg");
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        var result = listingService.deletePhoto(listing.getId(), OWNER_ID, photo2.getId());

        //then
        verify(fileStorage).delete("http://example.com/img2.jpg");
        assertNotNull(result);
        assertEquals(1, result.photos().size());
        assertEquals("http://example.com/img1.jpg", result.photos().get(0).url());
    }

    // ── setCoverPhoto ─────────────────────────────────────────────────────────

    @Test
    void shouldSetCoverPhotoWhenOwnerRequests() {
        //given
        var listing = buildListing(UUID.randomUUID());
        listing.addPhoto("http://example.com/img1.jpg");
        var second = listing.addPhoto("http://example.com/img2.jpg");
        when(listingRepository.findById(listing.getId())).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(buildOwner()));

        //when
        ListingResult result = listingService.setCoverPhoto(listing.getId(), OWNER_ID, second.getId());

        //then
        assertTrue(result.photos().stream()
                .filter(p -> p.id().equals(second.getId()))
                .findFirst().get().cover());
    }
}
