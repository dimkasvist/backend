package ru.dimkasvist.dimkasvist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.service.EmailService;

@Service
@ConditionalOnMissingBean(JavaMailSender.class)
@Slf4j
public class NoOpEmailService implements EmailService {

    @Override
    @Async
    public void sendLikeNotification(String email, String actorName, String mediaTitle) {
        log.debug("Email not configured - would send like notification to: {}", email);
    }

    @Override
    @Async
    public void sendCommentNotification(String email, String actorName, String mediaTitle, String commentText) {
        log.debug("Email not configured - would send comment notification to: {}", email);
    }

    @Override
    @Async
    public void sendCommentLikeNotification(String email, String actorName) {
        log.debug("Email not configured - would send comment like notification to: {}", email);
    }

    @Override
    @Async
    public void sendNewPinNotification(User recipient, String authorName, String mediaTitle) {
        log.debug("Email not configured - would send new pin notification to user: {}", recipient.getId());
    }

    @Override
    @Async
    public void sendFollowNotification(String email, String followerName) {
        log.debug("Email not configured - would send follow notification to: {}", email);
    }
}
