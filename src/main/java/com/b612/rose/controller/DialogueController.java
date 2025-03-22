package com.b612.rose.controller;

import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.GameProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dialogues")
@RequiredArgsConstructor
public class DialogueController {
    private final DialogueService dialogueService;
    private final GameProgressService gameProgressService;

    @GetMapping("/{userId}/current")
    public ResponseEntity<List<DialogueResponse>> getCurrentDialogues(@PathVariable UUID userId) {
        GameStage currentStage = gameProgressService.getCurrentStage(userId);
        if (currentStage == null) {
            return ResponseEntity.notFound().build();
        }

        List<DialogueResponse> dialogueResponses = dialogueService.getDialoguesForCurrentStage(userId, currentStage);
        return ResponseEntity.ok(dialogueResponses);
    }

    @GetMapping("/{userId}/{dialogueType}")
    public ResponseEntity<DialogueResponse> getDialogueByType(
            @PathVariable UUID userId,
            @PathVariable String dialogueType) {
        DialogueResponse dialogueResponse = dialogueService.getDialogueByType(dialogueType, userId);
        return ResponseEntity.ok(dialogueResponse);
    }

    @GetMapping("/{userId}/{dialogueType}/{npcId}")
    public ResponseEntity<DialogueResponse> getDialogueByTypeAndNpc(
            @PathVariable UUID userId,
            @PathVariable String dialogueType,
            @PathVariable Integer npcId) {
        DialogueResponse dialogueResponse = dialogueService.getDialogueByTypeAndNpcId(dialogueType, npcId, userId);
        return ResponseEntity.ok(dialogueResponse);
    }
}
