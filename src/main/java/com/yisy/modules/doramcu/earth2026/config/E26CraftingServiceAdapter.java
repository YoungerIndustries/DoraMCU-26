package com.yisy.modules.doramcu.earth2026.config;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.survival.crafting.E26CraftingProject;
import com.yisy.modules.doramcu.earth2026.survival.crafting.E26CraftingRecipe;
import com.yisy.modules.doramcu.earth2026.survival.crafting.E26CraftingService;
import com.yisy.modules.doramcu.gamecore.CraftingService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that wraps E26CraftingService to implement the shared CraftingService interface.
 */

@Service
public class E26CraftingServiceAdapter implements CraftingService {

    private final E26CraftingService delegate;

    public E26CraftingServiceAdapter(E26CraftingService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<CraftingRecipe> listRecipes() {
        List<E26CraftingRecipe> recipes = delegate.listRecipes();
        return recipes.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CraftingRecipe getRecipe(Long recipeId) {
        E26CraftingRecipe recipe = delegate.getRecipe(recipeId);
        return toDto(recipe);
    }

    @Override
    public CraftingProject startProject(Long characterId, Long recipeId) {
        E26CraftingProject project = delegate.startProject(characterId, recipeId);
        return toDto(project);
    }

    @Override
    public CraftingProject progressProject(Long projectId) {
        E26CraftingProject project = delegate.progressProject(projectId);
        return toDto(project);
    }

    @Override
    public CraftingProject completeProject(Long projectId) {
        E26CraftingProject project = delegate.completeProject(projectId);
        return toDto(project);
    }

    private CraftingRecipe toDto(E26CraftingRecipe recipe) {
        CraftingRecipe dto = new CraftingRecipe();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setCategory(recipe.getCategory());
        dto.setDifficulty(recipe.getDifficulty());
        dto.setTimeRequired(recipe.getTimeRequired());
        dto.setMaterialsRequired(recipe.getMaterialsJson());
        dto.setResultItem(recipe.getResultResource());
        return dto;
    }

    private CraftingProject toDto(E26CraftingProject project) {
        CraftingProject dto = new CraftingProject();
        dto.setId(project.getId());
        dto.setCharacterId(project.getCharacterId());
        dto.setRecipeId(project.getRecipeId());
        dto.setStatus(project.getStatus());
        dto.setProgress(project.getProgress());
        dto.setTotalProgress(project.getTotalProgress());
        return dto;
    }
}
