package ru.dimkasvist.dimkasvist.dto;

public record TypingNotification(
    Long chatId,
    Long userId,
    String displayName,
    Boolean isTyping
) {}
