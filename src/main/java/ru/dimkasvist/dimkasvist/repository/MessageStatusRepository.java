package ru.dimkasvist.dimkasvist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dimkasvist.dimkasvist.entity.MessageStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    Optional<MessageStatus> findByMessageIdAndUserId(Long messageId, Long userId);

    List<MessageStatus> findByMessageId(Long messageId);

    @Query("SELECT ms FROM MessageStatus ms WHERE ms.user.id = :userId AND ms.isRead = false")
    List<MessageStatus> findUnreadByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ms) FROM MessageStatus ms WHERE ms.user.id = :userId AND ms.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE MessageStatus ms SET ms.isRead = true, ms.readAt = CURRENT_TIMESTAMP WHERE ms.message.chat.id = :chatId AND ms.user.id = :userId AND ms.isRead = false")
    int markAllAsReadInChat(@Param("chatId") Long chatId, @Param("userId") Long userId);
}
