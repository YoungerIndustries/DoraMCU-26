package com.yisy.modules.doramcu.dnd5e.dto;

/**
 * Request DTO for creating a D&D 5e crafting recipe.
 */
public class CreateCraftingRecipeRequest {

    private String name;
    private String description;
    private String category;
    private String rarity;
    private String requiredTool;
    private String requiredAbility;
    private int dc;
    private int goldCost;
    private int hoursRequired;
    private String materialComponents;
    private String resultItemName;
    private String resultItemType;
    private int resultQuantity = 1;
    private boolean isMagicItem = false;
    private String prerequisites;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getRequiredTool() { return requiredTool; }
    public void setRequiredTool(String requiredTool) { this.requiredTool = requiredTool; }

    public String getRequiredAbility() { return requiredAbility; }
    public void setRequiredAbility(String requiredAbility) { this.requiredAbility = requiredAbility; }

    public int getDc() { return dc; }
    public void setDc(int dc) { this.dc = dc; }

    public int getGoldCost() { return goldCost; }
    public void setGoldCost(int goldCost) { this.goldCost = goldCost; }

    public int getHoursRequired() { return hoursRequired; }
    public void setHoursRequired(int hoursRequired) { this.hoursRequired = hoursRequired; }

    public String getMaterialComponents() { return materialComponents; }
    public void setMaterialComponents(String materialComponents) { this.materialComponents = materialComponents; }

    public String getResultItemName() { return resultItemName; }
    public void setResultItemName(String resultItemName) { this.resultItemName = resultItemName; }

    public String getResultItemType() { return resultItemType; }
    public void setResultItemType(String resultItemType) { this.resultItemType = resultItemType; }

    public int getResultQuantity() { return resultQuantity; }
    public void setResultQuantity(int resultQuantity) { this.resultQuantity = resultQuantity; }

    public boolean isMagicItem() { return isMagicItem; }
    public void setMagicItem(boolean magicItem) { isMagicItem = magicItem; }

    public String getPrerequisites() { return prerequisites; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }
}
