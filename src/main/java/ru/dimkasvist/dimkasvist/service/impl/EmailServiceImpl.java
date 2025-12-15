package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.repository.UserRepository;
import ru.dimkasvist.dimkasvist.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Override
    @Async
    public void sendLikeNotification(String email, String actorName, String mediaTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Новая оценка вашего дима");
            message.setText(String.format(
                    "Привет!\n\n%s оценил ваш дим \"%s\".\n\nПроверьте на DimkasList!",
                    actorName, mediaTitle
            ));
            mailSender.send(message);
            log.info("Like notification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send like notification email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendCommentNotification(String email, String actorName, String mediaTitle, String commentText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Новый комментарий к вашему диму");
            message.setText(String.format(
                    "Привет!\n\n%s прокомментировал ваш дим \"%s\":\n\n\"%s\"\n\nПроверьте на DimkasList!",
                    actorName, mediaTitle, commentText
            ));
            mailSender.send(message);
            log.info("Comment notification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send comment notification email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendCommentLikeNotification(String email, String actorName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Оценка вашего комментария");
            message.setText(String.format(
                    "Привет!\n\n%s оценил ваш комментарий.\n\nПроверьте на DimkasList!",
                    actorName
            ));
            mailSender.send(message);
            log.info("Comment like notification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send comment like notification email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendNewPinNotification(User recipient, String authorName, String mediaTitle) {
        try {
            User fullRecipient = userRepository.findById(recipient.getId()).orElse(null);
            if (fullRecipient == null) {
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(fullRecipient.getEmail());
            message.setSubject("Новый дим от " + authorName);
            message.setText(String.format(
                    "Привет!\n\n%s опубликовал новый дим \"%s\".\n\nПроверьте на DimkasList!",
                    authorName, mediaTitle
            ));
            mailSender.send(message);
            log.info("New pin notification email sent to: {}", fullRecipient.getEmail());
        } catch (Exception e) {
            log.error("Failed to send new pin notification email", e);
        }
    }

    @Override
    @Async
    public void sendFollowNotification(String email, String followerName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Новый подписчик");
            message.setText(String.format(
                    "Привет!\n\n%s подписался на вас.\n\nПроверьте на DimkasList!",
                    followerName
            ));
            mailSender.send(message);
            log.info("Follow notification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send follow notification email to: {}", email, e);
        }
    }
}
