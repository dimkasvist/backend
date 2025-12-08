package ru.dimkasvist.dimkasvist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.CommentRequest;
import ru.dimkasvist.dimkasvist.dto.CommentResponse;
import ru.dimkasvist.dimkasvist.dto.CommentsResponse;
import ru.dimkasvist.dimkasvist.service.CommentService;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/media/{mediaId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long mediaId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse response = commentService.addComment(mediaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/media/{mediaId}/comments")
    public ResponseEntity<CommentsResponse> getComments(
            @PathVariable Long mediaId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CommentsResponse response = commentService.getComments(mediaId, cursor, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
