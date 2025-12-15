package ru.dimkasvist.dimkasvist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "email_notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(name = "like_notifications", nullable = false)
    @Builder.Default
    private Boolean likeNotifications = true;

    @Column(name = "comment_notifications", nullable = false)
    @Builder.Default
    private Boolean commentNotifications = true;

    @Column(name = "comment_like_notifications", nullable = false)
    @Builder.Default
    private Boolean commentLikeNotifications = true;

    @Column(name = "new_pin_notifications", nullable = false)
    @Builder.Default
    private Boolean newPinNotifications = true;

    @Column(name = "follow_notifications", nullable = false)
    @Builder.Default
    private Boolean followNotifications = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
