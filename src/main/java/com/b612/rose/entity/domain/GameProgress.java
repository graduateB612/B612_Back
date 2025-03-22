package com.b612.rose.entity.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String currentStage;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}
