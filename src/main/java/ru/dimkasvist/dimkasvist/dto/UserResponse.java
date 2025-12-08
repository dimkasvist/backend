package ru.dimkasvist.dimkasvist.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
