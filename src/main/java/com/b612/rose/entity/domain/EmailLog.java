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
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer emailLogId;

    @Column
    private UUID userId;
    private String recipientEmail;
    private String subject;

    @Column(length = 2000)
    private String content;

    private LocalDateTime sentAt;
    private Boolean isDelivered;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}
