package com.b612.rose.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterProfileResponse {
    private List<DialogueResponse> dialogues;
    private List<NpcProfileResponse> profiles;
}
