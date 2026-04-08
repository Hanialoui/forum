package tn.esprit.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forum.services.AiCorrectionService;

import java.util.Map;

@RestController
@RequestMapping("/api/forums/ai")
public class AiCorrectionController {

    @Autowired
    private AiCorrectionService aiCorrectionService;

    @PostMapping("/correct-text")
    public ResponseEntity<?> correctText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
        }
        return ResponseEntity.ok(aiCorrectionService.correctText(text.trim()));
    }
}
