package com.b612.rose.service.impl;

import com.b612.rose.service.service.AiEmailGeneratorService;
import com.b612.rose.utils.NpcPersonaProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiEmailGeneratorServiceImpl implements AiEmailGeneratorService {

    private final NpcPersonaProvider npcPersonaProvider;

    @Value("${ai.email.enabled:false}")
    private boolean enabled;

    @Value("${ai.apiKey:}")
    private String apiKey;

    @Value("${ai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.baseUrl:https://api.openai.com}")
    private String baseUrl;

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String generateNpcEmailHtml(String npcName, String userName, String purifiedTypeName, String concern) {
        if (!isEnabled()) {
            return null;
        }

        try {
            String endpoint = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";

            String persona = npcPersonaProvider.getPersona(npcName);

            String system = (
                    "당신은 한국어 이메일 본문 HTML 스니펫을 작성하는 도우미입니다. " +
                    "제약: 단락 수준 태그(<p>, <div>, <ul>, <li>, <em>, <strong>, <blockquote> 등)만 사용하세요. " +
                    "<html>, <head>, <body>, <title> 등의 전체 문서 태그나 레이아웃은 포함하지 마세요. " +
                    "따뜻하고 공감 가는 톤으로, 간결하지만 의미 있게 작성하세요. " +
                    "페르소나: B612 세계관의 NPC '" + npcName + "'을(를) 반영해 말투와 분위기를 유지하세요. " +
                    "캐릭터 페르소나 가이드:\n" + persona + "\n" +
                    "정화된 별의 주제: '" + purifiedTypeName + "'을(를) 과하지 않게 자연스럽게 녹여주세요. ");

            String user = (
                    "수신자 이름: " + safe(userName) + "\n" +
                    "NPC: " + safe(npcName) + "\n" +
                    "정화된 별 유형: " + safe(purifiedTypeName) + "\n" +
                    "사용자 고민: " + safe(concern) + "\n\n" +
                    "단락 수: 3~6개, 전체 분량은 약 1800자 이내로 유지하세요.\n" +
                    "외부 이미지/링크는 넣지 마세요. 서명 라인은 템플릿에 있으므로 본문에 추가하지 마세요.");

            String body = "{"
                    + "\"model\":\"" + json(model) + "\"," 
                    + "\"temperature\":0.7," 
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":\"" + json(system) + "\"},"
                    + "{\"role\":\"user\",\"content\":\"" + json(user) + "\"}"
                    + "]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() / 100 != 2) {
                log.warn("AI email generation failed: status={}, body={}", response.statusCode(), response.body());
                return null;
            }

            String content = extractFirstMessageContent(response.body());
            if (content == null || content.isBlank()) {
                return null;
            }
            return content.trim();
        } catch (Exception e) {
            log.error("AI email generation exception: {}", e.getMessage(), e);
            return null;
        }
    }

    // 간단한 JSON 문자열 이스케이프 처리
    private String json(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // 외부 의존성 없이 간단히 첫 번째 메시지 content 추출
    private String extractFirstMessageContent(String responseJson) {
        try {
            // 패턴: "choices":[{"message":{"content":"..."}}] 에서 content 내용만 추출
            int idxChoices = responseJson.indexOf("\"choices\"");
            if (idxChoices < 0) return null;
            int idxContent = responseJson.indexOf("\"content\"\\s*:\\s*\"", idxChoices);
            if (idxContent < 0) idxContent = responseJson.indexOf("\"content\":\"", idxChoices);
            if (idxContent < 0) return null;
            int start = responseJson.indexOf('"', idxContent + "\"content\":\"".length());
            if (start < 0) return null;

            StringBuilder out = new StringBuilder();
            boolean escaped = false;
            for (int i = start + 1; i < responseJson.length(); i++) {
                char c = responseJson.charAt(i);
                if (escaped) {
                    if (c == 'n') out.append('\n');
                    else if (c == 't') out.append('\t');
                    else if (c == '"') out.append('"');
                    else if (c == '\\') out.append('\\');
                    else out.append(c);
                    escaped = false;
                    continue;
                }
                if (c == '\\') { escaped = true; continue; }
                if (c == '"') { break; }
                out.append(c);
            }
            return out.toString();
        } catch (Exception e) {
            log.warn("extractFirstMessageContent failed: {}", e.getMessage());
            return null;
        }
    }
}


