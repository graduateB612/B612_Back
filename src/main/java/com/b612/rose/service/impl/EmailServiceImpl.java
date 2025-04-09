package com.b612.rose.service.impl;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailLogRepository emailLogRepository;
    private final StarRepository starRepository;
    private final EmailTemplateManager emailTemplateManager;

    @Override
    @Transactional
    public boolean sendEmail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("User email is required");
        }

        if (user.getSelectedNpc() == null || user.getSelectedNpc().isEmpty()) {
            throw new IllegalArgumentException("Selected NPC is required");
        }

        String npcName = user.getSelectedNpc();
        String senderEmail = emailTemplateManager.getSenderEmail(npcName);

        StarType starType = emailTemplateManager.getStarTypeForNpc(npcName);
        Star star = starRepository.findByStarType(starType)
                .orElseThrow(() -> new IllegalArgumentException("Star not found for type: " + starType));

        String purifiedTypeName = star.getPurifiedType().getDescription();
        String subject = emailTemplateManager.getSubject(npcName, purifiedTypeName);
        String content = emailTemplateManager.getEmailContent(user, npcName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

            EmailLog emailLog = EmailLog.builder()
                    .userId(userId)
                    .recipientEmail(user.getEmail())
                    .subject(subject)
                    .content(content)
                    .sentAt(LocalDateTime.now())
                    .isDelivered(true)
                    .build();

            emailLogRepository.save(emailLog);

            return true;

        } catch (MessagingException e) {
            EmailLog failedLog = EmailLog.builder()
                    .userId(userId)
                    .recipientEmail(user.getEmail())
                    .subject(subject)
                    .content(content)
                    .sentAt(LocalDateTime.now())
                    .isDelivered(false)
                    .build();

            emailLogRepository.save(failedLog);

            return false;
        }
    }
}
