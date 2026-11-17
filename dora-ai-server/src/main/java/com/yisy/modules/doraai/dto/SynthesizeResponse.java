package com.yisy.modules.doraai.dto;

/**
 * Response for text-to-speech synthesis.
 */
public class SynthesizeResponse {

    private String audioBase64;
    private String format;
    private double duration;

    public SynthesizeResponse() {
    }

    public SynthesizeResponse(String audioBase64, String format, double duration) {
        this.audioBase64 = audioBase64;
        this.format = format;
        this.duration = duration;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
