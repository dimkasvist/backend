package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.Like;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndMediaId(Long userId, Long mediaId);

    boolean existsByUserIdAndMediaId(Long userId, Long mediaId);

    long countByMediaId(Long mediaId);

    long countByUserId(Long userId);

    void deleteByUserIdAndMediaId(Long userId, Long mediaId);

    @Query("""
            SELECT l FROM Like l
            JOIN FETCH l.media m
            LEFT JOIN FETCH m.user
            WHERE l.user.id = :userId
            ORDER BY l.createdAt DESC, l.id DESC
            """)
    List<Like> findUserLikesInitial(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT l FROM Like l
            JOIN FETCH l.media m
            LEFT JOIN FETCH m.user
            WHERE l.user.id = :userId
              AND (l.createdAt < :cursorCreatedAt OR (l.createdAt = :cursorCreatedAt AND l.id < :cursorId))
            ORDER BY l.createdAt DESC, l.id DESC
            """)
    List<Like> findUserLikesAfterCursor(
            @Param("userId") Long userId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
