package ru.dimkasvist.dimkasvist.dto;

import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
    @NotNull(message = "Recipient ID is required")
    Long recipientId,
    String content,
    String messageType,
    String attachmentUrl,
    Long sharedMediaId
) {}
