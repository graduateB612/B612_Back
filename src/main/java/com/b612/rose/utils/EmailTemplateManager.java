package com.b612.rose.utils;

import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.repository.StarRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailTemplateManager {
    private final StarRepository starRepository;

    private final Map<String, String> npcEmailMap = new HashMap<>();
    private final Map<String, StarType> npcStarTypeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        npcEmailMap.put("어린왕자", "little_p@b612_rose.com");
        npcEmailMap.put("장미", "rose@b612_rose.com");
        npcEmailMap.put("여우", "prettycutyfox@b612_rose.com");
        npcEmailMap.put("바오밥", "baobob123@b612_rose.com");

        npcStarTypeMap.put("어린왕자", StarType.PRIDE);
        npcStarTypeMap.put("장미", StarType.ENVY);
        npcStarTypeMap.put("여우", StarType.SAD);
        npcStarTypeMap.put("바오밥", StarType.LONELY);
    }

    public String getSenderEmail(String npcName) {
        return npcEmailMap.getOrDefault(npcName, "noreply@b612_rose.com");
    }

    public StarType getStarTypeForNpc(String npcName) {
        return npcStarTypeMap.get(npcName);
    }

    public String getSubject(String npcName, String purifiedTypeName) {
        return npcName + "이 보낸 선물 - " + purifiedTypeName + "의 별";
    }

    public String getEmailContent(User user, String npcName) {
        StarType starType = getStarTypeForNpc(npcName);
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new IllegalArgumentException("Star not found for type: " + starType));

        String purifiedTypeName = star.getPurifiedType().getDescription();

        String content = "<div style='font-family: Arial, sans-serif;'>" +
                "<h2>안녕하세요, " + user.getUserName() + "님!</h2>" +
                "<p>" + npcName + "가 보내는 " + purifiedTypeName + "의 별입니다.</p>" +
                "</div>";

        return content;
    }
}
