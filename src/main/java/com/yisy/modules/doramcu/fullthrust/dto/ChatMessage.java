package com.yisy.modules.doramcu.fullthrust.dto;

import java.time.LocalDateTime;

/**
 * DTO for chat messages between players.
 */
public class ChatMessage {
    private String sessionId;
    private String playerId;
    private String playerName;
    private String message;
    private LocalDateTime timestamp;
    private String type; // "CHAT", "SYSTEM"

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.type = "CHAT";
    }

    // Getters and setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
