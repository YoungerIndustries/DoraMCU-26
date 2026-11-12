package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonsterAbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying D&D 5e Monster Abilities.
 */
@Repository
public interface Dnd5eMonsterAbilityRepository extends JpaRepository<Dnd5eMonsterAbility, Long> {

    List<Dnd5eMonsterAbility> findByMonsterNameAndActiveTrue(String monsterName);

    List<Dnd5eMonsterAbility> findByMonsterNameAndTypeAndActiveTrue(String monsterName, String type);

    List<Dnd5eMonsterAbility> findByTypeAndActiveTrue(String type);

    List<Dnd5eMonsterAbility> findByMonsterNameAndSubtypeAndActiveTrue(String monsterName, String subtype);

    @Query("SELECT a FROM Dnd5eMonsterAbility a WHERE a.active = true AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eMonsterAbility> searchByName(@Param("keyword") String keyword);

    @Query("SELECT a FROM Dnd5eMonsterAbility a WHERE a.active = true AND a.monsterName = :monsterName AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eMonsterAbility> searchByMonsterAndName(@Param("monsterName") String monsterName, @Param("keyword") String keyword);

    long countByMonsterNameAndActiveTrue(String monsterName);

    List<Dnd5eMonsterAbility> findByMonsterNameAndDamageTypeAndActiveTrue(String monsterName, String damageType);

    List<Dnd5eMonsterAbility> findBySaveDCGreaterThanAndActiveTrue(int minDc);
}
