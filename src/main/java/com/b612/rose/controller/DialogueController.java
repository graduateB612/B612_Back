package com.b612.rose.controller;

import com.b612.rose.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<DialogueResponse>>> getCurrentDialogues(@PathVariable UUID userId) {
        GameStage currentStage = gameProgressService.getCurrentStage(userId);
        if (currentStage == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("G001", "현재 게임 스테이지를 찾을 수 없습니다."));
        }

        List<DialogueResponse> dialogueResponses = dialogueService.getDialoguesForCurrentStage(userId, currentStage);
        return ResponseEntity.ok(ApiResponse.success(dialogueResponses, "현재 스테이지의 대화를 성공적으로 조회했습니다."));
    }

    @GetMapping("/{userId}/{dialogueType}")
    public ResponseEntity<ApiResponse<DialogueResponse>> getDialogueByType(
            @PathVariable UUID userId,
            @PathVariable String dialogueType) {
        DialogueResponse dialogueResponse = dialogueService.getDialogueByType(dialogueType, userId);
        return ResponseEntity.ok(ApiResponse.success(dialogueResponse, "대화를 성공적으로 조회했습니다."));
    }

    @GetMapping("/{userId}/{dialogueType}/{npcId}")
    public ResponseEntity<ApiResponse<DialogueResponse>> getDialogueByTypeAndNpc(
            @PathVariable UUID userId,
            @PathVariable String dialogueType,
            @PathVariable Integer npcId) {
        DialogueResponse dialogueResponse = dialogueService.getDialogueByTypeAndNpcId(dialogueType, npcId, userId);
        return ResponseEntity.ok(ApiResponse.success(dialogueResponse, "NPC별 대화를 성공적으로 조회했습니다."));
    }
}
