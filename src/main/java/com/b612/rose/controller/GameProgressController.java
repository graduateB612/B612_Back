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
    public ResponseEntity<ApiResponse<GameStateResponse>> updateGameStage(
            @PathVariable UUID userId,
            @RequestBody GameStageUpdateRequest request) {
        GameStateResponse response = gameProgressService.updateGameStage(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "게임 스테이지가 성공적으로 업데이트되었습니다."));
    }

    @PostMapping("/{userId}/collect")
    public ResponseEntity<ApiResponse<GameStateResponse>> collectStar(
            @PathVariable UUID userId,
            @RequestBody StarActionRequest request) {
        GameStateResponse response = starActionService.onStarCollected(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "별을 성공적으로 수집했습니다."));
    }

    @PostMapping("/{userId}/deliver")
    public ResponseEntity<ApiResponse<GameStateResponse>> deliverStar(
            @PathVariable UUID userId,
            @RequestBody StarActionRequest request) {
        GameStateResponse response = starActionService.onStarDelivered(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "별을 성공적으로 전달했습니다."));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<GameStateResponse>> getCurrentGameState(@PathVariable UUID userId) {
        GameStateResponse response = gameProgressService.getCurrentGameState(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "현재 게임 상태를 성공적으로 조회했습니다."));
    }
}
