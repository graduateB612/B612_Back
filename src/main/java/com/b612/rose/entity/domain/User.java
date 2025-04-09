package com.b612.rose.entity.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
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
    private boolean isCompleted;
}
