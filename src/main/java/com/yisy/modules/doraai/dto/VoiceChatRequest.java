package com.yisy.modules.doraai.dto;

/**
 * Request for voice chat (STT + AI chat + TTS).
 */
public class VoiceChatRequest {

    private String audioBase64;
    private String sessionId;
    private String mode;

    public VoiceChatRequest() {
    }

    public VoiceChatRequest(String audioBase64, String sessionId) {
        this.audioBase64 = audioBase64;
        this.sessionId = sessionId;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
