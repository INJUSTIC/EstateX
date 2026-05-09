package com.estatex.adapter.persistence.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "listing_id", columnDefinition = "uuid")
    private UUID listingId;

    @Column(name = "initiator_id", nullable = false, columnDefinition = "uuid")
    private UUID initiatorId;

    @Column(name = "listing_owner_id", nullable = false, columnDefinition = "uuid")
    private UUID listingOwnerId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
}
