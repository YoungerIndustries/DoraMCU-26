package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eCampaignTemplateRepository extends JpaRepository<Dnd5eCampaignTemplate, Long> {

    Optional<Dnd5eCampaignTemplate> findByName(String name);

    List<Dnd5eCampaignTemplate> findByTheme(String theme);

    List<Dnd5eCampaignTemplate> findByActiveTrue();

    @Query("SELECT t FROM Dnd5eCampaignTemplate t WHERE t.active = true AND t.minLevel <= :partyLevel AND t.maxLevel >= :partyLevel")
    List<Dnd5eCampaignTemplate> findByPartyLevel(@Param("partyLevel") int partyLevel);

    @Query("SELECT t FROM Dnd5eCampaignTemplate t WHERE t.active = true AND t.minLevel >= :minLevel AND t.maxLevel <= :maxLevel")
    List<Dnd5eCampaignTemplate> findByLevelRange(@Param("minLevel") int minLevel, @Param("maxLevel") int maxLevel);

    @Query("SELECT t FROM Dnd5eCampaignTemplate t WHERE t.name LIKE %:keyword% OR t.description LIKE %:keyword% OR t.theme LIKE %:keyword%")
    List<Dnd5eCampaignTemplate> searchByKeyword(@Param("keyword") String keyword);

    List<Dnd5eCampaignTemplate> findByThemeAndActiveTrue(String theme);

    @Query("SELECT DISTINCT t.theme FROM Dnd5eCampaignTemplate t WHERE t.active = true")
    List<String> findAllThemes();

    boolean existsByName(String name);
}
