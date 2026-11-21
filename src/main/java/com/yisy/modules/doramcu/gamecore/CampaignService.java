package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for campaign/adventure management across all game modules.
 */
public interface CampaignService {

    /**
     * List all campaigns.
     *
     * @return list of campaigns
     */
    List<Campaign> listCampaigns();

    /**
     * Get a campaign by ID.
     *
     * @param campaignId the campaign ID
     * @return the campaign or null if not found
     */
    Campaign getCampaign(Long campaignId);

    /**
     * Create a new campaign.
     *
     * @param name the campaign name
     * @param description the campaign description
     * @return the created campaign
     */
    Campaign createCampaign(String name, String description);

    /**
     * Add a character to a campaign.
     *
     * @param campaignId the campaign ID
     * @param characterId the character ID
     */
    void addCharacterToCampaign(Long campaignId, Long characterId);

    /**
     * A campaign.
     */
    static class Campaign {
        private Long id;
        private String name;
        private String description;
        private String status;
        private int currentSession;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCurrentSession() { return currentSession; }
        public void setCurrentSession(int currentSession) { this.currentSession = currentSession; }
    }
}
