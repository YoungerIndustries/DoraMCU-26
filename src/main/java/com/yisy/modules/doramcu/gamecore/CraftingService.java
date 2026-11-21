package com.yisy.modules.doramcu.gamecore;

import java.util.List;

/**
 * Shared interface for crafting/manufacturing across all game modules.
 * <p>
 * D&amp;D uses crafting for item creation, Earth2026 uses it for manufacturing,
 * and Traveller uses it for ship repairs and equipment creation.
 * </p>
 */
public interface CraftingService {

    /**
     * List all available crafting recipes.
     *
     * @return list of crafting recipes
     */
    List<CraftingRecipe> listRecipes();

    /**
     * Get a crafting recipe by ID.
     *
     * @param recipeId the recipe ID
     * @return the recipe or null if not found
     */
    CraftingRecipe getRecipe(Long recipeId);

    /**
     * Start a new crafting project.
     *
     * @param characterId the character ID
     * @param recipeId the recipe ID
     * @return the created project
     */
    CraftingProject startProject(Long characterId, Long recipeId);

    /**
     * Progress a crafting project.
     *
     * @param projectId the project ID
     * @return the updated project
     */
    CraftingProject progressProject(Long projectId);

    /**
     * Complete a crafting project.
     *
     * @param projectId the project ID
     * @return the completed project with the crafted item
     */
    CraftingProject completeProject(Long projectId);

    /**
     * A crafting recipe.
     */
    static class CraftingRecipe {
        private Long id;
        private String name;
        private String description;
        private String category;
        private int difficulty;
        private int timeRequired;
        private String materialsRequired;
        private String resultItem;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getDifficulty() { return difficulty; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
        public int getTimeRequired() { return timeRequired; }
        public void setTimeRequired(int timeRequired) { this.timeRequired = timeRequired; }
        public String getMaterialsRequired() { return materialsRequired; }
        public void setMaterialsRequired(String materialsRequired) { this.materialsRequired = materialsRequired; }
        public String getResultItem() { return resultItem; }
        public void setResultItem(String resultItem) { this.resultItem = resultItem; }
    }

    /**
     * A crafting project in progress.
     */
    static class CraftingProject {
        private Long id;
        private Long characterId;
        private Long recipeId;
        private String status;
        private int progress;
        private int totalProgress;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCharacterId() { return characterId; }
        public void setCharacterId(Long characterId) { this.characterId = characterId; }
        public Long getRecipeId() { return recipeId; }
        public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getTotalProgress() { return totalProgress; }
        public void setTotalProgress(int totalProgress) { this.totalProgress = totalProgress; }
    }
}
