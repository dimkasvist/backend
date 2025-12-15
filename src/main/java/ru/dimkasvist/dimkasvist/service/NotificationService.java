package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.NotificationsResponse;
import ru.dimkasvist.dimkasvist.entity.Comment;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.Notification;
import ru.dimkasvist.dimkasvist.entity.User;

public interface NotificationService {
    void createLikeNotification(User recipient, User actor, Media media);
    void createCommentNotification(User recipient, User actor, Media media, Comment comment);
    void createCommentLikeNotification(User recipient, User actor, Comment comment);
    void createNewPinNotification(Media media, User author);
    void createFollowNotification(User recipient, User follower);
    NotificationsResponse getNotifications(int page, int size);
    NotificationsResponse getUnreadNotifications(int page, int size);
    void markAsRead(Long notificationId);
    void markAllAsRead();
    long getUnreadCount();
}
