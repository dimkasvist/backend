package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import ru.dimkasvist.dimkasvist.dto.MessageResponse;
import ru.dimkasvist.dimkasvist.dto.SendMessageRequest;
import ru.dimkasvist.dimkasvist.dto.TypingNotification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.security.GoogleUserPrincipal;
import ru.dimkasvist.dimkasvist.service.MessageService;
import ru.dimkasvist.dimkasvist.service.UserService;
import ru.dimkasvist.dimkasvist.service.WebSocketMessagingService;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final MessageService messageService;
    private final UserService userService;
    private final WebSocketMessagingService webSocketMessagingService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        try {
            User currentUser = getUserFromPrincipal(principal);
            MessageResponse response = messageService.sendMessage(currentUser.getId(), request);
            log.debug("Message sent via WebSocket from user {}", currentUser.getId());
        } catch (Exception e) {
            log.error("Error sending message via WebSocket", e);
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingNotification notification, Principal principal) {
        try {
            User currentUser = getUserFromPrincipal(principal);
            TypingNotification enrichedNotification = new TypingNotification(
                    notification.chatId(),
                    currentUser.getId(),
                    currentUser.getDisplayName(),
                    notification.isTyping()
            );
            
            Long recipientId = notification.userId();
            if (recipientId != null) {
                webSocketMessagingService.sendTypingNotification(recipientId, enrichedNotification);
            }
            
            log.debug("Typing notification sent from user {}", currentUser.getId());
        } catch (Exception e) {
            log.error("Error handling typing notification", e);
        }
    }

    @MessageMapping("/chat.delivered")
    public void markAsDelivered(@Payload MessageStatusUpdate update, Principal principal) {
        try {
            User currentUser = getUserFromPrincipal(principal);
            messageService.markMessageAsDelivered(update.messageId(), currentUser.getId());
            log.debug("Message {} marked as delivered by user {}", update.messageId(), currentUser.getId());
        } catch (Exception e) {
            log.error("Error marking message as delivered", e);
        }
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload MessageStatusUpdate update, Principal principal) {
        try {
            User currentUser = getUserFromPrincipal(principal);
            messageService.markMessageAsRead(update.messageId(), currentUser.getId());
            log.debug("Message {} marked as read by user {}", update.messageId(), currentUser.getId());
        } catch (Exception e) {
            log.error("Error marking message as read", e);
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            GoogleUserPrincipal googlePrincipal = (GoogleUserPrincipal) authToken.getPrincipal();
            return userService.getOrCreateUser(
                    googlePrincipal.googleId(),
                    googlePrincipal.email(),
                    googlePrincipal.name(),
                    googlePrincipal.pictureUrl()
            );
        }
        throw new IllegalStateException("Invalid principal type: " + principal.getClass());
    }

    private record MessageStatusUpdate(Long messageId) {}
}
