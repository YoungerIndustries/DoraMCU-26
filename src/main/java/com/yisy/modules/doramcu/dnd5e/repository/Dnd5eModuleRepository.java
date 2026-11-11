package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eModuleRepository extends JpaRepository<Dnd5eModule, Long> {

    List<Dnd5eModule> findByNameContainingIgnoreCase(String name);

    List<Dnd5eModule> findByTheme(String theme);

    List<Dnd5eModule> findByThemeAndActiveTrue(String theme);

    List<Dnd5eModule> findByStatus(String status);

    List<Dnd5eModule> findByStatusAndActiveTrue(String status);

    List<Dnd5eModule> findByActiveTrue();

    @Query("SELECT m FROM Dnd5eModule m WHERE m.active = true AND " +
           "m.minLevel >= :minLevel AND m.maxLevel <= :maxLevel")
    List<Dnd5eModule> findByLevelRange(@Param("minLevel") int minLevel, @Param("maxLevel") int maxLevel);

    @Query("SELECT m FROM Dnd5eModule m WHERE m.active = true AND " +
           "m.minLevel <= :partyLevel AND m.maxLevel >= :partyLevel")
    List<Dnd5eModule> findByPartyLevel(@Param("partyLevel") int partyLevel);

    @Query("SELECT m FROM Dnd5eModule m WHERE m.active = true AND " +
           "m.estimatedSessions >= :minSessions AND m.estimatedSessions <= :maxSessions")
    List<Dnd5eModule> findBySessionRange(@Param("minSessions") int minSessions, @Param("maxSessions") int maxSessions);

    @Query("SELECT m FROM Dnd5eModule m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.theme) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eModule> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT m FROM Dnd5eModule m WHERE m.active = true AND " +
           "m.rating >= :minRating")
    List<Dnd5eModule> findByRating(@Param("minRating") double minRating);

    @Query("SELECT m FROM Dnd5eModule m WHERE m.active = true AND " +
           "m.playCount > 0 ORDER BY m.playCount DESC")
    List<Dnd5eModule> findMostPlayed();

    long countByActiveTrue();

    long countByTheme(String theme);

    long countByStatus(String status);
}
