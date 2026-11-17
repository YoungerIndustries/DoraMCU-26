package com.yisy.modules.doramcu.controller.chat;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.hex.chat.ChatMessage;
import com.yisy.modules.doramcu.hex.chat.ChatMessage.Channel;
import com.yisy.modules.doramcu.service.chat.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * REST controller for in-game chat functionality.
 * <p>
 * Provides endpoints for sending chat messages, whispers, retrieving message history,
 * listing channels, and getting chat statistics.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/chat}.
 * </p>
 * <p>
 * For real-time chat, use the WebSocket endpoint at {@code /ws/v1/4x/session} with
 * the "chat" action type. This REST API is for history retrieval and non-WebSocket clients.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Send a chat message to a game session.
     *
     * @param sessionId the game session ID
     * @param body      JSON body: {"senderId": 1, "senderName": "Captain", "content": "Hello!", "channel": "general"}
     * @return the created message
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {

        long senderId = ((Number) body.getOrDefault("senderId", 0)).longValue();
        String senderName = (String) body.get("senderName");
        String content = (String) body.get("content");
        String channelName = (String) body.get("channel");

        Channel channel = channelName != null ? Channel.fromName(channelName) : Channel.GENERAL;

        ChatMessage message = chatService.sendMessage(sessionId, senderId, senderName, content, channel);
        return ResponseEntity.ok(messageToMap(message));
    }

    /**
     * Send a whisper message to a specific player.
     *
     * @param sessionId the game session ID
     * @param body      JSON body: {"senderId": 1, "senderName": "Captain", "targetPlayerId": 2, "content": "Secret"}
     * @return the created message
     */
    @PostMapping("/sessions/{sessionId}/whisper")
    public ResponseEntity<Map<String, Object>> sendWhisper(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {

        long senderId = ((Number) body.getOrDefault("senderId", 0)).longValue();
        String senderName = (String) body.get("senderName");
        long targetPlayerId = ((Number) body.getOrDefault("targetPlayerId", 0)).longValue();
        String content = (String) body.get("content");

        ChatMessage message = chatService.sendWhisper(sessionId, senderId, senderName, targetPlayerId, content);
        return ResponseEntity.ok(messageToMap(message));
    }

    /**
     * Get message history for a session.
     *
     * @param sessionId the game session ID
     * @param channel   optional channel filter
     * @param limit     optional message limit (default 50)
     * @return list of messages
     */
    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @PathVariable String sessionId,
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "50") int limit) {

        Channel ch = channel != null ? Channel.fromName(channel) : null;
        List<ChatMessage> messages = chatService.getHistory(sessionId, ch, limit);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage m : messages) {
            result.add(messageToMap(m));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get message history since a given timestamp.
     *
     * @param sessionId the game session ID
     * @param since     ISO-8601 timestamp
     * @return list of messages since the given time
     */
    @GetMapping("/sessions/{sessionId}/history/since")
    public ResponseEntity<List<Map<String, Object>>> getHistorySince(
            @PathVariable String sessionId,
            @RequestParam String since) {

        Instant sinceInstant = Instant.parse(since);
        List<ChatMessage> messages = chatService.getHistorySince(sessionId, sinceInstant);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage m : messages) {
            result.add(messageToMap(m));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get messages for a specific player (sent by or whispered to).
     *
     * @param sessionId the game session ID
     * @param playerId  the player ID
     * @return list of messages relevant to the player
     */
    @GetMapping("/sessions/{sessionId}/player/{playerId}")
    public ResponseEntity<List<Map<String, Object>>> getPlayerMessages(
            @PathVariable String sessionId,
            @PathVariable long playerId) {

        List<ChatMessage> messages = chatService.getPlayerMessages(sessionId, playerId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatMessage m : messages) {
            result.add(messageToMap(m));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get the list of available chat channels.
     *
     * @return list of channel names
     */
    @GetMapping("/channels")
    public ResponseEntity<List<String>> getChannels() {
        return ResponseEntity.ok(chatService.getChannels());
    }

    /**
     * Get chat statistics for a session.
     *
     * @param sessionId the game session ID
     * @return statistics map with totalMessages, messagesByChannel, messagesByType, uniqueSenders
     */
    @GetMapping("/sessions/{sessionId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getStats(sessionId));
    }

    /**
     * Get the message count for a session.
     *
     * @param sessionId the game session ID
     * @return map with count
     */
    @GetMapping("/sessions/{sessionId}/count")
    public ResponseEntity<Map<String, Object>> getMessageCount(@PathVariable String sessionId) {
        int count = chatService.getMessageCount(sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "count", count));
    }

    /**
     * Clear all chat messages for a session (admin).
     *
     * @param sessionId the game session ID
     * @return confirmation
     */
    @DeleteMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Map<String, Object>> clearMessages(@PathVariable String sessionId) {
        int count = chatService.getMessageCount(sessionId);
        chatService.clearSession(sessionId);
        log.info("Cleared {} chat messages for session {} via REST API", count, sessionId);
        return ResponseEntity.ok(Map.of("cleared", count, "sessionId", sessionId));
    }

    private Map<String, Object> messageToMap(ChatMessage message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", message.getId());
        map.put("sessionId", message.getSessionId());
        map.put("senderId", message.getSenderId());
        map.put("senderName", message.getSenderName());
        map.put("content", message.getContent());
        map.put("channel", message.getChannel().getName());
        map.put("type", message.getType().name());
        if (message.getTargetPlayerId() != null) {
            map.put("targetPlayerId", message.getTargetPlayerId());
        }
        map.put("timestamp", message.getTimestamp().toString());
        return map;
    }
}
