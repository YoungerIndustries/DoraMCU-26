package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Request DTO for creating a D&D 5e inventory item.
 */
public class CreateDnd5eInventoryItemRequest {

    private Long characterId;
    private String name;
    private String description;
    private String itemType;
    private String rarity;
    private int quantity = 1;
    private double weight;
    private int value;
    private boolean equipped;
    private boolean attuned;
    private boolean identified = true;
    private Integer charges;
    private Integer maxCharges;
    private Integer propertyAc;
    private String propertyDamage;
    private String propertyRange;
    private String propertyHealing;
    private String notes;

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean equipped) { this.equipped = equipped; }

    public boolean isAttuned() { return attuned; }
    public void setAttuned(boolean attuned) { this.attuned = attuned; }

    public boolean isIdentified() { return identified; }
    public void setIdentified(boolean identified) { this.identified = identified; }

    public Integer getCharges() { return charges; }
    public void setCharges(Integer charges) { this.charges = charges; }

    public Integer getMaxCharges() { return maxCharges; }
    public void setMaxCharges(Integer maxCharges) { this.maxCharges = maxCharges; }

    public Integer getPropertyAc() { return propertyAc; }
    public void setPropertyAc(Integer propertyAc) { this.propertyAc = propertyAc; }

    public String getPropertyDamage() { return propertyDamage; }
    public void setPropertyDamage(String propertyDamage) { this.propertyDamage = propertyDamage; }

    public String getPropertyRange() { return propertyRange; }
    public void setPropertyRange(String propertyRange) { this.propertyRange = propertyRange; }

    public String getPropertyHealing() { return propertyHealing; }
    public void setPropertyHealing(String propertyHealing) { this.propertyHealing = propertyHealing; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
