package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * DTO for adding a character to a campaign.
 */
public class AddCampaignCharacterRequest {

    private Long characterId;
    private String role;

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
