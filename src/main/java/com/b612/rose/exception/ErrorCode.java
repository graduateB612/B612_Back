package com.b612.rose.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "해당 값을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 에러"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    GAME_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "해당하는 게임 진척도를 찾을 수 없습니다."),
    STAR_NOT_FOUND(HttpStatus.NOT_FOUND, "G002", "해당 별을 찾을 수 없습니다."),
    STAR_ALREADY_COLLECTED(HttpStatus.BAD_REQUEST, "G003", "이미 찾은 별입니다."),
    STAR_NOT_COLLECTED(HttpStatus.BAD_REQUEST, "G004", "별 수집에 실패했습니다. 다시 시도해주세요."),
    STARS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "G005", "아직 모든 별을 수집, 전달하지 않았습니다."),

    DIALOGUE_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "해당 대화를 찾을 수 없습니다."),

    OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "해당 오브젝트를 찾을 수 없습니다."),
    OBJECT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "O002", "비활성화된 오브젝트입니다."),

    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "E001", "이메일 주소를 입력하지 않았습니다."),
    NPC_SELECTION_REQUIRED(HttpStatus.BAD_REQUEST, "E002", "NPC를 선택하지 않았습니다."),
    EMAIL_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E003", "이메일 전송에 실패했습니다. 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
