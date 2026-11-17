package com.yisy.modules.doramcu.ai.client;
import com.yisy.modules.doramcu.model.user.User;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * REST client for the standalone DoraAI Server.
 * <p>
 * Replaces all direct imports of doraai services after Phase 2 separation.
 * The AI server runs on port 8085 and provides REST endpoints for:
 * <ul>
 *   <li>LLM Chat — POST /api/ai/chat</li>
 *   <li>Voice TTS — POST /api/ai/voice/synthesize</li>
 *   <li>Voice STT — POST /api/ai/voice/transcribe</li>
 *   <li>Voice Commands — POST /api/ai/voice/command</li>
 * </ul>
 * </p>
 * <p>
 * Includes circuit breaker and retry logic for fault tolerance.
 * </p>
 */
@Component
public class AiRestClient {

    private static final Logger log = LoggerFactory.getLogger(AiRestClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final int maxRetries;
    private final long circuitBreakerThreshold;
    private final long circuitBreakerResetMs;
    private int consecutiveFailures = 0;
    private long circuitOpenSince = 0;

    public AiRestClient(
            @Value("${ai.rest-client.base-url:http://localhost:8085}") String baseUrl,
            @Value("${ai.rest-client.max-retries:3}") int maxRetries,
            @Value("${ai.rest-client.circuit-breaker-threshold:5}") long circuitBreakerThreshold,
            @Value("${ai.rest-client.circuit-breaker-reset-ms:30000}") long circuitBreakerResetMs) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
        this.circuitBreakerThreshold = circuitBreakerThreshold;
        this.circuitBreakerResetMs = circuitBreakerResetMs;
    }

