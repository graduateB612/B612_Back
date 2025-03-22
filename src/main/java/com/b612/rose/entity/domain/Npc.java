package com.b612.rose.entity.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
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
