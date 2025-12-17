package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.*;
import ru.dimkasvist.dimkasvist.entity.Chat;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.Message;
import ru.dimkasvist.dimkasvist.entity.MessageStatus;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.repository.MessageRepository;
import ru.dimkasvist.dimkasvist.repository.MessageStatusRepository;
import ru.dimkasvist.dimkasvist.repository.UserRepository;
import ru.dimkasvist.dimkasvist.service.ChatService;
import ru.dimkasvist.dimkasvist.service.MessageService;
import ru.dimkasvist.dimkasvist.service.WebSocketMessagingService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final ChatService chatService;
    private final WebSocketMessagingService webSocketMessagingService;

    @Override
    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderId));
        
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + request.recipientId()));

        Chat chat = chatService.getOrCreateChat(senderId, request.recipientId());

        Message.MessageType messageType = request.messageType() != null 
                ? Message.MessageType.valueOf(request.messageType()) 
                : Message.MessageType.TEXT;

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(request.content())
                .messageType(messageType)
                .attachmentUrl(request.attachmentUrl())
                .sharedMediaId(request.sharedMediaId())
                .build();

        message = messageRepository.save(message);

        MessageStatus status = MessageStatus.builder()
                .message(message)
                .user(recipient)
                .isDelivered(false)
                .isRead(false)
                .build();
        messageStatusRepository.save(status);

        chatService.updateChatTimestamp(chat.getId());

        MessageResponse response = toMessageResponse(message, senderId);
        
        webSocketMessagingService.sendMessageToUser(request.recipientId(), response);
        webSocketMessagingService.sendMessageToUser(senderId, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MessagesResponse getChatMessages(Long chatId, Long userId, Pageable pageable) {
        Page<Message> messagesPage = messageRepository.findByChatId(chatId, pageable);
        
        List<MessageResponse> messageResponses = messagesPage.getContent().stream()
                .map(message -> toMessageResponse(message, userId))
                .toList();

        return new MessagesResponse(
                messageResponses,
                messagesPage.getNumber(),
                messagesPage.getTotalPages(),
                messagesPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
    }

    @Override
    @Transactional
    public void markMessageAsDelivered(Long messageId, Long userId) {
        MessageStatus status = messageStatusRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new RuntimeException("Message status not found"));

        if (!status.getIsDelivered()) {
            status.setIsDelivered(true);
            status.setDeliveredAt(LocalDateTime.now());
            messageStatusRepository.save(status);

            Message message = getMessageById(messageId);
            webSocketMessagingService.notifyMessageDelivered(message.getSender().getId(), messageId);
        }
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long messageId, Long userId) {
        MessageStatus status = messageStatusRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new RuntimeException("Message status not found"));

        if (!status.getIsRead()) {
            status.setIsDelivered(true);
            status.setIsRead(true);
            status.setDeliveredAt(status.getDeliveredAt() != null ? status.getDeliveredAt() : LocalDateTime.now());
            status.setReadAt(LocalDateTime.now());
            messageStatusRepository.save(status);

            Message message = getMessageById(messageId);
            webSocketMessagingService.notifyMessageRead(message.getSender().getId(), messageId);
        }
    }

    @Override
    @Transactional
    public void markAllMessagesAsReadInChat(Long chatId, Long userId) {
        messageStatusRepository.markAllAsReadInChat(chatId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return messageStatusRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public MessageResponse updateMessage(Long messageId, Long userId, String newContent) {
        Message message = getMessageById(messageId);

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own messages");
        }

        message.setContent(newContent);
        message.setIsEdited(true);
        message = messageRepository.save(message);

        MessageResponse response = toMessageResponse(message, userId);
        
        Chat chat = message.getChat();
        Long recipientId = chat.getUser1().getId().equals(userId) 
                ? chat.getUser2().getId() 
                : chat.getUser1().getId();
        
        webSocketMessagingService.sendMessageToUser(userId, response);
        webSocketMessagingService.sendMessageToUser(recipientId, response);

        return response;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = getMessageById(messageId);

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own messages");
        }

        Chat chat = message.getChat();
        Long recipientId = chat.getUser1().getId().equals(userId) 
                ? chat.getUser2().getId() 
                : chat.getUser1().getId();
        
        Long chatId = chat.getId();
        
        messageRepository.delete(message);
        
        webSocketMessagingService.notifyMessageDeleted(userId, messageId, chatId);
        webSocketMessagingService.notifyMessageDeleted(recipientId, messageId, chatId);
    }

    @Override
    public MessageResponse toMessageResponse(Message message, Long currentUserId) {
        User sender = message.getSender();
        UserResponse senderResponse = UserResponse.builder()
                .id(sender.getId())
                .displayName(sender.getDisplayName())
                .avatarUrl(sender.getAvatarUrl())
                .createdAt(sender.getCreatedAt())
                .build();

        MessageStatusResponse statusResponse = null;
        
        if (sender.getId().equals(currentUserId)) {
            Chat chat = message.getChat();
            Long recipientId = chat.getUser1().getId().equals(currentUserId) 
                    ? chat.getUser2().getId() 
                    : chat.getUser1().getId();
            
            MessageStatus status = messageStatusRepository
                    .findByMessageIdAndUserId(message.getId(), recipientId)
                    .orElse(null);
            
            if (status != null) {
                statusResponse = new MessageStatusResponse(
                        status.getIsDelivered(),
                        status.getIsRead(),
                        status.getDeliveredAt(),
                        status.getReadAt()
                );
            }
        }

        MessageResponse.SharedMediaInfo sharedMedia = null;
        if (message.getSharedMediaId() != null) {
            Media media = mediaRepository.findById(message.getSharedMediaId()).orElse(null);
            if (media != null) {
                String mediaType = determineMediaType(media.getContentType());
                sharedMedia = new MessageResponse.SharedMediaInfo(
                        media.getId(),
                        media.getTitle(),
                        media.getFilePath(),
                        mediaType
                );
            }
        }

        return new MessageResponse(
                message.getId(),
                message.getChat().getId(),
                senderResponse,
                message.getContent(),
                message.getMessageType().name(),
                message.getAttachmentUrl(),
                sharedMedia,
                message.getIsEdited(),
                statusResponse,
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    private String determineMediaType(String contentType) {
        if (contentType == null) return "IMAGE";
        if (contentType.startsWith("video/")) return "VIDEO";
        if (contentType.startsWith("image/gif")) return "GIF";
        return "IMAGE";
    }
}
