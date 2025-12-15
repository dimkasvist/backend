package ru.dimkasvist.dimkasvist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsResponse {
    private Long id;
    private Boolean notificationsEnabled;
    private Boolean emailNotificationsEnabled;
    private Boolean likeNotifications;
    private Boolean commentNotifications;
    private Boolean commentLikeNotifications;
    private Boolean newPinNotifications;
    private Boolean followNotifications;
}
