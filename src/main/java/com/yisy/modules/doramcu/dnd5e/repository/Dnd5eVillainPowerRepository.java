package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillainPower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eVillainPowerRepository extends JpaRepository<Dnd5eVillainPower, Long> {

    List<Dnd5eVillainPower> findByVillainId(Long villainId);

    List<Dnd5eVillainPower> findByVillainIdAndPowerType(Long villainId, String powerType);

    List<Dnd5eVillainPower> findByPowerType(String powerType);

    List<Dnd5eVillainPower> findByLairOnlyTrue();

    List<Dnd5eVillainPower> findByIsRegionalTrue();

    @Query("SELECT vp FROM Dnd5eVillainPower vp WHERE vp.villainId = :villainId ORDER BY vp.sortOrder ASC")
    List<Dnd5eVillainPower> findByVillainIdOrdered(@Param("villainId") Long villainId);

    @Query("SELECT vp FROM Dnd5eVillainPower vp WHERE vp.villainId = :villainId AND vp.powerType = 'Legendary Action' ORDER BY vp.sortOrder ASC")
    List<Dnd5eVillainPower> findLegendaryActionsByVillainId(@Param("villainId") Long villainId);

    @Query("SELECT vp FROM Dnd5eVillainPower vp WHERE vp.villainId = :villainId AND vp.powerType = 'Lair Action' ORDER BY vp.sortOrder ASC")
    List<Dnd5eVillainPower> findLairActionsByVillainId(@Param("villainId") Long villainId);

    @Query("SELECT vp FROM Dnd5eVillainPower vp WHERE vp.villainId = :villainId AND vp.powerType = 'Regional Effect' ORDER BY vp.sortOrder ASC")
    List<Dnd5eVillainPower> findRegionalEffectsByVillainId(@Param("villainId") Long villainId);

    @Query("SELECT vp FROM Dnd5eVillainPower vp WHERE LOWER(vp.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(vp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eVillainPower> searchByKeyword(@Param("keyword") String keyword);

    void deleteByVillainId(Long villainId);
}
