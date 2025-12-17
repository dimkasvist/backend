package ru.dimkasvist.dimkasvist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.MessageResponse;
import ru.dimkasvist.dimkasvist.dto.MessagesResponse;
import ru.dimkasvist.dimkasvist.dto.SendMessageRequest;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.service.MessageService;
import ru.dimkasvist.dimkasvist.service.UserService;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<@NonNull MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request
    ) {
        User currentUser = userService.getCurrentUser();
        MessageResponse response = messageService.sendMessage(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<@NonNull MessagesResponse> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        MessagesResponse response = messageService.getChatMessages(chatId, currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<@NonNull MessageResponse> updateMessage(
            @PathVariable Long messageId,
            @RequestBody String newContent
    ) {
        User currentUser = userService.getCurrentUser();
        MessageResponse response = messageService.updateMessage(messageId, currentUser.getId(), newContent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        User currentUser = userService.getCurrentUser();
        messageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{messageId}/delivered")
    public ResponseEntity<Void> markAsDelivered(@PathVariable Long messageId) {
        User currentUser = userService.getCurrentUser();
        messageService.markMessageAsDelivered(messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        User currentUser = userService.getCurrentUser();
        messageService.markMessageAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/chat/{chatId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long chatId) {
        User currentUser = userService.getCurrentUser();
        messageService.markAllMessagesAsReadInChat(chatId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        long count = messageService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }
}
