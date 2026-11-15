package com.yisy.modules.doraai.dto;

/**
 * Response for voice chat (includes transcribed text, AI response, and synthesized audio).
 */
public class VoiceChatResponse {

    private String transcribedText;
    private String aiResponseText;
    private String audioBase64;

    public VoiceChatResponse() {
    }

    public VoiceChatResponse(String transcribedText, String aiResponseText, String audioBase64) {
        this.transcribedText = transcribedText;
        this.aiResponseText = aiResponseText;
        this.audioBase64 = audioBase64;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }

    public String getAiResponseText() {
        return aiResponseText;
    }

    public void setAiResponseText(String aiResponseText) {
        this.aiResponseText = aiResponseText;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}
