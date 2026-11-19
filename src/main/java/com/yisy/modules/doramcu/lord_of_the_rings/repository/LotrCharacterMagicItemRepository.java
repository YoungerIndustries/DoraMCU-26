package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacterMagicItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCharacterMagicItemRepository extends JpaRepository<LotrCharacterMagicItem, Long> {

    List<LotrCharacterMagicItem> findByCharacterId(Long characterId);

    List<LotrCharacterMagicItem> findByItemType(String itemType);
}
