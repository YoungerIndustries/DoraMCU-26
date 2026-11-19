package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request to start a crafting project.
 */
public class StartCraftingProjectRequest {

    private Long characterId;
    private Long recipeId;

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }
}
