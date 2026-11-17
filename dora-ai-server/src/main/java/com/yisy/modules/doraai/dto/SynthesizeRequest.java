package com.yisy.modules.doraai.dto;

/**
 * Request for text-to-speech synthesis.
 */
public class SynthesizeRequest {

    private String text;
    private String voice;
    private String model;
    private String language;

    public SynthesizeRequest() {
    }

    public SynthesizeRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
