package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.LikeResponse;
import ru.dimkasvist.dimkasvist.service.LikeService;

@RestController
@RequestMapping("/api/media/{mediaId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<@NonNull LikeResponse> toggleLike(@PathVariable Long mediaId) {
        LikeResponse response = likeService.toggleLike(mediaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<@NonNull LikeResponse> getLikeStatus(@PathVariable Long mediaId) {
        LikeResponse response = likeService.getLikeStatus(mediaId);
        return ResponseEntity.ok(response);
    }
}
