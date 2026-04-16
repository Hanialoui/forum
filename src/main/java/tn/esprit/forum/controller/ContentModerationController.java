package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.services.ContentModerationService;

import java.util.Map;

@RestController
@RequestMapping("/api/forums")
public class ContentModerationController {

    @Autowired
    private ContentModerationService moderationService;

    @PostMapping("/moderate-content")
    public ResponseEntity<Map<String, Object>> moderateContent(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        Map<String, Object> result = moderationService.moderateContent(content);
        return ResponseEntity.ok(result);
    }
}
