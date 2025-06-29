package com.b612.rose.mapper;

import com.b612.rose.dto.response.*;
import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.InteractiveObjectType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InteractionMapper {

    // InteractiveObject + UserInteraction → ObjectStatusResponse 변환
    public ObjectStatusResponse toObjectStatusResponse(InteractiveObject object, UserInteraction interaction) {
        boolean hasInteracted = interaction != null ? interaction.isHasInteracted() : false;
        boolean isActive = interaction != null ? interaction.isActive() : false;

        return ObjectStatusResponse.builder()
                .objectType(object.getObjectType())
                .hasInteracted(hasInteracted)
                .isActive(isActive)
                .build();
    }

    // StarGuideEntry → StarGuideEntryResponse 변환
    public StarGuideEntryResponse toStarGuideEntryResponse(StarGuideEntry entry) {
        return StarGuideEntryResponse.builder()
                .entryId(entry.getEntryId())
                .starName(entry.getStarName())
                .starSource(entry.getStarSource())
                .description(entry.getDescription())
                .build();
    }

    // StarGuideResponse 생성
    public StarGuideResponse toStarGuideResponse(List<DialogueResponse> dialogues, 
                                                List<StarGuideEntry> entries, 
                                                int totalPages, 
                                                int currentPage) {
        List<StarGuideEntryResponse> entryResponses = entries.stream()
                .map(this::toStarGuideEntryResponse)
                .collect(Collectors.toList());

        return StarGuideResponse.builder()
                .dialogues(dialogues)
                .starEntries(entryResponses)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }

    // Npc + NpcProfile → NpcProfileResponse 변환
    public NpcProfileResponse toNpcProfileResponse(Npc npc, NpcProfile profile) {
        String description = profile != null ? profile.getDescription() : "";

        return NpcProfileResponse.builder()
                .npcId(npc.getNpcId())
                .npcName(npc.getNpcName())
                .description(description)
                .build();
    }

    // CharacterProfileResponse 생성
    public CharacterProfileResponse toCharacterProfileResponse(List<DialogueResponse> dialogues, 
                                                              List<NpcProfileResponse> profiles) {
        return CharacterProfileResponse.builder()
                .dialogues(dialogues)
                .profiles(profiles)
                .build();
    }
} 