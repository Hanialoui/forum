package tn.esprit.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.forum.entity.Forum;
import tn.esprit.forum.services.ForumService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForumController.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ForumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForumService forumService;

    @Autowired
    private ObjectMapper objectMapper;

    private Forum buildPost(Long id, String content, int likes, int reposts) {
        Forum f = new Forum();
        f.setId(id);
        f.setContent(content);
        f.setLikes(likes);
        f.setReposts(reposts);
        f.setComments(0);
        f.setIsEdited(false);
        return f;
    }

    // ── POST /create-forum ──

    @Test
    void createPost_returns200WithSavedForum() throws Exception {
        Forum saved = buildPost(1L, "Hello World", 0, 0);
        when(forumService.createPost(any(Forum.class))).thenReturn(saved);

        mockMvc.perform(post("/api/forums/create-forum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPost(null, "Hello World", 0, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Hello World"));
    }

    // ── GET /get-all-forums ──

    @Test
    void getAllPosts_returns200WithList() throws Exception {
        when(forumService.getAllPosts()).thenReturn(List.of(buildPost(1L, "Post 1", 0, 0)));

        mockMvc.perform(get("/api/forums/get-all-forums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("Post 1"));
    }

    @Test
    void getAllPosts_emptyList_returns200WithEmptyArray() throws Exception {
        when(forumService.getAllPosts()).thenReturn(List.of());

        mockMvc.perform(get("/api/forums/get-all-forums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /get-forum-by-id/{id} ──

    @Test
    void getPostById_existingId_returns200() throws Exception {
        when(forumService.getPostById(1L)).thenReturn(buildPost(1L, "Found it", 0, 0));

        mockMvc.perform(get("/api/forums/get-forum-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Found it"));
    }

    // ── GET /get-top-level-forums ──

    @Test
    void getTopLevelPosts_returns200WithList() throws Exception {
        when(forumService.getTopLevelPosts()).thenReturn(List.of(buildPost(1L, "Top post", 0, 0)));

        mockMvc.perform(get("/api/forums/get-top-level-forums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("Top post"));
    }

    // ── GET /get-forums-by-topic/{topicId} ──

    @Test
    void getPostsByTopic_returns200WithMatchingPosts() throws Exception {
        when(forumService.getPostsByTopic(5L)).thenReturn(List.of(buildPost(1L, "Topic post", 0, 0)));

        mockMvc.perform(get("/api/forums/get-forums-by-topic/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Topic post"));
    }

    // ── GET /get-forums-by-user/{userId} ──

    @Test
    void getPostsByUser_returns200WithUserPosts() throws Exception {
        when(forumService.getPostsByUser(10L)).thenReturn(List.of(buildPost(1L, "User post", 0, 0)));

        mockMvc.perform(get("/api/forums/get-forums-by-user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── GET /get-replies/{id} ──

    @Test
    void getReplies_returns200WithReplies() throws Exception {
        when(forumService.getReplies(1L)).thenReturn(List.of(buildPost(2L, "Reply", 0, 0)));

        mockMvc.perform(get("/api/forums/get-replies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Reply"));
    }

    // ── PUT /update-forum/{id} ──

    @Test
    void updatePost_returns200WithUpdatedForum() throws Exception {
        Forum updated = buildPost(1L, "Updated content", 0, 0);
        updated.setIsEdited(true);
        when(forumService.updatePost(eq(1L), any(Forum.class))).thenReturn(updated);

        mockMvc.perform(put("/api/forums/update-forum/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.isEdited").value(true));
    }

    // ── DELETE /delete-forum/{id} ──

    @Test
    void deletePost_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/forums/delete-forum/1"))
                .andExpect(status().isNoContent());

        verify(forumService).deletePost(1L);
    }

    // ── PUT /like-forum/{id} ──

    @Test
    void likePost_returns200WithIncrementedLikes() throws Exception {
        Forum liked = buildPost(1L, "Post", 6, 0);
        when(forumService.likePost(1L)).thenReturn(liked);

        mockMvc.perform(put("/api/forums/like-forum/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(6));
    }

    // ── PUT /unlike-forum/{id} ──

    @Test
    void unlikePost_returns200WithDecrementedLikes() throws Exception {
        Forum unliked = buildPost(1L, "Post", 4, 0);
        when(forumService.unlikePost(1L)).thenReturn(unliked);

        mockMvc.perform(put("/api/forums/unlike-forum/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(4));
    }

    // ── PUT /repost-forum/{id} ──

    @Test
    void repostPost_returns200WithIncrementedReposts() throws Exception {
        Forum reposted = buildPost(1L, "Post", 0, 3);
        when(forumService.repostPost(1L)).thenReturn(reposted);

        mockMvc.perform(put("/api/forums/repost-forum/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reposts").value(3));
    }
}
