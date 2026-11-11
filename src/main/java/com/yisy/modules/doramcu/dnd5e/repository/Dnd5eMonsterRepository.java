package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying D&D 5e Monsters.
 */
@Repository
public interface Dnd5eMonsterRepository extends JpaRepository<Dnd5eMonster, Long> {

    Optional<Dnd5eMonster> findByName(String name);

    List<Dnd5eMonster> findByActiveTrue();

    List<Dnd5eMonster> findByTypeAndActiveTrue(String type);

    List<Dnd5eMonster> findBySizeAndActiveTrue(String size);

    List<Dnd5eMonster> findByCrAndActiveTrue(double cr);

    List<Dnd5eMonster> findByCrBetweenAndActiveTrue(double minCr, double maxCr);

    List<Dnd5eMonster> findByXpBetweenAndActiveTrue(int minXp, int maxXp);

    List<Dnd5eMonster> findByAlignmentAndActiveTrue(String alignment);

    List<Dnd5eMonster> findByHasLairActionsTrueAndActiveTrue();

    List<Dnd5eMonster> findByHasLegendaryActionsTrueAndActiveTrue();

    List<Dnd5eMonster> findByHasRegionalEffectsTrueAndActiveTrue();

    @Query("SELECT m FROM Dnd5eMonster m WHERE m.active = true AND LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eMonster> searchByName(@Param("keyword") String keyword);

    @Query("SELECT m FROM Dnd5eMonster m WHERE m.active = true AND LOWER(m.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eMonster> searchByTypeOrName(@Param("keyword") String keyword);

    long countByActiveTrue();

    List<Dnd5eMonster> findBySubtypeAndActiveTrue(String subtype);

    @Query("SELECT m FROM Dnd5eMonster m WHERE m.active = true AND m.cr BETWEEN :minCr AND :maxCr ORDER BY m.cr ASC")
    List<Dnd5eMonster> findByCrRange(@Param("minCr") double minCr, @Param("maxCr") double maxCr);

    @Query("SELECT m FROM Dnd5eMonster m WHERE m.active = true AND m.xp BETWEEN :minXp AND :maxXp ORDER BY m.xp ASC")
    List<Dnd5eMonster> findByXpRange(@Param("minXp") int minXp, @Param("maxXp") int maxXp);
}
