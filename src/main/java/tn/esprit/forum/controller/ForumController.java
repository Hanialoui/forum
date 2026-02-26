package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.entity.Forum;
import tn.esprit.forum.services.ForumService;

import java.util.List;

@RestController
@RequestMapping("/api/forums")
public class ForumController {

    @Autowired
    private ForumService forumService;

    // ── CREATE ──
    @PostMapping("/create-forum")
    public ResponseEntity<Forum> createPost(@RequestBody Forum post) {
        return ResponseEntity.ok(forumService.createPost(post));
    }

    // ── READ ──
    @GetMapping("/get-all-forums")
    public ResponseEntity<List<Forum>> getAllPosts() {
        return ResponseEntity.ok(forumService.getAllPosts());
    }

    @GetMapping("/get-forum-by-id/{id}")
    public ResponseEntity<Forum> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getPostById(id));
    }

    @GetMapping("/get-top-level-forums")
    public ResponseEntity<List<Forum>> getTopLevelPosts() {
        return ResponseEntity.ok(forumService.getTopLevelPosts());
    }

    @GetMapping("/get-forums-by-topic/{topicId}")
    public ResponseEntity<List<Forum>> getPostsByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(forumService.getPostsByTopic(topicId));
    }

    @GetMapping("/get-forums-by-user/{userId}")
    public ResponseEntity<List<Forum>> getPostsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(forumService.getPostsByUser(userId));
    }

    @GetMapping("/get-replies/{id}")
    public ResponseEntity<List<Forum>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getReplies(id));
    }

    // ── UPDATE ──
    @PutMapping("/update-forum/{id}")
    public ResponseEntity<Forum> updatePost(@PathVariable Long id, @RequestBody Forum post) {
        return ResponseEntity.ok(forumService.updatePost(id, post));
    }

    // ── DELETE ──
    @DeleteMapping("/delete-forum/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        forumService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // ── INTERACTIONS ──
    @PutMapping("/like-forum/{id}")
    public ResponseEntity<Forum> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.likePost(id));
    }

    @PutMapping("/repost-forum/{id}")
    public ResponseEntity<Forum> repostPost(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.repostPost(id));
    }
}
