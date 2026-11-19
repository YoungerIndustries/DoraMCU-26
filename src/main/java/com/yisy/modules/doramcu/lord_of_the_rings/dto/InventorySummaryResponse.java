package com.yisy.modules.doramcu.lord_of_the_rings.dto;

import java.util.List;

/**
 * Response summarizing a character's inventory in LOTR.
 */
public class InventorySummaryResponse {

    private Long characterId;
    private String characterName;
    private int totalItems;
    private int equippedItems;
    private double totalWeight;
    private double maxWeight;
    private List<String> equippedItemNames;

    public InventorySummaryResponse() {
    }

    public InventorySummaryResponse(Long characterId, String characterName, int totalItems,
                                    int equippedItems, double totalWeight, double maxWeight,
                                    List<String> equippedItemNames) {
        this.characterId = characterId;
        this.characterName = characterName;
        this.totalItems = totalItems;
        this.equippedItems = equippedItems;
        this.totalWeight = totalWeight;
        this.maxWeight = maxWeight;
        this.equippedItemNames = equippedItemNames;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getEquippedItems() {
        return equippedItems;
    }

    public void setEquippedItems(int equippedItems) {
        this.equippedItems = equippedItems;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public List<String> getEquippedItemNames() {
        return equippedItemNames;
    }

    public void setEquippedItemNames(List<String> equippedItemNames) {
        this.equippedItemNames = equippedItemNames;
    }
}
