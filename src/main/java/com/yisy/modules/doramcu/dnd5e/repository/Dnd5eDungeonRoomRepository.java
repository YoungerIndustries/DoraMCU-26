package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDungeonRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eDungeonRoomRepository extends JpaRepository<Dnd5eDungeonRoom, Long> {

    List<Dnd5eDungeonRoom> findByDungeonId(String dungeonId);

    List<Dnd5eDungeonRoom> findByDungeonIdAndActiveTrue(String dungeonId);

    List<Dnd5eDungeonRoom> findByTheme(String theme);

    List<Dnd5eDungeonRoom> findByThemeAndActiveTrue(String theme);

    List<Dnd5eDungeonRoom> findByActiveTrue();

    List<Dnd5eDungeonRoom> findByDiscoveredTrue();

    List<Dnd5eDungeonRoom> findByDiscoveredFalse();

    List<Dnd5eDungeonRoom> findByDungeonIdAndDiscoveredTrue(String dungeonId);

    List<Dnd5eDungeonRoom> findByDungeonIdAndDiscoveredFalse(String dungeonId);

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.active = true AND " +
           "r.minLevel <= :level AND r.maxLevel >= :level")
    List<Dnd5eDungeonRoom> findByCharacterLevel(@Param("level") int level);

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.active = true AND " +
           "r.dungeonId = :dungeonId AND " +
           "r.minLevel <= :level AND r.maxLevel >= :level")
    List<Dnd5eDungeonRoom> findByDungeonAndLevel(
            @Param("dungeonId") String dungeonId,
            @Param("level") int level);

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.active = true AND " +
           "r.minLevel <= :level AND r.maxLevel >= :level AND " +
           "r.theme = :theme")
    List<Dnd5eDungeonRoom> findByLevelAndTheme(
            @Param("level") int level,
            @Param("theme") String theme);

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.active = true AND " +
           "r.dungeonId = :dungeonId AND " +
           "r.minLevel <= :level AND r.maxLevel >= :level AND " +
           "r.discovered = false")
    List<Dnd5eDungeonRoom> findUndiscoveredByDungeonAndLevel(
            @Param("dungeonId") String dungeonId,
            @Param("level") int level);

    @Query("SELECT DISTINCT r.dungeonId FROM Dnd5eDungeonRoom r WHERE r.active = true")
    List<String> findDistinctDungeonIds();

    @Query("SELECT DISTINCT r.theme FROM Dnd5eDungeonRoom r WHERE r.active = true")
    List<String> findDistinctThemes();

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.trap IS NOT NULL AND r.active = true")
    List<Dnd5eDungeonRoom> findRoomsTraps();

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.treasure IS NOT NULL AND r.active = true")
    List<Dnd5eDungeonRoom> findRoomsTreasure();

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE r.encounter IS NOT NULL AND r.active = true")
    List<Dnd5eDungeonRoom> findRoomsEncounters();

    @Query("SELECT r FROM Dnd5eDungeonRoom r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.dmSecrets) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eDungeonRoom> searchByKeyword(@Param("keyword") String keyword);
}
