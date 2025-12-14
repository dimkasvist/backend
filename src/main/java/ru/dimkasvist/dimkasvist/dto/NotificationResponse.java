package ru.dimkasvist.dimkasvist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private UserResponse actor;
    private MediaResponse media;
    private Long commentId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String message;
}
