package com.estatex.adapter.persistence.listing;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingJpaEntity listing;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private boolean cover;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
