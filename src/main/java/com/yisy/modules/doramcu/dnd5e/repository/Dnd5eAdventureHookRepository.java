package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eAdventureHook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eAdventureHookRepository extends JpaRepository<Dnd5eAdventureHook, Long> {

    Optional<Dnd5eAdventureHook> findByName(String name);

    List<Dnd5eAdventureHook> findByType(String type);

    List<Dnd5eAdventureHook> findByTheme(String theme);

    List<Dnd5eAdventureHook> findByUrgency(String urgency);

    List<Dnd5eAdventureHook> findByTargetBackground(String targetBackground);

    List<Dnd5eAdventureHook> findByTargetRace(String targetRace);

    List<Dnd5eAdventureHook> findByTargetClass(String targetClass);

    List<Dnd5eAdventureHook> findByActiveTrue();

    List<Dnd5eAdventureHook> findByActiveFalse();

    List<Dnd5eAdventureHook> findByTypeAndActiveTrue(String type);

    List<Dnd5eAdventureHook> findByThemeAndActiveTrue(String theme);

    List<Dnd5eAdventureHook> findByUrgencyAndActiveTrue(String urgency);

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE h.active = true AND " +
           "h.minLevel <= :level AND h.maxLevel >= :level")
    List<Dnd5eAdventureHook> findByCharacterLevel(@Param("level") int level);

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE h.active = true AND " +
           "(h.targetBackground IS NULL OR h.targetBackground = :background)")
    List<Dnd5eAdventureHook> findByBackgroundOrAny(@Param("background") String background);

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE h.active = true AND " +
           "(h.targetRace IS NULL OR h.targetRace = :race)")
    List<Dnd5eAdventureHook> findByRaceOrAny(@Param("race") String race);

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE h.active = true AND " +
           "(h.targetClass IS NULL OR h.targetClass = :characterClass)")
    List<Dnd5eAdventureHook> findByClassOrAny(@Param("characterClass") String characterClass);

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE h.active = true AND " +
           "h.minLevel <= :level AND h.maxLevel >= :level AND " +
           "(h.targetBackground IS NULL OR h.targetBackground = :background) AND " +
           "(h.targetRace IS NULL OR h.targetRace = :race) AND " +
           "(h.targetClass IS NULL OR h.targetClass = :characterClass)")
    List<Dnd5eAdventureHook> findByCharacterProfile(
            @Param("level") int level,
            @Param("background") String background,
            @Param("race") String race,
            @Param("characterClass") String characterClass);

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE h.active = true AND " +
           "h.minLevel <= :level AND h.maxLevel >= :level AND " +
           "(h.targetBackground IS NULL OR h.targetBackground = :background) AND " +
           "(h.targetRace IS NULL OR h.targetRace = :race) AND " +
           "(h.targetClass IS NULL OR h.targetClass = :characterClass) AND " +
           "(h.theme IS NULL OR LOWER(h.theme) LIKE LOWER(CONCAT('%', :theme, '%')))")
    List<Dnd5eAdventureHook> findByCharacterProfileAndTheme(
            @Param("level") int level,
            @Param("background") String background,
            @Param("race") String race,
            @Param("characterClass") String characterClass,
            @Param("theme") String theme);

    @Query("SELECT DISTINCT h.type FROM Dnd5eAdventureHook h WHERE h.active = true")
    List<String> findDistinctTypes();

    @Query("SELECT DISTINCT h.theme FROM Dnd5eAdventureHook h WHERE h.active = true")
    List<String> findDistinctThemes();

    @Query("SELECT DISTINCT h.urgency FROM Dnd5eAdventureHook h WHERE h.active = true")
    List<String> findDistinctUrgencies();

    @Query("SELECT h FROM Dnd5eAdventureHook h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.narrative) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.dmNotes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eAdventureHook> searchByKeyword(@Param("keyword") String keyword);
}
