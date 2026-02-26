package tn.esprit.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.forum.entity.Forum;
import tn.esprit.forum.repository.ForumRepository;

import java.util.List;

@Service
public class ForumService {

    @Autowired
    private ForumRepository forumRepository;

    // ── CREATE ──
    public Forum createPost(Forum post) {
        return forumRepository.save(post);
    }

    // ── READ ──
    public List<Forum> getAllPosts() {
        return forumRepository.findAll();
    }

    public Forum getPostById(Long id) {
        return forumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public List<Forum> getTopLevelPosts() {
        return forumRepository.findByParentPostIdIsNullOrderByCreatedAtDesc();
    }

    public List<Forum> getPostsByTopic(Long topicId) {
        return forumRepository.findByTopicId(topicId);
    }

    public List<Forum> getPostsByUser(Long userId) {
        return forumRepository.findByUserId(userId);
    }

    public List<Forum> getReplies(Long parentPostId) {
        return forumRepository.findByParentPostId(parentPostId);
    }

    // ── UPDATE ──
    public Forum updatePost(Long id, Forum updatedPost) {
        Forum existing = getPostById(id);

        existing.setTopicId(updatedPost.getTopicId());
        existing.setContent(updatedPost.getContent());
        existing.setImage(updatedPost.getImage());
        existing.setAuthor(updatedPost.getAuthor());
        existing.setUsername(updatedPost.getUsername());
        existing.setAvatar(updatedPost.getAvatar());
        existing.setIsEdited(true);

        return forumRepository.save(existing);
    }

    // ── DELETE ──
    public void deletePost(Long id) {
        if (!forumRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        forumRepository.deleteById(id);
    }

    // ── INTERACTIONS ──
    public Forum likePost(Long id) {
        Forum post = getPostById(id);
        post.setLikes(post.getLikes() + 1);
        return forumRepository.save(post);
    }

    public Forum repostPost(Long id) {
        Forum post = getPostById(id);
        post.setReposts(post.getReposts() + 1);
        return forumRepository.save(post);
    }
}
