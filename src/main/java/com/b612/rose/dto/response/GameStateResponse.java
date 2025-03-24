package com.b612.rose.dto.response;

import com.b612.rose.entity.enums.GameStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {
    private UUID userId;
    private GameStage currentStage;
    private List<DialogueResponse> dialogues;
}
