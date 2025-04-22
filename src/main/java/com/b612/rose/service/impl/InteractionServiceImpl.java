package com.b612.rose.service.impl;

import com.b612.rose.dto.response.*;
import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.repository.*;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.InteractionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final StarGuideEntryRepository starGuideEntryRepository;
    private final NpcRepository npcRepository;
    private final DialogueService dialogueService;
    private final NpcProfileRepository npcProfileRepository;

    @Override
    public List<ObjectStatusResponse> getObjectStatus(UUID userId) {
        List<ObjectStatusResponse> responses = new ArrayList<>();

        List<InteractiveObject> objects = interactiveObjectRepository.findAll();
        for (InteractiveObject object : objects) {
            Optional<UserInteraction> interactionOpt = userInteractionRepository
                    .findByUserIdAndObjectId(userId, object.getObjectId());

            boolean hasInteracted = interactionOpt.map(UserInteraction::isHasInteracted).orElse(false);
            boolean isActive = interactionOpt.map(UserInteraction::isActive).orElse(false);

            responses.add(ObjectStatusResponse.builder()
                    .objectType(object.getObjectType())
                    .hasInteracted(hasInteracted)
                    .isActive(isActive)
                    .build());
        }

        return responses;
    }

    @Override
    @Transactional
    public StarGuideResponse getStarGuide(UUID userId, int page) {
        updateInteraction(userId, InteractiveObjectType.STAR_GUIDE);

        List<DialogueResponse> dialogues = dialogueService.getDialoguesByType("star_guide", userId);

        List<StarGuideEntry> allEntries = starGuideEntryRepository.findAllByOrderByOrderIndexAsc();

        final int ENTRIES_PER_PAGE = 4;
        int totalPages = (int) Math.ceil((double) allEntries.size() / ENTRIES_PER_PAGE);

        if (page < 0) {
            page = 0;
        } else if (page >= totalPages) {
            page = totalPages - 1;
        }

        int startIndex = page * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, allEntries.size());

        List<StarGuideEntry> pageEntries = allEntries.subList(startIndex, endIndex);

        List<StarGuideEntryResponse> entryResponses = pageEntries.stream()
                .map(entry -> StarGuideEntryResponse.builder()
                        .entryId(entry.getEntryId())
                        .starName(entry.getStarName())
                        .starSource(entry.getStarSource())
                        .description(entry.getDescription())
                        .build())
                .collect(Collectors.toList());

        return StarGuideResponse.builder()
                .dialogues(dialogues)
                .starEntries(entryResponses)
                .totalPages(totalPages)
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional
    public CharacterProfileResponse getCharacterProfile(UUID userId) {
        updateInteraction(userId, InteractiveObjectType.CHARACTER_PROFILE);
        List<DialogueResponse> dialogues = dialogueService.getDialoguesByType("character_profile", userId);

        List<Npc> npcs = npcRepository.findAll();
        List<NpcProfileResponse> profileResponses = npcs.stream()
                .map(npc -> {
                    Optional<NpcProfile> profileOpt = npcProfileRepository.findByNpcId(npc.getNpcId());

                    String description = profileOpt.map(NpcProfile::getDescription).orElse("");

                    return NpcProfileResponse.builder()
                            .npcId(npc.getNpcId())
                            .npcName(npc.getNpcName())
                            .description(description)
                            .build();
                })
                .collect(Collectors.toList());

        return CharacterProfileResponse.builder()
                .dialogues(dialogues)
                .profiles(profileResponses)
                .build();
    }


    @Override
    @Transactional
    public List<DialogueResponse> getRequestForm(UUID userId) {
        InteractiveObject requestFormObject = interactiveObjectRepository.findByObjectType(InteractiveObjectType.REQUEST_FORM)
                .orElseThrow(() -> new IllegalArgumentException("Request form object not found"));

        Optional<UserInteraction> interactionOpt = userInteractionRepository
                .findByUserIdAndObjectId(userId, requestFormObject.getObjectId());

        boolean isActive = interactionOpt.map(UserInteraction::isActive).orElse(false);

        if (!isActive) {
            return Collections.emptyList();
        }

        updateInteraction(userId, InteractiveObjectType.REQUEST_FORM);
        return Collections.emptyList();
    }

    @Transactional
    protected void updateInteraction(UUID userId, InteractiveObjectType objectType) {
        InteractiveObject object = interactiveObjectRepository.findByObjectType(objectType)
                .orElseThrow(() -> new IllegalArgumentException("Object not found with type: " + objectType));

        UserInteraction interaction = userInteractionRepository
                .findByUserIdAndObjectId(userId, object.getObjectId())
                .orElse(null);

        if (interaction == null) {
            interaction = UserInteraction.builder()
                    .userId(userId)
                    .objectId(object.getObjectId())
                    .hasInteracted(true)
                    .isActive(true)
                    .interactedAt(LocalDateTime.now())
                    .build();
        } else {
            interaction = UserInteraction.builder()
                    .interactionId(interaction.getInteractionId())
                    .userId(interaction.getUserId())
                    .objectId(interaction.getObjectId())
                    .hasInteracted(true)
                    .isActive(interaction.isActive())
                    .interactedAt(LocalDateTime.now())
                    .build();
        }

        userInteractionRepository.save(interaction);
    }
}
