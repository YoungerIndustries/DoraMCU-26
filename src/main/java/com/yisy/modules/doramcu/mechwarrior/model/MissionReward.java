package com.yisy.modules.doramcu.mechwarrior.model;

import java.util.UUID;

/**
 * Represents a reward for completing a MechWarrior mission.
 * <p>
 * Rewards include credits, salvage value, experience points, reputation,
 * and unlockable items like mechs, weapons, or traits.
 * </p>
 */
public class MissionReward {

    public enum RewardType {
        CREDITS,           // Credits for spending
        SALVAGE,           // Salvage value from destroyed mechs
        EXPERIENCE,        // Experience points for pilots
        REPUTATION,        // Reputation with a faction
        MECH_UNLOCK,       // Unlock a new mech
        WEAPON_UNLOCK,     // Unlock a new weapon
        TRAIT_UNLOCK,      // Unlock a pilot trait
        SKILL_BONUS,       // Bonus skill points
        REPAIR_CREDIT,     // Credits for mech repairs
        CONTRACT_BONUS     // Bonus for completing contract chain
    }

    private String id;
    private RewardType type;
    private int amount;            // amount of reward (credits, XP, etc.)
    private String itemName;       // name of item for unlock rewards
    private String description;    // description of the reward
    private String faction;        // faction for reputation rewards
    private boolean claimed;       // has the reward been claimed
    private boolean isBonus;       // bonus reward for exceptional performance
    private String requirement;    // requirement text for bonus rewards

    public MissionReward() {
    }

    public MissionReward(RewardType type, int amount) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.claimed = false;
        this.isBonus = false;
    }

    public MissionReward(RewardType type, String itemName, String description) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.amount = 0;
        this.claimed = false;
        this.isBonus = false;
    }

    /**
     * Claim the reward.
     */
    public void claim() {
        this.claimed = true;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RewardType getType() {
        return type;
    }

    public void setType(RewardType type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public boolean isBonus() {
        return isBonus;
    }

    public void setBonus(boolean bonus) {
        isBonus = bonus;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }
}
