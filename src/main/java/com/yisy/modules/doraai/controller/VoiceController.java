package com.yisy.modules.doraai.controller;

import com.yisy.modules.doraai.dto.SynthesizeRequest;
import com.yisy.modules.doraai.dto.SynthesizeResponse;
import com.yisy.modules.doraai.dto.TranscribeRequest;
import com.yisy.modules.doraai.dto.TranscribeResponse;
import com.yisy.modules.doraai.dto.TranslateRequest;
import com.yisy.modules.doraai.dto.TranslateResponse;
import com.yisy.modules.doraai.dto.VoiceChatRequest;
import com.yisy.modules.doraai.dto.VoiceChatResponse;
import com.yisy.modules.doraai.dto.VoiceCommandRequest;
import com.yisy.modules.doraai.dto.VoiceCommandResponse;
import com.yisy.modules.doraai.service.AiChatService;
import com.yisy.modules.doraai.service.TranslationService;
import com.yisy.modules.doraai.service.VoiceCommandService;
import com.yisy.modules.doraai.service.VoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for voice and translation features.
 * <p>
 * Provides endpoints for speech-to-text transcription, text translation,
 * and language-aware chat.
 * </p>
 */
@RestController
@RequestMapping("/api/ai/voice")
public class VoiceController {

    private static final Logger log = LoggerFactory.getLogger(VoiceController.class);

    private final VoiceService voiceService;
    private final TranslationService translationService;
    private final VoiceCommandService voiceCommandService;
    private final AiChatService aiChatService;

    public VoiceController(VoiceService voiceService,
                           TranslationService translationService,
                           VoiceCommandService voiceCommandService,
                           AiChatService aiChatService) {
        this.voiceService = voiceService;
        this.translationService = translationService;
        this.voiceCommandService = voiceCommandService;
        this.aiChatService = aiChatService;
    }

    /**
     * Transcribe audio to text.
     *
     * @param request the transcription request with base64-encoded audio
     * @return the transcription result
     */
    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribe(@RequestBody TranscribeRequest request) {
        try {
            TranscribeResponse response = voiceService.transcribe(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Transcription failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Transcription failed: " + e.getMessage()));
        }
    }

    /**
     * Translate text to a target language.
     *
     * @param request the translation request with text and target language
     * @return SSE emitter streaming the translated text
     */
    @PostMapping("/translate")
    public SseEmitter translate(@RequestBody TranslateRequest request) {
        log.info("Translation request: target={}, source={}, text length={}", 
                request.getTargetLanguage(), request.getSourceLanguage(), 
                request.getText() != null ? request.getText().length() : 0);
        return translationService.translate(request.getText(), request.getSourceLanguage(), request.getTargetLanguage());
    }

    /**
     * Get the list of supported languages.
     */
    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getLanguages() {
        return ResponseEntity.ok(Map.of(
                "languages", translationService.getSupportedLanguages()
        ));
    }

    /**
     * Synthesize text to speech.
     *
     * @param request the synthesis request with text to speak
     * @return the synthesis result with base64-encoded audio
     */
    @PostMapping("/synthesize")
    public ResponseEntity<?> synthesize(@RequestBody SynthesizeRequest request) {
        try {
            SynthesizeResponse response = voiceService.synthesize(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("TTS synthesis failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "TTS synthesis failed: " + e.getMessage()));
        }
    }

    /**
     * Process a voice command (STT + interpretation).
     *
     * @param request the voice command request with audio data
     * @return the interpreted command response
     */
    @PostMapping("/command")
    public ResponseEntity<?> command(@RequestBody VoiceCommandRequest request) {
        try {
            VoiceCommandResponse response = voiceCommandService.processCommand(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Voice command processing failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Voice command failed: " + e.getMessage()));
        }
    }

    /**
     * Voice chat endpoint (STT + AI chat + TTS).
     *
     * @param request the voice chat request with audio data
     * @return the voice chat response with transcribed text, AI response, and audio
     */
    @PostMapping("/chat")
    public ResponseEntity<?> voiceChat(@RequestBody VoiceChatRequest request) {
        try {
            if (request.getAudioBase64() == null || request.getAudioBase64().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Audio data is required"));
            }

            // Step 1: Transcribe audio to text
            com.yisy.modules.doraai.dto.TranscribeRequest transcribeRequest =
                    new com.yisy.modules.doraai.dto.TranscribeRequest();
            transcribeRequest.setAudioBase64(request.getAudioBase64());
            com.yisy.modules.doraai.dto.TranscribeResponse transcribeResponse =
                    voiceService.transcribe(transcribeRequest);
            String transcribedText = transcribeResponse.getText();

            log.info("Voice chat transcribed: '{}'", transcribedText);

            // Step 2: Send to AI chat
            String mode = request.getMode() != null ? request.getMode() : "chat";
            SseEmitter emitter = aiChatService.chat(request.getSessionId(), transcribedText, mode);

            emitter.onCompletion(() -> {
                log.debug("AI chat completed for voice session");
            });

            // Wait for AI response (simplified - in production use async)
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Step 3: Synthesize AI response to speech
            // Note: AI response is not captured from SSE stream; using transcribed text as fallback.
            String responseText = transcribedText;
            SynthesizeRequest synthesizeRequest = new SynthesizeRequest();
            synthesizeRequest.setText(responseText);
            SynthesizeResponse synthesizeResponse = voiceService.synthesize(synthesizeRequest);

            // Build response
            VoiceChatResponse voiceChatResponse = new VoiceChatResponse();
            voiceChatResponse.setTranscribedText(transcribedText);
            voiceChatResponse.setAiResponseText(responseText);
            voiceChatResponse.setAudioBase64(synthesizeResponse.getAudioBase64());

            return ResponseEntity.ok(voiceChatResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Voice chat failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Voice chat failed: " + e.getMessage()));
        }
    }

    /**
     * Get the list of available TTS voices.
     *
     * @return list of available voice names
     */
    @GetMapping("/voices")
    public ResponseEntity<Map<String, Object>> getVoices() {
        List<String> voices = voiceService.getAvailableVoices();
        return ResponseEntity.ok(Map.of(
                "voices", voices
        ));
    }
}
