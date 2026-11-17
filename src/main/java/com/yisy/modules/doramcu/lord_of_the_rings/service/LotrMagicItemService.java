package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMagicItem;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrMagicItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Magic Items.
 * <p>
 * Tracks magical artifacts in the Lord of the Rings galaxy, including
 * the Elven Rings, weapons like Sting and Andúril, and protective items
 * like the Phial of Galadriel. Items have charges, attunement requirements,
 * and special properties.
 * </p>
 * <p>
 * Key mechanics:
 * - Full CRUD for magic items with type, rarity, and properties
 * - Charge-based usage system
 * - Attunement tracking for powerful items
 * - AC bonuses, damage bonuses, healing, and range properties
 * </p>
 */
@Service
public class LotrMagicItemService {

    private static final Logger log = LoggerFactory.getLogger(LotrMagicItemService.class);

    private final LotrMagicItemRepository magicItemRepository;

    public LotrMagicItemService(LotrMagicItemRepository magicItemRepository) {
        this.magicItemRepository = magicItemRepository;
    }

    /**
     * Get all magic items.
     */
    @Transactional(readOnly = true)
    public List<LotrMagicItem> findAll() {
        return magicItemRepository.findAll();
    }

    /**
     * Get all active magic items.
     */
    @Transactional(readOnly = true)
    public List<LotrMagicItem> findActiveItems() {
        return magicItemRepository.findAll().stream()
                .filter(LotrMagicItem::isActive)
                .toList();
    }

    /**
     * Get magic item by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrMagicItem> findById(Long id) {
        return magicItemRepository.findById(id);
    }

    /**
     * Find magic items by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrMagicItem> findByName(String name) {
        return magicItemRepository.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find magic items by type.
     */
    @Transactional(readOnly = true)
    public List<LotrMagicItem> findByType(String itemType) {
        return magicItemRepository.findAll().stream()
                .filter(item -> itemType.equals(item.getItemType()))
                .toList();
    }

    /**
     * Find magic items by rarity.
     */
    @Transactional(readOnly = true)
    public List<LotrMagicItem> findByRarity(String rarity) {
        return magicItemRepository.findAll().stream()
                .filter(item -> rarity.equals(item.getRarity()))
                .toList();
    }

    /**
     * Create a new magic item.
     */
    @Transactional
    public LotrMagicItem createMagicItem(String name, String itemType, String rarity,
                                          String description, Integer propertyAc, String propertyDamage,
                                          String propertyRange, String propertyHealing,
                                          int maxCharges, boolean requiresAttunement) {
        LotrMagicItem item = new LotrMagicItem();
        item.setName(name);
        item.setItemType(itemType);
        item.setRarity(rarity);
        item.setDescription(description);
        item.setPropertyAc(propertyAc);
        item.setPropertyDamage(propertyDamage);
        item.setPropertyRange(propertyRange);
        item.setPropertyHealing(propertyHealing);
        item.setMaxCharges(maxCharges);
        item.setCharges(maxCharges);
        item.setRequiresAttunement(requiresAttunement);
        item.setActive(true);

        LotrMagicItem saved = magicItemRepository.save(item);
        log.info("Created magic item: {} ({}, {})", saved.getName(), saved.getItemType(), saved.getRarity());
        return saved;
    }

