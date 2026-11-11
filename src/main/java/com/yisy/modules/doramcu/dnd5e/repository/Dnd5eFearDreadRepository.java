package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFearDread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing D&D 5e Fear and Dread entities.
 */
@Repository
public interface Dnd5eFearDreadRepository extends JpaRepository<Dnd5eFearDread, Long> {

    /**
     * Find fear/dread state by character ID.
     */
    Optional<Dnd5eFearDread> findByCharacterId(Long characterId);

    /**
     * Check if fear/dread state exists for a character.
     */
    boolean existsByCharacterId(Long characterId);

    /**
     * Find characters in a specific Domain of Dread.
     */
    List<Dnd5eFearDread> findByDomain(String domain);

    /**
     * Find frightened characters.
     */
    List<Dnd5eFearDread> findByIsFrightenedTrue();

    /**
     * Find characters with fear level at or above a threshold.
     */
    List<Dnd5eFearDread> findByFearLevelGreaterThanEqual(int fearLevel);

    /**
     * Find characters with dread level at or above a threshold.
     */
    List<Dnd5eFearDread> findByDreadLevelGreaterThanEqual(int dreadLevel);

    /**
     * Find characters by Darklord.
     */
    List<Dnd5eFearDread> findByDarklord(String darklord);
}
