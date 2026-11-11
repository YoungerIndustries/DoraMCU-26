package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing D&D 5e Psion entities.
 */
@Repository
public interface Dnd5ePsionRepository extends JpaRepository<Dnd5ePsion, Long> {

    /**
     * Find a psion by character ID.
     */
    Optional<Dnd5ePsion> findByCharacterId(Long characterId);

    /**
     * Check if a psion exists for a character.
     */
    boolean existsByCharacterId(Long characterId);

    /**
     * Find psions by subclass.
     */
    List<Dnd5ePsion> findBySubclass(String subclass);

    /**
     * Find psions with active surge.
     */
    List<Dnd5ePsion> findByIsSurgeActiveTrue();

    /**
     * Find psions by primary ability.
     */
    List<Dnd5ePsion> findByPrimaryAbility(String primaryAbility);
}
