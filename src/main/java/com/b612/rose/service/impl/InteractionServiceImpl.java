package com.b612.rose.service.impl;

import com.b612.rose.dto.response.*;
import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
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

    // 오브젝트 상태 반환
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

    // 별 도감 데이터 조회
    @Override
    @Transactional
    public StarGuideResponse getStarGuide(UUID userId, int page, boolean includeDialogues) {
        updateInteraction(userId, InteractiveObjectType.STAR_GUIDE);

        List<DialogueResponse> dialogues = includeDialogues ?
                dialogueService.getDialoguesByType("star_guide", userId) :
                Collections.emptyList();

        List<StarGuideEntry> allEntries = starGuideEntryRepository.findAllByOrderByOrderIndexAsc();

        // 페이징
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

    // 캐릭터 프로필 조회
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

    // 의뢰서 상호작용
    @Override
    @Transactional
    public List<DialogueResponse> getRequestForm(UUID userId) {
        InteractiveObject requestFormObject = interactiveObjectRepository.findByObjectType(InteractiveObjectType.REQUEST_FORM)
                .orElseThrow(() -> new BusinessException(ErrorCode.OBJECT_NOT_FOUND,
                        "오브젝트를 찾을 수 없습니다: 의뢰서"));

        Optional<UserInteraction> interactionOpt = userInteractionRepository
                .findByUserIdAndObjectId(userId, requestFormObject.getObjectId());

        boolean isActive = interactionOpt.map(UserInteraction::isActive).orElse(false);

        if (!isActive) {
            throw new BusinessException(ErrorCode.OBJECT_NOT_ACTIVE,
                    "의뢰서는 아직 사용할 수 없습니다. userId: " + userId);
        }

        updateInteraction(userId, InteractiveObjectType.REQUEST_FORM);
        return Collections.emptyList();
    }

    // 상호작용 상태 업데이트
    @Transactional
    protected void updateInteraction(UUID userId, InteractiveObjectType objectType) {
        InteractiveObject object = interactiveObjectRepository.findByObjectType(objectType)
                .orElseThrow(() -> new BusinessException(ErrorCode.OBJECT_NOT_FOUND,
                        "오브젝트를 찾을 수 없습니다: " + objectType));

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
