package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.dto.InventorySummaryResponse;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing D&D 5e inventory items.
 * <p>
 * Provides CRUD operations for inventory items, equipment management,
 * attunement tracking, weight/value calculations, and item search.
 * </p>
 */
@Service
public class Dnd5eInventoryService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eInventoryService.class);

    private final Dnd5eInventoryRepository inventoryRepository;
    private final Dnd5eCharacterRepository characterRepository;

    public Dnd5eInventoryService(Dnd5eInventoryRepository inventoryRepository,
                                 Dnd5eCharacterRepository characterRepository) {
        this.inventoryRepository = inventoryRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Item CRUD ====================

    @Transactional
    public Dnd5eInventoryItem createItem(Long characterId, CreateDnd5eInventoryItemRequest request) {
        // Verify character exists
        characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eInventoryItem item = new Dnd5eInventoryItem();
        item.setCharacterId(characterId);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setItemType(request.getItemType());
        item.setRarity(request.getRarity() != null ? request.getRarity() : "Common");
        item.setQuantity(Math.max(1, request.getQuantity()));
        item.setWeight(BigDecimal.valueOf(Math.max(0, request.getWeight())));
        item.setValue(Math.max(0, request.getValue()));
        item.setEquipped(request.isEquipped());
        item.setAttuned(request.isAttuned());
        item.setIdentified(request.isIdentified());
        item.setCharges(request.getCharges());
        item.setMaxCharges(request.getMaxCharges());
        item.setPropertyAc(request.getPropertyAc());
        item.setPropertyDamage(request.getPropertyDamage());
        item.setPropertyRange(request.getPropertyRange());
        item.setPropertyHealing(request.getPropertyHealing());
        item.setNotes(request.getNotes());

        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Created inventory item '{}' for character {}", saved.getName(), characterId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Dnd5eInventoryItem getItem(Long itemId) {
        return inventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + itemId));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eInventoryItem> listItems(Long characterId) {
        verifyCharacterExists(characterId);
        return inventoryRepository.findByCharacterId(characterId);
    }

    @Transactional
    public Dnd5eInventoryItem updateItem(Long itemId, UpdateDnd5eInventoryItemRequest request) {
        Dnd5eInventoryItem item = getItem(itemId);

        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getItemType() != null) {
            item.setItemType(request.getItemType());
        }
        if (request.getRarity() != null) {
            item.setRarity(request.getRarity());
        }
        if (request.getQuantity() != null) {
            item.setQuantity(Math.max(0, request.getQuantity()));
        }
        if (request.getWeight() != null) {
            item.setWeight(BigDecimal.valueOf(Math.max(0, request.getWeight())));
        }
        if (request.getValue() != null) {
            item.setValue(Math.max(0, request.getValue()));
        }
        if (request.getEquipped() != null) {
            item.setEquipped(request.getEquipped());
        }
        if (request.getAttuned() != null) {
            item.setAttuned(request.getAttuned());
        }
        if (request.getIdentified() != null) {
            item.setIdentified(request.getIdentified());
        }
        if (request.getCharges() != null) {
            item.setCharges(request.getCharges());
        }
        if (request.getMaxCharges() != null) {
            item.setMaxCharges(request.getMaxCharges());
        }
        if (request.getPropertyAc() != null) {
            item.setPropertyAc(request.getPropertyAc());
        }
        if (request.getPropertyDamage() != null) {
            item.setPropertyDamage(request.getPropertyDamage());
        }
        if (request.getPropertyRange() != null) {
            item.setPropertyRange(request.getPropertyRange());
        }
        if (request.getPropertyHealing() != null) {
            item.setPropertyHealing(request.getPropertyHealing());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }

        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Updated inventory item '{}'", saved.getName());
        return saved;
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (!inventoryRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Inventory item not found: " + itemId);
        }
        inventoryRepository.deleteById(itemId);
        log.info("Deleted inventory item: {}", itemId);
    }

    // ==================== Equipment Management ====================

    @Transactional
    public Dnd5eInventoryItem equipItem(Long itemId) {
        Dnd5eInventoryItem item = getItem(itemId);
        Long characterId = item.getCharacterId();

        // Unequip any other item of the same type
        List<Dnd5eInventoryItem> sameTypeEquipped = inventoryRepository
                .findByCharacterIdAndItemTypeAndEquippedTrue(characterId, item.getItemType());
        for (Dnd5eInventoryItem other : sameTypeEquipped) {
            if (!other.getId().equals(itemId)) {
                other.setEquipped(false);
                inventoryRepository.save(other);
            }
        }

        item.setEquipped(true);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Equipped item '{}' for character {}", saved.getName(), characterId);
        return saved;
    }

    @Transactional
    public Dnd5eInventoryItem unequipItem(Long itemId) {
        Dnd5eInventoryItem item = getItem(itemId);
        item.setEquipped(false);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Unequipped item '{}' for character {}", saved.getName(), item.getCharacterId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Dnd5eInventoryItem> getEquippedItems(Long characterId) {
        verifyCharacterExists(characterId);
        return inventoryRepository.findByCharacterIdAndEquippedTrue(characterId);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eInventoryItem> getItemsByType(Long characterId, String itemType) {
        verifyCharacterExists(characterId);
        return inventoryRepository.findByCharacterIdAndItemType(characterId, itemType);
    }

    // ==================== Attunement ====================

    @Transactional
    public Dnd5eInventoryItem attuneItem(Long itemId) {
        Dnd5eInventoryItem item = getItem(itemId);
        Long characterId = item.getCharacterId();

        // Check attunement limit (max 3 attuned items)
        List<Dnd5eInventoryItem> attunedItems = inventoryRepository.findByCharacterIdAndAttunedTrue(characterId);
        if (attunedItems.size() >= 3 && !item.isAttuned()) {
            throw new IllegalArgumentException("Character already has 3 attuned items (maximum allowed)");
        }

        item.setAttuned(true);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Attuned item '{}' for character {}", saved.getName(), characterId);
        return saved;
    }

    @Transactional
    public Dnd5eInventoryItem unattuneItem(Long itemId) {
        Dnd5eInventoryItem item = getItem(itemId);
        item.setAttuned(false);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Unattuned item '{}' for character {}", saved.getName(), item.getCharacterId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Dnd5eInventoryItem> getAttunedItems(Long characterId) {
        verifyCharacterExists(characterId);
        return inventoryRepository.findByCharacterIdAndAttunedTrue(characterId);
    }

    // ==================== Quantity Management ====================

    @Transactional
    public Dnd5eInventoryItem addItemQuantity(Long itemId, int amount) {
        Dnd5eInventoryItem item = getItem(itemId);
        int newQuantity = Math.max(0, item.getQuantity() + amount);
        item.setQuantity(newQuantity);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Updated quantity of '{}' to {}", saved.getName(), newQuantity);
        return saved;
    }

    @Transactional
    public Dnd5eInventoryItem consumeItem(Long itemId, int amount) {
        Dnd5eInventoryItem item = getItem(itemId);
        int newQuantity = Math.max(0, item.getQuantity() - amount);
        item.setQuantity(newQuantity);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Consumed {} of '{}', remaining: {}", amount, saved.getName(), newQuantity);

        // Auto-delete if quantity reaches 0
        if (newQuantity <= 0) {
            inventoryRepository.delete(saved);
            log.info("Deleted depleted item '{}'", saved.getName());
        }
        return saved;
    }

    // ==================== Charges ====================

    @Transactional
    public Dnd5eInventoryItem useCharge(Long itemId) {
        Dnd5eInventoryItem item = getItem(itemId);
        if (item.getCharges() == null || item.getCharges() <= 0) {
            throw new IllegalArgumentException("Item has no charges remaining");
        }
        item.setCharges(item.getCharges() - 1);
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Used charge on '{}', remaining: {}", saved.getName(), saved.getCharges());
        return saved;
    }

    @Transactional
    public Dnd5eInventoryItem rechargeItem(Long itemId, int charges) {
        Dnd5eInventoryItem item = getItem(itemId);
        if (item.getMaxCharges() != null) {
            item.setCharges(Math.min(item.getMaxCharges(), (item.getCharges() != null ? item.getCharges() : 0) + charges));
        } else {
            item.setCharges((item.getCharges() != null ? item.getCharges() : 0) + charges);
        }
        Dnd5eInventoryItem saved = inventoryRepository.save(item);
        log.info("Recharged '{}' with {} charges", saved.getName(), charges);
        return saved;
    }

    // ==================== Search & Summary ====================

    @Transactional(readOnly = true)
    public List<Dnd5eInventoryItem> searchItems(Long characterId, String keyword) {
        verifyCharacterExists(characterId);
        return inventoryRepository.searchByCharacterIdAndName(characterId, keyword);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eInventoryItem> getItemsByRarity(Long characterId, String rarity) {
        verifyCharacterExists(characterId);
        return inventoryRepository.findByCharacterIdAndRarity(characterId, rarity);
    }

    @Transactional(readOnly = true)
    public InventorySummaryResponse getInventorySummary(Long characterId) {
        verifyCharacterExists(characterId);

        List<Dnd5eInventoryItem> items = inventoryRepository.findByCharacterId(characterId);
        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalValue = 0;
        int equippedCount = 0;
        int attunedCount = 0;

        for (Dnd5eInventoryItem item : items) {
            totalWeight = totalWeight.add(item.getWeight().multiply(BigDecimal.valueOf(item.getQuantity())));
            totalValue += item.getValue() * item.getQuantity();
            if (item.isEquipped()) equippedCount++;
            if (item.isAttuned()) attunedCount++;
        }

        InventorySummaryResponse summary = new InventorySummaryResponse();
        summary.setCharacterId(characterId);
        summary.setTotalItems(items.size());
        summary.setTotalWeight(totalWeight);
        summary.setTotalValue(totalValue);
        summary.setEquippedCount(equippedCount);
        summary.setAttunedCount(attunedCount);
        return summary;
    }

    // ==================== Helpers ====================

    private void verifyCharacterExists(Long characterId) {
        if (!characterRepository.existsById(characterId)) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
    }
}
