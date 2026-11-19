package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingRecipe;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCraftingRecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Lord of the Rings crafting recipes.
 */
@Service
public class LotrCraftingService {

    private static final Logger log = LoggerFactory.getLogger(LotrCraftingService.class);

    private final LotrCraftingRecipeRepository repository;

    public LotrCraftingService(LotrCraftingRecipeRepository repository) {
        this.repository = repository;
    }

    public List<LotrCraftingRecipe> findAll() {
        return repository.findAll();
    }

    public LotrCraftingRecipe findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    }

    public List<LotrCraftingRecipe> findBySkill(String skill) {
        return repository.findBySkillRequired(skill);
    }

    @Transactional
    public LotrCraftingRecipe createRecipe(String name, String description, String resultItem,
                                            String skillRequired, int skillDc, int goldCost) {
        LotrCraftingRecipe recipe = new LotrCraftingRecipe();
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setResultItem(resultItem);
        recipe.setSkillRequired(skillRequired);
        recipe.setSkillDc(skillDc);
        recipe.setGoldCost(goldCost);
        recipe.setCraftingTime(1);
        log.info("Created LOTR crafting recipe: {}", name);
        return repository.save(recipe);
    }

    @Transactional
    public LotrCraftingRecipe save(LotrCraftingRecipe recipe) {
        return repository.save(recipe);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
