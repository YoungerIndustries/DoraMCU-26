package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMagicItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eMagicItemRepository extends JpaRepository<Dnd5eMagicItem, Long> {

    Optional<Dnd5eMagicItem> findByName(String name);

    List<Dnd5eMagicItem> findByRarity(String rarity);

    List<Dnd5eMagicItem> findByCategory(String category);

    List<Dnd5eMagicItem> findByRequiresAttunementTrue();

    List<Dnd5eMagicItem> findByHasChargesTrue();

    List<Dnd5eMagicItem> findByCursedTrue();

    List<Dnd5eMagicItem> findByActiveTrue();

    List<Dnd5eMagicItem> findByRarityAndActiveTrue(String rarity);

    List<Dnd5eMagicItem> findByCategoryAndActiveTrue(String category);

    @Query("SELECT i FROM Dnd5eMagicItem i WHERE i.name LIKE %:keyword% OR i.description LIKE %:keyword% OR i.category LIKE %:keyword%")
    List<Dnd5eMagicItem> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT i FROM Dnd5eMagicItem i WHERE i.active = true AND i.rarity IN :rarities")
    List<Dnd5eMagicItem> findByRarityIn(@Param("rarities") List<String> rarities);

    @Query("SELECT DISTINCT i.rarity FROM Dnd5eMagicItem i WHERE i.active = true")
    List<String> findAllRarities();

    @Query("SELECT DISTINCT i.category FROM Dnd5eMagicItem i WHERE i.active = true")
    List<String> findAllCategories();

    boolean existsByName(String name);
}
