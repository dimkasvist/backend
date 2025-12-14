package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.NotificationResponse;
import ru.dimkasvist.dimkasvist.dto.NotificationsResponse;
import ru.dimkasvist.dimkasvist.entity.*;
import ru.dimkasvist.dimkasvist.mapper.NotificationMapper;
import ru.dimkasvist.dimkasvist.repository.FollowRepository;
import ru.dimkasvist.dimkasvist.repository.NotificationRepository;
import ru.dimkasvist.dimkasvist.repository.NotificationSettingsRepository;
import ru.dimkasvist.dimkasvist.service.EmailService;
import ru.dimkasvist.dimkasvist.service.NotificationService;
import ru.dimkasvist.dimkasvist.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final FollowRepository followRepository;
    private final NotificationMapper notificationMapper;
    private final UserService userService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createLikeNotification(User recipient, User actor, Media media) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        NotificationSettings settings = getOrCreateSettings(recipient);
        if (!settings.getNotificationsEnabled() || !settings.getLikeNotifications()) {
            return;
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .type(Notification.NotificationType.LIKE)
                .actor(actor)
                .media(media)
                .build();

        notificationRepository.save(notification);

        if (settings.getEmailNotificationsEnabled()) {
            emailService.sendLikeNotification(recipient.getEmail(), actor.getDisplayName(), media.getTitle());
        }
    }

    @Override
    @Transactional
    public void createCommentNotification(User recipient, User actor, Media media, Comment comment) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        NotificationSettings settings = getOrCreateSettings(recipient);
        if (!settings.getNotificationsEnabled() || !settings.getCommentNotifications()) {
            return;
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .type(Notification.NotificationType.COMMENT)
                .actor(actor)
                .media(media)
                .comment(comment)
                .build();

        notificationRepository.save(notification);

        if (settings.getEmailNotificationsEnabled()) {
            emailService.sendCommentNotification(recipient.getEmail(), actor.getDisplayName(), media.getTitle(), comment.getText());
        }
    }

    @Override
    @Transactional
    public void createCommentLikeNotification(User recipient, User actor, Comment comment) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        NotificationSettings settings = getOrCreateSettings(recipient);
        if (!settings.getNotificationsEnabled() || !settings.getCommentLikeNotifications()) {
            return;
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .type(Notification.NotificationType.COMMENT_LIKE)
                .actor(actor)
                .comment(comment)
                .build();

        notificationRepository.save(notification);

        if (settings.getEmailNotificationsEnabled()) {
            emailService.sendCommentLikeNotification(recipient.getEmail(), actor.getDisplayName());
        }
    }

    @Override
    @Transactional
    public void createNewPinNotification(Media media, User author) {
        List<Long> followerIds = followRepository.findFollowerIdsByUserId(author.getId());

        for (Long followerId : followerIds) {
            NotificationSettings settings = settingsRepository.findByUserId(followerId).orElse(null);
            if (settings == null || !settings.getNotificationsEnabled() || !settings.getNewPinNotifications()) {
                continue;
            }

            Notification notification = Notification.builder()
                    .user(new User())
                    .type(Notification.NotificationType.NEW_PIN_FROM_FOLLOWING)
                    .actor(author)
                    .media(media)
                    .build();
            notification.getUser().setId(followerId);

            notificationRepository.save(notification);

            if (settings.getEmailNotificationsEnabled()) {
                User follower = new User();
                follower.setId(followerId);
                emailService.sendNewPinNotification(follower, author.getDisplayName(), media.getTitle());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationsResponse getNotifications(int page, int size) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage = notificationRepository.findByUserId(currentUser.getId(), pageable);

        List<NotificationResponse> notifications = notificationsPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        long unreadCount = notificationRepository.countUnreadByUserId(currentUser.getId());

        return NotificationsResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .page(notificationsPage.getNumber())
                .size(notificationsPage.getSize())
                .totalElements(notificationsPage.getTotalElements())
                .totalPages(notificationsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationsResponse getUnreadNotifications(int page, int size) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage = notificationRepository.findUnreadByUserId(currentUser.getId(), pageable);

        List<NotificationResponse> notifications = notificationsPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return NotificationsResponse.builder()
                .notifications(notifications)
                .unreadCount(notificationsPage.getTotalElements())
                .page(notificationsPage.getNumber())
                .size(notificationsPage.getSize())
                .totalElements(notificationsPage.getTotalElements())
                .totalPages(notificationsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = userService.getCurrentUser();
        notificationRepository.markAsRead(notificationId, currentUser.getId());
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = userService.getCurrentUser();
        notificationRepository.markAllAsReadByUserId(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        return notificationRepository.countUnreadByUserId(currentUser.getId());
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
}
