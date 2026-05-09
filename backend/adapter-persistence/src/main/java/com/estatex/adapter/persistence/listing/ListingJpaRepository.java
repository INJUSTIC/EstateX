package com.estatex.adapter.persistence.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListingJpaRepository extends JpaRepository<ListingJpaEntity, UUID> {

    List<ListingJpaEntity> findByOwnerId(UUID ownerId);

    @Query("""
            SELECT l FROM ListingJpaEntity l
            WHERE l.status = 'ACTIVE'
            AND (:keyword IS NULL OR LOWER(l.title) LIKE :keyword OR LOWER(l.description) LIKE :keyword)
            AND (:city IS NULL OR LOWER(l.city) = :city)
            AND (:voivodeship IS NULL OR LOWER(l.voivodeship) = :voivodeship)
            AND (:minPrice IS NULL OR l.price >= :minPrice)
            AND (:maxPrice IS NULL OR l.price <= :maxPrice)
            AND (:propertyType IS NULL OR l.propertyType = CAST(:propertyType AS string))
            AND (:transactionType IS NULL OR l.transactionType = CAST(:transactionType AS string))
            AND (:minRooms IS NULL OR l.numberOfRooms >= :minRooms)
            AND (:maxRooms IS NULL OR l.numberOfRooms <= :maxRooms)
            AND (:minArea IS NULL OR l.areaSqMeters >= :minArea)
            AND (:maxArea IS NULL OR l.areaSqMeters <= :maxArea)
            AND (:availableEarliest IS NULL OR l.availableFrom IS NULL OR l.availableFrom >= :availableEarliest)
            AND (:availableLatest IS NULL OR l.availableFrom IS NULL OR l.availableFrom <= :availableLatest)
            """)
    Page<ListingJpaEntity> search(
            @org.springframework.data.repository.query.Param("keyword") String keyword, @org.springframework.data.repository.query.Param("city") String city, @org.springframework.data.repository.query.Param("voivodeship") String voivodeship,
            @org.springframework.data.repository.query.Param("minPrice") BigDecimal minPrice, @org.springframework.data.repository.query.Param("maxPrice") BigDecimal maxPrice,
            @org.springframework.data.repository.query.Param("propertyType") String propertyType, @org.springframework.data.repository.query.Param("transactionType") String transactionType,
            @org.springframework.data.repository.query.Param("minRooms") Integer minRooms, @org.springframework.data.repository.query.Param("maxRooms") Integer maxRooms,
            @org.springframework.data.repository.query.Param("minArea") Double minArea, @org.springframework.data.repository.query.Param("maxArea") Double maxArea,
            @org.springframework.data.repository.query.Param("availableEarliest") LocalDate availableEarliest,
            @org.springframework.data.repository.query.Param("availableLatest") LocalDate availableLatest,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ListingJpaEntity l SET l.viewCount = l.viewCount + 1 WHERE l.id = :id")
    void incrementViewCount(@org.springframework.data.repository.query.Param("id") UUID id);
}
