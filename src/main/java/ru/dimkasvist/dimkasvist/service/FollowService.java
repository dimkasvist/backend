package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.FollowResponse;
import ru.dimkasvist.dimkasvist.dto.FollowsResponse;

public interface FollowService {
    FollowResponse followUser(Long userId);
    void unfollowUser(Long userId);
    boolean isFollowing(Long userId);
    FollowsResponse getFollowers(Long userId, int page, int size);
    FollowsResponse getFollowing(Long userId, int page, int size);
    long getFollowersCount(Long userId);
    long getFollowingCount(Long userId);
}
