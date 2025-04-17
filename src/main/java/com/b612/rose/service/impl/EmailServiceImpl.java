package com.b612.rose.service.impl;

import com.b612.rose.dto.request.EmailRequest;
import com.b612.rose.entity.domain.EmailLog;
import com.b612.rose.entity.domain.Star;
import com.b612.rose.entity.domain.User;
import com.b612.rose.entity.enums.StarType;
import com.b612.rose.repository.EmailLogRepository;
import com.b612.rose.repository.StarRepository;
import com.b612.rose.repository.UserRepository;
import com.b612.rose.service.service.EmailService;
import com.b612.rose.utils.EmailTemplateManager;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailLogRepository emailLogRepository;
    private final StarRepository starRepository;
    private final EmailTemplateManager emailTemplateManager;

    @Override
    @Transactional
    public boolean sendEmail(UUID userId, EmailRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String npcName = request.getSelectedNpc();
        String senderEmail = emailTemplateManager.getSenderEmail(npcName);

        StarType starType = emailTemplateManager.getStarTypeForNpc(npcName);
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new IllegalArgumentException("Star not found for type: " + starType));

        String purifiedTypeName = star.getPurifiedType().getDescription();
        String subject = emailTemplateManager.getSubject(npcName, purifiedTypeName);
        String content = emailTemplateManager.getEmailContent(user, npcName);

        try {
            log.info("이메일 전송 시도: {} -> {}, 제목: {}", senderEmail, request.getEmail(), subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setReplyTo(senderEmail);
            helper.setTo(request.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);

            String imagePath = emailTemplateManager.getStarImagePath(npcName);
            Resource imageResource = new ClassPathResource(imagePath);
            String characterPath = emailTemplateManager.getCharacterImagePath(npcName);
            Resource characterResource = new ClassPathResource(characterPath);
            helper.addInline("starImage", imageResource);
            helper.addInline("characterImage", characterResource);

            mailSender.send(message);
            log.info("이메일 전송 성공: {}", request.getEmail());

            EmailLog emailLog = EmailLog.builder()
                    .userId(userId)
                    .recipientEmail(request.getEmail())
                    .subject(subject)
                    .sentAt(LocalDateTime.now())
                    .isDelivered(true)
                    .build();

            emailLogRepository.save(emailLog);

            return true;

        } catch (MessagingException e) {
            log.error("이메일 전송 실패 (MessagingException): {}", e.getMessage(), e);
            saveFailedEmailLog(userId, request.getEmail(), subject, content);
            return false;
        } catch (Exception e) {
            log.error("이메일 전송 실패 (일반 예외): {}", e.getMessage(), e);
            saveFailedEmailLog(userId, request.getEmail(), subject, content);
            return false;
        }
    }

    private void saveFailedEmailLog(UUID userId, String email, String subject, String content) {
        try {
            EmailLog failedLog = EmailLog.builder()
                    .userId(userId)
                    .recipientEmail(email)
                    .subject(subject)
                    .content(content)
                    .sentAt(LocalDateTime.now())
                    .isDelivered(false)
                    .build();

            emailLogRepository.save(failedLog);
        } catch (Exception e) {
            log.error("이메일 로그 저장 실패: {}", e.getMessage(), e);
        }
    }
}
