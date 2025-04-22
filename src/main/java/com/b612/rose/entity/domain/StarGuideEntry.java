package com.b612.rose.entity.domain;

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
public class StarGuideEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer entryId;

    private String starName; // 이름
    private String starSource; // 출처? 근원?이 되는 감정

    @Column(length = 1000)
    private String description;

    private Integer orderIndex;
}

