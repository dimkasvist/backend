package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.BoardMedia;

import java.util.Optional;

@Repository
public interface BoardMediaRepository extends JpaRepository<BoardMedia, Long> {

    @Query("SELECT bm FROM BoardMedia bm WHERE bm.board.id = :boardId ORDER BY bm.addedAt DESC")
    Page<BoardMedia> findByBoardId(@Param("boardId") Long boardId, Pageable pageable);

    @Query("SELECT bm FROM BoardMedia bm WHERE bm.board.id = :boardId AND bm.media.id = :mediaId")
    Optional<BoardMedia> findByBoardIdAndMediaId(@Param("boardId") Long boardId, @Param("mediaId") Long mediaId);

    @Query("SELECT COUNT(bm) FROM BoardMedia bm WHERE bm.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);

    boolean existsByBoardIdAndMediaId(Long boardId, Long mediaId);
}
