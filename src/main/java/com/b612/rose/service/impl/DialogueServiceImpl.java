package com.b612.rose.service.impl;

import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.entity.domain.Dialogue;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.mapper.DialogueMapper;
import com.b612.rose.repository.DialogueRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.DialogueService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DialogueServiceImpl implements DialogueService {

    private final DialogueRepository dialogueRepository;
    private final UserRepository userRepository;
    private final DialogueMapper dialogueMapper;

    private final ConcurrentHashMap<String, List<Dialogue>> dialogueCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        log.info("대화 데이터 캐싱 시작");
        try {
            // 모든 대화 유형을 스테이지와 함께 캐싱
            for (GameStage stage : GameStage.values()) {
                try {
                    String dialogueType = getDialogueTypeForStage(stage);
                    String cacheKey = "stage-" + stage.name();
                    List<Dialogue> dialogues = dialogueRepository.findByDialogueTypeOrderByNpcId(dialogueType);
                    if (!dialogues.isEmpty()) {
                        dialogueCache.put(cacheKey, dialogues);
                        log.info("게임 스테이지 '{}' 대화 데이터 캐싱 완료: {} 항목", stage.name(), dialogues.size());
                    }
                } catch (BusinessException e) {
                    // 특정 스테이지에 대한 대화가 없는 경우는 무시하고 계속 진행
                    log.warn("게임 스테이지 '{}' 대화 데이터 캐싱 건너뜀: {}", stage.name(), e.getMessage());
                }
            }

            // 일반적으로 많이 사용되는 대화 미리 캐싱
            String[] commonDialogueTypes = {"star_guide", "character_profile"};
            for (String dialogueType : commonDialogueTypes) {
                String cacheKey = "type-" + dialogueType;
                List<Dialogue> dialogues = dialogueRepository.findByDialogueTypeOrderByNpcId(dialogueType);
                if (!dialogues.isEmpty()) {
                    dialogueCache.put(cacheKey, dialogues);
                    log.info("대화 유형 '{}' 데이터 캐싱 완료: {} 항목", dialogueType, dialogues.size());
                }
            }

            log.info("대화 데이터 캐싱 완료: 총 {} 개 유형", dialogueCache.size());
        } catch (Exception e) {
            log.error("대화 데이터 캐싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    // 대화 유형에 따라 대화 내용 검색, npc 무관
    @Override
    public DialogueResponse getDialogueByType(String dialogueType, UUID userId) {
        String cacheKey = "single-" + dialogueType;

        List<Dialogue> cachedDialogues = dialogueCache.get(cacheKey);
        Dialogue dialogue;

        if (cachedDialogues != null && !cachedDialogues.isEmpty()) {
            dialogue = cachedDialogues.get(0);
        } else {
            dialogue = dialogueRepository.findByDialogueType(dialogueType)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                            "해당 대화를 찾을 수 없음: "+dialogueType));
            dialogueCache.put(cacheKey, List.of(dialogue));
        }

        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(user -> dialogueMapper.toResponse(dialogue, user))
                .orElse(dialogueMapper.toResponse(dialogue));
    }


    // 대화 유형과 npc에 따라 대화 내용 검색
    @Override
    public DialogueResponse getDialogueByTypeAndNpcId(String dialogueType, Integer npcId, UUID userId) {
        String cacheKey = "single-" + dialogueType + "-" + npcId;

        List<Dialogue> cachedDialogues = dialogueCache.get(cacheKey);
        Dialogue dialogue;

        if (cachedDialogues != null && !cachedDialogues.isEmpty()) {
            dialogue = cachedDialogues.get(0);
        } else {
            dialogue = dialogueRepository.findByDialogueTypeAndNpcId(dialogueType, npcId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                            "해당 대화를 찾을 수 없음: "+dialogueType+", npcId = "+npcId));
            dialogueCache.put(cacheKey, List.of(dialogue));
        }

        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(user -> dialogueMapper.toResponse(dialogue, user))
                .orElse(dialogueMapper.toResponse(dialogue));
    }

    // 현재 게임 진척도에 맞는 대화 검색
    @Override
    public List<DialogueResponse> getDialoguesForCurrentStage(UUID userId, GameStage currentStage) {
        String cacheKey = "stage-" + currentStage.name();

        List<Dialogue> cachedDialogues = dialogueCache.get(cacheKey);

        if (cachedDialogues == null) {
            String dialogueType = getDialogueTypeForStage(currentStage);
            cachedDialogues = dialogueRepository.findByDialogueTypeOrderByNpcId(dialogueType);

            if (cachedDialogues.isEmpty()) {
                throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                        "해당 대화를 찾을 수 없음: "+currentStage);
            }

            dialogueCache.put(cacheKey, cachedDialogues);
        }

        Optional<User> userOptional = userRepository.findById(userId);
        return cachedDialogues.stream()
                .map(dialogue -> userOptional.map(user -> dialogueMapper.toResponse(dialogue, user))
                        .orElse(dialogueMapper.toResponse(dialogue)))
                .collect(Collectors.toList());
    }

    // 대화 유형에 따라 대화 내용 검색 (여러 개)
    @Override
    public List<DialogueResponse> getDialoguesByType(String dialogueType, UUID userId) {
        String cacheKey = "type-" + dialogueType;

        List<Dialogue> cachedDialogues = dialogueCache.get(cacheKey);

        if (cachedDialogues == null) {
            cachedDialogues = dialogueRepository.findByDialogueTypeOrderByNpcId(dialogueType);

            if (cachedDialogues.isEmpty()) {
                throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                        "해당 대화를 찾을 수 없음: "+dialogueType);
            }

            dialogueCache.put(cacheKey, cachedDialogues);
        }

        Optional<User> userOptional = userRepository.findById(userId);
        return cachedDialogues.stream()
                .map(dialogue -> userOptional.map(user -> dialogueMapper.toResponse(dialogue, user))
                        .orElse(dialogueMapper.toResponse(dialogue)))
                .collect(Collectors.toList());
    }

    // 진척도에 맞는 대화 유형 매핑
    private String getDialogueTypeForStage(GameStage currentStage) {
        return switch (currentStage) {
            case GAME_START -> "tutorial";
            case COLLECT_PRIDE -> "click_pride";
            case COLLECT_ENVY -> "click_envy";
            case DELIVER_ENVY -> "deliver_envy";
            case COLLECT_LONELY -> "click_lonely";
            case DELIVER_LONELY -> "deliver_lonely";
            case COLLECT_SAD -> "click_sad";
            case DELIVER_SAD -> "deliver_sad";
            case REQUEST_INPUT -> "quest_end";
            case NPC_SELECTION -> "pick_npc";
            case GAME_COMPLETE -> "game_clear";
            default -> throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                    "해당 대화를 찾을 수 없음: "+currentStage);
        };
    }


}