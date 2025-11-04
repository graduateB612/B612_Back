package com.b612.rose.utils;

import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.service.service.AiEmailGeneratorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
@RequiredArgsConstructor
public class EmailTemplateManager {
    private final StarRepository starRepository;
    private final ResourceLoader resourceLoader;
    private final AiEmailGeneratorService aiEmailGeneratorService;

    private final Map<String, String> npcEmailMap = new HashMap<>();
    private final Map<String, StarType> npcStarTypeMap = new HashMap<>();
    private final Map<String, String> npcTemplatePathMap = new HashMap<>();
    private final Map<String, List<String>> npcTemplateCandidatesMap = new HashMap<>();
    private final Map<String, String> templateTypeNameMap = new HashMap<>();
    private final Map<String, String> npcImagePathMap = new HashMap<>();
    private final Map<String, String> templateStarImageMap = new HashMap<>();
    private final Map<String, String> npcCharacterImageMap = new HashMap<>();

    // npc별 이메일 데이터 초기화
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

        // 랜덤 선택용 템플릿 후보 (원본 + 대안)
        npcTemplateCandidatesMap.put("어린왕자", Arrays.asList(
                "classpath:templates/emails/little-prince-email.html",
                "classpath:templates/emails/little-prince-email-explore.html"
        ));
        npcTemplateCandidatesMap.put("장미", Arrays.asList(
                "classpath:templates/emails/rose-email.html",
                "classpath:templates/emails/rose-email-insight.html"
        ));
        npcTemplateCandidatesMap.put("여우", Arrays.asList(
                "classpath:templates/emails/fox-email.html",
                "classpath:templates/emails/fox-email-longing.html"
        ));
        npcTemplateCandidatesMap.put("바오밥", Arrays.asList(
                "classpath:templates/emails/baobab-email.html",
                "classpath:templates/emails/baobab-email-sacrifice.html"
        ));

        // 템플릿별 타입명(한글) 매핑
        templateTypeNameMap.put("classpath:templates/emails/little-prince-email.html", "순수");
        templateTypeNameMap.put("classpath:templates/emails/little-prince-email-explore.html", "탐구");
        templateTypeNameMap.put("classpath:templates/emails/rose-email.html", "사랑");
        templateTypeNameMap.put("classpath:templates/emails/rose-email-insight.html", "통찰");
        templateTypeNameMap.put("classpath:templates/emails/fox-email.html", "깨달음");
        templateTypeNameMap.put("classpath:templates/emails/fox-email-longing.html", "그리움");
        templateTypeNameMap.put("classpath:templates/emails/baobab-email.html", "인내");
        templateTypeNameMap.put("classpath:templates/emails/baobab-email-sacrifice.html", "희생");

        npcImagePathMap.put("어린왕자", "static/images/stars/LittlePrinceStar.png");
        npcImagePathMap.put("장미", "static/images/stars/RoseStar.png");
        npcImagePathMap.put("여우", "static/images/stars/FoxStar.png");
        npcImagePathMap.put("바오밥", "static/images/stars/BaobobStar.png");

        npcCharacterImageMap.put("여우", "static/images/character/fox_character.png");
        npcCharacterImageMap.put("바오밥", "static/images/character/baobab_character.png");
        npcCharacterImageMap.put("어린왕자", "static/images/character/prince_character.png");
        npcCharacterImageMap.put("장미", "static/images/character/rose_character.png");

