package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dimkasvist.dimkasvist.dto.CommentLikeResponse;
import ru.dimkasvist.dimkasvist.service.CommentLikeService;

@RestController
@RequestMapping("/api/comments/{commentId}/likes")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<@NonNull CommentLikeResponse> toggleLike(@PathVariable Long commentId) {
        CommentLikeResponse response = commentLikeService.toggleLike(commentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<@NonNull CommentLikeResponse> getLikeStatus(@PathVariable Long commentId) {
        CommentLikeResponse response = commentLikeService.getLikeStatus(commentId);
        return ResponseEntity.ok(response);
    }
}
