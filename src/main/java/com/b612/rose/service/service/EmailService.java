package com.b612.rose.service.service;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.dto.response.EmailResponse;

import java.util.UUID;

public interface EmailService {
    boolean sendEmail(UUID userId, EmailRequest request);
}