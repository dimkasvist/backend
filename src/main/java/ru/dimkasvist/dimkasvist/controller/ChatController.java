package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.ChatsResponse;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.service.ChatService;
import ru.dimkasvist.dimkasvist.service.UserService;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<@NonNull ChatsResponse> getUserChats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        ChatsResponse response = chatService.getUserChats(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/{recipientId}")
    public ResponseEntity<Void> createChat(@PathVariable Long recipientId) {
        User currentUser = userService.getCurrentUser();
        chatService.getOrCreateChat(currentUser.getId(), recipientId);
        return ResponseEntity.ok().build();
    }
}
