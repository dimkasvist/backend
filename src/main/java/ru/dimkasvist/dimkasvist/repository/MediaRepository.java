package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.Media;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("""
        SELECT m FROM Media m
        WHERE (m.createdAt < :cursor OR (m.createdAt = :cursor AND m.id < :cursorId))
        ORDER BY m.createdAt DESC, m.id DESC
        """)
    List<Media> findFeedAfterCursor(
            @Param("cursor") LocalDateTime cursor,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT m FROM Media m ORDER BY m.createdAt DESC, m.id DESC")
    List<Media> findFeedInitial(Pageable pageable);

    @Query("""
        SELECT DISTINCT m FROM Media m
        LEFT JOIN m.tags t
        WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY m.createdAt DESC, m.id DESC
        """)
    List<Media> searchMedia(@Param("query") String query, Pageable pageable);
}
