package com.b612.rose.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String code;
    private String message;
    private List<FieldError> errors;
    private String path;

    private ErrorResponse(ErrorCode errorCode, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.code = errorCode.getCode();
        this.message = message;
        this.errors = new ArrayList<>();
        this.path = path;
    }

    public static ErrorResponse of(ErrorCode errorCode, String path){
        return new ErrorResponse(errorCode, errorCode.getMessage(), path);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path){
        return new ErrorResponse(errorCode, message, path);
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult, String path) {
        ErrorResponse response = new ErrorResponse(errorCode, errorCode.getMessage(), path);

        for (org.springframework.validation.FieldError fieldError : bindingResult.getFieldErrors()) {
            response.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return response;
    }

    public void addFieldError(String field, String message) {
        this.errors.add(new FieldError(field, message));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private String message;

        private FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