        // 템플릿별 별 이미지 매핑
        templateStarImageMap.put("classpath:templates/emails/little-prince-email.html", "static/images/stars/LittlePrinceStar.png");
        templateStarImageMap.put("classpath:templates/emails/little-prince-email-explore.html", "static/images/stars/LittlePrinceStar_explore.png");
        templateStarImageMap.put("classpath:templates/emails/rose-email.html", "static/images/stars/RoseStar.png");
        templateStarImageMap.put("classpath:templates/emails/rose-email-insight.html", "static/images/stars/RoseStar_insight.png");
        templateStarImageMap.put("classpath:templates/emails/fox-email.html", "static/images/stars/FoxStar.png");
        templateStarImageMap.put("classpath:templates/emails/fox-email-longing.html", "static/images/stars/FoxStar_longing.png");
        templateStarImageMap.put("classpath:templates/emails/baobab-email.html", "static/images/stars/BaobobStar.png");
        templateStarImageMap.put("classpath:templates/emails/baobab-email-sacrifice.html", "static/images/stars/BaobobStar_sacrifice.png");
    }

    // 캐릭터 이름으로 보내는 사람 이메일 가져오기
    public String getSenderEmail(String npcName) {
        return npcEmailMap.getOrDefault(npcName, "noreply@b612.rose.com");
    }

    // 캐릭터 이름으로 별 정보 불러오기
    public StarType getStarTypeForNpc(String npcName) {
        return npcStarTypeMap.get(npcName);
    }

    // 제목 작성
    public String getSubject(String npcName, String purifiedTypeName) {
        return npcName + "의 선물 - " + purifiedTypeName + "의 별";
    }

    // 캐릭터별 별의 이미지 불러오기
    public String getStarImagePath(String npcName) {
        return npcImagePathMap.getOrDefault(npcName, "static/images/stars/default-star.png");
    }

    // 캐릭터 이미지 불러오기
    public String getCharacterImagePath(String npcName) {
        return npcCharacterImageMap.getOrDefault(npcName, "static/images/character/default-character.png");
    }

    // 기존 방식 유지 (단일 템플릿 경로 사용)
    public String getEmailContent(User user, String npcName) {
        StarType starType = getStarTypeForNpc(npcName);
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAR_NOT_FOUND, "해당 타입의 별을 찾을 수 없습니다. " + starType));

        String purifiedTypeName = star.getPurifiedType().getDescription();
        String templatePath = npcTemplatePathMap.getOrDefault(npcName, "classpath:templates/emails/default-email.html");
        return readAndFillTemplate(user, npcName, templatePath, purifiedTypeName, user.getConcern());
    }

    // 랜덤 템플릿 선택 방식
    public EmailContentResult getRandomEmailContent(User user, String npcName) {
        return getRandomEmailContent(user, npcName, user.getConcern());
    }

    // 랜덤 템플릿 선택 방식 (요청의 고민 전달)
    public EmailContentResult getRandomEmailContent(User user, String npcName, String concern) {
        List<String> candidates = npcTemplateCandidatesMap.get(npcName);
        String templatePath;
        if (candidates == null || candidates.isEmpty()) {
            templatePath = npcTemplatePathMap.getOrDefault(npcName, "classpath:templates/emails/default-email.html");
        } else {
            int index = new Random().nextInt(candidates.size());
            templatePath = candidates.get(index);
        }

        String purifiedTypeName = templateTypeNameMap.getOrDefault(templatePath, "별");
        String content = readAndFillTemplate(user, npcName, templatePath, purifiedTypeName, concern);
        String imagePath = templateStarImageMap.getOrDefault(templatePath, getStarImagePath(npcName));
        return EmailContentResult.builder()
                .content(content)
                .purifiedTypeName(purifiedTypeName)
                .starImagePath(imagePath)
                .build();
    }

    private String readAndFillTemplate(User user, String npcName, String templatePath, String purifiedTypeName, String concern) {
        try {
            Resource resource = resourceLoader.getResource(templatePath);
            String template = Files.readString(Paths.get(resource.getURI()));

            template = template.replace("{{userName}}", user.getUserName())
                    .replace("{{purifiedType}}", purifiedTypeName);

            if (concern != null && !concern.isEmpty()) {
                template = template.replace("{{concern}}", concern);
            }

            String generated = aiEmailGeneratorService.isEnabled()
                    ? aiEmailGeneratorService.generateNpcEmailHtml(npcName, user.getUserName(), purifiedTypeName, concern)
                    : null;

            if (generated != null && !generated.isBlank()) {
                template = template.replace("{{generatedContent}}", generated);
            } else {
                template = template.replace("{{generatedContent}}", "");
            }

            return template;
        } catch (IOException e) {
            return "<div style='font-family: Arial, sans-serif;'>" +
                    "<h2>안녕하세요, " + user.getUserName() + "님!</h2>" +
                    "<p>" + npcName + "의 선물 - " + purifiedTypeName + "의 별</p>" +
                    "</div>";
        }
    }
}
