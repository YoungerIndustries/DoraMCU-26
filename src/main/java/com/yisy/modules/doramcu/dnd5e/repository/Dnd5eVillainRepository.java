package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eVillainRepository extends JpaRepository<Dnd5eVillain, Long> {

    Optional<Dnd5eVillain> findByName(String name);

    List<Dnd5eVillain> findByVillainType(String villainType);

    List<Dnd5eVillain> findByChallengeRating(int challengeRating);

    List<Dnd5eVillain> findByIsLichTrue();

    List<Dnd5eVillain> findByIsDeathKnightTrue();

    @Query("SELECT v FROM Dnd5eVillain v WHERE v.challengeRating >= :minCr AND v.challengeRating <= :maxCr")
    List<Dnd5eVillain> findByChallengeRatingRange(@Param("minCr") int minCr, @Param("maxCr") int maxCr);

    @Query("SELECT v FROM Dnd5eVillain v WHERE v.lairAction IS NOT NULL AND v.lairAction != ''")
    List<Dnd5eVillain> findByHasLairAction();

    @Query("SELECT v FROM Dnd5eVillain v WHERE v.regionalEffect IS NOT NULL AND v.regionalEffect != ''")
    List<Dnd5eVillain> findByHasRegionalEffect();

    @Query("SELECT v FROM Dnd5eVillain v WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.motive) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eVillain> searchByKeyword(@Param("keyword") String keyword);
}
