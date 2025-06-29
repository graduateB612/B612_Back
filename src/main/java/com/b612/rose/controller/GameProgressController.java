package com.b612.rose.controller;

import com.b612.rose.dto.request.GameStageUpdateRequest;
import com.b612.rose.dto.request.StarActionRequest;
import com.b612.rose.dto.response.ApiResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.service.service.GameProgressService;
import com.b612.rose.service.service.StarActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game/progress")
@RequiredArgsConstructor
public class GameProgressController {

    private final GameProgressService gameProgressService;
    private final StarActionService starActionService;

    @PostMapping("/{userId}/start-game")
    public ResponseEntity<GameStateResponse> startGame(@PathVariable UUID userId) {
        GameStageUpdateRequest request = new GameStageUpdateRequest(GameStage.GAME_START);
        GameStateResponse response = gameProgressService.updateGameStage(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GameStateResponse> updateGameStage(
            @PathVariable UUID userId,
            @RequestBody GameStageUpdateRequest request) {
        GameStateResponse response = gameProgressService.updateGameStage(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/collect")
    public ResponseEntity<GameStateResponse> collectStar(
            @PathVariable UUID userId,
            @RequestBody StarActionRequest request) {
        GameStateResponse response = starActionService.onStarCollected(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/deliver")
    public ResponseEntity<GameStateResponse> deliverStar(
            @PathVariable UUID userId,
            @RequestBody StarActionRequest request) {
        GameStateResponse response = starActionService.onStarDelivered(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GameStateResponse> getCurrentGameState(@PathVariable UUID userId) {
        GameStateResponse response = gameProgressService.getCurrentGameState(userId);
        return ResponseEntity.ok(response);
    }
}
