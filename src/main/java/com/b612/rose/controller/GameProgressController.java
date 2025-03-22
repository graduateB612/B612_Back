package com.b612.rose.controller;

import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.response.GameProgressResponse;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.service.service.GameProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game/progress")
@RequiredArgsConstructor
public class GameProgressController {

    private final GameProgressService gameProgressService;

    @PostMapping("/{userId}/start-game")
    public ResponseEntity<GameProgressResponse> startGame(@PathVariable UUID userId) {
        GameStageUpdateRequest request = new GameStageUpdateRequest(GameStage.GAME_START);
        GameProgressResponse response = gameProgressService.updateGameStage(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GameProgressResponse> updateGameStage(
            @PathVariable UUID userId,
            @RequestBody GameStageUpdateRequest requestDto) {
        GameProgressResponse response = gameProgressService.updateGameStage(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GameProgressResponse> getCurrentProgress(@PathVariable UUID userId) {
        GameProgressResponse response = GameProgressResponse.builder()
                .userId(userId)
                .currentStage(gameProgressService.getCurrentStage(userId))
                .build();

        return ResponseEntity.ok(response);
    }
}
