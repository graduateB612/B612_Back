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
    public ResponseEntity<List<ObjectStatusResponse>> getObjectStatus(@PathVariable UUID userId) {
        List<ObjectStatusResponse> responses = interactionService.getObjectStatus(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{userId}/star-guide")
    public ResponseEntity<StarGuideResponse> getStarGuide(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "true") boolean includeDialogues) {
        StarGuideResponse response = interactionService.getStarGuide(userId, page, includeDialogues);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/character-profile")
    public ResponseEntity<CharacterProfileResponse> getCharacterProfile(@PathVariable UUID userId) {
        CharacterProfileResponse response = interactionService.getCharacterProfile(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/request-form")
    public ResponseEntity<List<DialogueResponse>> getRequestForm(@PathVariable UUID userId) {
        List<DialogueResponse> response = interactionService.getRequestForm(userId);
        return ResponseEntity.ok(response);
    }
}
