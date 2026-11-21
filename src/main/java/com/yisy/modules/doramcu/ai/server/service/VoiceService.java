package com.yisy.modules.doramcu.ai.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.ai.server.config.OllamaConfig;
import com.yisy.modules.doramcu.ai.server.dto.SynthesizeRequest;
import com.yisy.modules.doramcu.ai.server.dto.SynthesizeResponse;
import com.yisy.modules.doramcu.ai.server.dto.TranscribeRequest;
import com.yisy.modules.doramcu.ai.server.dto.TranscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for speech-to-text transcription using Ollama Whisper model.
 * <p>
 * Sends audio data to Ollama's Whisper model for transcription.
 * </p>
 */
@Service
public class VoiceService {

    private static final Logger log = LoggerFactory.getLogger(VoiceService.class);

    private final OllamaConfig ollamaConfig;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public VoiceService(OllamaConfig ollamaConfig, HttpClient httpClient) {
        this.ollamaConfig = ollamaConfig;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Transcribe audio to text using Ollama Whisper.
     *
     * @param request the transcription request with base64-encoded audio
     * @return the transcription result
     */
    public TranscribeResponse transcribe(TranscribeRequest request) {
        if (request.getAudioBase64() == null || request.getAudioBase64().isBlank()) {
            throw new IllegalArgumentException("Audio data is required");
        }

        log.info("Transcribing audio: {} bytes, language hint: {}",
                request.getAudioBase64().length(),
                request.getLanguageHint());

        try {
            // Build the request body for Ollama Whisper
            Map<String, Object> body = Map.of(
                    "model", ollamaConfig.getWhisperModel(),
                    "prompt", request.getLanguageHint() != null ?
                            "Transcribe in " + request.getLanguageHint() : "",
                    "images", java.util.List.of(request.getAudioBase64())
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaConfig.getBaseUrl() + "/api/generate"))
                    .timeout(java.time.Duration.ofMillis(ollamaConfig.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ollama transcription failed: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Transcription failed with status " + response.statusCode());
            }

            String text = extractTextFromResponse(response.body());
            String language = detectLanguage(text, request.getLanguageHint());
            return new TranscribeResponse(text, language);

        } catch (Exception e) {
            log.error("Transcription failed", e);
            throw new RuntimeException("Transcription failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.has("response")) {
                return root.get("response").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama response: {}", e.getMessage());
        }
        return response;
    }

    private String detectLanguage(String text, String hint) {
        if (hint != null && !hint.isBlank()) {
            return hint;
        }
        // Simple language detection based on common characters
        if (text.matches(".*[\\u4e00-\\u9fff].*")) return "zh";
        if (text.matches(".*[\\u3040-\\u309f\\u30a0-\\u30ff].*")) return "ja";
        if (text.matches(".*[\\uac00-\\ud7af].*")) return "ko";
        if (text.matches(".*[\\u0400-\\u04ff].*")) return "ru";
        if (text.matches(".*[\\u0600-\\u06ff].*")) return "ar";
        return "en";
    }

    /**
     * Synthesize text to speech using Ollama TTS model.
     *
     * @param request the synthesis request with text to speak
     * @return the synthesis result with base64-encoded audio
     */
    public SynthesizeResponse synthesize(SynthesizeRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("Text is required for synthesis");
        }

        String model = request.getModel() != null && !request.getModel().isBlank()
                ? request.getModel() : ollamaConfig.getTtsModel();
        String voice = request.getVoice() != null && !request.getVoice().isBlank()
                ? request.getVoice() : ollamaConfig.getTtsVoice();

        log.info("Synthesizing text: {} chars, model={}, voice={}",
                request.getText().length(), model, voice);

        try {
            // Build the request body for Ollama TTS
            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", request.getText(),
                    "voice", voice,
                    "stream", false
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaConfig.getBaseUrl() + "/api/generate"))
                    .timeout(java.time.Duration.ofMillis(ollamaConfig.getReadTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ollama TTS failed: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("TTS synthesis failed with status " + response.statusCode());
            }

            // Extract audio from response
            String audioBase64 = extractAudioFromResponse(response.body());
            if (audioBase64 == null || audioBase64.isBlank()) {
                // If no audio in response, the model may not support TTS - return the text as fallback
                log.warn("No audio data in TTS response; model may not support TTS");
                return new SynthesizeResponse("", "text", 0.0);
            }

            return new SynthesizeResponse(audioBase64, "wav", estimateDuration(request.getText()));

        } catch (Exception e) {
            log.error("TTS synthesis failed", e);
            throw new RuntimeException("TTS synthesis failed: " + e.getMessage(), e);
        }
    }

