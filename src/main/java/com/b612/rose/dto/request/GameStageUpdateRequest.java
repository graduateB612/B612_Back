package com.b612.rose.dto.request;

import com.b612.rose.entity.enums.GameStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStageUpdateRequest {
    private GameStage newStage;
}
