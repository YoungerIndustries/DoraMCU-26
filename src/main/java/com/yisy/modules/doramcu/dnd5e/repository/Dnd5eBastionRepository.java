package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBastion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eBastionRepository extends JpaRepository<Dnd5eBastion, Long> {

    Optional<Dnd5eBastion> findByName(String name);

    List<Dnd5eBastion> findByTheme(String theme);

    List<Dnd5eBastion> findByLevel(int level);

    List<Dnd5eBastion> findByActiveTrue();

    List<Dnd5eBastion> findByActiveFalse();

    @Query("SELECT b FROM Dnd5eBastion b WHERE b.level >= :minLevel AND b.level <= :maxLevel")
    List<Dnd5eBastion> findByLevelRange(@Param("minLevel") int minLevel, @Param("maxLevel") int maxLevel);

    @Query("SELECT b FROM Dnd5eBastion b WHERE b.hasCrafting = true")
    List<Dnd5eBastion> findByHasCrafting();

    @Query("SELECT b FROM Dnd5eBastion b WHERE b.hasTraining = true")
    List<Dnd5eBastion> findByHasTraining();

    @Query("SELECT b FROM Dnd5eBastion b WHERE b.hasLibrary = true")
    List<Dnd5eBastion> findByHasLibrary();

    @Query("SELECT b FROM Dnd5eBastion b WHERE b.hasHealing = true")
    List<Dnd5eBastion> findByHasHealing();

    @Query("SELECT b FROM Dnd5eBastion b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eBastion> searchByKeyword(@Param("keyword") String keyword);
}
