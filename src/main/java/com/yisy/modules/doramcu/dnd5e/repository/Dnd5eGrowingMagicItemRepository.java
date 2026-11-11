package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eGrowingMagicItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing D&D 5e Growing Magic Item entities.
 */
@Repository
public interface Dnd5eGrowingMagicItemRepository extends JpaRepository<Dnd5eGrowingMagicItem, Long> {

    /**
     * Find a growing magic item by name.
     */
    Optional<Dnd5eGrowingMagicItem> findByName(String name);

    /**
     * Find growing magic items by category.
     */
    List<Dnd5eGrowingMagicItem> findByCategory(String category);

    /**
     * Find growing magic items by rarity.
     */
    List<Dnd5eGrowingMagicItem> findByRarity(String rarity);

    /**
     * Search growing magic items by name.
     */
    List<Dnd5eGrowingMagicItem> findByNameContainingIgnoreCase(String searchTerm);

    /**
     * Check if a growing magic item exists by name.
     */
    boolean existsByName(String name);
}
