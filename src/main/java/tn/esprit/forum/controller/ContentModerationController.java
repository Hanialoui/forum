package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.entity.ContentWarning;
import tn.esprit.forum.services.ContentModerationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forums")
public class ContentModerationController {

    @Autowired
    private ContentModerationService moderationService;

    // ── Moderate post content before publishing ──
    @PostMapping("/moderate-content")
    public ResponseEntity<Map<String, Object>> moderateContent(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        Long userId = request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null;
        String userName = (String) request.get("userName");
        String userEmail = (String) request.get("userEmail");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Content is required"));
        }
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }

        Map<String, Object> result = moderationService.moderatePost(content.trim(), userId, userName, userEmail);
        return ResponseEntity.ok(result);
    }

    // ── Admin: Get all content warnings ──
    @GetMapping("/content-warnings")
    public ResponseEntity<List<ContentWarning>> getAllWarnings() {
        return ResponseEntity.ok(moderationService.getAllWarnings());
    }

    // ── Admin: Get warnings by user ──
    @GetMapping("/content-warnings/user/{userId}")
    public ResponseEntity<List<ContentWarning>> getWarningsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(moderationService.getWarningsByUser(userId));
    }

    // ── Admin: Get warnings by status ──
    @GetMapping("/content-warnings/status/{status}")
    public ResponseEntity<List<ContentWarning>> getWarningsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(moderationService.getWarningsByStatus(status));
    }

    // ── Admin: Update warning status ──
    @PutMapping("/content-warnings/{id}")
    public ResponseEntity<ContentWarning> updateWarning(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String adminNote = body.get("adminNote");
        return ResponseEntity.ok(moderationService.updateWarningStatus(id, status, adminNote));
    }

    // ── Admin: Delete warning ──
    @DeleteMapping("/content-warnings/{id}")
    public ResponseEntity<Void> deleteWarning(@PathVariable Long id) {
        moderationService.deleteWarning(id);
        return ResponseEntity.noContent().build();
    }
}
