package tn.esprit.forum.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.forum.entity.Friendship;
import tn.esprit.forum.entity.FriendshipStatus;
import tn.esprit.forum.repository.FriendshipRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    private Friendship friendship;

    @BeforeEach
    void setUp() {
        friendship = Friendship.builder()
                .id(1L)
                .userId(10L)
                .userName("Alice")
                .userAvatar("avatar_alice.png")
                .friendId(20L)
                .friendName("Bob")
                .friendAvatar("avatar_bob.png")
                .status(FriendshipStatus.PENDING)
                .build();
    }

    // ── sendRequest ──

    @Test
    void sendRequest_newRequest_setsPendingAndSaves() {
        when(friendshipRepository.findFriendshipBetween(10L, 20L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship result = friendshipService.sendRequest(friendship);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void sendRequest_existingPendingRequest_returnsExistingWithoutSaving() {
        when(friendshipRepository.findFriendshipBetween(10L, 20L)).thenReturn(Optional.of(friendship));

        Friendship result = friendshipService.sendRequest(friendship);

        assertThat(result).isEqualTo(friendship);
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequest_previouslyRejected_resetsStatusToPending() {
        friendship.setStatus(FriendshipStatus.REJECTED);
        when(friendshipRepository.findFriendshipBetween(10L, 20L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship incoming = Friendship.builder()
                .userId(10L).userName("Alice").userAvatar("a.png")
                .friendId(20L).friendName("Bob").friendAvatar("b.png")
                .build();

        Friendship result = friendshipService.sendRequest(incoming);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void sendRequest_existingAccepted_returnsExistingWithoutSaving() {
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findFriendshipBetween(10L, 20L)).thenReturn(Optional.of(friendship));

        Friendship result = friendshipService.sendRequest(friendship);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository, never()).save(any());
    }

    // ── acceptRequest ──

    @Test
    void acceptRequest_setsStatusToAccepted() {
        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship result = friendshipService.acceptRequest(1L);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void acceptRequest_notFound_throwsRuntimeException() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.acceptRequest(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Friendship not found");
    }

    // ── rejectRequest ──

    @Test
    void rejectRequest_setsStatusToRejected() {
        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship result = friendshipService.rejectRequest(1L);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.REJECTED);
    }

    @Test
    void rejectRequest_notFound_throwsRuntimeException() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.rejectRequest(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Friendship not found");
    }

    // ── removeFriend ──

    @Test
    void removeFriend_callsDeleteById() {
        friendshipService.removeFriend(1L);

        verify(friendshipRepository).deleteById(1L);
    }

    // ── getAcceptedFriends ──

    @Test
    void getAcceptedFriends_returnsListFromRepository() {
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findAcceptedFriends(10L)).thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getAcceptedFriends(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void getAcceptedFriends_noFriends_returnsEmptyList() {
        when(friendshipRepository.findAcceptedFriends(10L)).thenReturn(List.of());

        assertThat(friendshipService.getAcceptedFriends(10L)).isEmpty();
    }

    // ── getPendingRequests ──

    @Test
    void getPendingRequests_returnsPendingRequestsForUser() {
        when(friendshipRepository.findPendingRequestsForUser(10L)).thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getPendingRequests(10L);

        assertThat(result).contains(friendship);
    }

    // ── getSentRequests ──

    @Test
    void getSentRequests_returnsSentRequestsByUser() {
        when(friendshipRepository.findPendingSentByUser(10L)).thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getSentRequests(10L);

        assertThat(result).contains(friendship);
    }

    // ── getFriendshipBetween ──

    @Test
    void getFriendshipBetween_existingPair_returnsOptionalWithFriendship() {
        when(friendshipRepository.findFriendshipBetween(10L, 20L)).thenReturn(Optional.of(friendship));

        Optional<Friendship> result = friendshipService.getFriendshipBetween(10L, 20L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(friendship);
    }

    @Test
    void getFriendshipBetween_nonExistingPair_returnsEmptyOptional() {
        when(friendshipRepository.findFriendshipBetween(10L, 99L)).thenReturn(Optional.empty());

        Optional<Friendship> result = friendshipService.getFriendshipBetween(10L, 99L);

        assertThat(result).isEmpty();
    }

    // ── blockUser ──

    @Test
    void blockUser_setsStatusToBlocked() {
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship result = friendshipService.blockUser(1L);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.BLOCKED);
    }

    @Test
    void blockUser_notFound_throwsRuntimeException() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.blockUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Friendship not found");
    }

    // ── unblockUser ──

    @Test
    void unblockUser_setsStatusToAccepted() {
        friendship.setStatus(FriendshipStatus.BLOCKED);
        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship result = friendshipService.unblockUser(1L);

        assertThat(result.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void unblockUser_notFound_throwsRuntimeException() {
        when(friendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendshipService.unblockUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Friendship not found");
    }

    // ── getBlockedUsers ──

    @Test
    void getBlockedUsers_returnsBlockedUsersFromRepository() {
        friendship.setStatus(FriendshipStatus.BLOCKED);
        when(friendshipRepository.findByUserAndStatus(10L, FriendshipStatus.BLOCKED))
                .thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getBlockedUsers(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(FriendshipStatus.BLOCKED);
    }

    // ── getMutualFriendsCount ──

    @Test
    void getMutualFriendsCount_noMutualFriends_returnsZero() {
        // User 10 is friends with 30; User 20 is friends with 40 — no overlap
        Friendship f1 = Friendship.builder().userId(10L).friendId(30L).build();
        Friendship f2 = Friendship.builder().userId(20L).friendId(40L).build();

        when(friendshipRepository.findAcceptedFriends(10L)).thenReturn(List.of(f1));
        when(friendshipRepository.findAcceptedFriends(20L)).thenReturn(List.of(f2));

        int count = friendshipService.getMutualFriendsCount(10L, 20L);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void getMutualFriendsCount_oneMutualFriend_returnsOne() {
        // User 10 is friends with 30; User 20 is also friends with 30
        Friendship f1 = Friendship.builder().userId(10L).friendId(30L).build();
        Friendship f2 = Friendship.builder().userId(20L).friendId(30L).build();

        when(friendshipRepository.findAcceptedFriends(10L)).thenReturn(List.of(f1));
        when(friendshipRepository.findAcceptedFriends(20L)).thenReturn(List.of(f2));

        int count = friendshipService.getMutualFriendsCount(10L, 20L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void getMutualFriendsCount_bidirectionalFriendship_countedCorrectly() {
        // User 30 is friends with user 10 (reversed direction) and user 20 is friends with user 30
        Friendship f1 = Friendship.builder().userId(30L).friendId(10L).build();
        Friendship f2 = Friendship.builder().userId(20L).friendId(30L).build();

        when(friendshipRepository.findAcceptedFriends(10L)).thenReturn(List.of(f1));
        when(friendshipRepository.findAcceptedFriends(20L)).thenReturn(List.of(f2));

        int count = friendshipService.getMutualFriendsCount(10L, 20L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void getMutualFriendsCount_emptyFriendLists_returnsZero() {
        when(friendshipRepository.findAcceptedFriends(10L)).thenReturn(List.of());
        when(friendshipRepository.findAcceptedFriends(20L)).thenReturn(List.of());

        int count = friendshipService.getMutualFriendsCount(10L, 20L);

        assertThat(count).isEqualTo(0);
    }
}
