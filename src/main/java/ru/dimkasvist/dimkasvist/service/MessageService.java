package ru.dimkasvist.dimkasvist.service;

import org.springframework.data.domain.Pageable;
import ru.dimkasvist.dimkasvist.dto.*;
import ru.dimkasvist.dimkasvist.entity.Message;

public interface MessageService {
    
    MessageResponse sendMessage(Long senderId, SendMessageRequest request);
    
    MessagesResponse getChatMessages(Long chatId, Long userId, Pageable pageable);
    
    Message getMessageById(Long messageId);
    
    void markMessageAsDelivered(Long messageId, Long userId);
    
    void markMessageAsRead(Long messageId, Long userId);
    
    void markAllMessagesAsReadInChat(Long chatId, Long userId);
    
    long getUnreadCount(Long userId);
    
    MessageResponse updateMessage(Long messageId, Long userId, String newContent);
    
    void deleteMessage(Long messageId, Long userId);
    
    MessageResponse toMessageResponse(Message message, Long currentUserId);
}
