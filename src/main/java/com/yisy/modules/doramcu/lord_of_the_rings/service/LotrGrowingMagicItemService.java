package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrGrowingMagicItem;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrGrowingMagicItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Growing Magic Items.
 * <p>
 * Growing magic items evolve with the character, gaining power as the
 * character levels up. They have levels, rarities, and descriptions.
 * </p>
 */
@Service
public class LotrGrowingMagicItemService {

    private static final Logger log = LoggerFactory.getLogger(LotrGrowingMagicItemService.class);

    private final LotrGrowingMagicItemRepository growingMagicItemRepository;

    public LotrGrowingMagicItemService(LotrGrowingMagicItemRepository growingMagicItemRepository) {
        this.growingMagicItemRepository = growingMagicItemRepository;
    }

    /**
     * Get all growing magic items.
     */
    @Transactional(readOnly = true)
    public List<LotrGrowingMagicItem> findAll() {
        return growingMagicItemRepository.findAll();
    }

    /**
     * Get all active growing magic items.
     */
    @Transactional(readOnly = true)
    public List<LotrGrowingMagicItem> findActiveItems() {
        return growingMagicItemRepository.findAll().stream()
                .filter(LotrGrowingMagicItem::isActive)
                .toList();
    }

    /**
     * Get growing magic item by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrGrowingMagicItem> findById(Long id) {
        return growingMagicItemRepository.findById(id);
    }

    /**
     * Find growing magic items by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrGrowingMagicItem> findByName(String name) {
        return growingMagicItemRepository.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find growing magic items by rarity.
     */
    @Transactional(readOnly = true)
    public List<LotrGrowingMagicItem> findByRarity(String rarity) {
        return growingMagicItemRepository.findAll().stream()
                .filter(item -> rarity.equals(item.getRarity()))
                .toList();
    }

    /**
     * Create a new growing magic item.
     */
    @Transactional
    public LotrGrowingMagicItem createGrowingMagicItem(String name, String description,
                                                        int maxLevel, String rarity) {
        LotrGrowingMagicItem item = new LotrGrowingMagicItem();
        item.setName(name);
        item.setDescription(description);
        item.setLevel(1);
        item.setMaxLevel(maxLevel);
        item.setRarity(rarity);
        item.setActive(true);

        LotrGrowingMagicItem saved = growingMagicItemRepository.save(item);
        log.info("Created growing magic item: {} (max level: {})", saved.getName(), saved.getMaxLevel());
        return saved;
    }

    /**
     * Update an existing growing magic item.
     */
    @Transactional
    public LotrGrowingMagicItem updateGrowingMagicItem(Long id, String name, String description,
                                                        int maxLevel, String rarity) {
        LotrGrowingMagicItem item = growingMagicItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Growing magic item not found: " + id));

        item.setName(name);
        item.setDescription(description);
        item.setMaxLevel(maxLevel);
        item.setRarity(rarity);

        LotrGrowingMagicItem saved = growingMagicItemRepository.save(item);
        log.info("Updated growing magic item: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a growing magic item.
     */
    @Transactional
    public void deleteGrowingMagicItem(Long id) {
        if (!growingMagicItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Growing magic item not found: " + id);
        }
        growingMagicItemRepository.deleteById(id);
        log.info("Deleted growing magic item: {}", id);
    }

    /**
     * Level up a growing magic item.
     */
    @Transactional
    public String levelUpItem(Long id) {
        LotrGrowingMagicItem item = growingMagicItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Growing magic item not found: " + id));

        if (item.getLevel() >= item.getMaxLevel()) {
            return String.format("%s is already at maximum level (%d).", item.getName(), item.getMaxLevel());
        }

        int newLevel = item.getLevel() + 1;
        item.setLevel(newLevel);
        growingMagicItemRepository.save(item);

        if (newLevel >= item.getMaxLevel()) {
            return String.format("%s reaches maximum power! Level: %d/%d", item.getName(), newLevel, item.getMaxLevel());
        }
        return String.format("%s grows stronger! Level: %d/%d", item.getName(), newLevel, item.getMaxLevel());
    }

    /**
     * Initialize the growing magic item database with LOTR-themed items.
     */
    @Transactional
    public void initializeGrowingMagicItemDatabase() {
        if (!growingMagicItemRepository.findAll().isEmpty()) {
            log.info("Growing magic item database already initialized, skipping.");
            return;
        }

        createGrowingMagicItem("Sting (Growing)",
                "An elven blade that grows sharper with each victory over darkness.",
                10, "Rare");
        createGrowingMagicItem("Phial of Galadriel (Growing)",
                "The light within grows brighter as the bearer resists darkness.",
                10, "Legendary");
        createGrowingMagicItem("Mithril Coat (Growing)",
                "The mithril weave tightens, offering better protection over time.",
                10, "Rare");
        createGrowingMagicItem("Elven Cloak (Growing)",
                "The cloak's enchantment deepens, making the wearer harder to detect.",
                10, "Uncommon");
        createGrowingMagicItem("Palantír (Growing)",
                "The seeing stone's power grows, revealing farther and clearer visions.",
                10, "Legendary");

        log.info("Initialized growing magic item database with {} LOTR items.", growingMagicItemRepository.count());
    }
}
