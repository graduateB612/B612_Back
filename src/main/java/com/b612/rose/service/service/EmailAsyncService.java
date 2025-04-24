package com.b612.rose.service.service;

import com.b612.rose.dto.request.EmailRequest;

import java.util.UUID;

public interface EmailAsyncService {
    void sendEmailAsync(UUID userId, EmailRequest request);
}
