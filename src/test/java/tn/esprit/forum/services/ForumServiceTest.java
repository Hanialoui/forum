package tn.esprit.forum.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.forum.entity.Forum;
import tn.esprit.forum.repository.ForumRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumServiceTest {

    @Mock
    private ForumRepository forumRepository;

    @InjectMocks
    private ForumService forumService;

    private Forum post;

    @BeforeEach
    void setUp() {
        post = new Forum();
        post.setId(1L);
        post.setContent("Test content");
        post.setUserId(10L);
        post.setTopicId(5L);
        post.setLikes(0);
        post.setReposts(0);
        post.setComments(0);
        post.setIsEdited(false);
    }

    // ── createPost ──

    @Test
    void createPost_savesAndReturnsPost() {
        when(forumRepository.save(post)).thenReturn(post);

        Forum result = forumService.createPost(post);

        assertThat(result).isEqualTo(post);
        verify(forumRepository).save(post);
    }

    // ── getAllPosts ──

    @Test
    void getAllPosts_returnsAllPosts() {
        when(forumRepository.findAll()).thenReturn(List.of(post));

        List<Forum> result = forumService.getAllPosts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Test content");
    }

    @Test
    void getAllPosts_emptyRepository_returnsEmptyList() {
        when(forumRepository.findAll()).thenReturn(List.of());

        assertThat(forumService.getAllPosts()).isEmpty();
    }

    // ── getPostById ──

    @Test
    void getPostById_existingId_returnsPost() {
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));

        Forum result = forumService.getPostById(1L);

        assertThat(result).isEqualTo(post);
    }

    @Test
    void getPostById_nonExistingId_throwsRuntimeException() {
        when(forumRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.getPostById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ── getTopLevelPosts ──

    @Test
    void getTopLevelPosts_returnsPostsWithNullParentId() {
        when(forumRepository.findByParentPostIdIsNullOrderByCreatedAtDesc()).thenReturn(List.of(post));

        List<Forum> result = forumService.getTopLevelPosts();

        assertThat(result).contains(post);
    }

    // ── getPostsByTopic ──

    @Test
    void getPostsByTopic_returnsMatchingPosts() {
        when(forumRepository.findByTopicId(5L)).thenReturn(List.of(post));

        List<Forum> result = forumService.getPostsByTopic(5L);

        assertThat(result).contains(post);
    }

    @Test
    void getPostsByTopic_noMatch_returnsEmptyList() {
        when(forumRepository.findByTopicId(99L)).thenReturn(List.of());

        assertThat(forumService.getPostsByTopic(99L)).isEmpty();
    }

    // ── getPostsByUser ──

    @Test
    void getPostsByUser_returnsPostsForGivenUser() {
        when(forumRepository.findByUserId(10L)).thenReturn(List.of(post));

        List<Forum> result = forumService.getPostsByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    // ── getReplies ──

    @Test
    void getReplies_returnsRepliesForPost() {
        Forum reply = new Forum();
        reply.setId(2L);
        reply.setContent("A reply");
        reply.setParentPostId(1L);

        when(forumRepository.findByParentPostId(1L)).thenReturn(List.of(reply));

        List<Forum> result = forumService.getReplies(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParentPostId()).isEqualTo(1L);
    }

    // ── updatePost ──

    @Test
    void updatePost_updatesAllFieldsAndSetsIsEdited() {
        Forum incoming = new Forum();
        incoming.setContent("Updated content");
        incoming.setTopicId(3L);
        incoming.setAuthor("Alice");
        incoming.setUsername("alice");
        incoming.setAvatar("avatar.png");
        incoming.setImage("img.jpg");

        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(any(Forum.class))).thenAnswer(inv -> inv.getArgument(0));

        Forum result = forumService.updatePost(1L, incoming);

        assertThat(result.getContent()).isEqualTo("Updated content");
        assertThat(result.getTopicId()).isEqualTo(3L);
        assertThat(result.getAuthor()).isEqualTo("Alice");
        assertThat(result.getIsEdited()).isTrue();
    }

    @Test
    void updatePost_nonExistingId_throwsException() {
        when(forumRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.updatePost(99L, new Forum()))
                .isInstanceOf(RuntimeException.class);
    }

    // ── deletePost ──

    @Test
    void deletePost_existingId_deletesSuccessfully() {
        when(forumRepository.existsById(1L)).thenReturn(true);

        forumService.deletePost(1L);

        verify(forumRepository).deleteById(1L);
    }

    @Test
    void deletePost_nonExistingId_throwsException() {
        when(forumRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> forumService.deletePost(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ── likePost ──

    @Test
    void likePost_incrementsLikesByOne() {
        post.setLikes(5);
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(post)).thenReturn(post);

        Forum result = forumService.likePost(1L);

        assertThat(result.getLikes()).isEqualTo(6);
    }

    @Test
    void likePost_fromZero_likesBecomesOne() {
        post.setLikes(0);
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(post)).thenReturn(post);

        Forum result = forumService.likePost(1L);

        assertThat(result.getLikes()).isEqualTo(1);
    }

    // ── unlikePost ──

    @Test
    void unlikePost_decrementsLikesByOne() {
        post.setLikes(3);
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(post)).thenReturn(post);

        Forum result = forumService.unlikePost(1L);

        assertThat(result.getLikes()).isEqualTo(2);
    }

    @Test
    void unlikePost_whenLikesIsZero_staysAtZero() {
        post.setLikes(0);
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(post)).thenReturn(post);

        Forum result = forumService.unlikePost(1L);

        assertThat(result.getLikes()).isEqualTo(0);
    }

    // ── repostPost ──

    @Test
    void repostPost_incrementsRepostsByOne() {
        post.setReposts(2);
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(post)).thenReturn(post);

        Forum result = forumService.repostPost(1L);

        assertThat(result.getReposts()).isEqualTo(3);
    }

    @Test
    void repostPost_savesUpdatedPost() {
        when(forumRepository.findById(1L)).thenReturn(Optional.of(post));
        when(forumRepository.save(post)).thenReturn(post);

        forumService.repostPost(1L);

        verify(forumRepository).save(post);
    }
}
