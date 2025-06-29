package com.b612.rose.controller;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.response.ApiResponse;
import com.b612.rose.dto.response.EmailResponse;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.service.service.EmailService;
import com.b612.rose.service.service.GameCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameCompletionController {

    private final GameCompletionService gameCompletionService;

    @PostMapping("/{userId}/complete")
    public ResponseEntity<ApiResponse<GameStateResponse>> completeGameAndSendEmail(
            @PathVariable UUID userId,
            @RequestBody EmailRequest request) {
        GameStateResponse response = gameCompletionService.completeGameAndSendEmail(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "게임이 성공적으로 완료되었고 이메일이 전송되었습니다."));
    }
}