package com.b612.rose.entity.domain;

import com.b612.rose.entity.enums.InteractiveObjectType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractiveObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "object_id")
    private Integer objectId;

    @Enumerated(EnumType.STRING)
    private InteractiveObjectType objectType;

    @Column(length = 1000)
    private String description;
}
