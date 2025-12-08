package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dimkasvist.dimkasvist.dto.UserLikesResponse;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.service.LikeService;
import ru.dimkasvist.dimkasvist.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserLikeController {

    private final LikeService likeService;
    private final UserService userService;

    @GetMapping("/me/likes")
    public ResponseEntity<UserLikesResponse> getCurrentUserLikes(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = userService.getCurrentUser();
        UserLikesResponse response = likeService.getUserLikes(currentUser.getId(), cursor, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/likes")
    public ResponseEntity<UserLikesResponse> getUserLikes(
            @PathVariable Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserLikesResponse response = likeService.getUserLikes(userId, cursor, size);
        return ResponseEntity.ok(response);
    }
}
