package ru.dimkasvist.dimkasvist.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedItemResponse {

    private Long id;
    private MediaType mediaType;
    private String title;
    private String description;
    private String url;
    private Integer width;
    private Integer height;
    private Long durationSeconds;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
    private AuthorInfo author;
    private long likesCount;
    private long commentsCount;

    @Data
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String displayName;
        private String avatarUrl;
    }
}
