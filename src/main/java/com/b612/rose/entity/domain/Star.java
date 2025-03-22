package com.b612.rose.entity.domain;

import com.b612.rose.entity.enums.PurifiedType;
import com.b612.rose.entity.enums.StarType;
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
public class Star {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "star_id")
    private Integer starId;

    @Enumerated(EnumType.STRING)
    private StarType starType;

    @Column(name = "npc_id")
    private Integer npcId;

    @Enumerated(EnumType.STRING)
    private PurifiedType purifiedType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", referencedColumnName = "npc_id", insertable = false, updatable = false)
    private Npc npc;

}
