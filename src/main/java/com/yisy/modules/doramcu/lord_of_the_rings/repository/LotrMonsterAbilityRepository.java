package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonsterAbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrMonsterAbilityRepository extends JpaRepository<LotrMonsterAbility, Long> {

    List<LotrMonsterAbility> findByMonsterName(String monsterName);

    List<LotrMonsterAbility> findByMonsterNameAndType(String monsterName, String type);

    List<LotrMonsterAbility> findByMonsterNameAndTypeContainingIgnoreCase(String monsterName, String subtype);

    @Query("SELECT a FROM LotrMonsterAbility a WHERE a.monsterName = :monsterName AND " +
            "(LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<LotrMonsterAbility> searchByMonsterNameAndKeyword(String monsterName, String keyword);

    List<LotrMonsterAbility> findByIsActiveTrue();

    List<LotrMonsterAbility> findByType(String type);
}
