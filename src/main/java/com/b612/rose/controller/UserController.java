package com.b612.rose.controller;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.ApiResponse;
import com.b612.rose.dto.response.UserResponse;
import com.b612.rose.exception.BusinessException;
import com.b612.rose.exception.ErrorCode;
import com.b612.rose.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserCreateRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "사용자가 성공적으로 생성되었습니다."));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserInfo(@PathVariable UUID userId) {
        UserResponse userResponse = userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "User not found with ID: " + userId));
        return ResponseEntity.ok(ApiResponse.success(userResponse, "사용자 정보를 성공적으로 조회했습니다."));
    }
}
