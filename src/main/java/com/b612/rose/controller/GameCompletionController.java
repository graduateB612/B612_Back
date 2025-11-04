package com.b612.rose.controller;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.response.GameStateResponse;
import com.b612.rose.service.service.GameCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@Slf4j
public class GameCompletionController {

    private final GameCompletionService gameCompletionService;

    @PostMapping("/{userId}/complete")
    public ResponseEntity<GameStateResponse> completeGameAndSendEmail(
            @PathVariable UUID userId,
            @RequestBody EmailRequest request) {
        log.info("클라이언트 concern 수신: {}", request.getConcern());
        GameStateResponse response = gameCompletionService.completeGameAndSendEmail(userId, request);
        return ResponseEntity.ok(response);
    }
}