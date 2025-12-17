package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.Chat;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE (c.user1.id = :userId1 AND c.user2.id = :userId2) OR (c.user1.id = :userId2 AND c.user2.id = :userId1)")
    Optional<Chat> findByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT c FROM Chat c WHERE c.user1.id = :userId OR c.user2.id = :userId ORDER BY c.updatedAt DESC")
    Page<Chat> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Chat c WHERE (c.user1.id = :userId1 AND c.user2.id = :userId2) OR (c.user1.id = :userId2 AND c.user2.id = :userId1)")
    boolean existsByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
