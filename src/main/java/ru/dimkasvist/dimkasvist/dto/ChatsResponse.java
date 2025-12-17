package ru.dimkasvist.dimkasvist.dto;

import java.util.List;

public record ChatsResponse(
    List<ChatResponse> chats,
    int currentPage,
    int totalPages,
    long totalItems
) {}
