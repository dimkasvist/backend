package ru.dimkasvist.dimkasvist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MessageResponse(
    Long id,
    Long chatId,
    UserResponse sender,
    String content,
    String messageType,
    String attachmentUrl,
    SharedMediaInfo sharedMedia,
    Boolean isEdited,
    MessageStatusResponse status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {
    public record SharedMediaInfo(
        Long id,
        String title,
        String thumbnailUrl,
        String mediaType
    ) {}
}
