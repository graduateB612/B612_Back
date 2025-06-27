package com.b612.rose.entity.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectedStar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer collectionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "star_id", nullable = false)
    private Integer starId;

    @Column(nullable = false)
    private boolean collected;

    @Column(nullable = false)
    private boolean delivered;

    private LocalDateTime collectedAt;
    private LocalDateTime deliveredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_id", referencedColumnName = "star_id", insertable = false, updatable = false)
    private Star star;
}
