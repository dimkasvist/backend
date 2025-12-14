package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.NotificationSettingsRequest;
import ru.dimkasvist.dimkasvist.dto.NotificationSettingsResponse;
import ru.dimkasvist.dimkasvist.service.NotificationSettingsService;

@RestController
@RequestMapping("/api/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsService settingsService;

    @GetMapping
    public ResponseEntity<@NonNull NotificationSettingsResponse> getSettings() {
        NotificationSettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<@NonNull NotificationSettingsResponse> updateSettings(
            @RequestBody NotificationSettingsRequest request
    ) {
        NotificationSettingsResponse response = settingsService.updateSettings(request);
        return ResponseEntity.ok(response);
    }
}
