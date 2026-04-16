package tn.esprit.forum.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ContentModerationService {

    @Value("${ollama.url:http://localhost:11434/api/chat}")
    private String ollamaUrl;

    @Value("${ollama.model:qwen2.5:3b}")
    private String ollamaModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    public Map<String, Object> moderateContent(String content) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);

        if (content == null || content.isBlank()) {
            result.put("isSafe", true);
            result.put("reason", "");
            return result;
        }

        try {
            String systemPrompt = """
                You are a content moderator for MinoLingo, a children's English learning platform.
                Your job is to check if a forum post is appropriate for children.

                Flag content that contains:
                - Profanity, swear words, or vulgar language (in any language)
                - Hate speech, bullying, or harassment
                - Violence or threats
                - Sexual or adult content
                - Spam or scam links
                - Personal information sharing (phone numbers, addresses)
                - Discrimination based on race, gender, religion, etc.

                Reply ONLY in this exact JSON format (no markdown, no code fences):
                {"isSafe": true or false, "reason": "short explanation if not safe, empty string if safe"}
                """;

            String userPrompt = "Check this forum post: \"" + content.replace("\"", "'") + "\"";

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", ollamaModel);
            requestBody.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(java.time.Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String aiContent = root.path("message").path("content").asText("");

                // Try to parse as JSON
                int jsonStart = aiContent.indexOf('{');
                int jsonEnd = aiContent.lastIndexOf('}');
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonStr = aiContent.substring(jsonStart, jsonEnd + 1);
                    JsonNode aiResponse = objectMapper.readTree(jsonStr);
                    result.put("isSafe", aiResponse.path("isSafe").asBoolean(true));
                    result.put("reason", aiResponse.path("reason").asText(""));
                } else {
                    result.put("isSafe", true);
                    result.put("reason", "");
                }
            } else {
                result.put("isSafe", true);
                result.put("reason", "");
            }
        } catch (Exception e) {
            System.err.println("[ContentModeration] Error: " + e.getMessage());
            result.put("isSafe", true);
            result.put("reason", "");
        }

        return result;
    }
}
