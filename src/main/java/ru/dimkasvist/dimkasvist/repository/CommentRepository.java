package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByMediaId(Long mediaId);

    @Query("SELECT c FROM Comment c WHERE c.media.id = :mediaId ORDER BY c.createdAt DESC")
    List<Comment> findByMediaIdInitial(@Param("mediaId") Long mediaId, Pageable pageable);

    @Query("""
        SELECT c FROM Comment c 
        WHERE c.media.id = :mediaId 
        AND (c.createdAt < :cursor OR (c.createdAt = :cursor AND c.id < :cursorId))
        ORDER BY c.createdAt DESC, c.id DESC
        """)
    List<Comment> findByMediaIdAfterCursor(
            @Param("mediaId") Long mediaId,
            @Param("cursor") LocalDateTime cursor,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
