package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for D&D 5e crafting recipes.
 */
public interface Dnd5eCraftingRecipeRepository extends JpaRepository<Dnd5eCraftingRecipe, Long> {

    List<Dnd5eCraftingRecipe> findByCategory(String category);

    List<Dnd5eCraftingRecipe> findByRequiredTool(String requiredTool);

    List<Dnd5eCraftingRecipe> findByRarity(String rarity);

    List<Dnd5eCraftingRecipe> findByCategoryAndRarity(String category, String rarity);

    @Query("SELECT r FROM Dnd5eCraftingRecipe r WHERE r.name LIKE %:keyword% OR r.description LIKE %:keyword%")
    List<Dnd5eCraftingRecipe> searchByNameOrDescription(@Param("keyword") String keyword);

    @Query("SELECT r FROM Dnd5eCraftingRecipe r WHERE r.requiredTool = :tool AND r.category = :category")
    List<Dnd5eCraftingRecipe> findByToolAndCategory(@Param("tool") String tool, @Param("category") String category);

    List<Dnd5eCraftingRecipe> findByIsMagicItemTrue();

    List<Dnd5eCraftingRecipe> findByIsMagicItemFalse();
}
