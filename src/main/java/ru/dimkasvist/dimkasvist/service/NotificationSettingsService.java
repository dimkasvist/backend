package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.NotificationSettingsRequest;
import ru.dimkasvist.dimkasvist.dto.NotificationSettingsResponse;

public interface NotificationSettingsService {
    NotificationSettingsResponse getSettings();
    NotificationSettingsResponse updateSettings(NotificationSettingsRequest request);
}
