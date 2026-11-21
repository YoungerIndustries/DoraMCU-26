package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for inventory management across all game modules.
 */
public interface InventoryService {

    /**
     * Get inventory for a character.
     *
     * @param characterId the character ID
     * @return list of inventory items
     */
    List<InventoryItem> getInventory(Long characterId);

    /**
     * Add an item to a character's inventory.
     *
     * @param characterId the character ID
     * @param item the item to add
     * @return the added item
     */
    InventoryItem addItem(Long characterId, InventoryItem item);

    /**
     * Remove an item from a character's inventory.
     *
     * @param itemId the item ID to remove
     * @return true if removed
     */
    boolean removeItem(Long itemId);

    /**
     * Use an item from inventory.
     *
     * @param characterId the character ID
     * @param itemId the item ID to use
     * @return the usage result message
     */
    String useItem(Long characterId, Long itemId);

    /**
     * An inventory item.
     */
    static class InventoryItem {
        private Long id;
        private String name;
        private String description;
        private String itemType;
        private int quantity;
        private int weight;
        private boolean equipped;
        private String magicRarity;
        private String specialProperties;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
        public boolean isEquipped() { return equipped; }
        public void setEquipped(boolean equipped) { this.equipped = equipped; }
        public String getMagicRarity() { return magicRarity; }
        public void setMagicRarity(String magicRarity) { this.magicRarity = magicRarity; }
        public String getSpecialProperties() { return specialProperties; }
        public void setSpecialProperties(String specialProperties) { this.specialProperties = specialProperties; }
    }
}
