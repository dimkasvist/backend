package ru.dimkasvist.dimkasvist.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dimkasvist.dimkasvist.dto.NotificationResponse;
import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.entity.Notification;
import ru.dimkasvist.dimkasvist.entity.User;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final MediaMapper mediaMapper;

    public NotificationResponse toResponse(Notification notification) {
        UserResponse actorResponse = notification.getActor() != null ? buildUser(notification.getActor()) : null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .actor(actorResponse)
                .media(notification.getMedia() != null ? mediaMapper.toResponse(notification.getMedia()) : null)
                .commentId(notification.getComment() != null ? notification.getComment().getId() : null)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .message(buildMessage(notification))
                .build();
    }

    private String buildMessage(Notification notification) {
        String actorName = notification.getActor() != null ? notification.getActor().getDisplayName() : "Кто-то";
        
        return switch (notification.getType()) {
            case LIKE -> actorName + " оценил ваш пин";
            case COMMENT -> actorName + " прокомментировал ваш пин";
            case COMMENT_LIKE -> actorName + " оценил ваш комментарий";
            case NEW_PIN_FROM_FOLLOWING -> actorName + " опубликовал новый пин";
        };
    }

    private UserResponse buildUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
