package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.CommentLike;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    @Query("""
            SELECT cl.comment.id AS commentId, COUNT(cl.id) AS likesCount
            FROM CommentLike cl
            WHERE cl.comment.id IN :commentIds
            GROUP BY cl.comment.id
            """)
    List<CommentLikeCount> countByCommentIds(@Param("commentIds") Collection<Long> commentIds);

    @Query("""
            SELECT cl.comment.id
            FROM CommentLike cl
            WHERE cl.comment.id IN :commentIds
              AND cl.user.id = :userId
            """)
    List<Long> findLikedCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") Collection<Long> commentIds
    );

    interface CommentLikeCount {
        Long getCommentId();
        long getLikesCount();
    }
}
