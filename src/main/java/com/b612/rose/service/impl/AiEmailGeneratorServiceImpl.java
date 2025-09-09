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
                    "제약: 태그는 사용하지 말고, 내용만 작성하세요." +
                    "<html>, <head>, <body>, <title> 등의 전체 문서 태그나 레이아웃 또한한 포함하지 마세요. " +
                    "페르소나: B612 세계관의 NPC '" + npcName + "'을(를) 반영해 말투와 분위기를 유지하되, 동화속 등장 인물임을 잊지 마세요. " +
                    "캐릭터 페르소나 가이드:\n" + persona + "\n" +
                    "정화된 별의 주제: '" + purifiedTypeName + "'을(를) 과하지 않게 자연스럽게 녹여주세요. ");

            String user = (
                    "수신자 이름: " + safe(userName) + "\n" +
                    "NPC: " + safe(npcName) + "\n" +
                    "정화된 별 유형: " + safe(purifiedTypeName) + "\n" +
                    "사용자 고민: " + safe(concern) + "\n\n" +
                    "전체 분량은 200자 이내로 작성, 단락은 3개를 넘지 않도록 해주세요.\n" +
                    "외부 이미지/링크는 넣지 마세요. 서명 라인은 템플릿에 있으므로 본문에 추가하지 마세요. 인삿말, 끝맺음 등은 이미 작성되어있으니, 사용자 고민에 대한 답변만 하세요.");

            String body = "{"
                    + "\"model\":\"" + json(model) + "\"," 
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

    // 외부 의존성 없이 첫 번째 메시지 content 추출 (여러 응답 포맷 대응)
    private String extractFirstMessageContent(String responseJson) {
        try {
            int idxChoices = responseJson.indexOf("\"choices\"");
            // 1) chat/completions 표준: choices[0].message.content: "..."
            String content = idxChoices >= 0 ? extractAfterAnchor(responseJson, idxChoices, "\"content\":\"") : null;
            if (content != null && !content.isBlank()) return content;

            // 2) responses API 스타일: output[0].content[0].text
            int idxOutput = responseJson.indexOf("\"output\"");
            if (idxOutput >= 0) {
                int idxContent = responseJson.indexOf("\"content\"", idxOutput);
                int searchFrom = idxContent >= 0 ? idxContent : idxOutput;
                content = extractAfterAnchor(responseJson, searchFrom, "\"text\":\"");
                if (content != null && !content.isBlank()) return content;
            }

            // 2-1) responses API의 요약 message 블록: message.content: "..."
            int idxMessage = responseJson.indexOf("\"message\"");
            if (idxMessage >= 0) {
                content = extractAfterAnchor(responseJson, idxMessage, "\"content\":\"");
                if (content != null && !content.isBlank()) return content;
            }

            // 3) 기타 일부 응답: content 배열 내 text 필드 (범용)
            content = extractAfterAnchor(responseJson, 0, "\"text\":\"");
            if (content != null && !content.isBlank()) return content;

            // 4) 스트리밍 누적 형태 등 예외 포맷 방어적 처리
            int anyContentIdx = responseJson.indexOf("\"content\":\"");
            if (anyContentIdx >= 0) {
                content = extractAfterAnchor(responseJson, anyContentIdx, "\"content\":\"");
                if (content != null && !content.isBlank()) return content;
            }

            // 파싱 실패 시 본문 스니펫 로그
            int anchorStart = Math.max(0, Math.max(idxChoices, idxOutput));
            int end = Math.min(responseJson.length(), anchorStart + 500);
            String snippet = responseJson.substring(anchorStart, end);
            log.warn("AI email generation parse failed. bodySnippet={}...", snippet);
            return null;
        } catch (Exception e) {
            log.warn("extractFirstMessageContent failed: {}", e.getMessage());
            return null;
        }
    }

    // 앵커(예: "content":" ) 이후부터 닫는 쌍따옴표 전까지 추출
    private String extractAfterAnchor(String json, int fromIndex, String anchor) {
        int anchorIndex = json.indexOf(anchor, fromIndex);
        if (anchorIndex < 0) return null;
        int i = anchorIndex + anchor.length();
        StringBuilder out = new StringBuilder();
        boolean escaped = false;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
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
    }
}


