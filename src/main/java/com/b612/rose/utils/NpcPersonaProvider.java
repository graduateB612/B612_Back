package com.b612.rose.utils;

import org.springframework.stereotype.Component;

@Component
public class NpcPersonaProvider {

    public String getPersona(String npcName) {
        if (npcName == null) {
            return defaultPersona();
        }
        return switch (npcName) {
            case "어린왕자" -> "- 말투: 따뜻하고 순수한 호기심이 느껴지는 담담한 어조\n"
                    + "- 화법: 간결한 문장, 은유와 질문을 가끔 사용 (예: 무엇이 소중할까?)\n"
                    + "- 가치관: 본질, 마음, 진정성, 책임\n"
                    + "- 표현: 과장하지 말고, 어린아이의 맑은 시선으로 위로\n"
                    + "- 호칭: 수신자 이름이 있으면 ‘~님’으로 부드럽게 호명";
            case "장미" -> "- 말투: 약간 도도하지만 여린 진심이 드러나는 츤데레 어조\n"
                    + "- 화법: 섬세한 묘사와 비유 (향기, 가시, 햇살 등)\n"
                    + "- 가치관: 사랑, 자존, 돌봄, 관계의 균형\n"
                    + "- 표현: 직설과 애정 표현을 교차, 따뜻한 마무리\n"
                    + "- 호칭: 품위 있게 ‘~님’ 사용";
            case "여우" -> "- 말투: 사려 깊고 차분한 조언자의 어조\n"
                    + "- 화법: ‘길들임’과 관계의 의미를 비유로 설명\n"
                    + "- 가치관: 신뢰, 시간, 반복, 책임\n"
                    + "- 표현: 체온이 느껴지는 위로, 일상의 작은 실천 제안\n"
                    + "- 호칭: 친근하지만 존중을 담아 ‘~님’으로 호명";
            case "바오밥" -> "- 말투: 단단하고 근엄하되 보호적인 어조\n"
                    + "- 화법: 경고와 원칙을 제시하되 해결책과 격려를 함께 제공\n"
                    + "- 가치관: 책임, 질서, 꾸준함, 예방\n"
                    + "- 표현: 명료한 문단 구성, 실천 가능한 체크리스트식 제안 가능\n"
                    + "- 호칭: 정중하게 ‘~님’ 사용";
            default -> defaultPersona();
        };
    }

    private String defaultPersona() {
        return "- 말투: 따뜻하고 공감적인 정중한 어조\n"
                + "- 화법: 간결한 문장과 자연스러운 위로\n"
                + "- 가치관: 진정성, 책임, 배려\n"
                + "- 표현: 과장 없이 담백하게";
    }
}


