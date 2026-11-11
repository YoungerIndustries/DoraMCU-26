package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterGrowingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing D&D 5e Character Growing Item entities.
 */
@Repository
public interface Dnd5eCharacterGrowingItemRepository extends JpaRepository<Dnd5eCharacterGrowingItem, Long> {

    /**
     * Find all growing items for a character.
     */
    List<Dnd5eCharacterGrowingItem> findByCharacterId(Long characterId);

    /**
     * Find active growing items for a character.
     */
    List<Dnd5eCharacterGrowingItem> findByCharacterIdAndActiveTrue(Long characterId);

    /**
     * Find a character's growing item by item ID.
     */
    Optional<Dnd5eCharacterGrowingItem> findByCharacterIdAndGrowingItemId(Long characterId, Long growingItemId);

    /**
     * Check if a character has a specific growing item.
     */
    boolean existsByCharacterIdAndGrowingItemIdAndActiveTrue(Long characterId, Long growingItemId);

    /**
     * Find all attuned growing items.
     */
    List<Dnd5eCharacterGrowingItem> findByIsAttunedTrue();

    /**
     * Find growing items by item ID.
     */
    List<Dnd5eCharacterGrowingItem> findByGrowingItemId(Long growingItemId);
}
