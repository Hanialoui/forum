package tn.esprit.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.forum.entity.ChatMessage;
import tn.esprit.forum.repository.ChatMessageRepository;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // Send a message
    public ChatMessage sendMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    // Get conversation between two users
    public List<ChatMessage> getConversation(Long userId1, Long userId2) {
        return chatMessageRepository.findConversation(userId1, userId2);
    }

    // Get unread messages for user
    public List<ChatMessage> getUnreadMessages(Long userId) {
        return chatMessageRepository.findUnreadMessages(userId);
    }

    // Get unread count from specific sender
    public Long getUnreadCount(Long senderId, Long receiverId) {
        return chatMessageRepository.countUnreadFrom(senderId, receiverId);
    }

    // Mark messages as read
    public void markConversationRead(Long senderId, Long receiverId) {
        List<ChatMessage> messages = chatMessageRepository.findConversation(senderId, receiverId);
        for (ChatMessage msg : messages) {
            if (msg.getReceiverId().equals(receiverId) && !msg.getIsRead()) {
                msg.setIsRead(true);
                chatMessageRepository.save(msg);
            }
        }
    }

    // Get last message between two users
    public ChatMessage getLastMessage(Long userId1, Long userId2) {
        return chatMessageRepository.findLastMessage(userId1, userId2);
    }

    // Get all messages for a user
    public List<ChatMessage> getAllMessagesForUser(Long userId) {
        return chatMessageRepository.findAllMessagesForUser(userId);
    }

    // Delete a message (for everyone)
    public void deleteMessage(Long messageId) {
        chatMessageRepository.deleteById(messageId);
    }

    // Delete a message for a specific user (soft delete)
    public ChatMessage deleteMessageForUser(Long messageId, Long userId) {
        ChatMessage msg = chatMessageRepository.findById(messageId).orElse(null);
        if (msg == null) return null;
        String deleted = msg.getDeletedForUsers();
        if (deleted == null || deleted.isEmpty()) {
            deleted = userId.toString();
        } else {
            deleted = deleted + "," + userId;
        }
        msg.setDeletedForUsers(deleted);
        return chatMessageRepository.save(msg);
    }

    // Add/toggle reaction on a message
    public ChatMessage toggleReaction(Long messageId, Long userId, String emoji) {
        ChatMessage msg = chatMessageRepository.findById(messageId).orElse(null);
        if (msg == null) return null;
        String key = userId + ":" + emoji;
        String reactions = msg.getReactions();
        if (reactions == null || reactions.isEmpty()) {
            reactions = key;
        } else if (reactions.contains(key)) {
            // Remove reaction
            reactions = reactions.replace(key, "").replace(",,", ",");
            if (reactions.startsWith(",")) reactions = reactions.substring(1);
            if (reactions.endsWith(",")) reactions = reactions.substring(0, reactions.length() - 1);
        } else {
            reactions = reactions + "," + key;
        }
        msg.setReactions(reactions.isEmpty() ? null : reactions);
        return chatMessageRepository.save(msg);
    }
}
