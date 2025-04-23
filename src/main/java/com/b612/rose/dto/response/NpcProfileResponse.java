package com.b612.rose.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NpcProfileResponse {
    private Integer npcId;
    private String npcName;
    private String description;
}
