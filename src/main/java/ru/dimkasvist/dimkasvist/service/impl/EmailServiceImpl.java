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
            message.setSubject("New Like on Your Pin");
            message.setText(String.format(
                    "Hello!\n\n%s liked your pin \"%s\".\n\nCheck it out on DimkasList!",
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
            message.setSubject("New Comment on Your Pin");
            message.setText(String.format(
                    "Hello!\n\n%s commented on your pin \"%s\":\n\n\"%s\"\n\nCheck it out on DimkasList!",
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
            message.setSubject("Someone Liked Your Comment");
            message.setText(String.format(
                    "Hello!\n\n%s liked your comment.\n\nCheck it out on DimkasList!",
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
            message.setSubject("New Pin from " + authorName);
            message.setText(String.format(
                    "Hello!\n\n%s posted a new pin \"%s\".\n\nCheck it out on DimkasList!",
                    authorName, mediaTitle
            ));
            mailSender.send(message);
            log.info("New pin notification email sent to: {}", fullRecipient.getEmail());
        } catch (Exception e) {
            log.error("Failed to send new pin notification email", e);
        }
    }
}
