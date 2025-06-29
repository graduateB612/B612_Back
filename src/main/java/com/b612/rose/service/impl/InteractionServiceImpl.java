package com.b612.rose.service.impl;

import com.b612.rose.dto.response.*;
import com.b612.rose.entity.domain.*;
import com.b612.rose.entity.enums.InteractiveObjectType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.*;
import com.b612.rose.mapper.InteractionMapper;
import com.b612.rose.service.service.DialogueService;
import com.b612.rose.service.service.AsyncTaskService;
import com.b612.rose.service.service.InteractionService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionServiceImpl implements InteractionService {
    private final InteractiveObjectRepository interactiveObjectRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final StarGuideEntryRepository starGuideEntryRepository;
    private final NpcRepository npcRepository;
    private final DialogueService dialogueService;
    private final NpcProfileRepository npcProfileRepository;
    private final AsyncTaskService asyncTaskService;
    private final InteractionMapper interactionMapper;

    private final ConcurrentHashMap<String, List<StarGuideEntry>> starGuideCache = new ConcurrentHashMap<>();

    // 서버 킬 때 데이터 미리 캐싱
    @PostConstruct
    public void initCache() {
        log.info("별 도감 데이터 캐싱 시작");
        try {
            String cacheKey = "star-guide-entries";
            List<StarGuideEntry> allEntries = starGuideEntryRepository.findAllByOrderByOrderIndexAsc();
            starGuideCache.put(cacheKey, allEntries);
            log.info("별 도감 데이터 캐싱 완료: {} 항목 로드됨", allEntries.size());
        } catch (Exception e) {
            log.error("별 도감 데이터 캐싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }

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

            responses.add(interactionMapper.toObjectStatusResponse(object, interactionOpt.orElse(null)));
        }

        return responses;
    }

    // 별 도감 데이터 조회
    @Override
    @Transactional
    public StarGuideResponse getStarGuide(UUID userId, int page, boolean includeDialogues) {
        if (page == 0 && includeDialogues) {
            asyncTaskService.updateInteractionAsync(userId, InteractiveObjectType.STAR_GUIDE);
        }

        List<DialogueResponse> dialogues = includeDialogues ?
                dialogueService.getDialoguesByType("star_guide", userId) :
                Collections.emptyList();

        String cacheKey = "star-guide-entries";
        List<StarGuideEntry> allEntries = starGuideCache.get(cacheKey);

        if (allEntries == null) {
            allEntries = starGuideEntryRepository.findAllByOrderByOrderIndexAsc();
            starGuideCache.put(cacheKey, allEntries);
        }

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

        return interactionMapper.toStarGuideResponse(dialogues, pageEntries, totalPages, page);
    }

    // 캐릭터 프로필 조회
    @Override
    @Transactional
    public CharacterProfileResponse getCharacterProfile(UUID userId) {
        asyncTaskService.updateInteractionAsync(userId, InteractiveObjectType.CHARACTER_PROFILE);
        List<DialogueResponse> dialogues = dialogueService.getDialoguesByType("character_profile", userId);

        List<Npc> npcs = npcRepository.findAll();
        List<NpcProfileResponse> profileResponses = npcs.stream()
                .map(npc -> {
                    Optional<NpcProfile> profileOpt = npcProfileRepository.findByNpcId(npc.getNpcId());
                    return interactionMapper.toNpcProfileResponse(npc, profileOpt.orElse(null));
                })
                .collect(Collectors.toList());

        return interactionMapper.toCharacterProfileResponse(dialogues, profileResponses);
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

        asyncTaskService.updateInteractionAsync(userId, InteractiveObjectType.REQUEST_FORM);
        return Collections.emptyList();
    }
}
