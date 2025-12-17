package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.dimkasvist.dimkasvist.dto.MessageResponse;
import ru.dimkasvist.dimkasvist.dto.MessageStatusResponse;
import ru.dimkasvist.dimkasvist.dto.TypingNotification;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.repository.UserRepository;
import ru.dimkasvist.dimkasvist.service.WebSocketMessagingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessagingServiceImpl implements WebSocketMessagingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Override
    public void sendMessageToUser(Long userId, MessageResponse message) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            messagingTemplate.convertAndSendToUser(
                    user.getGoogleId(),
                    "/queue/messages",
                    message
            );
            log.info("Sent message to user {} (googleId: {}) via WebSocket", userId, user.getGoogleId());
        } catch (Exception e) {
            log.error("Failed to send message to user {} via WebSocket", userId, e);
        }
    }

    @Override
    public void sendTypingNotification(Long recipientId, TypingNotification notification) {
        try {
            User user = userRepository.findById(recipientId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + recipientId));
            
            messagingTemplate.convertAndSendToUser(
                    user.getGoogleId(),
                    "/queue/typing",
                    notification
            );
            log.debug("Typing notification sent to user {} (googleId: {})", recipientId, user.getGoogleId());
        } catch (Exception e) {
            log.error("Failed to send typing notification to user {}", recipientId, e);
        }
    }

    @Override
    public void sendMessageStatus(Long userId, Long messageId, MessageStatusResponse status) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            messagingTemplate.convertAndSendToUser(
                    user.getGoogleId(),
                    "/queue/status",
                    new MessageStatusUpdate(messageId, status)
            );
            log.debug("Message status sent to user {} (googleId: {})", userId, user.getGoogleId());
        } catch (Exception e) {
            log.error("Failed to send message status to user {}", userId, e);
        }
    }

    @Override
    public void notifyMessageDelivered(Long senderId, Long messageId) {
        try {
            MessageStatusResponse status = new MessageStatusResponse(
                    true,
                    false,
                    java.time.LocalDateTime.now(),
                    null
            );
            sendMessageStatus(senderId, messageId, status);
        } catch (Exception e) {
            log.error("Failed to notify message delivered", e);
        }
    }

    @Override
    public void notifyMessageRead(Long senderId, Long messageId) {
        try {
            MessageStatusResponse status = new MessageStatusResponse(
                    true,
                    true,
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now()
            );
            sendMessageStatus(senderId, messageId, status);
        } catch (Exception e) {
            log.error("Failed to notify message read", e);
        }
    }

    @Override
    public void notifyMessageDeleted(Long userId, Long messageId, Long chatId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            messagingTemplate.convertAndSendToUser(
                    user.getGoogleId(),
                    "/queue/messages",
                    new MessageDeletedNotification(messageId, chatId, "DELETED")
            );
            log.info("Notified user {} (googleId: {}) about deleted message {}", userId, user.getGoogleId(), messageId);
        } catch (Exception e) {
            log.error("Failed to notify message deleted to user {}", userId, e);
        }
    }

    private record MessageStatusUpdate(Long messageId, MessageStatusResponse status) {}
    private record MessageDeletedNotification(Long messageId, Long chatId, String action) {}
}
