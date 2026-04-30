package tn.esprit.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.forum.entity.Friendship;
import tn.esprit.forum.entity.FriendshipStatus;
import tn.esprit.forum.services.FriendshipService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendshipController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendshipService friendshipService;

    @Autowired
    private ObjectMapper objectMapper;

    private Friendship buildFriendship(Long id, FriendshipStatus status) {
        return Friendship.builder()
                .id(id)
                .userId(10L)
                .userName("Alice")
                .userAvatar("a.png")
                .friendId(20L)
                .friendName("Bob")
                .friendAvatar("b.png")
                .status(status)
                .build();
    }

    // ── POST /send-friend-request ──

    @Test
    void sendRequest_returns200WithSavedFriendship() throws Exception {
        Friendship pending = buildFriendship(1L, FriendshipStatus.PENDING);
        when(friendshipService.sendRequest(any(Friendship.class))).thenReturn(pending);

        mockMvc.perform(post("/api/forums/send-friend-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pending)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void sendRequest_serviceThrowsException_returns400WithError() throws Exception {
        when(friendshipService.sendRequest(any(Friendship.class)))
                .thenThrow(new RuntimeException("Already friends"));

        mockMvc.perform(post("/api/forums/send-friend-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildFriendship(null, FriendshipStatus.PENDING))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Already friends"));
    }

    // ── PUT /accept-friend-request/{id} ──

    @Test
    void acceptRequest_returns200WithAcceptedFriendship() throws Exception {
        Friendship accepted = buildFriendship(1L, FriendshipStatus.ACCEPTED);
        when(friendshipService.acceptRequest(1L)).thenReturn(accepted);

        mockMvc.perform(put("/api/forums/accept-friend-request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void acceptRequest_notFound_returns400WithError() throws Exception {
        when(friendshipService.acceptRequest(99L))
                .thenThrow(new RuntimeException("Friendship not found"));

        mockMvc.perform(put("/api/forums/accept-friend-request/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Friendship not found"));
    }

    // ── PUT /reject-friend-request/{id} ──

    @Test
    void rejectRequest_returns200WithRejectedFriendship() throws Exception {
        Friendship rejected = buildFriendship(1L, FriendshipStatus.REJECTED);
        when(friendshipService.rejectRequest(1L)).thenReturn(rejected);

        mockMvc.perform(put("/api/forums/reject-friend-request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void rejectRequest_notFound_returns400WithError() throws Exception {
        when(friendshipService.rejectRequest(99L))
                .thenThrow(new RuntimeException("Friendship not found"));

        mockMvc.perform(put("/api/forums/reject-friend-request/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Friendship not found"));
    }

    // ── DELETE /remove-friend/{id} ──

    @Test
    void removeFriend_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/forums/remove-friend/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFriend_serviceThrowsException_returns400() throws Exception {
        doThrow(new RuntimeException("Not found")).when(friendshipService).removeFriend(99L);

        mockMvc.perform(delete("/api/forums/remove-friend/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Not found"));
    }

    // ── GET /get-friends/{userId} ──

    @Test
    void getAcceptedFriends_returns200WithList() throws Exception {
        Friendship accepted = buildFriendship(1L, FriendshipStatus.ACCEPTED);
        when(friendshipService.getAcceptedFriends(10L)).thenReturn(List.of(accepted));

        mockMvc.perform(get("/api/forums/get-friends/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    // ── GET /get-pending-requests/{userId} ──

    @Test
    void getPendingRequests_returns200WithList() throws Exception {
        when(friendshipService.getPendingRequests(10L))
                .thenReturn(List.of(buildFriendship(1L, FriendshipStatus.PENDING)));

        mockMvc.perform(get("/api/forums/get-pending-requests/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    // ── GET /get-sent-requests/{userId} ──

    @Test
    void getSentRequests_returns200WithList() throws Exception {
        when(friendshipService.getSentRequests(10L))
                .thenReturn(List.of(buildFriendship(1L, FriendshipStatus.PENDING)));

        mockMvc.perform(get("/api/forums/get-sent-requests/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET /get-friendship-status/{userId1}/{userId2} ──

    @Test
    void getFriendshipStatus_found_returns200() throws Exception {
        Friendship f = buildFriendship(1L, FriendshipStatus.ACCEPTED);
        when(friendshipService.getFriendshipBetween(10L, 20L)).thenReturn(Optional.of(f));

        mockMvc.perform(get("/api/forums/get-friendship-status/10/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void getFriendshipStatus_notFound_returns404() throws Exception {
        when(friendshipService.getFriendshipBetween(10L, 99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/forums/get-friendship-status/10/99"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /block-user/{id} ──

    @Test
    void blockUser_returns200WithBlockedFriendship() throws Exception {
        Friendship blocked = buildFriendship(1L, FriendshipStatus.BLOCKED);
        when(friendshipService.blockUser(1L)).thenReturn(blocked);

        mockMvc.perform(put("/api/forums/block-user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void blockUser_notFound_returns400() throws Exception {
        when(friendshipService.blockUser(99L))
                .thenThrow(new RuntimeException("Friendship not found"));

        mockMvc.perform(put("/api/forums/block-user/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Friendship not found"));
    }

    // ── PUT /unblock-user/{id} ──

    @Test
    void unblockUser_returns200WithAcceptedFriendship() throws Exception {
        Friendship unblocked = buildFriendship(1L, FriendshipStatus.ACCEPTED);
        when(friendshipService.unblockUser(1L)).thenReturn(unblocked);

        mockMvc.perform(put("/api/forums/unblock-user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    // ── GET /get-blocked-users/{userId} ──

    @Test
    void getBlockedUsers_returns200WithList() throws Exception {
        when(friendshipService.getBlockedUsers(10L))
                .thenReturn(List.of(buildFriendship(1L, FriendshipStatus.BLOCKED)));

        mockMvc.perform(get("/api/forums/get-blocked-users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("BLOCKED"));
    }

    // ── GET /get-mutual-friends-count/{userId1}/{userId2} ──

    @Test
    void getMutualFriendsCount_returns200WithCount() throws Exception {
        when(friendshipService.getMutualFriendsCount(10L, 20L)).thenReturn(3);

        mockMvc.perform(get("/api/forums/get-mutual-friends-count/10/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void getMutualFriendsCount_noMutuals_returns200WithZero() throws Exception {
        when(friendshipService.getMutualFriendsCount(10L, 20L)).thenReturn(0);

        mockMvc.perform(get("/api/forums/get-mutual-friends-count/10/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
}
