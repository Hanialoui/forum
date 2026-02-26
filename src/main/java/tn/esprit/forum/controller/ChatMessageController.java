package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.entity.ChatMessage;
import tn.esprit.forum.services.ChatMessageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forums")
@CrossOrigin(origins = "*")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/send-message")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        return ResponseEntity.ok(chatMessageService.sendMessage(message));
    }

    @GetMapping("/get-conversation/{userId1}/{userId2}")
    public ResponseEntity<List<ChatMessage>> getConversation(@PathVariable Long userId1, @PathVariable Long userId2) {
        return ResponseEntity.ok(chatMessageService.getConversation(userId1, userId2));
    }

    @GetMapping("/get-unread-messages/{userId}")
    public ResponseEntity<List<ChatMessage>> getUnreadMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(chatMessageService.getUnreadMessages(userId));
    }

    @GetMapping("/get-unread-message-count/{senderId}/{receiverId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long senderId, @PathVariable Long receiverId) {
        Long count = chatMessageService.getUnreadCount(senderId, receiverId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/mark-conversation-read/{senderId}/{receiverId}")
    public ResponseEntity<Void> markConversationRead(@PathVariable Long senderId, @PathVariable Long receiverId) {
        chatMessageService.markConversationRead(senderId, receiverId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-all-messages/{userId}")
    public ResponseEntity<List<ChatMessage>> getAllMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(chatMessageService.getAllMessagesForUser(userId));
    }

    @DeleteMapping("/delete-message/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        chatMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
