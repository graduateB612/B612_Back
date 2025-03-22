package com.b612.rose.entity.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Npc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "npc_id")
    private Integer npcId;

    private String npcName;
}
