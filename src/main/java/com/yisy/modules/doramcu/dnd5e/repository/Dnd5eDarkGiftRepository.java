package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDarkGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing D&D 5e Dark Gift entities.
 */
@Repository
public interface Dnd5eDarkGiftRepository extends JpaRepository<Dnd5eDarkGift, Long> {

    /**
     * Find a Dark Gift by name.
     */
    Optional<Dnd5eDarkGift> findByName(String name);

    /**
     * Find Dark Gifts by category.
     */
    List<Dnd5eDarkGift> findByCategory(String category);

    /**
     * Find Dark Gifts by associated Darklord.
     */
    List<Dnd5eDarkGift> findByDarklord(String darklord);

    /**
     * Find Dark Gifts available at or below a given level.
     */
    List<Dnd5eDarkGift> findByMinLevelLessThanEqual(int level);

    /**
     * Find repeatable Dark Gifts.
     */
    List<Dnd5eDarkGift> findByRepeatableTrue();

    /**
     * Search Dark Gifts by name.
     */
    List<Dnd5eDarkGift> findByNameContainingIgnoreCase(String searchTerm);

    /**
     * Check if a Dark Gift exists by name.
     */
    boolean existsByName(String name);
}
