package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCraftingRecipeRepository extends JpaRepository<LotrCraftingRecipe, Long> {

    List<LotrCraftingRecipe> findByNameContainingIgnoreCase(String name);

    List<LotrCraftingRecipe> findBySkillRequired(String skillRequired);
}
