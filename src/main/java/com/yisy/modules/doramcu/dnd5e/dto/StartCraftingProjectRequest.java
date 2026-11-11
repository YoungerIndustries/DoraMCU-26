package com.yisy.modules.doramcu.dnd5e.dto;

import java.util.List;

/**
 * Request DTO for starting a D&D 5e crafting project.
 */
public class StartCraftingProjectRequest {

    private Long characterId;
    private Long recipeId;
    private boolean isGroupCraft = false;
    private List<Long> groupMemberIds;
    private String notes;

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public boolean isGroupCraft() { return isGroupCraft; }
    public void setGroupCraft(boolean groupCraft) { isGroupCraft = groupCraft; }

    public List<Long> getGroupMemberIds() { return groupMemberIds; }
    public void setGroupMemberIds(List<Long> groupMemberIds) { this.groupMemberIds = groupMemberIds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
