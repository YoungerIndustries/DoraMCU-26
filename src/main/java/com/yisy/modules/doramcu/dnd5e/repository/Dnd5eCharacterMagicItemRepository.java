package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterMagicItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eCharacterMagicItemRepository extends JpaRepository<Dnd5eCharacterMagicItem, Long> {

    List<Dnd5eCharacterMagicItem> findByCharacterIdAndActiveTrue(Long characterId);

    List<Dnd5eCharacterMagicItem> findByCharacterId(Long characterId);

    List<Dnd5eCharacterMagicItem> findByMagicItemId(Long magicItemId);

    List<Dnd5eCharacterMagicItem> findByCharacterIdAndIsAttunedTrue(Long characterId);

    @Query("SELECT c FROM Dnd5eCharacterMagicItem c WHERE c.characterId = :characterId AND c.active = true AND c.isAttuned = true")
    List<Dnd5eCharacterMagicItem> findActiveAttunedByCharacter(@Param("characterId") Long characterId);

    @Query("SELECT COUNT(c) FROM Dnd5eCharacterMagicItem c WHERE c.characterId = :characterId AND c.active = true AND c.isAttuned = true")
    int countActiveAttunedByCharacter(@Param("characterId") Long characterId);

    Optional<Dnd5eCharacterMagicItem> findByCharacterIdAndMagicItemId(Long characterId, Long magicItemId);

    List<Dnd5eCharacterMagicItem> findByCharacterIdAndActiveTrueAndIsAttunedTrue(Long characterId);

    @Query("SELECT c FROM Dnd5eCharacterMagicItem c WHERE c.characterName LIKE %:keyword% OR c.itemName LIKE %:keyword%")
    List<Dnd5eCharacterMagicItem> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByCharacterIdAndMagicItemId(Long characterId, Long magicItemId);
}