    private String extractAudioFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            // Check for audio_base64 field (Ollama TTS response format)
            if (root.has("audio_base64")) {
                return root.get("audio_base64").asText();
            }
            // Check for response field that may contain audio data
            if (root.has("response")) {
                String resp = root.get("response").asText();
                // If response looks like base64 audio data, return it
                if (resp.length() > 100 && !resp.contains(" ")) {
                    return resp;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama TTS response: {}", e.getMessage());
        }
        return null;
    }

    private double estimateDuration(String text) {
        // Rough estimate: ~150 words per minute for normal speech
        int wordCount = text.split("\\s+").length;
        return wordCount / 150.0 * 60.0;
    }

    /**
     * Synthesize text to speech using a voice profile's settings.
     * Applies profile-specific model, voice, pitch, speed, and tone to the TTS request.
     *
     * @param request the base synthesis request with text
     * @param profileModel TTS model from profile (null to use config default)
     * @param profileVoice voice name from profile (null to use config default)
     * @param profilePitch pitch multiplier (0.5-2.0, 1.0 for neutral)
     * @param profileSpeed speed multiplier (0.5-2.0, 1.0 for normal)
     * @param profileTone tone descriptor for prompt (e.g., "warm", "deep", "mechanical")
     * @param profileLanguage language code (null to use default)
     * @return the synthesis result with base64-encoded audio
     */
    public SynthesizeResponse synthesizeWithProfile(
            SynthesizeRequest request,
            String profileModel,
            String profileVoice,
            double profilePitch,
            double profileSpeed,
            String profileTone,
            String profileLanguage) {

        if (request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("Text is required for synthesis");
        }

        // Profile overrides request, request overrides config defaults
        String model = (profileModel != null && !profileModel.isBlank())
                ? profileModel
                : (request.getModel() != null && !request.getModel().isBlank()
                        ? request.getModel() : ollamaConfig.getTtsModel());
        String voice = (profileVoice != null && !profileVoice.isBlank())
                ? profileVoice
                : (request.getVoice() != null && !request.getVoice().isBlank()
                        ? request.getVoice() : ollamaConfig.getTtsVoice());

        // Build prompt with tone/speed instructions for Ollama TTS
        String text = request.getText();
        if (profileTone != null && !profileTone.isBlank()) {
            text = "[Tone: " + profileTone + "] " + text;
        }
        if (profileSpeed != 1.0) {
            String speedHint = profileSpeed < 1.0 ? "slow" : "fast";
            text = "[Speed: " + speedHint + "] " + text;
        }
        if (profilePitch != 1.0) {
            String pitchHint = profilePitch < 1.0 ? "low" : "high";
            text = "[Pitch: " + pitchHint + "] " + text;
        }

        // Use modified text for synthesis
        SynthesizeRequest profiledRequest = new SynthesizeRequest();
        profiledRequest.setText(text);
        profiledRequest.setModel(model);
        profiledRequest.setVoice(voice);
        if (profileLanguage != null && !profileLanguage.isBlank()) {
            profiledRequest.setLanguage(profileLanguage);
        }

        log.info("Synthesizing with profile: model={}, voice={}, pitch={}, speed={}, tone={}",
                model, voice, profilePitch, profileSpeed, profileTone);

        return synthesize(profiledRequest);
    }

    /**
     * Get the list of available TTS voices from Ollama.
     *
     * @return list of available voice names
     */
    public List<String> getAvailableVoices() {
        List<String> voices = new ArrayList<>();
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaConfig.getBaseUrl() + "/api/tags"))
                    .timeout(java.time.Duration.ofMillis(ollamaConfig.getConnectTimeoutMs()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("models")) {
                    for (JsonNode model : root.get("models")) {
                        String name = model.path("name").asText("");
                        if (name.contains("tts") || name.contains("victor") || name.contains("voice")) {
                            voices.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch available voices from Ollama: {}", e.getMessage());
        }

        // Return default voices if none found
        if (voices.isEmpty()) {
            voices.add("default");
            voices.add("male");
            voices.add("female");
        }

        return voices;
    }
}
