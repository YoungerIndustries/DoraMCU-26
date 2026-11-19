package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request to create a LOTR crafting recipe.
 */
public class CreateCraftingRecipeRequest {

    private String name;
    private String description;
    private String itemType;
    private String rarity;
    private String materials;
    private Integer craftingTime;
    private Integer goldCost;
    private String requiredCalling;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getMaterials() {
        return materials;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public Integer getCraftingTime() {
        return craftingTime;
    }

    public void setCraftingTime(Integer craftingTime) {
        this.craftingTime = craftingTime;
    }

    public Integer getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(Integer goldCost) {
        this.goldCost = goldCost;
    }

    public String getRequiredCalling() {
        return requiredCalling;
    }

    public void setRequiredCalling(String requiredCalling) {
        this.requiredCalling = requiredCalling;
    }
}
