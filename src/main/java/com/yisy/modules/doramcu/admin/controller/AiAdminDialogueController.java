package com.yisy.modules.doramcu.admin.controller;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.service.character.CharacterDialogueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * REST controller for chatting with AI admin characters.
 * <p>
 * Provides SSE-streamed dialogue with AI admins that includes role-aware
 * system prompts, current activity context, and disposition toward the player.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/ai-orders/admins")
public class AiAdminDialogueController {

    private static final Logger log = LoggerFactory.getLogger(AiAdminDialogueController.class);

    private final CharacterDialogueService characterDialogueService;

    public AiAdminDialogueController(CharacterDialogueService characterDialogueService) {
        this.characterDialogueService = characterDialogueService;
    }

    /**
     * Chat with an AI admin character. The response is streamed via SSE.
     *
     * @param aiAdminId the AI admin character ID
     * @param request body containing sessionId, playerId, and message
     * @return SseEmitter streaming text chunks
     */
    @PostMapping("/{aiAdminId}/chat")
    public SseEmitter chatWithAiAdmin(@PathVariable Long aiAdminId,
                                       @RequestBody AiAdminChatRequest request) {
        log.info("Chat request with AI admin {}: sessionId={}, playerId={}",
                aiAdminId, request.getSessionId(), request.getPlayerId());

        String sessionId = request.getSessionId() != null && !request.getSessionId().isBlank()
                ? request.getSessionId()
                : "ai_admin_" + aiAdminId + "_" + System.currentTimeMillis();

        SseEmitter emitter = characterDialogueService.chatWithAiAdmin(
                aiAdminId, request.getPlayerId(), sessionId, request.getMessage());

        // Add session ID to the first event
        try {
            emitter.send(SseEmitter.event().name("session").data(sessionId));
        } catch (IOException e) {
            log.error("Failed to send session event", e);
        }

        return emitter;
    }

    /**
     * Request body for AI admin chat.
     */
    public static class AiAdminChatRequest {
        private String sessionId;
        private Long playerId;
        private String message;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
