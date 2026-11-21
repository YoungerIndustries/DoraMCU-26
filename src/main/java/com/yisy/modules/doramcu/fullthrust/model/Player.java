package com.yisy.modules.doramcu.fullthrust.model;
import java.util.*;

/**
 * Represents a player in the game.
 */
public class Player {
    private String id;
    private String name;
    private String faction;
    private List<String> shipIds; // Ships owned by this player
    private int credits;
    private boolean isAI; // Whether this player is controlled by AI
    private String aiDifficulty; // "easy", "medium", "hard" - only meaningful when isAI=true

    public Player() {
        this.shipIds = new ArrayList<>();
        this.credits = 1000;
        this.isAI = false;
        this.aiDifficulty = "medium";
    }

    public Player(String id, String name, String faction) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.shipIds = new ArrayList<>();
        this.credits = 1000;
        this.isAI = false;
        this.aiDifficulty = "medium";
    }

    public Player(String id, String name, String faction, boolean isAI) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.shipIds = new ArrayList<>();
        this.credits = 1000;
        this.isAI = isAI;
        this.aiDifficulty = "medium";
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public List<String> getShipIds() { return shipIds; }
    public void setShipIds(List<String> shipIds) { this.shipIds = shipIds; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    @com.fasterxml.jackson.annotation.JsonProperty("isAI")
    public boolean isAI() { return isAI; }
    public void setAI(boolean ai) { isAI = ai; }
    @com.fasterxml.jackson.annotation.JsonProperty("aiDifficulty")
    public String getAiDifficulty() { return aiDifficulty; }
    public void setAiDifficulty(String aiDifficulty) { this.aiDifficulty = aiDifficulty; }

    /**
     * Add a ship to this player
     */
    public void addShip(String shipId) {
        this.shipIds.add(shipId);
    }

    /**
     * Remove a ship from this player
     */
    public void removeShip(String shipId) {
        this.shipIds.remove(shipId);
    }
}
