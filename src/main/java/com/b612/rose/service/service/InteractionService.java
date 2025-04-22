package com.b612.rose.service.service;

import com.b612.rose.dto.response.CharacterProfileResponse;
import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.dto.response.ObjectStatusResponse;
import com.b612.rose.dto.response.StarGuideResponse;

import java.util.List;
import java.util.UUID;

public interface InteractionService {
    List<ObjectStatusResponse> getObjectStatus(UUID userId);
    StarGuideResponse interactWithStarGuide(UUID userId, int page);
    CharacterProfileResponse interactWithCharacterProfile(UUID userId);
    List<DialogueResponse> interactWithRequestForm(UUID userId);
}
