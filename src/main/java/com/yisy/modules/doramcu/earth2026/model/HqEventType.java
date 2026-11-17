package com.yisy.modules.doramcu.earth2026.model;

import java.math.BigDecimal;

/**
 * Enum defining the types of events that can occur in the Younger Industries HQ.
 * Each event has a duration, frequency, and potential rewards/penalties.
 */
public enum HqEventType {

    TRADE_SHOW("Trade Show", "A major trade exhibition opens in the HQ lobby. Corporations can set up booths and trade resources at discounted rates.",
            "info", 48, 168, "+20% trade prices", "Requires office level 2+"),

    SECURITY_BREACH("Security Breach", "Unauthorized access detected in the HQ basement. Corporations can help security investigate and earn reputation.",
            "error", 12, 720, "Reputation +5, Credits reward", "Risk of equipment damage"),

    POWER_OUTAGE("Power Outage", "A power grid failure affects the HQ. Corporations with backup generators can sell power to others.",
            "warning", 6, 336, "Credits from power sales", "Kiosk upgrades disabled during outage"),

    FIRE_DRILL("Fire Drill", "Mandatory fire drill evacuates all floors. Quick response earns bonus reputation.",
            "warning", 1, 168, "Reputation +2", "Brief interruption to operations"),

    VIP_VISIT("VIP Visit", "A high-profile visitor arrives at the HQ. Corporations can request meetings for business opportunities.",
            "success", 24, 240, "Special contract offer, Reputation +10", "Requires office level 3+"),

    CORPORATION_PARTY("Corporation Party", "A celebration event in the lobby. Networking boosts morale and unlocks social bonuses.",
            "info", 12, 168, "Employee morale +15%", "Entry fee required"),

    SYSTEM_HACK("System Hack", "A cyber attack targets HQ networks. Corporations with secure terminals can help defend and earn rewards.",
            "error", 8, 504, "Tech resources +50, Reputation +5", "Data loss risk without secure terminal"),

    RIVAL_SABOTAGE("Rival Sabotage", "A rival corporation attempts sabotage. Help security thwart the attack.",
            "error", 6, 336, "Reputation +8, Credits reward", "Equipment damage if failed"),

    RECRUITMENT_FAIR("Recruitment Fair", "Top AI talent gathers at the HQ. Hire characters at reduced salaries.",
            "success", 24, 240, "Hiring cost -30%", "Limited availability"),

    TECHNOLOGY_DEMO("Technology Demo", "Younger Industries showcases new technology. Early adopters get exclusive access.",
            "info", 12, 168, "Research speed +25%", "Requires research console upgrade"),

    INSPECTION("Regulatory Inspection", "Government inspectors audit the HQ. Compliant corporations receive bonuses.",
            "warning", 4, 720, "Reputation +3, Tax reduction", "Fines for non-compliance"),

    BLACK_MARKET("Black Market", "An underground market appears in the basement. High-risk, high-reward trading.",
            "warning", 6, 168, "Rare items available", "Reputation risk if caught"),

    PORTAL_ANOMALY("Portal Anomaly", "Unusual energy readings from basement portals. Investigate for discovery rewards.",
            "info", 12, 504, "Tech resources +100, Discovery points", "Energy cost to investigate"),

    LOBBY_AUCTION("Lobby Auction", "Rare items and resources are auctioned in the lobby. Bid credits to win.",
            "info", 6, 168, "Rare items and upgrades", "Credits spent on bidding"),

    EMERGENCY_DRILL("Emergency Drill", "Random emergency simulation. Test your corporation's response capabilities.",
            "warning", 2, 72, "Reputation +1, Employee training", "Brief operations pause");

    private final String displayName;
    private final String description;
    private final String severity;
    private final int durationHours;
    private final int cooldownHours;
    private final String reward;
    private final String penalty;

    HqEventType(String displayName, String description, String severity, int durationHours, int cooldownHours, String reward, String penalty) {
        this.displayName = displayName;
        this.description = description;
        this.severity = severity;
        this.durationHours = durationHours;
        this.cooldownHours = cooldownHours;
        this.reward = reward;
        this.penalty = penalty;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public int getCooldownHours() {
        return cooldownHours;
    }

    public String getReward() {
        return reward;
    }

    public String getPenalty() {
        return penalty;
    }
}
