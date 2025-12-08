package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.CommentLikeResponse;
import ru.dimkasvist.dimkasvist.dto.CommentRequest;
import ru.dimkasvist.dimkasvist.dto.CommentResponse;
import ru.dimkasvist.dimkasvist.dto.CommentsResponse;
import ru.dimkasvist.dimkasvist.entity.Comment;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ForbiddenException;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.repository.CommentRepository;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.service.CommentLikeService;
import ru.dimkasvist.dimkasvist.service.CommentService;
import ru.dimkasvist.dimkasvist.service.UserService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MediaRepository mediaRepository;
    private final UserService userService;
    private final CommentLikeService commentLikeService;

    @Override
    @Transactional
    public CommentResponse addComment(Long mediaId, CommentRequest request) {
        User currentUser = userService.getCurrentUser();
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        Comment comment = Comment.builder()
                .text(request.getText())
                .user(currentUser)
                .media(media)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return toResponse(savedComment, CommentLikeResponse.builder()
                .likesCount(0)
                .liked(false)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentsResponse getComments(Long mediaId, String cursor, int size) {
        if (!mediaRepository.existsById(mediaId)) {
            throw new ResourceNotFoundException("Media not found with id: " + mediaId);
        }

        int fetchSize = size + 1;
        PageRequest pageRequest = PageRequest.of(0, fetchSize);

        List<Comment> comments;
        if (cursor == null || cursor.isBlank()) {
            comments = commentRepository.findByMediaIdInitial(mediaId, pageRequest);
        } else {
            CursorData cursorData = decodeCursor(cursor);
            comments = commentRepository.findByMediaIdAfterCursor(
                    mediaId,
                    cursorData.createdAt(),
                    cursorData.id(),
                    pageRequest
            );
        }

        boolean hasMore = comments.size() > size;
        if (hasMore) {
            comments = comments.subList(0, size);
        }

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        var likeInfo = commentLikeService.getLikeInfo(commentIds);

        List<CommentResponse> responses = comments.stream()
                .map(comment -> toResponse(comment, likeInfo.get(comment.getId())))
                .toList();

        String nextCursor = null;
        if (hasMore && !comments.isEmpty()) {
            Comment lastComment = comments.getLast();
            nextCursor = encodeCursor(lastComment.getCreatedAt(), lastComment.getId());
        }

        long totalCount = commentRepository.countByMediaId(mediaId);

        return CommentsResponse.builder()
                .comments(responses)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .totalCount(totalCount)
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User currentUser = userService.getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment, CommentLikeResponse likeInfo) {
        User user = comment.getUser();
        CommentLikeResponse effectiveLikeInfo = likeInfo == null
                ? CommentLikeResponse.builder().likesCount(0).liked(false).build()
                : likeInfo;
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(CommentResponse.AuthorInfo.builder()
                        .id(user.getId())
                        .displayName(user.getDisplayName())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .createdAt(comment.getCreatedAt())
                .likesCount(effectiveLikeInfo.getLikesCount())
                .liked(effectiveLikeInfo.isLiked())
                .build();
    }

    private record CursorData(LocalDateTime createdAt, Long id) {}

    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String raw = createdAt.toString() + "|" + id;
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
}
