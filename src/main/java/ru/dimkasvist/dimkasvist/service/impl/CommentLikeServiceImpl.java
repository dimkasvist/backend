package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.CommentLikeResponse;
import ru.dimkasvist.dimkasvist.entity.Comment;
import ru.dimkasvist.dimkasvist.entity.CommentLike;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.repository.CommentLikeRepository;
import ru.dimkasvist.dimkasvist.repository.CommentRepository;
import ru.dimkasvist.dimkasvist.security.GoogleUserPrincipal;
import ru.dimkasvist.dimkasvist.service.CommentLikeService;
import ru.dimkasvist.dimkasvist.service.NotificationService;
import ru.dimkasvist.dimkasvist.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentLikeResponse toggleLike(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User currentUser = userService.getCurrentUser();
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId);

        boolean liked;
        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            CommentLike like = CommentLike.builder()
                    .comment(comment)
                    .user(currentUser)
                    .build();
            commentLikeRepository.save(like);
            liked = true;
            
            notificationService.createCommentLikeNotification(comment.getUser(), currentUser, comment);
        }

        long likesCount = commentLikeRepository.countByCommentId(commentId);

        return CommentLikeResponse.builder()
                .liked(liked)
                .likesCount(likesCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentLikeResponse getLikeStatus(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        User currentUser = userService.getCurrentUser();
        boolean liked = commentLikeRepository.existsByUserIdAndCommentId(currentUser.getId(), commentId);
        long likesCount = commentLikeRepository.countByCommentId(commentId);

        return CommentLikeResponse.builder()
                .liked(liked)
                .likesCount(likesCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, CommentLikeResponse> getLikeInfo(Collection<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = commentLikeRepository.countByCommentIds(commentIds).stream()
                .collect(Collectors.toMap(
                        CommentLikeRepository.CommentLikeCount::getCommentId,
                        CommentLikeRepository.CommentLikeCount::getLikesCount
                ));

        Set<Long> likedCommentIds = Set.of();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof GoogleUserPrincipal) {
            User currentUser = userService.getCurrentUser();
            likedCommentIds = commentLikeRepository.findLikedCommentIds(currentUser.getId(), commentIds).stream()
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalLikedCommentIds = likedCommentIds;
        return commentIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> CommentLikeResponse.builder()
                                .likesCount(counts.getOrDefault(id, 0L))
                                .liked(finalLikedCommentIds.contains(id))
                                .build()
                ));
    }
}
