package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.FollowResponse;
import ru.dimkasvist.dimkasvist.dto.FollowsResponse;
import ru.dimkasvist.dimkasvist.service.FollowService;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public ResponseEntity<@NonNull FollowResponse> followUser(@PathVariable Long userId) {
        FollowResponse response = followService.followUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<@NonNull Void> unfollowUser(@PathVariable Long userId) {
        followService.unfollowUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<@NonNull Boolean> isFollowing(@PathVariable Long userId) {
        boolean isFollowing = followService.isFollowing(userId);
        return ResponseEntity.ok(isFollowing);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<@NonNull FollowsResponse> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FollowsResponse response = followService.getFollowers(userId, page, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<@NonNull FollowsResponse> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FollowsResponse response = followService.getFollowing(userId, page, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<@NonNull FollowStats> getFollowStats(@PathVariable Long userId) {
        long followersCount = followService.getFollowersCount(userId);
        long followingCount = followService.getFollowingCount(userId);
        return ResponseEntity.ok(new FollowStats(followersCount, followingCount));
    }

    public record FollowStats(long followersCount, long followingCount) {}
}
