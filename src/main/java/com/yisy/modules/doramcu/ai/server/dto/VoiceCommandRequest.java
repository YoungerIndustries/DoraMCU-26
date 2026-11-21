package com.yisy.modules.doramcu.ai.server.dto;

import java.util.Map;

/**
 * Request for voice command interpretation.
 */
public class VoiceCommandRequest {

    private String audioBase64;
    private String sessionId;
    private String playerId;
    private String gameMode;

    public VoiceCommandRequest() {
    }

    public VoiceCommandRequest(String audioBase64, String sessionId, String playerId) {
        this.audioBase64 = audioBase64;
        this.sessionId = sessionId;
        this.playerId = playerId;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
}
