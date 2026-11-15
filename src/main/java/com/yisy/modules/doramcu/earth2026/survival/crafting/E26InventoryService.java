package com.yisy.modules.doramcu.earth2026.survival.crafting;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalResource;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalResourceRepository;
import com.yisy.modules.doramcu.gamecore.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Inventory service for Earth2026 survival mode.
 * <p>
 * Manages character inventory using a simple in-memory map backed by the
 * character's equipment JSON field for persistence. Implements the shared
 * InventoryService interface.
 * </p>
 */
@Service
public class E26InventoryService implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(E26InventoryService.class);

    private final Earth2026CharacterRepository characterRepository;
    private final SurvivalResourceRepository resourceRepository;

    // In-memory inventory cache: characterId -> list of inventory items
    private final Map<Long, List<InventoryItem>> inventoryCache = new HashMap<>();

    private long nextItemId = 1;

    public E26InventoryService(Earth2026CharacterRepository characterRepository,
                                SurvivalResourceRepository resourceRepository) {
        this.characterRepository = characterRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getInventory(Long characterId) {
        verifyCharacterExists(characterId);
        return inventoryCache.computeIfAbsent(characterId, k -> new ArrayList<>());
    }

    @Override
    @Transactional
    public InventoryItem addItem(Long characterId, InventoryItem item) {
        verifyCharacterExists(characterId);

        if (item.getId() == null) {
            item.setId(nextItemId++);
        }

        List<InventoryItem> inventory = inventoryCache.computeIfAbsent(characterId, k -> new ArrayList<>());
        inventory.add(item);
        log.info("Added '{}' to character {}'s inventory", item.getName(), characterId);
        return item;
    }

    @Override
    @Transactional
    public boolean removeItem(Long itemId) {
        for (Map.Entry<Long, List<InventoryItem>> entry : inventoryCache.entrySet()) {
            List<InventoryItem> inventory = entry.getValue();
            Iterator<InventoryItem> iterator = inventory.iterator();
            while (iterator.hasNext()) {
                InventoryItem item = iterator.next();
                if (item.getId().equals(itemId)) {
                    iterator.remove();
                    log.info("Removed '{}' from inventory", item.getName());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public String useItem(Long characterId, Long itemId) {
        List<InventoryItem> inventory = getInventory(characterId);
        InventoryItem item = null;
        for (InventoryItem invItem : inventory) {
            if (invItem.getId().equals(itemId)) {
                item = invItem;
                break;
            }
        }

        if (item == null) {
            return "Item not found in inventory";
        }

        // Find the corresponding resource
        SurvivalResource resource = resourceRepository.findByName(item.getName()).orElse(null);
        if (resource == null) {
            return "Unknown item: " + item.getName();
        }

        StringBuilder result = new StringBuilder();
        result.append("Used ").append(item.getName()).append(". ");

        if (resource.isConsumable()) {
            // Reduce quantity
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                result.append("Quantity remaining: ").append(item.getQuantity()).append(". ");
            } else {
                // Remove from inventory
                inventory.remove(item);
                result.append("Item consumed. ");
            }
        }

        // Describe effects
        if (resource.getHungerValue() > 0) {
            result.append("Restored ").append(resource.getHungerValue()).append(" hunger. ");
        }
        if (resource.getThirstValue() > 0) {
            result.append("Restored ").append(resource.getThirstValue()).append(" thirst. ");
        }
        if (resource.getHealthValue() > 0) {
            result.append("Restored ").append(resource.getHealthValue()).append(" health. ");
        }
        if (resource.getFatigueReduction() > 0) {
            result.append("Reduced fatigue by ").append(resource.getFatigueReduction()).append(". ");
        }

        return result.toString().trim();
    }

    /**
     * Add a survival resource to the character's inventory.
     *
     * @param characterId the character ID
     * @param resource the resource to add
     * @param quantity the quantity
     * @return the added inventory item
     */
    @Transactional
    public InventoryItem addResource(Long characterId, SurvivalResource resource, int quantity) {
        InventoryItem item = new InventoryItem();
        item.setId(nextItemId++);
        item.setName(resource.getName());
        item.setDescription(resource.getDescription());
        item.setItemType(resource.getCategory());
        item.setQuantity(quantity);
        item.setWeight((int) (resource.getWeight() * 1000));
        item.setEquipped(false);
        item.setMagicRarity(resource.getRarity());
        item.setSpecialProperties("");

        List<InventoryItem> inventory = inventoryCache.computeIfAbsent(characterId, k -> new ArrayList<>());
        inventory.add(item);
        log.info("Added {} x '{}' to character {}'s inventory", quantity, resource.getName(), characterId);
        return item;
    }

    /**
     * Get total weight of inventory for a character.
     *
     * @param characterId the character ID
     * @return total weight in kg
     */
    @Transactional(readOnly = true)
    public double getTotalWeight(Long characterId) {
        List<InventoryItem> inventory = getInventory(characterId);
        return inventory.stream().mapToDouble(i -> i.getWeight()).sum() / 1000.0;
    }

    /**
     * Get the weight limit for a character based on STR score.
     *
     * @param characterId the character ID
     * @return weight limit in kg
     */
    @Transactional(readOnly = true)
    public double getWeightLimit(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        // Base 20kg + 2kg per STR point above 10
        return 20.0 + Math.max(0, (character.getStrScore() - 10)) * 2.0;
    }

    private void verifyCharacterExists(Long characterId) {
        characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
    }
}
