package com.estatex.adapter.persistence.listing;

import com.estatex.domain.listing.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ListingRepositoryAdapter implements ListingRepository {

    private final ListingJpaRepository jpa;

    public ListingRepositoryAdapter(ListingJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Listing save(Listing listing) {
        return toDomain(jpa.save(toEntity(listing)));
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Listing> findByOwnerId(UUID ownerId) {
        return jpa.findByOwnerId(ownerId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public ListingRepository.ListingPage search(ListingSearchCriteria c, int page, int size) {
        var sort = buildSort(c);
        var pageable = PageRequest.of(page, size, sort);
        String keywordParam = c.keyword() != null ? "%" + c.keyword().toLowerCase() + "%" : null;
        String cityParam = c.city() != null ? c.city().toLowerCase() : null;
        String voivodeshipParam = c.voivodeship() != null ? c.voivodeship().toLowerCase() : null;
        var result = jpa.search(
                keywordParam, cityParam, voivodeshipParam,
                c.minPrice(), c.maxPrice(),
                c.propertyType() != null ? c.propertyType().name() : null,
                c.transactionType() != null ? c.transactionType().name() : null,
                c.minRooms(), c.maxRooms(), c.minArea(), c.maxArea(),
                c.availableEarliest(), c.availableLatest(),
                pageable
        );
        var items = result.getContent().stream().map(this::toDomain).collect(Collectors.toList());
        return new ListingRepository.ListingPage(items, result.getTotalElements(),
                result.getTotalPages(), result.getNumber());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID id) {
        jpa.incrementViewCount(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Sort buildSort(ListingSearchCriteria c) {
        var dir = c.sortDirection() == ListingSearchCriteria.SortDirection.ASC
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        var field = switch (c.sortBy()) {
            case PRICE -> "price";
            case AREA -> "areaSqMeters";
            default -> "createdAt";
        };
        return Sort.by(dir, field);
    }

    private ListingJpaEntity toEntity(Listing l) {
        var entity = ListingJpaEntity.builder()
                .id(l.getId())
                .title(l.getTitle())
                .description(l.getDescription())
                .street(l.getAddress().street())
                .city(l.getAddress().city())
                .voivodeship(l.getAddress().voivodeship())
                .postalCode(l.getAddress().postalCode())
                .country(l.getAddress().country())
                .latitude(l.getAddress().latitude())
                .longitude(l.getAddress().longitude())
                .propertyType(l.getPropertyType().name())
                .transactionType(l.getTransactionType().name())
                .price(l.getPrice().amount())
                .currency("PLN")
                .areaSqMeters(l.getAreaSqMeters())
                .numberOfRooms(l.getNumberOfRooms())
                .status(l.getStatus().name())
                .ownerId(l.getOwnerId())
                .viewCount(l.getViewCount())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .availableFrom(l.getAvailableFrom())
                .build();

        l.getPhotos().forEach(p -> entity.getPhotos().add(
                PhotoJpaEntity.builder().id(p.getId()).listing(entity)
                        .url(p.getUrl()).cover(p.isCover()).uploadedAt(p.getUploadedAt()).build()
        ));
        return entity;
    }

    private Listing toDomain(ListingJpaEntity e) {
        var address = Address.of(e.getStreet(), e.getCity(), e.getVoivodeship(),
                e.getPostalCode(), e.getCountry(), e.getLatitude(), e.getLongitude());
        var money = new Money(e.getPrice());
        var photos = e.getPhotos().stream()
                .map(p -> new Photo(p.getId(), e.getId(), p.getUrl(), p.isCover(), p.getUploadedAt()))
                .collect(Collectors.toList());
        return new Listing(e.getId(), e.getTitle(), e.getDescription(), address,
                PropertyType.valueOf(e.getPropertyType()),
                ListingTransactionType.valueOf(e.getTransactionType()),
                money, e.getAreaSqMeters(), e.getNumberOfRooms(),
                ListingStatus.valueOf(e.getStatus()), e.getOwnerId(),
                photos, e.getViewCount(), e.getCreatedAt(), e.getUpdatedAt(),
                e.getAvailableFrom());
    }
}
