package com.yisy.modules.doramcu.dnd5e.dto;

import java.math.BigDecimal;

/**
 * Response DTO for inventory summary (total weight, value, item count).
 */
public class InventorySummaryResponse {

    private Long characterId;
    private int totalItems;
    private BigDecimal totalWeight;
    private int totalValue;
    private int equippedCount;
    private int attunedCount;

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public BigDecimal getTotalWeight() { return totalWeight; }
    public void setTotalWeight(BigDecimal totalWeight) { this.totalWeight = totalWeight; }

    public int getTotalValue() { return totalValue; }
    public void setTotalValue(int totalValue) { this.totalValue = totalValue; }

    public int getEquippedCount() { return equippedCount; }
    public void setEquippedCount(int equippedCount) { this.equippedCount = equippedCount; }

    public int getAttunedCount() { return attunedCount; }
    public void setAttunedCount(int attunedCount) { this.attunedCount = attunedCount; }
}
