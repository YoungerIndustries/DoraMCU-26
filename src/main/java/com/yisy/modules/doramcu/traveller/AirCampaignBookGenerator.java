package com.yisy.modules.doramcu.traveller;

/**
 * Generates narrative books from air campaign logs.
 */
public class AirCampaignBookGenerator {

    public String generateBook(AirCampaignLog campaign) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(campaign.getCampaignName()).append("\n\n");
        if (campaign.getNationality() != null) {
            sb.append("**Nationality:** ").append(campaign.getNationality()).append("\n\n");
        }
        if (campaign.getStartDate() != null) {
            sb.append("**Period:** ").append(campaign.getStartDate());
            if (campaign.getEndDate() != null) {
                sb.append(" - ").append(campaign.getEndDate());
            }
            sb.append("\n\n");
        }
        sb.append("---\n\n");
        sb.append("## Table of Contents\n\n");
        for (int i = 0; i < campaign.getBattles().size(); i++) {
            AirBattleExperience exp = campaign.getBattles().get(i);
            sb.append("1. [Battle ").append(i + 1).append("](#battle-").append(i + 1).append(")\n");
        }
        sb.append("\n---\n\n");
        for (int i = 0; i < campaign.getBattles().size(); i++) {
            AirBattleExperience exp = campaign.getBattles().get(i);
            sb.append("## Battle ").append(i + 1).append("\n\n");
            sb.append("**Air Force:** ").append(exp.getAirForceName()).append("\n");
            if (exp.getAltitude() != null) {
                sb.append("**Altitude:** ").append(exp.getAltitude()).append("\n");
            }
            if (exp.getCrtResult() != null) {
                sb.append("**CRT Result:** ").append(exp.getCrtResult()).append("\n");
            }
            sb.append("**Losses:** ").append(exp.getLosses()).append("\n\n");
            if (exp.getNarrative() != null) {
                sb.append(exp.getNarrative()).append("\n\n");
            }
            sb.append("---\n\n");
        }
        sb.append("## Epilogue\n\n");
        sb.append(campaign.generateSummary()).append("\n");
        return sb.toString();
    }
}
