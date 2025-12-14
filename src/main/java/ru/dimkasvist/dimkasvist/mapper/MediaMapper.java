package ru.dimkasvist.dimkasvist.mapper;

import org.springframework.stereotype.Component;
import ru.dimkasvist.dimkasvist.dto.FeedItemResponse;
import ru.dimkasvist.dimkasvist.dto.MediaResponse;
import ru.dimkasvist.dimkasvist.dto.MediaType;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.Tag;
import ru.dimkasvist.dimkasvist.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MediaMapper {

    public MediaResponse toResponse(Media media) {
        MediaResponse.AuthorInfo authorInfo = buildAuthor(media.getUser());

        return MediaResponse.builder()
                .id(media.getId())
                .mediaType(resolveMediaType(media.getContentType()))
                .title(media.getTitle())
                .description(media.getDescription())
                .url(media.getFilePath())
                .width(media.getWidth())
                .height(media.getHeight())
                .durationSeconds(media.getDurationSeconds())
                .fileSize(media.getFileSize())
                .contentType(media.getContentType())
                .createdAt(media.getCreatedAt())
                .author(authorInfo)
                .likesCount(0)
                .commentsCount(0)
                .tags(extractTagNames(media.getTags()))
                .build();
    }

    public FeedItemResponse toFeedItem(Media media) {
        FeedItemResponse.AuthorInfo authorInfo = null;
        User user = media.getUser();
        if (user != null) {
            authorInfo = FeedItemResponse.AuthorInfo.builder()
                    .id(user.getId())
                    .displayName(user.getDisplayName())
                    .avatarUrl(user.getAvatarUrl())
                    .build();
        }

        return FeedItemResponse.builder()
                .id(media.getId())
                .mediaType(resolveMediaType(media.getContentType()))
                .title(media.getTitle())
                .description(media.getDescription())
                .url(media.getFilePath())
                .width(media.getWidth())
                .height(media.getHeight())
                .durationSeconds(media.getDurationSeconds())
                .fileSize(media.getFileSize())
                .contentType(media.getContentType())
                .createdAt(media.getCreatedAt())
                .author(authorInfo)
                .likesCount(0)
                .commentsCount(0)
                .tags(extractTagNames(media.getTags()))
                .build();
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return MediaType.PHOTO;
        }
        return MediaType.VIDEO;
    }

    private MediaResponse.AuthorInfo buildAuthor(User user) {
        if (user == null) {
            return null;
        }
        return MediaResponse.AuthorInfo.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private Set<String> extractTagNames(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }
}
