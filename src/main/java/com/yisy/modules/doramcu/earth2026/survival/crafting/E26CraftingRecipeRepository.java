package com.yisy.modules.doramcu.earth2026.survival.crafting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface E26CraftingRecipeRepository extends JpaRepository<E26CraftingRecipe, Long> {

    List<E26CraftingRecipe> findByCategory(String category);

    List<E26CraftingRecipe> findByRequiredSkill(String requiredSkill);

    List<E26CraftingRecipe> findByRequiresWorkshopTrue();

    boolean existsByName(String name);
}
