package com.yisy.modules.doramcu.traveller.shipcombat;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages campaigns across multiple ship-to-ship battles.
 * <p>
 * A campaign tracks a series of related battles, recording outcomes,
 * ships lost, and overall progress. Campaigns are stored in memory.
 * </p>
 */

@Service
public class ShipCombatCampaignService {

    private static final Logger log = LoggerFactory.getLogger(ShipCombatCampaignService.class);

    private final Map<String, Campaign> campaigns = new ConcurrentHashMap<>();

    /**
     * Represents a campaign of battles.
     */
    public static class Campaign {
        private final String id;
        private final String name;
        private final String description;
        private final String sector;
        private final String faction0;
        private final String faction1;
        private final List<CampaignBattle> battles;
        private final Date createdAt;

        private Campaign(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description != null ? builder.description : "";
            this.sector = builder.sector != null ? builder.sector : "Unknown Sector";
            this.faction0 = builder.faction0 != null ? builder.faction0 : "Faction 0";
            this.faction1 = builder.faction1 != null ? builder.faction1 : "Faction 1";
            this.battles = new ArrayList<>();
            this.createdAt = new Date();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getSector() { return sector; }
        public String getFaction0() { return faction0; }
        public String getFaction1() { return faction1; }
        public List<CampaignBattle> getBattles() { return Collections.unmodifiableList(battles); }
        public Date getCreatedAt() { return createdAt; }

        void addBattle(CampaignBattle battle) {
            this.battles.add(battle);
        }

        /**
         * Returns the number of battles won by faction 0.
         */
        public int getFaction0Wins() {
            return (int) battles.stream()
                    .filter(b -> b.getOutcome() != null && b.getOutcome().contains("SIDE_0"))
                    .count();
        }

        /**
         * Returns the number of battles won by faction 1.
         */
        public int getFaction1Wins() {
            return (int) battles.stream()
                    .filter(b -> b.getOutcome() != null && b.getOutcome().contains("SIDE_1"))
                    .count();
        }

        /**
         * Returns the number of stalemates.
         */
        public int getStalemates() {
            return (int) battles.stream()
                    .filter(b -> b.getOutcome() != null &&
                            (b.getOutcome().contains("STALEMATE") || b.getOutcome().contains("MUTUAL")))
                    .count();
        }

        /**
         * Returns a summary map of the campaign.
         */
        public Map<String, Object> toSummary() {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", id);
            summary.put("name", name);
            summary.put("sector", sector);
            summary.put("faction0", faction0);
            summary.put("faction1", faction1);
            summary.put("totalBattles", battles.size());
            summary.put("faction0Wins", getFaction0Wins());
            summary.put("faction1Wins", getFaction1Wins());
            summary.put("stalemates", getStalemates());

            List<Map<String, Object>> battleSummaries = new ArrayList<>();
            for (CampaignBattle battle : battles) {
                Map<String, Object> b = new HashMap<>();
                b.put("battleId", battle.getBattleId());
                b.put("outcome", battle.getOutcome());
                b.put("rounds", battle.getRounds());
                b.put("date", battle.getDate());
                battleSummaries.add(b);
            }
            summary.put("battles", battleSummaries);
            return summary;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String description;
            private String sector;
            private String faction0;
            private String faction1;

            public Builder id(String id) { this.id = id; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder sector(String sector) { this.sector = sector; return this; }
            public Builder faction0(String faction0) { this.faction0 = faction0; return this; }
            public Builder faction1(String faction1) { this.faction1 = faction1; return this; }

            public Campaign build() {
                return new Campaign(this);
            }
        }
    }

    /**
     * Represents a battle record within a campaign.
     */
    public static class CampaignBattle {
        private final String battleId;
        private final String outcome;
        private final int rounds;
        private final Date date;

        public CampaignBattle(String battleId, String outcome, int rounds) {
            this.battleId = battleId;
            this.outcome = outcome;
            this.rounds = rounds;
            this.date = new Date();
        }

        public String getBattleId() { return battleId; }
        public String getOutcome() { return outcome; }
        public int getRounds() { return rounds; }
        public Date getDate() { return date; }
    }

    /**
     * Creates a new campaign.
     */
    public Campaign createCampaign(String id, String name, String description,
                                    String sector, String faction0, String faction1) {
        Campaign campaign = Campaign.builder()
                .id(id)
                .name(name)
                .description(description)
                .sector(sector)
                .faction0(faction0)
                .faction1(faction1)
                .build();

        campaigns.put(id, campaign);
        log.info("Created campaign {}: {} in {}", id, name, sector);
        return campaign;
    }

    /**
     * Gets a campaign by ID.
     */
    public Campaign getCampaign(String id) {
        Campaign campaign = campaigns.get(id);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign not found: " + id);
        }
        return campaign;
    }

    /**
     * Adds a battle result to a campaign.
     */
    public Campaign addBattle(String campaignId, String battleId, String outcome, int rounds) {
        Campaign campaign = getCampaign(campaignId);
        campaign.addBattle(new CampaignBattle(battleId, outcome, rounds));
        log.info("Added battle {} to campaign {}: {}", battleId, campaignId, outcome);
        return campaign;
    }

    /**
     * Lists all campaign IDs.
     */
    public List<String> listCampaigns() {
        return new ArrayList<>(campaigns.keySet());
    }

    /**
     * Deletes a campaign.
     */
    public boolean deleteCampaign(String id) {
        Campaign removed = campaigns.remove(id);
        if (removed != null) {
            log.info("Deleted campaign: {}", id);
        }
        return removed != null;
    }
}
