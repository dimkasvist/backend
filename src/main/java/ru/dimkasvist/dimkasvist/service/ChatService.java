package ru.dimkasvist.dimkasvist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dimkasvist.dimkasvist.dto.ChatResponse;
import ru.dimkasvist.dimkasvist.dto.ChatsResponse;
import ru.dimkasvist.dimkasvist.entity.Chat;

public interface ChatService {
    
    Chat getOrCreateChat(Long userId1, Long userId2);
    
    Chat getChatById(Long chatId);
    
    ChatsResponse getUserChats(Long userId, Pageable pageable);
    
    ChatResponse toChatResponse(Chat chat, Long currentUserId);
    
    void updateChatTimestamp(Long chatId);
}
