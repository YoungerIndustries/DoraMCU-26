package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Request DTO for updating a D&D 5e inventory item.
 */
public class UpdateDnd5eInventoryItemRequest {

    private String name;
    private String description;
    private String itemType;
    private String rarity;
    private Integer quantity;
    private Double weight;
    private Integer value;
    private Boolean equipped;
    private Boolean attuned;
    private Boolean identified;
    private Integer charges;
    private Integer maxCharges;
    private Integer propertyAc;
    private String propertyDamage;
    private String propertyRange;
    private String propertyHealing;
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public Boolean getEquipped() { return equipped; }
    public void setEquipped(Boolean equipped) { this.equipped = equipped; }

    public Boolean getAttuned() { return attuned; }
    public void setAttuned(Boolean attuned) { this.attuned = attuned; }

    public Boolean getIdentified() { return identified; }
    public void setIdentified(Boolean identified) { this.identified = identified; }

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
