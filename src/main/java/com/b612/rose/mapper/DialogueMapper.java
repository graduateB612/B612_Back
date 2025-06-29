package com.b612.rose.mapper;

import com.b612.rose.dto.response.DialogueResponse;
import com.b612.rose.entity.domain.Dialogue;
import com.b612.rose.entity.domain.User;
import org.springframework.stereotype.Component;

@Component
public class DialogueMapper {

    // Dialogue Entity → DialogueResponse 변환
    public DialogueResponse toResponse(Dialogue dialogue) {
        return DialogueResponse.builder()
                .dialogueId(dialogue.getDialogueId())
                .npcId(dialogue.getNpcId())
                .npcName(dialogue.getNpc() != null ? dialogue.getNpc().getNpcName() : null)
                .dialogueText(dialogue.getDialogueText())
                .build();
    }

    // Dialogue Entity → DialogueResponse 변환 (사용자 이름 포맷팅)
    public DialogueResponse toResponse(Dialogue dialogue, User user) {
        String formattedText = dialogue.getDialogueText();
        if (user != null && user.getUserName() != null) {
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