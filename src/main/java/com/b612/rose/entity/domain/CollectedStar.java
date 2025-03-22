package com.b612.rose.entity.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectedStar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer collectionId;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Star star;

    @Column(nullable = false)
    private boolean collected;

    @Column(nullable = false)
    private boolean delivered;

    private LocalDateTime collectedAt;
    private LocalDateTime deliveredAt;
}
