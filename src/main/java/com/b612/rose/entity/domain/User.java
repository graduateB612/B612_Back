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
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="user_id")
    private UUID userId;

    @Column(nullable = false)
    private String userName;

    private String email;
    private String concern;
    private String selectedNpc;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private boolean isCompleted;
}
