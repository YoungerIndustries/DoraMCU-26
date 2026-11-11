package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMagicItem;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterMagicItemRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eMagicItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for managing D&D 5e Magic Items (2024 PHB).
 * <p>
 * This service handles the full magic item system including:
 * - CRUD operations for magic item definitions
 * - Character-item bindings with attunement tracking
 * - Charge management and recharging
 * - Curse handling
 * - Attunement limit enforcement (max 3 attuned items)
 * - Auto-population of 30+ classic magic items
 * </p>
 * <p>
 * Key mechanics:
 * - A character can attune to at most 3 items that require attunement
 * - Items with charges track remaining charges; using an item may expend charges
 * - Charges may recharge after short rest, long rest, or dawn
 * - Cursed items have detrimental effects until removed
 * - Rarity determines item power: Common, Uncommon, Rare, Very Rare, Legendary, Artifact
 * </p>
 */
@Service
public class Dnd5eMagicItemService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eMagicItemService.class);
    private static final int EXPECTED_ITEM_COUNT_MIN = 20;
    private static final int MAX_ATTUNED_ITEMS = 3;

    private final Dnd5eMagicItemRepository magicItemRepository;
    private final Dnd5eCharacterMagicItemRepository charItemRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private boolean initialized = false;

    public Dnd5eMagicItemService(Dnd5eMagicItemRepository magicItemRepository,
                                 Dnd5eCharacterMagicItemRepository charItemRepository,
                                 Dnd5eCharacterRepository characterRepository) {
        this.magicItemRepository = magicItemRepository;
        this.charItemRepository = charItemRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeMagicItemsDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = magicItemRepository.count();
        if (existingCount >= EXPECTED_ITEM_COUNT_MIN) {
            log.info("D&D 5e magic items database already populated with {} items", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e magic items database...");
        populateMagicItems();
        initialized = true;
        log.info("D&D 5e magic items database initialized: {} items", magicItemRepository.count());
    }

    // ==================== Magic Item CRUD ====================

    /**
     * Create a new magic item definition.
     */
    @Transactional
    public Dnd5eMagicItem createMagicItem(Dnd5eMagicItem item) {
        return magicItemRepository.save(item);
    }

    /**
     * Get a magic item by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Dnd5eMagicItem> getMagicItem(Long id) {
        return magicItemRepository.findById(id);
    }

    /**
     * Get a magic item by name.
     */
    @Transactional(readOnly = true)
    public Optional<Dnd5eMagicItem> getMagicItemByName(String name) {
        return magicItemRepository.findByName(name);
    }

    /**
     * List all magic items.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listMagicItems() {
        return magicItemRepository.findAll();
    }

    /**
     * List active magic items.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listActiveMagicItems() {
        return magicItemRepository.findByActiveTrue();
    }

    /**
     * List magic items by rarity.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listMagicItemsByRarity(String rarity) {
        return magicItemRepository.findByRarityAndActiveTrue(rarity);
    }

    /**
     * List magic items by category.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listMagicItemsByCategory(String category) {
        return magicItemRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * List items that require attunement.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listAttunementItems() {
        return magicItemRepository.findByRequiresAttunementTrue();
    }

    /**
     * List items with charges.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listChargedItems() {
        return magicItemRepository.findByHasChargesTrue();
    }

    /**
     * List cursed items.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> listCursedItems() {
        return magicItemRepository.findByCursedTrue();
    }

    /**
     * Search magic items by keyword.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eMagicItem> searchMagicItems(String keyword) {
        return magicItemRepository.searchByKeyword(keyword);
    }

    /**
     * List all rarities.
     */
    @Transactional(readOnly = true)
    public List<String> listAllRarities() {
        return magicItemRepository.findAllRarities();
    }

    /**
     * List all categories.
     */
    @Transactional(readOnly = true)
    public List<String> listAllCategories() {
        return magicItemRepository.findAllCategories();
    }

    /**
     * Update a magic item.
     */
    @Transactional
    public Dnd5eMagicItem updateMagicItem(Long id, Dnd5eMagicItem updated) {
        Dnd5eMagicItem existing = magicItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Magic item not found: " + id));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setRarity(updated.getRarity());
        existing.setCategory(updated.getCategory());
        existing.setRequiresAttunement(updated.isRequiresAttunement());
        existing.setAttunementClass(updated.getAttunementClass());
        existing.setHasCharges(updated.isHasCharges());
        existing.setMaxCharges(updated.getMaxCharges());
        existing.setRechargeRule(updated.getRechargeRule());
        existing.setRechargeDc(updated.getRechargeDc());
        existing.setCursed(updated.isCursed());
        existing.setCurseDescription(updated.getCurseDescription());
        existing.setUseAction(updated.getUseAction());
        existing.setEffectDuration(updated.getEffectDuration());
        existing.setEffectRange(updated.getEffectRange());
        existing.setActive(updated.isActive());
        return magicItemRepository.save(existing);
    }

    /**
     * Delete a magic item.
     */
    @Transactional
    public void deleteMagicItem(Long id) {
        magicItemRepository.deleteById(id);
    }

    // ==================== Character-Item Binding ====================

    /**
     * Give a magic item to a character.
     */
    @Transactional
    public Dnd5eCharacterMagicItem giveItemToCharacter(Long characterId, Long magicItemId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new NoSuchElementException("Character not found: " + characterId));
        Dnd5eMagicItem item = magicItemRepository.findById(magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Magic item not found: " + magicItemId));

        // Check if character already has this item
        if (charItemRepository.existsByCharacterIdAndMagicItemId(characterId, magicItemId)) {
            throw new IllegalStateException("Character already has this item");
        }

        Dnd5eCharacterMagicItem binding = new Dnd5eCharacterMagicItem();
        binding.setCharacterId(characterId);
        binding.setMagicItemId(magicItemId);
        binding.setCharacterName(character.getName());
        binding.setItemName(item.getName());
        binding.setActive(true);
        binding.setAttuned(false);

        // Auto-set charges if item has charges
        if (item.isHasCharges() && item.getMaxCharges() != null) {
            binding.setChargesRemaining(item.getMaxCharges());
        }

        // Auto-attune if item requires attunement and character has room
        if (item.isRequiresAttunement()) {
            int currentAttuned = charItemRepository.countActiveAttunedByCharacter(characterId);
            if (currentAttuned < MAX_ATTUNED_ITEMS) {
                binding.setAttuned(true);
            }
        }

        return charItemRepository.save(binding);
    }

    /**
     * List all items for a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterMagicItem> listCharacterItems(Long characterId) {
        return charItemRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * List attuned items for a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterMagicItem> listAttunedItems(Long characterId) {
        return charItemRepository.findActiveAttunedByCharacter(characterId);
    }

    /**
     * Count attuned items for a character.
     */
    @Transactional(readOnly = true)
    public int countAttunedItems(Long characterId) {
        return charItemRepository.countActiveAttunedByCharacter(characterId);
    }

    /**
     * Get a specific character-item binding.
     */
    @Transactional(readOnly = true)
    public Optional<Dnd5eCharacterMagicItem> getCharacterItem(Long characterId, Long magicItemId) {
        return charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId);
    }

    /**
     * Remove an item from a character.
     */
    @Transactional
    public void removeItemFromCharacter(Long characterId, Long magicItemId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));
        binding.setActive(false);
        binding.setAttuned(false);
        charItemRepository.save(binding);
    }

    // ==================== Attunement Management ====================

    /**
     * Attune to an item.
     */
    @Transactional
    public Dnd5eCharacterMagicItem attuneToItem(Long characterId, Long magicItemId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));

        Dnd5eMagicItem item = magicItemRepository.findById(magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Magic item not found: " + magicItemId));

        if (!item.isRequiresAttunement()) {
            throw new IllegalStateException("This item does not require attunement");
        }

        if (binding.isAttuned()) {
            throw new IllegalStateException("Character is already attuned to this item");
        }

        int currentAttuned = charItemRepository.countActiveAttunedByCharacter(characterId);
        if (currentAttuned >= MAX_ATTUNED_ITEMS) {
            throw new IllegalStateException("Character is already attuned to " + MAX_ATTUNED_ITEMS + " items (maximum)");
        }

        binding.setAttuned(true);
        return charItemRepository.save(binding);
    }

    /**
     * Attune to an item by binding ID.
     */
    @Transactional
    public Dnd5eCharacterMagicItem attuneToItemByBinding(Long bindingId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findById(bindingId)
                .orElseThrow(() -> new NoSuchElementException("Binding not found: " + bindingId));

        Dnd5eMagicItem item = magicItemRepository.findById(binding.getMagicItemId())
                .orElseThrow(() -> new NoSuchElementException("Magic item not found"));

        if (!item.isRequiresAttunement()) {
            throw new IllegalStateException("This item does not require attunement");
        }

        if (binding.isAttuned()) {
            throw new IllegalStateException("Character is already attuned to this item");
        }

        int currentAttuned = charItemRepository.countActiveAttunedByCharacter(binding.getCharacterId());
        if (currentAttuned >= MAX_ATTUNED_ITEMS) {
            throw new IllegalStateException("Character is already attuned to " + MAX_ATTUNED_ITEMS + " items (maximum)");
        }

        binding.setAttuned(true);
        return charItemRepository.save(binding);
    }

    /**
     * De-attune from an item.
     */
    @Transactional
    public Dnd5eCharacterMagicItem deattuneFromItem(Long characterId, Long magicItemId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));

        if (!binding.isAttuned()) {
            throw new IllegalStateException("Character is not attuned to this item");
        }

        binding.setAttuned(false);
        return charItemRepository.save(binding);
    }

    /**
     * De-attune from an item by binding ID.
     */
    @Transactional
    public Dnd5eCharacterMagicItem deattuneFromItemByBinding(Long bindingId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findById(bindingId)
                .orElseThrow(() -> new NoSuchElementException("Binding not found: " + bindingId));

        if (!binding.isAttuned()) {
            throw new IllegalStateException("Character is not attuned to this item");
        }

        binding.setAttuned(false);
        return charItemRepository.save(binding);
    }

    /**
     * Check if a character can attune to more items.
     */
    @Transactional(readOnly = true)
    public boolean canAttuneMore(Long characterId) {
        int currentAttuned = charItemRepository.countActiveAttunedByCharacter(characterId);
        return currentAttuned < MAX_ATTUNED_ITEMS;
    }

    // ==================== Charge Management ====================

    /**
     * Use an item's property, expending charges if applicable.
     */
    @Transactional
    public UseItemResult useItem(Long characterId, Long magicItemId, int chargesToSpend) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));

        Dnd5eMagicItem item = magicItemRepository.findById(magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Magic item not found: " + magicItemId));

        // Check attunement if required
        if (item.isRequiresAttunement() && !binding.isAttuned()) {
            throw new IllegalStateException("Character must be attuned to use this item");
        }

        UseItemResult result = new UseItemResult();
        result.setItemName(item.getName());
        result.setSuccess(false);

        // Handle charges
        if (item.isHasCharges()) {
            Integer currentCharges = binding.getChargesRemaining();
            if (currentCharges == null || currentCharges < chargesToSpend) {
                result.setMessage("Not enough charges. Current: " + currentCharges + ", Required: " + chargesToSpend);
                return result;
            }

            binding.setChargesRemaining(currentCharges - chargesToSpend);
            result.setChargesRemaining(currentCharges - chargesToSpend);
            result.setChargesExpended(chargesToSpend);
            result.setSuccess(true);
            result.setMessage("Used " + chargesToSpend + " charge(s). Remaining: " + (currentCharges - chargesToSpend));
        } else {
            result.setSuccess(true);
            result.setMessage("Item used successfully (no charges expended)");
        }

        binding.setUseCount(binding.getUseCount() + 1);
        charItemRepository.save(binding);
        return result;
    }

    /**
     * Recharge an item's charges.
     */
    @Transactional
    public Dnd5eCharacterMagicItem rechargeItem(Long characterId, Long magicItemId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));

        Dnd5eMagicItem item = magicItemRepository.findById(magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Magic item not found: " + magicItemId));

        if (!item.isHasCharges()) {
            throw new IllegalStateException("This item does not have charges");
        }

        // Check recharge rule
        String rule = item.getRechargeRule();
        if ("None".equals(rule)) {
            throw new IllegalStateException("This item does not recharge");
        }

        // For Dawn recharge, check if it's a new day
        if ("Dawn".equals(rule) && item.getRechargeDc() != null) {
            int roll = ThreadLocalRandom.current().nextInt(1, 21);
            if (roll < item.getRechargeDc()) {
                log.info("Item {} failed to recharge (rolled {}, DC {})", item.getName(), roll, item.getRechargeDc());
                return binding;
            }
        }

        binding.setChargesRemaining(item.getMaxCharges());
        binding.setLastRechargedAt(LocalDateTime.now());
        return charItemRepository.save(binding);
    }

    /**
     * Recharge all items for a character after a short rest.
     */
    @Transactional
    public int rechargeAfterShortRest(Long characterId) {
        List<Dnd5eCharacterMagicItem> items = charItemRepository.findByCharacterIdAndActiveTrue(characterId);
        int recharged = 0;

        for (Dnd5eCharacterMagicItem binding : items) {
            Dnd5eMagicItem item = magicItemRepository.findById(binding.getMagicItemId()).orElse(null);
            if (item != null && item.isHasCharges() && "Short Rest".equals(item.getRechargeRule())) {
                binding.setChargesRemaining(item.getMaxCharges());
                binding.setLastRechargedAt(LocalDateTime.now());
                charItemRepository.save(binding);
                recharged++;
            }
        }

        return recharged;
    }

    /**
     * Recharge all items for a character after a long rest.
     */
    @Transactional
    public int rechargeAfterLongRest(Long characterId) {
        List<Dnd5eCharacterMagicItem> items = charItemRepository.findByCharacterIdAndActiveTrue(characterId);
        int recharged = 0;

        for (Dnd5eCharacterMagicItem binding : items) {
            Dnd5eMagicItem item = magicItemRepository.findById(binding.getMagicItemId()).orElse(null);
            if (item != null && item.isHasCharges() && "Long Rest".equals(item.getRechargeRule())) {
                binding.setChargesRemaining(item.getMaxCharges());
                binding.setLastRechargedAt(LocalDateTime.now());
                charItemRepository.save(binding);
                recharged++;
            }
        }

        return recharged;
    }

    /**
     * Recharge all items for a character at dawn.
     */
    @Transactional
    public int rechargeAtDawn(Long characterId) {
        List<Dnd5eCharacterMagicItem> items = charItemRepository.findByCharacterIdAndActiveTrue(characterId);
        int recharged = 0;

        for (Dnd5eCharacterMagicItem binding : items) {
            Dnd5eMagicItem item = magicItemRepository.findById(binding.getMagicItemId()).orElse(null);
            if (item != null && item.isHasCharges() && "Dawn".equals(item.getRechargeRule())) {
                if (item.getRechargeDc() == null || ThreadLocalRandom.current().nextInt(1, 21) >= item.getRechargeDc()) {
                    binding.setChargesRemaining(item.getMaxCharges());
                    binding.setLastRechargedAt(LocalDateTime.now());
                    charItemRepository.save(binding);
                    recharged++;
                }
            }
        }

        return recharged;
    }

    // ==================== Curse Management ====================

    /**
     * Reveal a curse on an item.
     */
    @Transactional
    public Dnd5eCharacterMagicItem revealCurse(Long characterId, Long magicItemId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));

        Dnd5eMagicItem item = magicItemRepository.findById(magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Magic item not found: " + magicItemId));

        if (!item.isCursed()) {
            throw new IllegalStateException("This item is not cursed");
        }

        binding.setCurseRevealed(true);
        return charItemRepository.save(binding);
    }

    /**
     * Attempt to remove a curse (requires Remove Curse spell or similar).
     */
    @Transactional
    public boolean removeCurse(Long characterId, Long magicItemId) {
        Dnd5eCharacterMagicItem binding = charItemRepository.findByCharacterIdAndMagicItemId(characterId, magicItemId)
                .orElseThrow(() -> new NoSuchElementException("Character does not have this item"));

        if (!binding.isCurseRevealed()) {
            return false;
        }

        // Curse is removed; character can now remove the item
        binding.setCurseRevealed(false);
        charItemRepository.save(binding);
        return true;
    }

    // ==================== DTO ====================

    /**
     * Result of using a magic item.
     */
    public static class UseItemResult {
        private String itemName;
        private boolean success;
        private String message;
        private Integer chargesRemaining;
        private Integer chargesExpended;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getChargesRemaining() { return chargesRemaining; }
        public void setChargesRemaining(Integer chargesRemaining) { this.chargesRemaining = chargesRemaining; }
        public Integer getChargesExpended() { return chargesExpended; }
        public void setChargesExpended(Integer chargesExpended) { this.chargesExpended = chargesExpended; }
    }

    // ==================== Auto-Population ====================

    @Transactional
    public void populateMagicItems() {
        // --- Common Items ---
        magicItemRepository.save(buildItem("Bag of Holding", "Wondrous Item", "Common", false, null,
                "This bag has a depth of 2 feet, regardless of its exterior size, and weighs 15 pounds. " +
                        "It can hold up to 300 pounds, not exceeding a volume of 64 cubic feet. " +
                        "The bag can be carried on a belt or slung over a shoulder.",
                false, null, null, null, "No Action", "Instantaneous", "Self"));

        magicItemRepository.save(buildItem("Beads of Force", "Wondrous Item", "Common", false, null,
                "These beads appear to be simple glass marbles. You can use an action to throw " +
                        "one of the beads up to 60 feet. When you hit a target within 5 feet of " +
                        "the throw's target, the bead explodes in a 10-foot-radius sphere.",
                false, null, null, null, "Action", "Instantaneous", "60 feet"));

        magicItemRepository.save(buildItem("Chime of Opening", "Wondrous Item", "Common", false, null,
                "When you hold this chime and speak the command word, it emits a single tone. " +
                        "Any object that the sound reaches opens: doors, chests, jars, bottles, " +
                        "scroll cases, and so on. The chime can be used a number of times.",
                true, 6, "Long Rest", null, "Action", "Instantaneous", "Touch"));

        magicItemRepository.save(buildItem("Cloak of Elvenkind", "Wondrous Item", "Uncommon", true, null,
                "While you wear this cloak with its hood up, Advantage on DEX (Stealth) checks " +
                        "that rely on hiding. The cloak's elvenkind property makes you harder to notice.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        // --- Uncommon Items ---
        magicItemRepository.save(buildItem("Boots of Speed", "Wondrous Item", "Uncommon", true, null,
                "While wearing these boots, you can use a bonus action and remain attuned to them " +
                        "to gain 30 feet of speed and advantage on DEX (Acrobatics) checks. " +
                        "After 10 minutes, the boots slow you and you must take a short rest.",
                true, 3, "Dawn", 5, "Bonus Action", "10 minutes", "Self"));

        magicItemRepository.save(buildItem("Helm of Telepathy", "Wondrous Item", "Uncommon", true, null,
                "While wearing this helm, you can use an action to attempt to telepathically " +
                        "attract or repel a creature you can see within 60 feet.",
                false, null, null, null, "Action", "Instantaneous", "60 feet"));

        magicItemRepository.save(buildItem("Potion of Healing", "Potion", "Common", false, null,
                "You regain 2d4 + 2 hit points when you drink this potion. The drink takes " +
                        "an action. Nonpotions are typically stored in glass vials that hold " +
                        "about 4 fluid ounces.",
                false, null, null, null, "Action", "Instantaneous", "Self"));

        magicItemRepository.save(buildItem("Potion of Invisibility", "Potion", "Rare", false, null,
                "When you drink this potion, you become invisible for 1 hour. " +
                        "Any gear you wear or carry is invisible with you. " +
                        "The effect ends on you early if you attack or cast a spell.",
                false, null, null, null, "Action", "1 hour", "Self"));

        // --- Rare Items ---
        magicItemRepository.save(buildItem("Winged Boots", "Wondrous Item", "Rare", true, null,
                "While wearing these boots, you have a flying speed equal to your walking speed. " +
                        "The boots grant you the ability to fly as long as you wear them.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Amulet of Health", "Wondrous Item", "Rare", true, null,
                "While wearing this amulet, your CON score is 19. The amulet has no effect " +
                        "on you if your CON is already 19 or higher.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Ring of Protection", "Ring", "Rare", true, null,
                "You gain +1 to AC and saving throws while wearing this ring.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Weapon +1", "Weapon", "Magic", false, null,
                "You have a +1 bonus to attack and damage rolls made with this magic weapon.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Armor +1", "Armor", "Magic", false, null,
                "You have a +1 bonus to AC while wearing this armor.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Pearl of Power", "Wondrous Item", "Uncommon", false, null,
                "Hold this pearl and speak the command word to recover one expended spell slot. " +
                        "The slot must be of 1st level or lower. Once you use the pearl, " +
                        "it ceases to be a magic item.",
                false, null, null, null, "Action", "Instantaneous", "Self"));

        // --- Very Rare Items ---
        magicItemRepository.save(buildItem("Ring of Spell Storing", "Ring", "Very Rare", true, null,
                "This ring can store spells. When you find or create this ring, you can put " +
                        "one or more spells into it by casting them into the ring. " +
                        "The ring can store a total of 5 levels worth of spells.",
                true, 5, "None", null, "Action", "Varies", "Varies"));

        magicItemRepository.save(buildItem("Staff of Healing", "Staff", "Very Rare", true, "Cleric",
                "This staff has 10 charges. As an action, you can expend 1 or more charges " +
                        "to cast cure wounds from this staff. The spell's potency depends on " +
                        "the number of charges expended.",
                true, 10, "Dawn", 5, "Action", "Instantaneous", "Touch"));

        magicItemRepository.save(buildItem("Robe of the Archmagi", "Armor", "Very Rare", true, null,
                "While wearing this robe, you have advantage on spell saving throws. " +
                        "Casting spells that restore hit points to you is more effective. " +
                        "You can use an action to cast one of the robe's spells.",
                true, 3, "Dawn", 5, "Action", "Varies", "Varies"));

        magicItemRepository.save(buildItem("Headband of Intellect", "Wondrous Item", "Uncommon", false, null,
                "This headband sets your INT to 19 if it's lower. If your INT is 19 or higher, " +
                        "it grants a +1 bonus to INT. You must wear it for 24 hours to attune.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        // --- Legendary Items ---
        magicItemRepository.save(buildItem("Deck of Many Things", "Wondrous Item", "Legendary", false, null,
                "This deck contains the powerful cards of fate. Drawing a card can grant " +
                        "wonderful boons or terrible curses. The deck has 22 cards, each " +
                        "with a unique effect. Once all cards are drawn, the deck vanishes.",
                true, 22, "None", null, "Action", "Instantaneous", "Self"));

        magicItemRepository.save(buildItem("Cloak of Dispelling", "Wondrous Item", "Very Rare", true, null,
                "While wearing this cloak, spells of 6th level or lower have no effect on you. " +
                        "You can use an action to end the effects of all spells on a creature " +
                        "you can see within 60 feet.",
                true, 3, "Dawn", 5, "Action", "Instantaneous", "60 feet"));

        magicItemRepository.save(buildItem("Ioun Stone of Absorption", "Wondrous Item", "Very Rare", true, null,
                "This stone hovers around your head. If you would be reduced to 0 HP, " +
                        "the stone absorbs the damage and you drop to 1 HP instead. " +
                        "The stone then vanishes.",
                false, null, null, null, "No Action", "Until destroyed", "Self"));

        // --- Cursed Items ---
        magicItemRepository.save(buildItem("Cursed Ring of Mind Shielding", "Ring", "Uncommon", true, null,
                "While wearing this ring, you are immune to divination magic. " +
                        "However, the ring is cursed: while attuned, you can't cast spells " +
                        "or use magic items that require concentration.",
                true, null, null, null, "No Action", "Until removed", "Self",
                true, "While attuned, you cannot cast spells or use magic items requiring concentration."));

        magicItemRepository.save(buildItem("Cursed Dagger of Venom", "Weapon", "Uncommon", false, null,
                "This dagger deals an extra 1d6 poison damage. However, each time you hit " +
                        "with it, you must succeed on a CON save or gain a level of exhaustion.",
                true, null, null, null, "No Action", "Until removed", "Self",
                true, "Each hit with this dagger may cause you to gain a level of exhaustion."));

        // --- Additional Items ---
        magicItemRepository.save(buildItem("Bag of Tricks", "Wondrous Item", "Uncommon", false, null,
                "This small canvas bag contains 3d4 pieces of colored chalk. " +
                        "You can use an action to pull out a piece and throw it to create " +
                        "a nonmagical animal within 5 feet of the target.",
                false, null, null, null, "Action", "1 hour", "60 feet"));

        magicItemRepository.save(buildItem("Belt of Hill Giant Strength", "Wondrous Item", "Uncommon", false, null,
                "This belt sets your STR to 18 if it's lower. If your STR is 18 or higher, " +
                        "it grants a +1 bonus to STR.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Boots of Striding and Springing", "Wondrous Item", "Uncommon", false, null,
                "While wearing these boots, your jumping distance is tripled. " +
                        "You can use a bonus action to double your walking speed until the end of the turn.",
                false, null, null, null, "Bonus Action", "1 turn", "Self"));

        magicItemRepository.save(buildItem("Gloves of Missile Snaring", "Wondrous Item", "Uncommon", false, null,
                "While wearing these gloves, you have advantage on DEX saves against ranged attacks. " +
                        "You can use a reaction to catch a ranged weapon.",
                false, null, null, null, "Reaction", "Instantaneous", "Self"));

        magicItemRepository.save(buildItem("Hammer of Thunderbolts", "Weapon", "Very Rare", true, "Druid",
                "You gain a +2 bonus to attack and damage rolls. When you hit a metal object, " +
                        "you can deal lightning damage. The hammer has 7 charges that recharge at dawn.",
                true, 7, "Dawn", 5, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Oathbow", "Weapon", "Very Rare", true, null,
                "You gain a +2 bonus to attack and damage rolls. When you hit a creature, " +
                        "you can mark it. You have advantage on attacks against the marked creature.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Staff of Power", "Staff", "Very Rare", true, null,
                "You gain a +2 bonus to attack and damage rolls. The staff has 10 charges " +
                        "for various spells. You can use an action to expend charges to cast spells.",
                true, 10, "Dawn", 5, "Action", "Varies", "Varies"));

        magicItemRepository.save(buildItem("Wand of Wonder", "Wand", "Very Rare", false, null,
                "When you draw a line (1 charge), a d100 is rolled for a random effect. " +
                        "Effects range from harmless to devastating. The wand has 7 charges.",
                true, 7, "Dawn", 6, "Action", "Varies", "Varies"));

        magicItemRepository.save(buildItem("Wand of the War Mage", "Wand", "Rare", false, null,
                "You gain a +1 bonus to spell attack rolls. You have advantage on " +
                        "DEX saves against spell scrolls.",
                false, null, null, null, "No Action", "Until removed", "Self"));

        magicItemRepository.save(buildItem("Amulet of the Planes", "Wondrous Item", "Legendary", true, null,
                "While wearing this amulet, you can use an action to speak the command word " +
                        "and choose a plane of existence. You teleport to that plane.",
                true, 3, "Dawn", 5, "Action", "Instantaneous", "Self"));

        magicItemRepository.save(buildItem("Armor of Vulnerability", "Armor", "Very Rare", true, null,
                "While wearing this armor, creatures have advantage on attack rolls against you. " +
                        "This is a cursed item that weakens the wearer.",
                true, null, null, null, "No Action", "Until removed", "Self",
                true, "Creatures have advantage on attack rolls against you while wearing this armor."));

        log.info("Populated {} magic items", magicItemRepository.count());
    }

    private Dnd5eMagicItem buildItem(String name, String category, String rarity, boolean requiresAttunement,
                                      String attunementClass, String description, boolean hasCharges,
                                      Integer maxCharges, String rechargeRule, Integer rechargeDc,
                                      String useAction, String effectDuration, String effectRange) {
        return buildItem(name, category, rarity, requiresAttunement, attunementClass, description,
                hasCharges, maxCharges, rechargeRule, rechargeDc, useAction, effectDuration, effectRange,
                false, null);
    }

    private Dnd5eMagicItem buildItem(String name, String category, String rarity, boolean requiresAttunement,
                                      String attunementClass, String description, boolean hasCharges,
                                      Integer maxCharges, String rechargeRule, Integer rechargeDc,
                                      String useAction, String effectDuration, String effectRange,
                                      boolean isCursed, String curseDescription) {
        Dnd5eMagicItem item = new Dnd5eMagicItem();
        item.setName(name);
        item.setCategory(category);
        item.setRarity(rarity);
        item.setDescription(description);
        item.setRequiresAttunement(requiresAttunement);
        item.setAttunementClass(attunementClass);
        item.setHasCharges(hasCharges);
        item.setMaxCharges(maxCharges);
        item.setRechargeRule(rechargeRule != null ? rechargeRule : "None");
        item.setRechargeDc(rechargeDc);
        item.setUseAction(useAction);
        item.setEffectDuration(effectDuration);
        item.setEffectRange(effectRange);
        item.setCursed(isCursed);
        item.setCurseDescription(curseDescription);
        item.setActive(true);
        return item;
    }
}
