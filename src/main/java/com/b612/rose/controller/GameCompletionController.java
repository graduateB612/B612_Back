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
    public ResponseEntity<GameStateResponse> completeGameAndSendEmail(
            @PathVariable UUID userId,
            @RequestBody EmailRequest request) {
        GameStateResponse response = gameCompletionService.completeGameAndSendEmail(userId, request);
        return ResponseEntity.ok(response);
    }
}