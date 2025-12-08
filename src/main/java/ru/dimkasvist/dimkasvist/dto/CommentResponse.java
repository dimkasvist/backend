package ru.dimkasvist.dimkasvist.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private String text;
    private AuthorInfo author;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String displayName;
        private String avatarUrl;
    }
}
