package com.b612.rose.controller;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.response.EmailResponse;
import com.b612.rose.service.service.EmailService;
import com.b612.rose.utils.GameStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameCompletionController {

    private final GameStateManager gameStateManager;
    private final EmailService emailService;

    @PostMapping("/{userId}/complete")
    public ResponseEntity<Void> completeGameAndSendEmail(
            @PathVariable UUID userId,
            @RequestBody EmailRequest request) {

        if (!gameStateManager.areAllStarsCollectedAndDelivered(userId)) {
            return ResponseEntity.badRequest().build();
        }

        gameStateManager.completeGame(userId, request.getEmail(), request.getConcern(), request.getSelectedNpc());

        boolean isSuccess = emailService.sendEmail(userId);

        if (isSuccess) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