    /**
     * Update an existing magic item.
     */
    @Transactional
    public LotrMagicItem updateMagicItem(Long id, String name, String itemType, String rarity,
                                          String description, Integer propertyAc, String propertyDamage,
                                          String propertyRange, String propertyHealing,
                                          int maxCharges, boolean requiresAttunement) {
        LotrMagicItem item = magicItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Magic item not found: " + id));

        item.setName(name);
        item.setItemType(itemType);
        item.setRarity(rarity);
        item.setDescription(description);
        item.setPropertyAc(propertyAc);
        item.setPropertyDamage(propertyDamage);
        item.setPropertyRange(propertyRange);
        item.setPropertyHealing(propertyHealing);
        item.setMaxCharges(maxCharges);
        item.setRequiresAttunement(requiresAttunement);

        LotrMagicItem saved = magicItemRepository.save(item);
        log.info("Updated magic item: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a magic item.
     */
    @Transactional
    public void deleteMagicItem(Long id) {
        if (!magicItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Magic item not found: " + id);
        }
        magicItemRepository.deleteById(id);
        log.info("Deleted magic item: {}", id);
    }

    /**
     * Use a magic item, consuming a charge.
     */
    @Transactional
    public String useMagicItem(Long id) {
        LotrMagicItem item = magicItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Magic item not found: " + id));

        if (item.getCharges() <= 0) {
            return String.format("%s has no charges remaining.", item.getName());
        }

        int newCharges = item.getCharges() - 1;
        item.setCharges(newCharges);

        if (newCharges <= 0) {
            item.setActive(false);
        }

        magicItemRepository.save(item);

        String effect = describeItemEffect(item);
        return String.format("%s is used! %s (Charges remaining: %d)", item.getName(), effect, newCharges);
    }

    /**
     * Recharge a magic item.
     */
    @Transactional
    public String rechargeMagicItem(Long id) {
        LotrMagicItem item = magicItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Magic item not found: " + id));

        item.setCharges(item.getMaxCharges());
        item.setActive(true);
        magicItemRepository.save(item);

        return String.format("%s has been recharged to %d charges.", item.getName(), item.getMaxCharges());
    }

    /**
     * Initialize the magic item database with LOTR-themed items.
     */
    @Transactional
    public void initializeMagicItemDatabase() {
        if (!magicItemRepository.findAll().isEmpty()) {
            log.info("Magic item database already initialized, skipping.");
            return;
        }

        // The One Ring
        createMagicItem("The One Ring", "Ring", "Legendary",
                "One Ring to rule them all. Grants invisibility but increases corruption.",
                null, null, null, null, 0, true);

        // Elven Rings
        createMagicItem("Narya (Ring of Fire)", "Ring", "Legendary",
                "The Ring of Fire, worn by Gandalf. Kindles the hearts of friends.",
                null, null, null, null, 0, true);
        createMagicItem("Nenya (Ring of Water)", "Ring", "Legendary",
                "The Ring of Water, worn by Galadriel. Preserves and protects Lothlórien.",
                null, null, null, null, 0, true);
        createMagicItem("Vilya (Ring of Air)", "Ring", "Legendary",
                "The most powerful of the Elven Rings, worn by Elrond.",
                null, null, null, null, 0, true);

        // Weapons
        createMagicItem("Sting", "Weapon", "Rare",
                "Elven blade that glows blue near Orcs. +1d4 piercing damage.",
                null, "1d4", null, null, 0, false);
        createMagicItem("Andúril", "Weapon", "Legendary",
                "The Flame of the West, reforged sword of Aragorn. +2d6 slashing damage.",
                null, "2d6", null, null, 0, true);
        createMagicItem("Glamdring", "Weapon", "Rare",
                "Battle-sword of Elrond, now wielded by Gandalf. +1d6 slashing damage.",
                null, "1d6", null, null, 0, false);
        createMagicItem("Orodreth", "Weapon", "Uncommon",
                "Elven blade of Faramir. +1d4 piercing damage.",
                null, "1d4", null, null, 0, false);

        // Protective Items
        createMagicItem("Phial of Galadriel", "Wondrous", "Rare",
                "Contains the light of Eärendil. Drives back shadows. 3d8 radiant damage vs undead.",
                null, null, null, "3d8", 10, false);
        createMagicItem("Mithril Coat", "Armor", "Rare",
                "Lightweight mithril armor from Bilbo. +3 AC.",
                3, null, null, null, 0, false);
        createMagicItem("Elven Cloak", "Wondrous", "Uncommon",
                "Grants advantage on Stealth checks in forested areas.",
                null, null, null, null, 0, false);

        // Other Items
        createMagicItem("Palantír", "Wondrous", "Legendary",
                "Seeing stone for scrying and communication. Can be used by Sauron to track bearer.",
                null, null, null, null, 5, true);
        createMagicItem("Entmoot Horn", "Wondrous", "Uncommon",
                "When blown, calls Ents to aid the bearer. Once per long rest.",
                null, null, null, null, 1, false);
        createMagicItem("Lembas Bread", "Wondrous", "Common",
                "Waybread of the Elves. Restores 2d4 hit points when eaten.",
                null, null, null, "2d4", 20, false);

        log.info("Initialized magic item database with {} LOTR items.", magicItemRepository.count());
    }

    // --- Helper Methods ---

    private String describeItemEffect(LotrMagicItem item) {
        if (item.getPropertyDamage() != null && !item.getPropertyDamage().isEmpty()) {
            return String.format("Deals %s damage.", item.getPropertyDamage());
        }
        if (item.getPropertyHealing() != null && !item.getPropertyHealing().isEmpty()) {
            return String.format("Heals %d hit points.", parseHealingValue(item.getPropertyHealing()));
        }
        if (item.getPropertyAc() != null) {
            return String.format("Grants +{} AC.", item.getPropertyAc());
        }
        return "Activates its magical power.";
    }

    private int parseHealingValue(String healing) {
        // Simple parsing: extract number from "2d4" format
        if (healing == null) return 0;
        try {
            String[] parts = healing.split("d");
            if (parts.length == 2) {
                int count = Integer.parseInt(parts[0]);
                int sides = Integer.parseInt(parts[1]);
                return count * (sides / 2 + 1); // Average value
            }
            return Integer.parseInt(healing);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
