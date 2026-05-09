package com.estatex.adapter.persistence.favourite;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "favourites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "listing_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavouriteJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid") private UUID userId;
    @Column(name = "listing_id", nullable = false, columnDefinition = "uuid") private UUID listingId;
    @Column(name = "saved_at", nullable = false) private LocalDateTime savedAt;
}
