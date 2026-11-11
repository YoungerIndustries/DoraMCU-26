package com.yisy.modules.doramcu.traveller;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates air battle experiences into a campaign.
 */
public class AirCampaignLog {
    private String campaignId;
    private String campaignName;
    private String nationality;
    private String startDate;
    private String endDate;
    private List<AirBattleExperience> battles = new ArrayList<>();
    private int totalAttackerLosses;
    private int totalDefenderLosses;

    public AirCampaignLog() {}

    public AirCampaignLog(String campaignId, String campaignName) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
    }

    public void addBattle(AirBattleExperience battle) {
        battles.add(battle);
    }

    public String generateSummary() {
        return campaignName + ": " + battles.size() + " battles fought, " +
                totalAttackerLosses + " attacker losses, " + totalDefenderLosses + " defender losses";
    }

    public String getCampaignId() { return campaignId; }
    public String getCampaignName() { return campaignName; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public List<AirBattleExperience> getBattles() { return battles; }
    public int getTotalAttackerLosses() { return totalAttackerLosses; }
    public void setTotalAttackerLosses(int losses) { this.totalAttackerLosses = losses; }
    public int getTotalDefenderLosses() { return totalDefenderLosses; }
    public void setTotalDefenderLosses(int losses) { this.totalDefenderLosses = losses; }
}
