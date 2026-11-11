package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for D&D 5e crafting tools.
 */
public interface Dnd5eCraftingToolRepository extends JpaRepository<Dnd5eCraftingTool, Long> {

    Optional<Dnd5eCraftingTool> findByName(String name);

    List<Dnd5eCraftingTool> findByToolType(String toolType);

    List<Dnd5eCraftingTool> findByAssociatedAbility(String associatedAbility);
}
