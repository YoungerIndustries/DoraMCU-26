package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for D&D 5e crafting projects.
 */
public interface Dnd5eCraftingProjectRepository extends JpaRepository<Dnd5eCraftingProject, Long> {

    List<Dnd5eCraftingProject> findByCharacterId(Long characterId);

    List<Dnd5eCraftingProject> findByCharacterIdAndStatus(Long characterId, String status);

    List<Dnd5eCraftingProject> findByStatus(String status);

    List<Dnd5eCraftingProject> findByRecipeId(Long recipeId);

    List<Dnd5eCraftingProject> findByCharacterIdAndStatusNot(Long characterId, String status);
}
