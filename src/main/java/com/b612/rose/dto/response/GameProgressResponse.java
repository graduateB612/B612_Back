package com.b612.rose.dto.response;

import com.b612.rose.entity.enums.GameStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameProgressResponse {
    private UUID userId;
    private GameStage currentStage;
}
