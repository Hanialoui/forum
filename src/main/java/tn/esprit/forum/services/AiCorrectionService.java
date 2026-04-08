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
public class AiCorrectionService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Takes raw speech text and returns correction suggestions.
     * Response map keys:
     *   - originalText: the original input
     *   - correctedText: the AI-corrected version
     *   - hasCorrections: boolean
     *   - explanation: kid-friendly explanation of what was wrong
     */
    public Map<String, Object> correctText(String text) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalText", text);

        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            // No API key configured — return original text unchanged
            result.put("correctedText", text);
            result.put("hasCorrections", false);
            result.put("explanation", "");
            return result;
        }

        try {
            String systemPrompt = """
                You are a friendly English language helper for kids learning English.
                The user will give you a sentence that was transcribed from speech.
                Your job:
                1. Check if the sentence has any grammar, spelling, or word-choice errors.
                2. If it does, provide the corrected version and a SHORT, fun, kid-friendly explanation.
                3. If the sentence is already correct, say so.

                Reply ONLY in this exact JSON format (no markdown, no code fences):
                {"correctedText": "the corrected sentence", "hasCorrections": true or false, "explanation": "short kid-friendly explanation or empty string"}
                """;

            String userPrompt = "Please check this sentence: \"" + text + "\"";

            // Build the OpenAI API request body
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 200);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").get(0).path("message").path("content").asText();

                // Parse the AI JSON response
                JsonNode aiResponse = objectMapper.readTree(content);
                result.put("correctedText", aiResponse.path("correctedText").asText(text));
                result.put("hasCorrections", aiResponse.path("hasCorrections").asBoolean(false));
                result.put("explanation", aiResponse.path("explanation").asText(""));
            } else {
                System.err.println("[AI Correction] OpenAI API error: " + response.statusCode() + " - " + response.body());
                result.put("correctedText", text);
                result.put("hasCorrections", false);
                result.put("explanation", "");
            }
        } catch (Exception e) {
            System.err.println("[AI Correction] Error: " + e.getMessage());
            e.printStackTrace();
            result.put("correctedText", text);
            result.put("hasCorrections", false);
            result.put("explanation", "");
        }

        return result;
    }
}
