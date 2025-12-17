package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.Message;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt DESC")
    Page<Message> findByChatId(@Param("chatId") Long chatId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastMessageByChatId(@Param("chatId") Long chatId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId")
    long countByChatId(@Param("chatId") Long chatId);
}
