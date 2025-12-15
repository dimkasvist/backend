package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.FollowResponse;
import ru.dimkasvist.dimkasvist.dto.FollowsResponse;
import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.entity.Follow;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.repository.FollowRepository;
import ru.dimkasvist.dimkasvist.repository.UserRepository;
import ru.dimkasvist.dimkasvist.service.FollowService;
import ru.dimkasvist.dimkasvist.service.NotificationService;import ru.dimkasvist.dimkasvist.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public FollowResponse followUser(Long userId) {
        User currentUser = userService.getCurrentUser();
        
        if (currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        User userToFollow = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), userId)) {
            throw new IllegalStateException("Already following this user");
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(userToFollow)
                .build();

        Follow saved = followRepository.save(follow);
        
        notificationService.createFollowNotification(userToFollow, currentUser);
        
        return toResponse(saved, userToFollow);
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId) {
        User currentUser = userService.getCurrentUser();

        Follow follow = followRepository.findByFollowerIdAndFollowingId(currentUser.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relationship not found"));

        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId) {
        User currentUser = userService.getCurrentUser();
        return followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowsResponse getFollowers(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followsPage = followRepository.findFollowersByUserId(userId, pageable);

        List<FollowResponse> follows = followsPage.getContent().stream()
                .map(follow -> toResponse(follow, follow.getFollower()))
                .collect(Collectors.toList());

        return FollowsResponse.builder()
                .follows(follows)
                .page(followsPage.getNumber())
                .size(followsPage.getSize())
                .totalElements(followsPage.getTotalElements())
                .totalPages(followsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FollowsResponse getFollowing(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> followsPage = followRepository.findFollowingByUserId(userId, pageable);

        List<FollowResponse> follows = followsPage.getContent().stream()
                .map(follow -> toResponse(follow, follow.getFollowing()))
                .collect(Collectors.toList());

        return FollowsResponse.builder()
                .follows(follows)
                .page(followsPage.getNumber())
                .size(followsPage.getSize())
                .totalElements(followsPage.getTotalElements())
                .totalPages(followsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return followRepository.countFollowers(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countFollowing(userId);
    }

    private FollowResponse toResponse(Follow follow, User user) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return FollowResponse.builder()
                .id(follow.getId())
                .user(userResponse)
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
