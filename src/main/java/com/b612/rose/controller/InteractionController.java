package com.b612.rose.controller;

import com.b612.rose.dto.request.StarGuidePageRequest;
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
    public ResponseEntity<List<ObjectStatusResponse>> getObjectStatus(@PathVariable UUID userId) {
        List<ObjectStatusResponse> responses = interactionService.getObjectStatus(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{userId}/star-guide")
    public ResponseEntity<StarGuideResponse> interactWithStarGuide(
            @PathVariable UUID userId,
            @RequestBody(required = false) StarGuidePageRequest pageRequest) {
        int page = pageRequest != null ? pageRequest.getPage() : 0;
        StarGuideResponse response = interactionService.interactWithStarGuide(userId, page);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/character-profile")
    public ResponseEntity<CharacterProfileResponse> interactWithCharacterProfile(@PathVariable UUID userId) {
        CharacterProfileResponse response = interactionService.interactWithCharacterProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/request-form")
    public ResponseEntity<List<DialogueResponse>> interactWithRequestForm(@PathVariable UUID userId) {
        List<DialogueResponse> response = interactionService.interactWithRequestForm(userId);
        return ResponseEntity.ok(response);
    }
}
