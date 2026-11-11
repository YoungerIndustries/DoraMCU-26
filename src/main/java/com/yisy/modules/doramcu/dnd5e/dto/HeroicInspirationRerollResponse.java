package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Response DTO for a Heroic Inspiration reroll.
 */
public class HeroicInspirationRerollResponse {

    private Long inspirationId;
    private Long characterId;
    private String characterName;
    private String description;
    private int originalRoll;
    private int rerollValue;
    private int finalValue;
    private boolean improved;
    private int pointsRemaining;
    private String source;

    public Long getInspirationId() {
        return inspirationId;
    }

    public void setInspirationId(Long inspirationId) {
        this.inspirationId = inspirationId;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOriginalRoll() {
        return originalRoll;
    }

    public void setOriginalRoll(int originalRoll) {
        this.originalRoll = originalRoll;
    }

    public int getRerollValue() {
        return rerollValue;
    }

    public void setRerollValue(int rerollValue) {
        this.rerollValue = rerollValue;
    }

    public int getFinalValue() {
        return finalValue;
    }

    public void setFinalValue(int finalValue) {
        this.finalValue = finalValue;
    }

    public boolean isImproved() {
        return improved;
    }

    public void setImproved(boolean improved) {
        this.improved = improved;
    }

    public int getPointsRemaining() {
        return pointsRemaining;
    }

    public void setPointsRemaining(int pointsRemaining) {
        this.pointsRemaining = pointsRemaining;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
