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
public class Dialogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dialogueId;

    @Column(name = "npc_id")
    private Integer npcId;

    private String dialogueType;

    @Column(length = 1000)
    private String dialogueText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", referencedColumnName = "npc_id", insertable = false, updatable = false)
    private Npc npc;
}
