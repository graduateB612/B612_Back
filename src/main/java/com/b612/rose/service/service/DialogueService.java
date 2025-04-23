package com.b612.rose.service.service;

import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.entity.enums.GameStage;

import java.util.List;
import java.util.UUID;

public interface DialogueService {
    DialogueResponse getDialogueByType(String dialogueType, UUID userId);
    DialogueResponse getDialogueByTypeAndNpcId(String dialogueType, Integer npcId, UUID userId);
    List<DialogueResponse> getDialoguesForCurrentStage(UUID userId, GameStage currentStage);
    List<DialogueResponse> getDialoguesByType(String dialogueType, UUID userId);
}
