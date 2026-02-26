package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.forum.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get conversation between two users ordered by time
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :uid1 AND m.receiverId = :uid2) OR (m.senderId = :uid2 AND m.receiverId = :uid1) ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(@Param("uid1") Long userId1, @Param("uid2") Long userId2);

    // Get unread messages for a user
    @Query("SELECT m FROM ChatMessage m WHERE m.receiverId = :uid AND m.isRead = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findUnreadMessages(@Param("uid") Long userId);

    // Get unread count from a specific sender
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    Long countUnreadFrom(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // Get last message between two users
    @Query(value = "SELECT * FROM chat_message WHERE (sender_id = :uid1 AND receiver_id = :uid2) OR (sender_id = :uid2 AND receiver_id = :uid1) ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    ChatMessage findLastMessage(@Param("uid1") Long userId1, @Param("uid2") Long userId2);

    // Get all conversations for a user (last message per friend)
    @Query("SELECT m FROM ChatMessage m WHERE m.senderId = :uid OR m.receiverId = :uid ORDER BY m.createdAt DESC")
    List<ChatMessage> findAllMessagesForUser(@Param("uid") Long userId);
}
