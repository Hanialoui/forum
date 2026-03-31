package tn.esprit.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.forum.entity.UserStatus;
import tn.esprit.forum.repository.UserStatusRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserStatusService {

    @Autowired
    private UserStatusRepository userStatusRepository;

    // Set user online (heartbeat)
    public UserStatus setOnline(UserStatus status) {
        Optional<UserStatus> existing = userStatusRepository.findById(status.getUserId());
        if (existing.isPresent()) {
            UserStatus s = existing.get();
            s.setIsOnline(true);
            s.setLastSeen(LocalDateTime.now());
            if (status.getUserName() != null) s.setUserName(status.getUserName());
            if (status.getUserAvatar() != null) s.setUserAvatar(status.getUserAvatar());
            return userStatusRepository.save(s);
        }
        status.setIsOnline(true);
        status.setLastSeen(LocalDateTime.now());
        return userStatusRepository.save(status);
    }

    // Set user offline
    public UserStatus setOffline(Long userId) {
        Optional<UserStatus> existing = userStatusRepository.findById(userId);
        if (existing.isPresent()) {
            UserStatus s = existing.get();
            s.setIsOnline(false);
            s.setLastSeen(LocalDateTime.now());
            return userStatusRepository.save(s);
        }
        return null;
    }

    // Get status for a user
    public UserStatus getStatus(Long userId) {
        return userStatusRepository.findById(userId).orElse(null);
    }

    // Get statuses for multiple users
    public List<UserStatus> getStatuses(List<Long> userIds) {
        return userStatusRepository.findByUserIds(userIds);
    }

    // Get all online users
    public List<UserStatus> getOnlineUsers() {
        return userStatusRepository.findOnlineUsers();
    }

    // Set typing indicator
    public UserStatus setTyping(Long userId, Long typingToUserId) {
        Optional<UserStatus> existing = userStatusRepository.findById(userId);
        if (existing.isPresent()) {
            UserStatus s = existing.get();
            s.setTypingToUserId(typingToUserId);
            s.setTypingStartedAt(LocalDateTime.now());
            return userStatusRepository.save(s);
        }
        return null;
    }

    // Clear typing indicator
    public UserStatus clearTyping(Long userId) {
        Optional<UserStatus> existing = userStatusRepository.findById(userId);
        if (existing.isPresent()) {
            UserStatus s = existing.get();
            s.setTypingToUserId(null);
            s.setTypingStartedAt(null);
            return userStatusRepository.save(s);
        }
        return null;
    }

    // Check if a user is typing to another user
    public boolean isTypingTo(Long typerId, Long receiverId) {
        Optional<UserStatus> existing = userStatusRepository.findById(typerId);
        if (existing.isPresent()) {
            UserStatus s = existing.get();
            if (s.getTypingToUserId() != null && s.getTypingToUserId().equals(receiverId)) {
                // Auto-expire typing after 5 seconds
                if (s.getTypingStartedAt() != null && s.getTypingStartedAt().isAfter(LocalDateTime.now().minusSeconds(5))) {
                    return true;
                }
                // Expired, clear it
                s.setTypingToUserId(null);
                s.setTypingStartedAt(null);
                userStatusRepository.save(s);
            }
        }
        return false;
    }
}
