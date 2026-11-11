package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eClassRepository extends JpaRepository<Dnd5eClass, Long> {

    Optional<Dnd5eClass> findByName(String name);

    List<Dnd5eClass> findByActiveTrue();

    List<Dnd5eClass> findByHitDieAndActiveTrue(String hitDie);

    List<Dnd5eClass> findByPrimaryAbilityAndActiveTrue(String primaryAbility);

    List<Dnd5eClass> findByHasSpellcastingTrueAndActiveTrue();

    List<Dnd5eClass> findBySpellcastingTypeAndActiveTrue(String spellcastingType);

    @Query("SELECT c FROM Dnd5eClass c WHERE c.active = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eClass> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByName(String name);
}
