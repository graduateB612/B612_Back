package com.b612.rose.service.impl;

import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.entity.domain.Dialogue;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.GameStage;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.DialogueRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.DialogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DialogueServiceImpl implements DialogueService {

    private final DialogueRepository dialogueRepository;
    private final UserRepository userRepository;

    // 대화 유형에 따라 대화 내용 검색, npc 무관
    @Override
    public DialogueResponse getDialogueByType(String dialogueType, UUID userId) {
        Dialogue dialogue = dialogueRepository.findByDialogueType(dialogueType)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                        "해당 대화를 찾을 수 없음: "+dialogueType));

        return formatDialogueResponse(dialogue, userId);
    }

    // 대화 유형과 npc에 따라 대화 내용 검색
    @Override
    public DialogueResponse getDialogueByTypeAndNpcId(String dialogueType, Integer npcId, UUID userId) {
        Dialogue dialogue = dialogueRepository.findByDialogueTypeAndNpcId(dialogueType, npcId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                        "해당 대화를 찾을 수 없음: "+dialogueType+", npcId = "+npcId));

        return formatDialogueResponse(dialogue, userId);
    }

    // 현재 게임 진척도에 맞는 대화 검색
    @Override
    public List<DialogueResponse> getDialoguesForCurrentStage(UUID userId, GameStage currentStage) {
        String dialogueType = getDialogueTypeForStage(currentStage);

        List<Dialogue> dialogues = dialogueRepository.findByDialogueTypeOrderByNpcId(dialogueType);
        if (dialogues.isEmpty()) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                    "해당 대화를 찾을 수 없음: "+currentStage);
        }

        return dialogues.stream()
                .map(dialogue -> formatDialogueResponse(dialogue, userId))
                .collect(Collectors.toList());
    }

    // 대화 유형에 따라 대화 내용 검색 (여러 개)
    @Override
    public List<DialogueResponse> getDialoguesByType(String dialogueType, UUID userId) {
        List<Dialogue> dialogues = dialogueRepository.findByDialogueTypeOrderByNpcId(dialogueType);
        if (dialogues.isEmpty()) {
            throw new BusinessException(ErrorCode.DIALOGUE_NOT_FOUND,
                    "해당 대화를 찾을 수 없음: "+dialogueType);
        }

        return dialogues.stream()
                .map(dialogue -> formatDialogueResponse(dialogue, userId))
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

    // 대화 내용에 userName이 들어가야 하는 경우 포맷팅
    private DialogueResponse formatDialogueResponse(Dialogue dialogue, UUID userId) {
        String formattedText = dialogue.getDialogueText();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            formattedText = formattedText.replace("{userName}", user.getUserName());
        }

        return DialogueResponse.builder()
                .dialogueId(dialogue.getDialogueId())
                .npcId(dialogue.getNpcId())
                .npcName(dialogue.getNpc() != null ? dialogue.getNpc().getNpcName() : null)
                .dialogueText(formattedText)
                .build();
    }
}