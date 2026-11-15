package com.yisy.modules.doramcu.eve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a TTS voice profile that can be assigned to AI characters.
 * Multiple characters can share the same profile, and profiles can be managed independently.
 */
@Entity
@Table(name = "voice_profiles")
@EntityListeners(CdcEntityListener.class)
public class VoiceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Display name of the voice profile, e.g., "Default Male", "Robot".
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the voice profile's characteristics.
     */
    @Column(length = 500)
    private String description;

    /**
     * TTS model to use, e.g., "victrims/victor". Null means use OllamaConfig default.
     */
    @Column(name = "tts_model", length = 100)
    private String ttsModel;

    /**
     * Voice name/identifier for the TTS model, e.g., "alloy", "echo".
     */
    @Column(name = "tts_voice", length = 100)
    private String ttsVoice;

    /**
     * Pitch multiplier (0.5 - 2.0). 1.0 is neutral.
     */
    @Column(nullable = false)
    private double pitch = 1.0;

    /**
     * Speed multiplier (0.5 - 2.0). 1.0 is normal speed.
     */
    @Column(nullable = false)
    private double speed = 1.0;

    /**
     * Tone descriptor for TTS prompt, e.g., "warm", "deep", "bright", "mechanical".
     */
    @Column(length = 50)
    private String tone;

    /**
     * Language code for TTS, e.g., "en-US". Null means use default.
     */
    @Column(length = 10)
    private String language;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        clampValues();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        clampValues();
    }

    /**
     * Clamps pitch and speed to valid ranges (0.5 - 2.0).
     */
    private void clampValues() {
        this.pitch = Math.max(0.5, Math.min(2.0, this.pitch));
        this.speed = Math.max(0.5, Math.min(2.0, this.speed));
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTtsModel() {
        return ttsModel;
    }

    public void setTtsModel(String ttsModel) {
        this.ttsModel = ttsModel;
    }

    public String getTtsVoice() {
        return ttsVoice;
    }

    public void setTtsVoice(String ttsVoice) {
        this.ttsVoice = ttsVoice;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = Math.max(0.5, Math.min(2.0, pitch));
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = Math.max(0.5, Math.min(2.0, speed));
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "VoiceProfile{id=" + id + ", name='" + name + "', pitch=" + pitch +
                ", speed=" + speed + ", tone='" + tone + "', voice='" + ttsVoice + "'}";
    }
}
