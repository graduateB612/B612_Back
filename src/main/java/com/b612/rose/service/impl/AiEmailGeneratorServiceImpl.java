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
                            "사용자가 입력한 고민에 대한 답변을 해주세요." +
                            "인삿말, 끝맺음 등은 이미 작성되어있습니다. 쓰지 마세요. 사용자 고민에 대한 답변만 하세요." +
                            "사용자의 고민을 있는 그대로 받아들여 답변을 작성하세요." +
                            "페르소나: B612 세계관의 NPC '" + npcName + "'을(를) 반영해 말투와 분위기를 유지해주세요." +
                            "캐릭터 페르소나 가이드:\n" + persona + "\n" +
                            "캐릭터 페르소나 가이드에 적혀있는 예시 대사를 참고하되, 사용하지는 마세요." +
                            "실제 대화를 한다 생각하세요."+
                            "실제 캐릭터가 편지를 쓴다고 생각하고 작성해주세요." +
                            "캐릭터들은 한국어를 사용합니다."+
                            "비유적·시적 표현은 꼭 필요할 때만 사용하세요. 일상적인 대화처럼 자연스럽게, 담백하게 작성하세요." +
                            "캐릭터 가이드에 있는 말투와 화법을 반드시 따르세요. 다른 스타일의 문체는 쓰지 마세요."+
                            "npc가 직접 상황을 듣고 대화하듯이 반응하세요."+
                            "너무 현실적인 조언보다는 조금 동화적인 조언을 해주세요."
            );
            String user = (
                    "수신자 이름: " + safe(userName) + "\n" +
                    "NPC: " + safe(npcName) + "\n" +
                    "사용자 고민: " + safe(concern) + "\n\n" +
                    "사용자 고민에 대한 답변을 작성하되, 전체 분량은 300자 이내로 작성, 단락은 3개를 넘지 않도록 해주세요.\n" +
                    "외부 이미지/링크는 넣지 마세요. 서명 라인은 템플릿에 있으므로 본문에 추가하지 마세요."+
                    "사용자 이름을 임의로 수정해서 부르지 마세요.");

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
            String content = idxChoices >= 0 ? extractStringValueForKey(responseJson, idxChoices, "content") : null;
            if (content != null && !content.isBlank()) return content;

            // 2) responses API 스타일: output[0].content[0].text
            int idxOutput = responseJson.indexOf("\"output\"");
            if (idxOutput >= 0) {
                int idxContent = responseJson.indexOf("\"content\"", idxOutput);
                int searchFrom = idxContent >= 0 ? idxContent : idxOutput;
                content = extractStringValueForKey(responseJson, searchFrom, "text");
                if (content != null && !content.isBlank()) return content;
            }

            // 2-1) responses API의 요약 message 블록: message.content: "..."
            int idxMessage = responseJson.indexOf("\"message\"");
            if (idxMessage >= 0) {
                content = extractStringValueForKey(responseJson, idxMessage, "content");
                if (content != null && !content.isBlank()) return content;
            }

            // 3) 기타 일부 응답: content 배열 내 text 필드 (범용)
            content = extractStringValueForKey(responseJson, 0, "text");
            if (content != null && !content.isBlank()) return content;

            // 4) 스트리밍 누적 형태 등 예외 포맷 방어적 처리
            int anyContentIdx = responseJson.indexOf("\"content\"");
            if (anyContentIdx >= 0) {
                content = extractStringValueForKey(responseJson, anyContentIdx, "content");
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

    // 키(예: content) 뒤 공백/개행 허용하여 문자열 값 추출
    private String extractStringValueForKey(String json, int fromIndex, String key) {
        String keyToken = "\"" + key + "\"";
        int keyIndex = json.indexOf(keyToken, fromIndex);
        if (keyIndex < 0) return null;
        int i = keyIndex + keyToken.length();
        // 공백/개행 스킵
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length() || json.charAt(i) != ':') return null;
        i++;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length() || json.charAt(i) != '"') return null;
        // 시작 따옴표 다음부터 수집
        i++;
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


