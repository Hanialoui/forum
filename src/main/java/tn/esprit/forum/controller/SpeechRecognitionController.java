package tn.esprit.forum.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

@RestController
@RequestMapping("/api/forums/speech")
public class SpeechRecognitionController {

    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribeAudio(@RequestParam("audio") MultipartFile audioFile) {
        try {
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No audio file provided"));
            }

            // Save uploaded audio to temporary file
            Path tempAudioFile = Files.createTempFile("speech_", ".wav");
            audioFile.transferTo(tempAudioFile.toFile());

            Path tempPythonScript = null;
            try {
                // Extract Python script from resources to temp file
                InputStream scriptStream = getClass().getClassLoader()
                        .getResourceAsStream("speech_recognition.py");
                
                if (scriptStream == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("success", false, "text": "", "error", "Python script not found in resources"));
                }

                tempPythonScript = Files.createTempFile("speech_recognition_", ".py");
                Files.copy(scriptStream, tempPythonScript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                scriptStream.close();

                // Make script executable (Linux)
                tempPythonScript.toFile().setExecutable(true);

                System.out.println("[Speech Recognition] Python script extracted to: " + tempPythonScript);
                System.out.println("[Speech Recognition] Audio file: " + tempAudioFile);

                // Call Python script via ProcessBuilder
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python3", tempPythonScript.toString(), tempAudioFile.toString()
                );
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // Read output
                StringBuilder output = new StringBuilder();
                Scanner scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    output.append(scanner.nextLine());
                }
                scanner.close();

                int exitCode = process.waitFor();
                System.out.println("[Speech Recognition] Exit code: " + exitCode);
                System.out.println("[Speech Recognition] Output: " + output);

                // Parse JSON output
                String jsonOutput = output.toString();
                boolean success = jsonOutput.contains("\"success\":true");
                String text = extractJsonValue(jsonOutput, "text");
                String error = extractJsonValue(jsonOutput, "error");

                if (success && text != null && !text.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "text", text
                    ));
                } else {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "text", "",
                            "error", error != null ? error : "Failed to transcribe audio. Output: " + jsonOutput
                    ));
                }

            } finally {
                // Clean up temp files
                Files.deleteIfExists(tempAudioFile);
                if (tempPythonScript != null) {
                    Files.deleteIfExists(tempPythonScript);
                }
            }

        } catch (Exception e) {
            System.err.println("[Speech Recognition] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "text": "",
                            "error", "Failed to transcribe audio: " + e.getMessage()
                    ));
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex += searchKey.length();
        if (startIndex >= json.length()) return null;
        
        char nextChar = json.charAt(startIndex);
        if (nextChar == '"') {
            // String value
            int endIndex = json.indexOf("\"", startIndex + 1);
            if (endIndex == -1) return null;
            return json.substring(startIndex + 1, endIndex);
        } else if (nextChar == 't' || nextChar == 'f') {
            // Boolean value
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            if (endIndex == -1) return null;
            return json.substring(startIndex, endIndex).trim();
        }
        return null;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            String pythonScriptPath = getClass().getClassLoader()
                    .getResource("speech_recognition.py")
                    .getPath();
            
            if (pythonScriptPath != null) {
                return ResponseEntity.ok(Map.of(
                        "status", "healthy",
                        "service": "speech-recognition",
                        "python_script", pythonScriptPath
                ));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("status", "unhealthy", "error": "Python script not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "unhealthy", "error", e.getMessage()));
        }
    }
}
