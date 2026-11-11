package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for D&D 5e Feat definitions.
 */
public interface Dnd5eFeatRepository extends JpaRepository<Dnd5eFeat, Long> {

    Optional<Dnd5eFeat> findByName(String name);

    List<Dnd5eFeat> findByCategory(String category);

    List<Dnd5eFeat> findByAbilityRequirement(String abilityRequirement);

    @Query("SELECT f FROM Dnd5eFeat f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eFeat> searchByNameOrDescription(@Param("keyword") String keyword);

    @Query("SELECT f FROM Dnd5eFeat f WHERE f.abilityScoreRequired <= :score")
    List<Dnd5eFeat> findByAbilityScoreLessThanEqual(@Param("score") int score);

    List<Dnd5eFeat> findByRepeatableTrue();
}
