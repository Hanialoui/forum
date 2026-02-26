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

    // Delete a message
    public void deleteMessage(Long messageId) {
        chatMessageRepository.deleteById(messageId);
    }
}
