package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request to add a character to a campaign.
 */
public class AddCampaignCharacterRequest {

    private Long campaignId;
    private Long characterId;

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }
}
