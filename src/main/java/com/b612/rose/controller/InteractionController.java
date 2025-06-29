package com.b612.rose.controller;

import com.b612.rose.dto.request.StarGuidePageRequest;
import com.b612.rose.dto.response.ApiResponse;
import com.b612.rose.dto.response.CharacterProfileResponse;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.ObjectStatusResponse;
import com.b612.rose.dto.response.StarGuideResponse;
import com.b612.rose.service.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<List<ObjectStatusResponse>>> getObjectStatus(@PathVariable UUID userId) {
        List<ObjectStatusResponse> responses = interactionService.getObjectStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "오브젝트 상태를 성공적으로 조회했습니다."));
    }

    @GetMapping("/{userId}/star-guide")
    public ResponseEntity<ApiResponse<StarGuideResponse>> getStarGuide(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "true") boolean includeDialogues) {
        StarGuideResponse response = interactionService.getStarGuide(userId, page, includeDialogues);
        return ResponseEntity.ok(ApiResponse.success(response, "별 가이드를 성공적으로 조회했습니다."));
    }

    @GetMapping("/{userId}/character-profile")
    public ResponseEntity<ApiResponse<CharacterProfileResponse>> getCharacterProfile(@PathVariable UUID userId) {
        CharacterProfileResponse response = interactionService.getCharacterProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "캐릭터 프로필을 성공적으로 조회했습니다."));
    }

    @GetMapping("/{userId}/request-form")
    public ResponseEntity<ApiResponse<List<DialogueResponse>>> getRequestForm(@PathVariable UUID userId) {
        List<DialogueResponse> response = interactionService.getRequestForm(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "의뢰서를 성공적으로 조회했습니다."));
    }
}
