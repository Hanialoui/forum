package tn.esprit.forum.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.forum.entity.ContentWarning;
import tn.esprit.forum.entity.ForumReportStatus;
import tn.esprit.forum.repository.ContentWarningRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ContentModerationService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    @Autowired
    private ContentWarningRepository contentWarningRepository;

    @Autowired
    private ForumEmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Analyze post content for toxicity using GPT.
     * Returns a map with: isToxic, category, explanation
     */
    public Map<String, Object> analyzeContent(String content) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            // No API key — allow all content
            result.put("isToxic", false);
            result.put("category", "");
            result.put("explanation", "");
            return result;
        }

        try {
            String systemPrompt = """
                You are a content moderation AI for a kids' language-learning platform called MiNoLingo.
                Your job is to analyze forum post content and detect if it contains:
                - Profanity or bad words (including masked ones like f***, sh1t, etc.)
                - Bullying, personal attacks, or flaming
                - Hate speech, racism, discrimination
                - Negative energy, threats, or intimidation
                - Sexually inappropriate content
                - Spam or scam content

                Be strict — this is a platform for children learning languages.

                Reply ONLY in this exact JSON format (no markdown, no code fences):
                {"isToxic": true or false, "category": "PROFANITY|BULLYING|HATE_SPEECH|INAPPROPRIATE|NEGATIVE|SPAM|CLEAN", "explanation": "A professional but friendly explanation of why this content is not allowed, addressed to the user. If clean, leave empty."}
                """;

            String userPrompt = "Analyze this forum post content: \"" + content + "\"";

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.1);
            requestBody.put("max_tokens", 300);

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
                String aiContent = root.path("choices").get(0).path("message").path("content").asText();
                JsonNode aiResponse = objectMapper.readTree(aiContent);

                result.put("isToxic", aiResponse.path("isToxic").asBoolean(false));
                result.put("category", aiResponse.path("category").asText("CLEAN"));
                result.put("explanation", aiResponse.path("explanation").asText(""));
            } else {
                System.err.println("[Content Moderation] OpenAI API error: " + response.statusCode() + " - " + response.body());
                result.put("isToxic", false);
                result.put("category", "CLEAN");
                result.put("explanation", "");
            }
        } catch (Exception e) {
            System.err.println("[Content Moderation] Error: " + e.getMessage());
            e.printStackTrace();
            result.put("isToxic", false);
            result.put("category", "CLEAN");
            result.put("explanation", "");
        }

        return result;
    }

    /**
     * Full moderation flow:
     * 1. Analyze content with GPT
     * 2. If toxic, count user's past offenses
     * 3. Save warning to DB
     * 4. On 2nd+ offense, send warning email
     * 5. Return result with offense count and whether email was sent
     */
    public Map<String, Object> moderatePost(String content, Long userId, String userName, String userEmail) {
        Map<String, Object> analysis = analyzeContent(content);
        boolean isToxic = (boolean) analysis.getOrDefault("isToxic", false);

        Map<String, Object> result = new LinkedHashMap<>(analysis);

        if (!isToxic) {
            result.put("allowed", true);
            result.put("offenseCount", 0);
            result.put("emailSent", false);
            return result;
        }

        // Count previous offenses for this user
        long previousOffenses = contentWarningRepository.countByUserId(userId);
        int offenseNumber = (int) previousOffenses + 1;

        // Save the warning
        ContentWarning warning = new ContentWarning();
        warning.setUserId(userId);
        warning.setUserName(userName);
        warning.setUserEmail(userEmail);
        warning.setBlockedContent(content);
        warning.setCategory((String) analysis.getOrDefault("category", "TOXICITY"));
        warning.setAiExplanation((String) analysis.getOrDefault("explanation", ""));
        warning.setOffenseNumber(offenseNumber);
        warning.setStatus(ForumReportStatus.PENDING);

        boolean emailSent = false;

        // On 2nd+ offense, send warning email
        if (offenseNumber >= 2 && userEmail != null && !userEmail.isBlank()) {
            try {
                emailService.sendContentWarningEmail(
                        userEmail, userName, content,
                        (String) analysis.getOrDefault("category", "TOXICITY"),
                        (String) analysis.getOrDefault("explanation", ""),
                        offenseNumber
                );
                emailSent = true;
            } catch (Exception e) {
                System.err.println("[Content Moderation] Failed to send warning email: " + e.getMessage());
            }
        }

        warning.setEmailSent(emailSent);
        contentWarningRepository.save(warning);

        result.put("allowed", false);
        result.put("offenseCount", offenseNumber);
        result.put("emailSent", emailSent);
        return result;
    }

    // ── Admin methods ──

    public List<ContentWarning> getAllWarnings() {
        return contentWarningRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ContentWarning> getWarningsByUser(Long userId) {
        return contentWarningRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ContentWarning> getWarningsByStatus(String status) {
        return contentWarningRepository.findByStatus(ForumReportStatus.valueOf(status));
    }

    public ContentWarning updateWarningStatus(Long id, String status, String adminNote) {
        ContentWarning warning = contentWarningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warning not found: " + id));
        warning.setStatus(ForumReportStatus.valueOf(status));
        if (adminNote != null) warning.setAdminNote(adminNote);
        return contentWarningRepository.save(warning);
    }

    public void deleteWarning(Long id) {
        contentWarningRepository.deleteById(id);
    }
}
