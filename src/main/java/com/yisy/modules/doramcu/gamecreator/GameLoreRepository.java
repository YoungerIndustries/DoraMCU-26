package com.yisy.modules.doramcu.gamecreator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link GameLore} entities.
 * <p>
 * Provides queries for finding lores by slug, genre, category, and search terms.
 * </p>
 */
@Repository
public interface GameLoreRepository extends JpaRepository<GameLore, Long> {

    /**
     * Find a lore by its unique slug.
     */
    Optional<GameLore> findBySlug(String slug);

    /**
     * Check if a lore with the given slug exists.
     */
    boolean existsBySlug(String slug);

    /**
     * Find all active lores of a given genre.
     */
    List<GameLore> findByGenreAndActiveTrue(String genre);

    /**
     * Find all active lores in a given category.
     */
    List<GameLore> findByCategoryAndActiveTrue(String category);

    /**
     * Find all active lores.
     */
    List<GameLore> findByActiveTrue();

    /**
     * Search lores by name (case-insensitive, partial match).
     */
    @Query("SELECT g FROM GameLore g WHERE g.active = true AND LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<GameLore> searchByName(@Param("keyword") String keyword);

    /**
     * Search lores by name or description (case-insensitive, partial match).
     */
    @Query("SELECT g FROM GameLore g WHERE g.active = true AND " +
            "(LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(g.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<GameLore> searchByNameOrDescription(@Param("keyword") String keyword);

    /**
     * Find lores suitable for a specific game mode.
     */
    @Query("SELECT g FROM GameLore g WHERE g.active = true AND " +
            "(g.suitableForRpg = :mode OR g.suitableFor4x = :mode OR " +
            "g.suitableForTycoon = :mode OR g.suitableForTactical = :mode OR " +
            "g.suitableForSurvival = :mode)")
    List<GameLore> findByGameModeSuitability(@Param("mode") boolean mode);
}
