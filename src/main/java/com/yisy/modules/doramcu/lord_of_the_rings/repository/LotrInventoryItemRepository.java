package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrInventoryItemRepository extends JpaRepository<LotrInventoryItem, Long> {

    List<LotrInventoryItem> findByCharacterId(Long characterId);

    List<LotrInventoryItem> findByItemType(String itemType);

    List<LotrInventoryItem> findByCharacterIdAndEquippedTrue(Long characterId);
}
