package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for D&D 5e inventory items.
 */
public interface Dnd5eInventoryRepository extends JpaRepository<Dnd5eInventoryItem, Long> {

    List<Dnd5eInventoryItem> findByCharacterId(Long characterId);

    List<Dnd5eInventoryItem> findByCharacterIdAndItemType(Long characterId, String itemType);

    List<Dnd5eInventoryItem> findByCharacterIdAndEquippedTrue(Long characterId);

    List<Dnd5eInventoryItem> findByCharacterIdAndAttunedTrue(Long characterId);

    List<Dnd5eInventoryItem> findByCharacterIdAndItemTypeAndEquippedTrue(Long characterId, String itemType);

    @Query("SELECT i FROM Dnd5eInventoryItem i WHERE i.characterId = :characterId AND i.itemType = :itemType AND i.equipped = :equipped")
    List<Dnd5eInventoryItem> findByCharacterIdAndItemTypeAndEquipped(
            @Param("characterId") Long characterId,
            @Param("itemType") String itemType,
            @Param("equipped") boolean equipped);

    @Query("SELECT i FROM Dnd5eInventoryItem i WHERE i.characterId = :characterId AND i.name LIKE %:keyword%")
    List<Dnd5eInventoryItem> searchByCharacterIdAndName(@Param("characterId") Long characterId, @Param("keyword") String keyword);

    @Query("SELECT i FROM Dnd5eInventoryItem i WHERE i.characterId = :characterId AND i.rarity = :rarity")
    List<Dnd5eInventoryItem> findByCharacterIdAndRarity(@Param("characterId") Long characterId, @Param("rarity") String rarity);

    @Query("SELECT COALESCE(SUM(i.weight * i.quantity), 0) FROM Dnd5eInventoryItem i WHERE i.characterId = :characterId")
    BigDecimal calculateTotalWeight(@Param("characterId") Long characterId);

    @Query("SELECT COALESCE(SUM(i.value * i.quantity), 0), COALESCE(SUM(i.weight * i.quantity), 0) FROM Dnd5eInventoryItem i WHERE i.characterId = :characterId")
    Object[] calculateTotalValueAndWeight(@Param("characterId") Long characterId);
}
