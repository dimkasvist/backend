package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.entity.User;

public interface EmailService {
    void sendLikeNotification(String email, String actorName, String mediaTitle);
    void sendCommentNotification(String email, String actorName, String mediaTitle, String commentText);
    void sendCommentLikeNotification(String email, String actorName);
    void sendNewPinNotification(User recipient, String authorName, String mediaTitle);
    void sendFollowNotification(String email, String followerName);
}
