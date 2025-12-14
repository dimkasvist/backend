package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.NotificationSettingsRequest;
import ru.dimkasvist.dimkasvist.dto.NotificationSettingsResponse;
import ru.dimkasvist.dimkasvist.entity.NotificationSettings;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.repository.NotificationSettingsRepository;
import ru.dimkasvist.dimkasvist.service.NotificationSettingsService;
import ru.dimkasvist.dimkasvist.service.UserService;

@Service
@RequiredArgsConstructor
public class NotificationSettingsServiceImpl implements NotificationSettingsService {

    private final NotificationSettingsRepository settingsRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public NotificationSettingsResponse getSettings() {
        User currentUser = userService.getCurrentUser();
        NotificationSettings settings = getOrCreateSettings(currentUser);
        return toResponse(settings);
    }

    @Override
    @Transactional
    public NotificationSettingsResponse updateSettings(NotificationSettingsRequest request) {
        User currentUser = userService.getCurrentUser();
        NotificationSettings settings = getOrCreateSettings(currentUser);

        if (request.getNotificationsEnabled() != null) {
            settings.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            settings.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getLikeNotifications() != null) {
            settings.setLikeNotifications(request.getLikeNotifications());
        }
        if (request.getCommentNotifications() != null) {
            settings.setCommentNotifications(request.getCommentNotifications());
        }
        if (request.getCommentLikeNotifications() != null) {
            settings.setCommentLikeNotifications(request.getCommentLikeNotifications());
        }
        if (request.getNewPinNotifications() != null) {
            settings.setNewPinNotifications(request.getNewPinNotifications());
        }

        NotificationSettings updated = settingsRepository.save(settings);
        return toResponse(updated);
    }

    private NotificationSettings getOrCreateSettings(User user) {
        return settingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    NotificationSettings settings = NotificationSettings.builder()
                            .user(user)
                            .build();
                    return settingsRepository.save(settings);
                });
    }

    private NotificationSettingsResponse toResponse(NotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .id(settings.getId())
                .notificationsEnabled(settings.getNotificationsEnabled())
                .emailNotificationsEnabled(settings.getEmailNotificationsEnabled())
                .likeNotifications(settings.getLikeNotifications())
                .commentNotifications(settings.getCommentNotifications())
                .commentLikeNotifications(settings.getCommentLikeNotifications())
                .newPinNotifications(settings.getNewPinNotifications())
                .build();
    }
}
