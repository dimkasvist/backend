package ru.dimkasvist.dimkasvist.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dimkasvist.dimkasvist.dto.CommentLikeResponse;
import ru.dimkasvist.dimkasvist.entity.Comment;
import ru.dimkasvist.dimkasvist.entity.CommentLike;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.repository.CommentLikeRepository;
import ru.dimkasvist.dimkasvist.repository.CommentRepository;
import ru.dimkasvist.dimkasvist.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceImplTest {

    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private CommentLikeServiceImpl commentLikeService;

    private User currentUser;
    private Comment comment;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(10L).build();
        comment = Comment.builder().id(5L).build();

        lenient().when(userService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void toggleLike_whenLikeDoesNotExist_createsLike() {
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        given(commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId()))
                .willReturn(Optional.empty());
        given(commentLikeRepository.countByCommentId(comment.getId())).willReturn(1L);

        CommentLikeResponse response = commentLikeService.toggleLike(comment.getId());

        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikesCount()).isEqualTo(1L);
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    void toggleLike_whenLikeExists_removesLike() {
        CommentLike existingLike = CommentLike.builder().id(7L).build();

        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        given(commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId()))
                .willReturn(Optional.of(existingLike));
        given(commentLikeRepository.countByCommentId(comment.getId())).willReturn(0L);

        CommentLikeResponse response = commentLikeService.toggleLike(comment.getId());

        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikesCount()).isZero();
        verify(commentLikeRepository).delete(existingLike);
    }

    @Test
    void toggleLike_whenCommentMissing_throws() {
        given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentLikeService.toggleLike(comment.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLikeStatus_returnsCurrentInfo() {
        given(commentRepository.existsById(comment.getId())).willReturn(true);
        given(commentLikeRepository.existsByUserIdAndCommentId(currentUser.getId(), comment.getId())).willReturn(true);
        given(commentLikeRepository.countByCommentId(comment.getId())).willReturn(3L);

        CommentLikeResponse response = commentLikeService.getLikeStatus(comment.getId());

        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikesCount()).isEqualTo(3L);
    }

    @Test
    void getLikeStatus_whenCommentMissing_throws() {
        given(commentRepository.existsById(comment.getId())).willReturn(false);

        assertThatThrownBy(() -> commentLikeService.getLikeStatus(comment.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLikeInfo_returnsAggregatedData() {
        Long commentId = comment.getId();
        given(commentLikeRepository.countByCommentIds(anyCollection())).willReturn(List.of(
                new CommentLikeRepository.CommentLikeCount() {
                    @Override
                    public Long getCommentId() {
                        return commentId;
                    }

                    @Override
                    public long getLikesCount() {
                        return 2L;
                    }
                }
        ));
        given(commentLikeRepository.findLikedCommentIds(eq(currentUser.getId()), anyCollection()))
                .willReturn(List.of(commentId));

        Map<Long, CommentLikeResponse> result = commentLikeService.getLikeInfo(Set.of(commentId));

        assertThat(result).containsEntry(commentId, CommentLikeResponse.builder()
                .likesCount(2L)
                .liked(true)
                .build());
    }
}
