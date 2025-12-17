package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.MessageResponse;
import ru.dimkasvist.dimkasvist.dto.MessageStatusResponse;
import ru.dimkasvist.dimkasvist.dto.TypingNotification;

public interface WebSocketMessagingService {
    
    void sendMessageToUser(Long userId, MessageResponse message);
    
    void sendTypingNotification(Long recipientId, TypingNotification notification);
    
    void sendMessageStatus(Long userId, Long messageId, MessageStatusResponse status);
    
    void notifyMessageDelivered(Long senderId, Long messageId);
    
    void notifyMessageRead(Long senderId, Long messageId);
    
    void notifyMessageDeleted(Long userId, Long messageId, Long chatId);
}
