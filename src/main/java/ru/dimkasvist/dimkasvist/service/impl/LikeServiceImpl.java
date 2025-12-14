package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.FeedItemResponse;
import ru.dimkasvist.dimkasvist.dto.LikeResponse;
import ru.dimkasvist.dimkasvist.dto.UserLikesResponse;
import ru.dimkasvist.dimkasvist.entity.Like;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.mapper.MediaMapper;
import ru.dimkasvist.dimkasvist.repository.LikeRepository;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.service.LikeService;
import ru.dimkasvist.dimkasvist.service.NotificationService;
import ru.dimkasvist.dimkasvist.service.UserService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final MediaRepository mediaRepository;
    private final UserService userService;
    private final MediaMapper mediaMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public LikeResponse toggleLike(Long mediaId) {
        User currentUser = userService.getCurrentUser();
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        Optional<Like> existingLike = likeRepository.findByUserIdAndMediaId(currentUser.getId(), mediaId);

        boolean liked;
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            liked = false;
        } else {
            Like like = Like.builder()
                    .user(currentUser)
                    .media(media)
                    .build();
            likeRepository.save(like);
            liked = true;
            
            notificationService.createLikeNotification(media.getUser(), currentUser, media);
        }

        long likesCount = likeRepository.countByMediaId(mediaId);

        return LikeResponse.builder()
                .liked(liked)
                .likesCount(likesCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LikeResponse getLikeStatus(Long mediaId) {
        if (!mediaRepository.existsById(mediaId)) {
            throw new ResourceNotFoundException("Media not found with id: " + mediaId);
        }

        User currentUser = userService.getCurrentUser();
        boolean liked = likeRepository.existsByUserIdAndMediaId(currentUser.getId(), mediaId);
        long likesCount = likeRepository.countByMediaId(mediaId);

        return LikeResponse.builder()
                .liked(liked)
                .likesCount(likesCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserLikesResponse getUserLikes(Long userId, String cursor, int size) {
        userService.getUserById(userId);

        int pageSize = Math.max(1, Math.min(size, 50));
        int fetchSize = pageSize + 1;
        PageRequest pageRequest = PageRequest.of(0, fetchSize);

        CursorData cursorData = cursor == null || cursor.isBlank() ? null : decodeCursor(cursor);

        List<Like> likes = cursorData == null
                ? likeRepository.findUserLikesInitial(userId, pageRequest)
                : likeRepository.findUserLikesAfterCursor(
                        userId,
                        cursorData.createdAt(),
                        cursorData.id(),
                        pageRequest
                );

        boolean hasMore = likes.size() > pageSize;
        List<Like> pageLikes = hasMore ? likes.subList(0, pageSize) : likes;

        List<FeedItemResponse> items = pageLikes.stream()
                .map(Like::getMedia)
                .map(mediaMapper::toFeedItem)
                .toList();

        String nextCursor = null;
        if (hasMore && !pageLikes.isEmpty()) {
            Like lastLike = pageLikes.getLast();
            nextCursor = encodeCursor(lastLike.getCreatedAt(), lastLike.getId());
        }

        long totalCount = likeRepository.countByUserId(userId);

        return UserLikesResponse.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .totalCount(totalCount)
                .build();
    }

    private String encodeCursor(LocalDateTime createdAt, Long likeId) {
        String raw = createdAt.toString() + "|" + likeId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
    }

    private CursorData decodeCursor(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decoded.split("\\|");
            return new CursorData(
                    LocalDateTime.parse(parts[0]),
                    Long.parseLong(parts[1])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format");
        }
    }

    private record CursorData(LocalDateTime createdAt, Long id) {
    }
}
