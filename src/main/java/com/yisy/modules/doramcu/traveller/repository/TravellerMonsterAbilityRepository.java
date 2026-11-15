package com.yisy.modules.doramcu.traveller.repository;

import com.yisy.modules.doramcu.traveller.model.TravellerMonsterAbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravellerMonsterAbilityRepository extends JpaRepository<TravellerMonsterAbility, Long> {

    List<TravellerMonsterAbility> findByMonsterNameAndActiveTrue(String monsterName);

    List<TravellerMonsterAbility> findByMonsterNameAndTypeAndActiveTrue(String monsterName, String type);

    List<TravellerMonsterAbility> findByMonsterNameAndSubtypeAndActiveTrue(String monsterName, String subtype);

    List<TravellerMonsterAbility> findByDamageTypeAndActiveTrue(String damageType);

    List<TravellerMonsterAbility> findBySaveDCGreaterThan(int saveDC);

    @Query("SELECT a FROM TravellerMonsterAbility a WHERE a.active = true AND LOWER(a.monsterName) LIKE LOWER(CONCAT('%', :monsterName, '%')) AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TravellerMonsterAbility> searchByMonsterAndName(@Param("monsterName") String monsterName, @Param("keyword") String keyword);
}
