package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.Board;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b WHERE b.user.id = :userId ORDER BY b.updatedAt DESC")
    Page<Board> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Board b WHERE b.isPrivate = false ORDER BY b.updatedAt DESC")
    Page<Board> findPublicBoards(Pageable pageable);

    @Query("SELECT b FROM Board b WHERE b.id = :id AND (b.isPrivate = false OR b.user.id = :userId)")
    Optional<Board> findByIdAndAccessible(@Param("id") Long id, @Param("userId") Long userId);
}