    /**
     * Checks if the AI server is available.
     */
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/actuator/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("AI server health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Sends a chat message to the AI server and returns the complete response.
     *
     * @param message the user message
     * @param systemPrompt optional system prompt
     * @param providerId optional provider ID (ollama, gemini, deepseek)
     * @return the AI response text
     */
    public String chat(String message, String systemPrompt, String providerId) {
        if (!isCircuitClosed()) {
            log.warn("Circuit breaker is open, returning fallback response");
            return "AI service is currently unavailable. Please try again later.";
        }

        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setSystemPrompt(systemPrompt);
        request.setProviderId(providerId);

        String result = executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    baseUrl + "/api/ai/chat",
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                if (root.has("content")) {
                    return root.get("content").asText();
                }
            }
            throw new RestClientException("Invalid response from AI server");
        });

        if (result == null) {
            return "AI service is currently unavailable.";
        }
        return result;
    }

    /**
     * Sends a chat message and returns a streaming SSE response.
     *
     * @param sessionId the conversation session ID
     * @param message the user message
     * @param mode the chat mode
     * @param systemPrompt optional system prompt
     * @param providerId optional provider ID
     * @return SseEmitter for streaming the AI response
     */
    public SseEmitter chatStream(String sessionId, String message, String mode, String systemPrompt, String providerId) {
        SseEmitter emitter = new SseEmitter(0L);

        if (!isCircuitClosed()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("AI service is currently unavailable"));
                emitter.complete();
            } catch (Exception e) {
                log.error("Failed to send error event", e);
            }
            return emitter;
        }

        try {
            ChatRequest request = new ChatRequest();
            request.setSessionId(sessionId);
            request.setMessage(message);
            request.setMode(mode);
            request.setSystemPrompt(systemPrompt);
            request.setProviderId(providerId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(
                    baseUrl + "/api/ai/chat/stream",
                    HttpMethod.POST,
                    entity,
                    String.class);
        } catch (Exception e) {
            recordFailure();
            log.error("Failed to stream chat from AI server: {}", e.getMessage());
            try {
                emitter.send(SseEmitter.event().name("error").data("Error: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                log.error("Failed to send error event", ex);
            }
        }

        return emitter;
    }

    /**
     * Synthesize text to speech.
     *
     * @param text the text to synthesize
     * @param model optional TTS model
     * @param voice optional voice name
     * @return the synthesis response with base64-encoded audio
     */
    public SynthesizeResponse synthesize(String text, String model, String voice) {
        return synthesizeWithProfile(text, model, voice, null, 1.0, null);
    }

    /**
     * Synthesize text to speech with voice profile settings.
     *
     * @param text the text to synthesize
     * @param model TTS model from profile
     * @param voice voice name from profile
     * @param pitch pitch multiplier
     * @param speed speed multiplier
     * @param tone tone descriptor
     * @param language language code
     * @return the synthesis response with base64-encoded audio
     */
    public SynthesizeResponse synthesizeWithProfile(
            String text, String model, String voice,
            Double pitch, Double speed, String tone) {

        if (!isCircuitClosed()) {
            log.warn("Circuit breaker is open, returning empty synthesis response");
            return new SynthesizeResponse("", "wav", 0.0);
        }

        SynthesizeRequest request = new SynthesizeRequest();
        request.setText(text);
        request.setModel(model);
        request.setVoice(voice);
        if (pitch != null && pitch != 1.0) {
            request.setText("[Pitch: " + (pitch < 1.0 ? "low" : "high") + "] " + text);
        }
        if (speed != null && speed != 1.0) {
            request.setText("[Speed: " + (speed < 1.0 ? "slow" : "fast") + "] " + request.getText());
        }
        if (tone != null && !tone.isBlank()) {
            request.setText("[Tone: " + tone + "] " + request.getText());
        }

        SynthesizeResponse result = executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SynthesizeRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    baseUrl + "/api/ai/voice/synthesize",
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                return new SynthesizeResponse(
                        root.path("audioBase64").asText(""),
                        root.path("format").asText("wav"),
                        root.path("durationSeconds").asDouble(0.0)
                );
            }
            throw new RestClientException("Invalid response from AI server");
        });

        if (result == null) {
            return new SynthesizeResponse("", "wav", 0.0);
        }
        return result;
    }

    /**
     * Process a voice command.
     *
     * @param audioBase64 base64-encoded audio data
     * @return the voice command response
     */
    public VoiceCommandResponse processVoiceCommand(String audioBase64) {
        if (!isCircuitClosed()) {
            log.warn("Circuit breaker is open, returning fallback voice command response");
            return new VoiceCommandResponse("UNKNOWN", "", Map.of());
        }

        VoiceCommandRequest request = new VoiceCommandRequest();
        request.setAudioBase64(audioBase64);

        VoiceCommandResponse result = executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<VoiceCommandRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    baseUrl + "/api/ai/voice/command",
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                return new VoiceCommandResponse(
                        root.path("command").asText("UNKNOWN"),
                        root.path("transcript").asText(""),
                        Map.of()
                );
            }
            throw new RestClientException("Invalid response from AI server");
        });

        if (result == null) {
            return new VoiceCommandResponse("UNKNOWN", "", Map.of());
        }
        return result;
    }

    // --- Circuit Breaker ---

    private boolean isCircuitClosed() {
        if (consecutiveFailures < circuitBreakerThreshold) {
            return true;
        }
        if (System.currentTimeMillis() - circuitOpenSince > circuitBreakerResetMs) {
            log.info("Circuit breaker resetting after {}ms", circuitBreakerResetMs);
            consecutiveFailures = 0;
            return true;
        }
        return false;
    }

    private void recordSuccess() {
        consecutiveFailures = 0;
    }

    private void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= circuitBreakerThreshold) {
            circuitOpenSince = System.currentTimeMillis();
            log.warn("Circuit breaker opened after {} consecutive failures", consecutiveFailures);
        }
    }

    // --- Retry Logic ---

    private <T> T executeWithRetry(RetryableSupplier<T> supplier) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                T result = supplier.get();
                recordSuccess();
                return result;
            } catch (Exception e) {
                lastException = e;
                recordFailure();
                log.warn("AI server call failed (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.error("AI server call failed after {} attempts", maxRetries);
        return null;
    }

    @FunctionalInterface
    interface RetryableSupplier<T> {
        T get() throws Exception;
    }

    // --- Request/Response DTOs ---

    public static class ChatRequest {
        private String sessionId;
        private String message;
        private String mode;
        private String systemPrompt;
        private String providerId;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
        public String getProviderId() { return providerId; }
        public void setProviderId(String providerId) { this.providerId = providerId; }
    }

    public static class SynthesizeRequest {
        private String text;
        private String model;
        private String voice;
        private String language;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getVoice() { return voice; }
        public void setVoice(String voice) { this.voice = voice; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class SynthesizeResponse {
        private final String audioBase64;
        private final String format;
        private final double durationSeconds;

        public SynthesizeResponse(String audioBase64, String format, double durationSeconds) {
            this.audioBase64 = audioBase64;
            this.format = format;
            this.durationSeconds = durationSeconds;
        }

        public String getAudioBase64() { return audioBase64; }
        public String getFormat() { return format; }
        public double getDurationSeconds() { return durationSeconds; }
    }

    public static class VoiceCommandRequest {
        private String audioBase64;

        public String getAudioBase64() { return audioBase64; }
        public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }
    }

    public static class VoiceCommandResponse {
        private final String command;
        private final String transcript;
        private final Map<String, Object> parameters;

        public VoiceCommandResponse(String command, String transcript, Map<String, Object> parameters) {
            this.command = command;
            this.transcript = transcript;
            this.parameters = parameters;
        }

        public String getCommand() { return command; }
        public String getCommandType() { return command; }
        public String getTranscript() { return transcript; }
        public String getMessage() { return transcript; }
        public boolean isExecuted() { return !"UNKNOWN".equals(command); }
        public Map<String, Object> getParameters() { return parameters; }
    }
}
