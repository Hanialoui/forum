package tn.esprit.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.forum.entity.Friendship;
import tn.esprit.forum.entity.FriendshipStatus;
import tn.esprit.forum.repository.FriendshipRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    // Send friend request
    public Friendship sendRequest(Friendship friendship) {
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(
                friendship.getUserId(), friendship.getFriendId());
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.REJECTED) {
                f.setStatus(FriendshipStatus.PENDING);
                f.setUserId(friendship.getUserId());
                f.setUserName(friendship.getUserName());
                f.setUserAvatar(friendship.getUserAvatar());
                f.setFriendId(friendship.getFriendId());
                f.setFriendName(friendship.getFriendName());
                f.setFriendAvatar(friendship.getFriendAvatar());
                return friendshipRepository.save(f);
            }
            return f;
        }
        friendship.setStatus(FriendshipStatus.PENDING);
        return friendshipRepository.save(friendship);
    }

    // Accept friend request
    public Friendship acceptRequest(Long friendshipId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));
        f.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(f);
    }

    // Reject friend request
    public Friendship rejectRequest(Long friendshipId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));
        f.setStatus(FriendshipStatus.REJECTED);
        return friendshipRepository.save(f);
    }

    // Remove friend
    public void removeFriend(Long friendshipId) {
        friendshipRepository.deleteById(friendshipId);
    }

    // Get accepted friends
    public List<Friendship> getAcceptedFriends(Long userId) {
        return friendshipRepository.findAcceptedFriends(userId);
    }

    // Get pending requests for user
    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findPendingRequestsForUser(userId);
    }

    // Get sent requests
    public List<Friendship> getSentRequests(Long userId) {
        return friendshipRepository.findPendingSentByUser(userId);
    }

    // Check friendship status between two users
    public Optional<Friendship> getFriendshipBetween(Long userId1, Long userId2) {
        return friendshipRepository.findFriendshipBetween(userId1, userId2);
    }

    // Block user
    public Friendship blockUser(Long friendshipId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));
        f.setStatus(FriendshipStatus.BLOCKED);
        return friendshipRepository.save(f);
    }
}
