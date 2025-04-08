package com.b612.rose.service.service;

import com.b612.rose.dto.request.UserCreateRequest;
import com.b612.rose.dto.response.UserResponse;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest requestDto);
    Optional<UserResponse> getUserById(UUID id);
    Optional<UserResponse> getUserByEmail(String email);
    boolean isUserExists(String email);
}
