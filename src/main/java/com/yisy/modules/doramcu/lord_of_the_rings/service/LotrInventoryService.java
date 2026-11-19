package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrInventoryItem;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrInventoryItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for Lord of the Rings inventory management.
 */
@Service
public class LotrInventoryService {

    private static final Logger log = LoggerFactory.getLogger(LotrInventoryService.class);

    private final LotrInventoryItemRepository repository;

    public LotrInventoryService(LotrInventoryItemRepository repository) {
        this.repository = repository;
    }

    public List<LotrInventoryItem> findAll() {
        return repository.findAll();
    }

    public List<LotrInventoryItem> findByCharacterId(Long characterId) {
        return repository.findByCharacterId(characterId);
    }

    public List<LotrInventoryItem> findEquipped(Long characterId) {
        return repository.findByCharacterIdAndEquippedTrue(characterId);
    }

    public LotrInventoryItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
    }

    @Transactional
    public LotrInventoryItem createItem(Long characterId, String name, String itemType, String rarity) {
        LotrInventoryItem item = new LotrInventoryItem();
        item.setCharacterId(characterId);
        item.setName(name);
        item.setItemType(itemType);
        item.setRarity(rarity != null ? rarity : "Common");
        item.setQuantity(1);
        item.setWeight(BigDecimal.ZERO);
        item.setValue(0);
        item.setEquipped(false);
        item.setIdentified(true);
        log.info("Created LOTR inventory item: {} for character {}", name, characterId);
        return repository.save(item);
    }

    @Transactional
    public LotrInventoryItem save(LotrInventoryItem item) {
        return repository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void equipItem(Long id) {
        LotrInventoryItem item = findById(id);
        item.setEquipped(true);
        repository.save(item);
    }

    @Transactional
    public void unequipItem(Long id) {
        LotrInventoryItem item = findById(id);
        item.setEquipped(false);
        repository.save(item);
    }
}
