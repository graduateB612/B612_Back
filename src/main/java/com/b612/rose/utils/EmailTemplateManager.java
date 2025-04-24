package com.b612.rose.utils;

import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.StarRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailTemplateManager {
    private final StarRepository starRepository;
    private final ResourceLoader resourceLoader;

    private final Map<String, String> npcEmailMap = new HashMap<>();
    private final Map<String, StarType> npcStarTypeMap = new HashMap<>();
    private final Map<String, String> npcTemplatePathMap = new HashMap<>();
    private final Map<String, String> npcImagePathMap = new HashMap<>();
    private final Map<String, String> npcCharacterImageMap = new HashMap<>();

    @PostConstruct
    public void init() {
        npcEmailMap.put("어린왕자", "little_p@b612.rose.com");
        npcEmailMap.put("장미", "rose@b612.rose.com");
        npcEmailMap.put("여우", "prettycutyfox@b612.rose.com");
        npcEmailMap.put("바오밥", "baobob123@b612.rose.com");

        npcStarTypeMap.put("어린왕자", StarType.PRIDE);
        npcStarTypeMap.put("장미", StarType.ENVY);
        npcStarTypeMap.put("여우", StarType.SAD);
        npcStarTypeMap.put("바오밥", StarType.LONELY);

        npcTemplatePathMap.put("어린왕자", "classpath:templates/emails/little-prince-email.html");
        npcTemplatePathMap.put("장미", "classpath:templates/emails/rose-email.html");
        npcTemplatePathMap.put("여우", "classpath:templates/emails/fox-email.html");
        npcTemplatePathMap.put("바오밥", "classpath:templates/emails/baobab-email.html");

        npcImagePathMap.put("어린왕자", "static/images/stars/LittlePrinceStar.png");
        npcImagePathMap.put("장미", "static/images/stars/RoseStar.png");
        npcImagePathMap.put("여우", "static/images/stars/FoxStar.png");
        npcImagePathMap.put("바오밥", "static/images/stars/BaobobStar.png");

        npcCharacterImageMap.put("여우", "static/images/character/fox_character.png");
    }

    public String getSenderEmail(String npcName) {
        return npcEmailMap.getOrDefault(npcName, "noreply@b612.rose.com");
    }

    public StarType getStarTypeForNpc(String npcName) {
        return npcStarTypeMap.get(npcName);
    }

    public String getSubject(String npcName, String purifiedTypeName) {
        return npcName + "의 선물 - " + purifiedTypeName + "의 별";
    }

    public String getStarImagePath(String npcName) {
        return npcImagePathMap.getOrDefault(npcName, "static/images/stars/default-star.png");
    }

    public String getCharacterImagePath(String npcName) {
        return npcCharacterImageMap.getOrDefault(npcName, "static/images/character/default-character.png");
    }

    public String getEmailContent(User user, String npcName) {
        StarType starType = getStarTypeForNpc(npcName);
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND, "해당 타입의 별을 찾을 수 없습니다. " + starType));

        String purifiedTypeName = star.getPurifiedType().getDescription();
        String templatePath = npcTemplatePathMap.getOrDefault(npcName, "classpath:templates/emails/default-email.html");

        try {
            Resource resource = resourceLoader.getResource(templatePath);
            String template = Files.readString(Paths.get(resource.getURI()));

            template = template.replace("{{userName}}", user.getUserName())
                    .replace("{{purifiedType}}", purifiedTypeName);

            if (user.getConcern() != null && !user.getConcern().isEmpty()) {
                template = template.replace("{{concern}}", user.getConcern());
            }

            return template;
        } catch (IOException e) {
            return "<div style='font-family: Arial, sans-serif;'>" +
                    "<h2>안녕하세요, " + user.getUserName() + "님!</h2>" +
                    "<p>" + npcName + "의 힘으로 정화한 " + purifiedTypeName + "의 별입니다.</p>" +
                    "</div>";
        }
    }
}
