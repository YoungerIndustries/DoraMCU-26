package com.yisy.modules.doramcu.earth2026.tycoon;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a building in the Corporation Tycoon game.
 * Buildings generate resources, provide bonuses, and can be upgraded.
 */
public class TycoonBuilding {

    public enum BuildingType {
        HEADQUARTERS("Headquarters", "Main corporate office, generates influence and credits"),
        RESEARCH_LAB("Research Lab", "Research facility for developing new technologies"),
        MANUFACTURING_PLANT("Manufacturing Plant", "Produces goods and equipment for sale"),
        TRADING_POST("Trading Post", "Generates credits through trade operations"),
        SECURITY_OFFICE("Security Office", "Trains security personnel and mercenaries"),
        DATA_CENTER("Data Center", "Processes data and generates intelligence"),
        RECRUITING_OFFICE("Recruiting Office", "Attracts and trains employees"),
        BANK_BRANCH("Bank Branch", "Generates passive income through financial operations"),
        MEDIA_OFFICE("Media Office", "Generates influence and public relations"),
        PORT_FACILITY("Port Facility", "Handles logistics and shipping operations"),
        MINE("Mine", "Extracts raw materials and resources"),
        VEHICLE_DEPOT("Vehicle Depot", "Stores and maintains vehicles"),
        HOTEL("Hotel", "Provides lodging and generates income"),
        WAREHOUSE("Warehouse", "Stores goods and materials"),
        POWER_PLANT("Power Plant", "Generates energy for operations"),
        COMMUNICATION_TOWER("Communication Tower", "Provides network coverage"),
        AGRICULTURAL_COMPLEX("Agricultural Complex", "Produces food and biological resources");

        private final String displayName;
        private final String description;

        BuildingType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    private BuildingType type;
    private int level;
    private int maxLevel;
    private long creditsPerHour;
    private long influencePerHour;
    private long techPerHour;
    private long materialsPerHour;
    private long upgradeCost;
    private int employeeSlots;
    private boolean constructed;

    public TycoonBuilding() {
    }

    public TycoonBuilding(BuildingType type) {
        this.type = type;
        this.level = 0;
        this.maxLevel = 10;
        this.constructed = false;
        this.calculateStats();
    }

    private void calculateStats() {
        if (level == 0) {
            // Not constructed yet
            this.creditsPerHour = 0;
            this.influencePerHour = 0;
            this.techPerHour = 0;
            this.materialsPerHour = 0;
            this.employeeSlots = 0;
            return;
        }

        switch (type) {
            case HEADQUARTERS:
                this.creditsPerHour = 100 * level;
                this.influencePerHour = 50 * level;
                this.techPerHour = 0;
                this.materialsPerHour = 0;
                this.employeeSlots = 5 * level;
                break;
            case RESEARCH_LAB:
                this.creditsPerHour = 10 * level;
                this.influencePerHour = 5 * level;
                this.techPerHour = 100 * level;
                this.materialsPerHour = 0;
                this.employeeSlots = 3 * level;
                break;
            case MANUFACTURING_PLANT:
                this.creditsPerHour = 50 * level;
                this.influencePerHour = 0;
                this.techPerHour = 0;
                this.materialsPerHour = 80 * level;
                this.employeeSlots = 10 * level;
                break;
            case TRADING_POST:
                this.creditsPerHour = 200 * level;
                this.influencePerHour = 10 * level;
                this.techPerHour = 0;
                this.materialsPerHour = 0;
                this.employeeSlots = 2 * level;
                break;
            case SECURITY_OFFICE:
                this.creditsPerHour = 20 * level;
                this.influencePerHour = 30 * level;
                this.techPerHour = 0;
                this.materialsPerHour = 20 * level;
                this.employeeSlots = 8 * level;
                break;
            case DATA_CENTER:
                this.creditsPerHour = 30 * level;
                this.influencePerHour = 20 * level;
                this.techPerHour = 50 * level;
                this.materialsPerHour = 0;
                this.employeeSlots = 4 * level;
                break;
            case RECRUITING_OFFICE:
                this.creditsPerHour = 10 * level;
                this.influencePerHour = 40 * level;
                this.techPerHour = 0;
                this.materialsPerHour = 0;
                this.employeeSlots = 6 * level;
                break;
            case BANK_BRANCH:
                this.creditsPerHour = 300 * level;
                this.influencePerHour = 10 * level;
                this.techPerHour = 0;
                this.materialsPerHour = 0;
                this.employeeSlots = 3 * level;
                break;
            case MEDIA_OFFICE:
                this.creditsPerHour = 40 * level;
                this.influencePerHour = 100 * level;
                this.techPerHour = 0;
                this.materialsPerHour = 0;
                this.employeeSlots = 4 * level;
                break;
            case PORT_FACILITY:
                this.creditsPerHour = 80 * level;
                this.influencePerHour = 0;
                this.techPerHour = 0;
                this.materialsPerHour = 60 * level;
                this.employeeSlots = 7 * level;
                break;
        }

        // Calculate upgrade cost for next level
        if (level < maxLevel) {
            this.upgradeCost = (long) (5000 * Math.pow(1.5, level));
        } else {
            this.upgradeCost = -1; // Max level
        }
    }

    public void construct() {
        this.level = 1;
        this.constructed = true;
        this.calculateStats();
    }

    public void upgrade() {
        if (level < maxLevel) {
            this.level++;
            this.calculateStats();
        }
    }

    public long getConstructionCost() {
        // Base construction cost
        return (long) (2000 * Math.pow(1.2, level));
    }

    // Getters and setters
    public BuildingType getType() {
        return type;
    }

    public void setType(BuildingType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.calculateStats();
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public long getCreditsPerHour() {
        return creditsPerHour;
    }

    public void setCreditsPerHour(long creditsPerHour) {
        this.creditsPerHour = creditsPerHour;
    }

    public long getInfluencePerHour() {
        return influencePerHour;
    }

    public void setInfluencePerHour(long influencePerHour) {
        this.influencePerHour = influencePerHour;
    }

    public long getTechPerHour() {
        return techPerHour;
    }

    public void setTechPerHour(long techPerHour) {
        this.techPerHour = techPerHour;
    }

    public long getMaterialsPerHour() {
        return materialsPerHour;
    }

    public void setMaterialsPerHour(long materialsPerHour) {
        this.materialsPerHour = materialsPerHour;
    }

    public long getUpgradeCost() {
        return upgradeCost;
    }

    public void setUpgradeCost(long upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    public int getEmployeeSlots() {
        return employeeSlots;
    }

    public void setEmployeeSlots(int employeeSlots) {
        this.employeeSlots = employeeSlots;
    }

    public boolean isConstructed() {
        return constructed;
    }

    public void setConstructed(boolean constructed) {
        this.constructed = constructed;
    }

    @Override
    public String toString() {
        return "TycoonBuilding{" +
                "type=" + type +
                ", level=" + level +
                ", constructed=" + constructed +
                ", creditsPerHour=" + creditsPerHour +
                ", influencePerHour=" + influencePerHour +
                '}';
    }
}
