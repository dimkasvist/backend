package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.ChatResponse;
import ru.dimkasvist.dimkasvist.dto.ChatsResponse;
import ru.dimkasvist.dimkasvist.dto.MessageResponse;
import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.entity.Chat;
import ru.dimkasvist.dimkasvist.entity.Message;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.repository.ChatRepository;
import ru.dimkasvist.dimkasvist.repository.MessageRepository;
import ru.dimkasvist.dimkasvist.repository.MessageStatusRepository;
import ru.dimkasvist.dimkasvist.repository.UserRepository;
import ru.dimkasvist.dimkasvist.service.ChatService;
import ru.dimkasvist.dimkasvist.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final MessageService messageService;

    public ChatServiceImpl(
            ChatRepository chatRepository,
            UserRepository userRepository,
            MessageRepository messageRepository,
            MessageStatusRepository messageStatusRepository,
            @org.springframework.context.annotation.Lazy MessageService messageService
    ) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageStatusRepository = messageStatusRepository;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public Chat getOrCreateChat(Long userId1, Long userId2) {
        Long minUserId = Math.min(userId1, userId2);
        Long maxUserId = Math.max(userId1, userId2);

        return chatRepository.findByUsers(minUserId, maxUserId)
                .orElseGet(() -> {
                    User user1 = userRepository.findById(minUserId)
                            .orElseThrow(() -> new RuntimeException("User not found: " + minUserId));
                    User user2 = userRepository.findById(maxUserId)
                            .orElseThrow(() -> new RuntimeException("User not found: " + maxUserId));

                    Chat chat = Chat.builder()
                            .user1(user1)
                            .user2(user2)
                            .build();
                    return chatRepository.save(chat);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Chat getChatById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
    }

    @Override
    @Transactional(readOnly = true)
    public ChatsResponse getUserChats(Long userId, Pageable pageable) {
        Page<Chat> chatsPage = chatRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        
        List<ChatResponse> chatResponses = chatsPage.getContent().stream()
                .map(chat -> toChatResponse(chat, userId))
                .toList();

        return new ChatsResponse(
                chatResponses,
                chatsPage.getNumber(),
                chatsPage.getTotalPages(),
                chatsPage.getTotalElements()
        );
    }

    @Override
    public ChatResponse toChatResponse(Chat chat, Long currentUserId) {
        User otherUser = chat.getUser1().getId().equals(currentUserId) 
                ? chat.getUser2() 
                : chat.getUser1();

        UserResponse userResponse = UserResponse.builder()
                .id(otherUser.getId())
                .displayName(otherUser.getDisplayName())
                .avatarUrl(otherUser.getAvatarUrl())
                .createdAt(otherUser.getCreatedAt())
                .build();

        Message lastMessage = messageRepository.findLastMessageByChatId(chat.getId()).orElse(null);
        MessageResponse lastMessageResponse = lastMessage != null 
                ? messageService.toMessageResponse(lastMessage, currentUserId) 
                : null;

        long unreadCount = messageStatusRepository.countUnreadByUserId(currentUserId);

        return new ChatResponse(
                chat.getId(),
                userResponse,
                lastMessageResponse,
                unreadCount,
                chat.getCreatedAt(),
                chat.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void updateChatTimestamp(Long chatId) {
        Chat chat = getChatById(chatId);
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }
}
