package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsionicPower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing D&D 5e Psionic Power entities.
 */
@Repository
public interface Dnd5ePsionicPowerRepository extends JpaRepository<Dnd5ePsionicPower, Long> {

    /**
     * Find a psionic power by name.
     */
    Optional<Dnd5ePsionicPower> findByName(String name);

    /**
     * Check if a psionic power exists by name.
     */
    boolean existsByName(String name);

    /**
     * Find psionic powers by level.
     */
    List<Dnd5ePsionicPower> findByPowerLevel(int powerLevel);

    /**
     * Find psionic powers by subclass.
     */
    List<Dnd5ePsionicPower> findBySubclass(String subclass);

    /**
     * Find psionic powers by school.
     */
    List<Dnd5ePsionicPower> findBySchool(String school);


    /**
     * Find psionic powers that require concentration.
     */
    List<Dnd5ePsionicPower> findByConcentrationTrue();

    /**
     * Find psionic powers that can be cast as rituals.
     */
    List<Dnd5ePsionicPower> findByRitualTrue();

    /**
     * Find psionic powers by name containing a search term.
     */
    List<Dnd5ePsionicPower> findByNameContainingIgnoreCase(String searchTerm);

    /**
     * Find psionic powers by subclass and level.
     */
    List<Dnd5ePsionicPower> findBySubclassAndPowerLevel(String subclass, int powerLevel);
}
