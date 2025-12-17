package ru.dimkasvist.dimkasvist.dto;

import java.util.List;

public record MessagesResponse(
    List<MessageResponse> messages,
    int currentPage,
    int totalPages,
    long totalItems
) {}
