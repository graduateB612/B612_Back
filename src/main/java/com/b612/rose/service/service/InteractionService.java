package com.b612.rose.service.service;

import com.b612.rose.dto.response.CharacterProfileResponse;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.ObjectStatusResponse;
import com.b612.rose.dto.response.StarGuideResponse;

import java.util.List;
import java.util.UUID;

public interface InteractionService {
    List<ObjectStatusResponse> getObjectStatus(UUID userId);
    StarGuideResponse getStarGuide(UUID userId, int page, boolean includeDialogues);
    CharacterProfileResponse getCharacterProfile(UUID userId);
    List<DialogueResponse> getRequestForm(UUID userId);
}
