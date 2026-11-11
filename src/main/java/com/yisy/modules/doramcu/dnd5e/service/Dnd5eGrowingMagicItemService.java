package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterGrowingItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eGrowingMagicItem;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterGrowingItemRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eGrowingMagicItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing D&D 5e Growing Magic Items from Arcana Unleashed.
 * <p>
 * This service handles the growing magic item system where items scale with
 * character level, unlocking new abilities at certain level thresholds.
 * </p>
 * <p>
 * Key mechanics:
 * - Items grow with the character's level
 * - Abilities unlock at levels 1, 5, 10, 15, and 20
 * - Stat bonuses increase at each tier
 * - Attunement is required to benefit from the item
 * - Leveling up the character automatically levels up the item
 * </p>
 */
@Service
public class Dnd5eGrowingMagicItemService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eGrowingMagicItemService.class);
    private static final int EXPECTED_ITEM_COUNT_MIN = 5;

    private final Dnd5eGrowingMagicItemRepository growingItemRepository;
    private final Dnd5eCharacterGrowingItemRepository charItemRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private boolean initialized = false;

    public Dnd5eGrowingMagicItemService(Dnd5eGrowingMagicItemRepository growingItemRepository,
                                        Dnd5eCharacterGrowingItemRepository charItemRepository,
                                        Dnd5eCharacterRepository characterRepository) {
        this.growingItemRepository = growingItemRepository;
        this.charItemRepository = charItemRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeGrowingItemsDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = growingItemRepository.count();
        if (existingCount >= EXPECTED_ITEM_COUNT_MIN) {
            log.info("D&D 5e growing magic items database already populated with {} items", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e growing magic items database...");
        populateGrowingItems();
        initialized = true;
        log.info("D&D 5e growing magic items database initialized: {} items", growingItemRepository.count());
    }

    // ==================== Growing Item CRUD ====================

    /**
     * Get a growing magic item by name.
     */
    @Transactional(readOnly = true)
    public Dnd5eGrowingMagicItem getGrowingItemByName(String name) {
        return growingItemRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Growing magic item not found: " + name));
    }

    /**
     * Get a growing magic item by ID.
     */
    @Transactional(readOnly = true)
    public Dnd5eGrowingMagicItem getGrowingItem(Long id) {
        return growingItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Growing magic item not found: " + id));
    }

    /**
     * List all available growing magic items.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eGrowingMagicItem> listGrowingItems() {
        return growingItemRepository.findAll();
    }

    /**
     * List growing magic items by category.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eGrowingMagicItem> listGrowingItemsByCategory(String category) {
        return growingItemRepository.findByCategory(category);
    }

    /**
     * List growing magic items by rarity.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eGrowingMagicItem> listGrowingItemsByRarity(String rarity) {
        return growingItemRepository.findByRarity(rarity);
    }

    /**
     * Search growing magic items by name.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eGrowingMagicItem> searchGrowingItems(String searchTerm) {
        return growingItemRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Create a new growing magic item.
     */
    @Transactional
    public Dnd5eGrowingMagicItem createGrowingItem(String name, String category, String rarity,
                                                    int maxLevel, String description,
                                                    String ability1, String ability5, String ability10,
                                                    String ability15, String ability20,
                                                    String statBonus1, String statBonus5, String statBonus10,
                                                    String statBonus15, String statBonus20) {
        Dnd5eGrowingMagicItem item = new Dnd5eGrowingMagicItem();
        item.setName(name);
        item.setCategory(category);
        item.setRarity(rarity);
        item.setMaxLevel(maxLevel);
        item.setDescription(description);
        item.setAbility1(ability1);
        item.setAbility5(ability5);
        item.setAbility10(ability10);
        item.setAbility15(ability15);
        item.setAbility20(ability20);
        item.setStatBonus1(statBonus1);
        item.setStatBonus5(statBonus5);
        item.setStatBonus10(statBonus10);
        item.setStatBonus15(statBonus15);
        item.setStatBonus20(statBonus20);
        return growingItemRepository.save(item);
    }

    /**
     * Delete a growing magic item by ID.
     */
    @Transactional
    public void deleteGrowingItem(Long id) {
        growingItemRepository.deleteById(id);
    }

    // ==================== Character Item Binding ====================

    /**
     * Bind a growing magic item to a character.
     * <p>
     * The item starts at level 1 and scales with the character's level.
     * </p>
     */
    @Transactional
    public Dnd5eCharacterGrowingItem bindItemToCharacter(Long characterId, Long growingItemId) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        Dnd5eGrowingMagicItem item = getGrowingItem(growingItemId);

        // Check if already bound
        if (charItemRepository.existsByCharacterIdAndGrowingItemIdAndActiveTrue(characterId, growingItemId)) {
            throw new IllegalStateException("Character already has this growing item");
        }

        Dnd5eCharacterGrowingItem charItem = new Dnd5eCharacterGrowingItem();
        charItem.setCharacterId(characterId);
        charItem.setCharacterName(character.getName());
        charItem.setGrowingItemId(growingItemId);
        charItem.setItemName(item.getName());
        charItem.setItemLevel(Math.min(character.getLevel(), item.getMaxLevel()));
        charItem.setAttuned(true);
        charItem.setActive(true);
        charItem.setAbility1UnlockedAt(1);

        // If character is already at a high level, set unlock levels
        int charLevel = character.getLevel();
        if (charLevel >= 5) charItem.setAbility5UnlockedAt(5);
        if (charLevel >= 10) charItem.setAbility10UnlockedAt(10);
        if (charLevel >= 15) charItem.setAbility15UnlockedAt(15);
        if (charLevel >= 20) charItem.setAbility20UnlockedAt(20);

        return charItemRepository.save(charItem);
    }

    /**
     * Get all growing items for a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterGrowingItem> listCharacterGrowingItems(Long characterId) {
        verifyCharacterExists(characterId);
        return charItemRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    /**
     * Get a specific growing item for a character.
     */
    @Transactional(readOnly = true)
    public Dnd5eCharacterGrowingItem getCharacterGrowingItem(Long characterId, Long growingItemId) {
        verifyCharacterExists(characterId);
        return charItemRepository.findByCharacterIdAndGrowingItemId(characterId, growingItemId)
                .orElseThrow(() -> new IllegalArgumentException("Growing item not found for character"));
    }

    /**
     * Unbind a growing item from a character.
     */
    @Transactional
    public void unbindItemFromCharacter(Long characterId, Long growingItemId) {
        Dnd5eCharacterGrowingItem charItem = getCharacterGrowingItem(characterId, growingItemId);
        charItem.setActive(false);
        charItem.setAttuned(false);
        charItemRepository.save(charItem);
    }

    /**
     * Toggle attunement for a character's growing item.
     */
    @Transactional
    public Dnd5eCharacterGrowingItem toggleAttunement(Long characterId, Long growingItemId) {
        Dnd5eCharacterGrowingItem charItem = getCharacterGrowingItem(characterId, growingItemId);
        charItem.setAttuned(!charItem.isAttuned());
        return charItemRepository.save(charItem);
    }

    // ==================== Level Up Mechanics ====================

    /**
     * Level up a character's growing item.
     * <p>
     * When a character levels up, their growing items level up too,
     * potentially unlocking new abilities.
     * </p>
     *
     * @param characterId the character's ID
     * @param newLevel the character's new level
     * @return a list of items that were leveled up with any new abilities unlocked
     */
    @Transactional
    public List<ItemLevelUpResult> levelUpItems(Long characterId, int newLevel) {
        verifyCharacterExists(characterId);
        List<Dnd5eCharacterGrowingItem> items = charItemRepository.findByCharacterIdAndActiveTrue(characterId);
        List<ItemLevelUpResult> results = new ArrayList<>();

        for (Dnd5eCharacterGrowingItem charItem : items) {
            Dnd5eGrowingMagicItem item = getGrowingItem(charItem.getGrowingItemId());
            int oldLevel = charItem.getItemLevel();
            int newLevelCapped = Math.min(newLevel, item.getMaxLevel());

            if (newLevelCapped <= oldLevel) {
                continue;
            }

            charItem.setItemLevel(newLevelCapped);
            List<String> newAbilities = new ArrayList<>();

            // Check for newly unlocked abilities
            if (newLevelCapped >= 5 && charItem.getAbility5UnlockedAt() == null) {
                charItem.setAbility5UnlockedAt(newLevelCapped);
                if (item.getAbility5() != null) {
                    newAbilities.add(item.getAbility5());
                }
            }
            if (newLevelCapped >= 10 && charItem.getAbility10UnlockedAt() == null) {
                charItem.setAbility10UnlockedAt(newLevelCapped);
                if (item.getAbility10() != null) {
                    newAbilities.add(item.getAbility10());
                }
            }
            if (newLevelCapped >= 15 && charItem.getAbility15UnlockedAt() == null) {
                charItem.setAbility15UnlockedAt(newLevelCapped);
                if (item.getAbility15() != null) {
                    newAbilities.add(item.getAbility15());
                }
            }
            if (newLevelCapped >= 20 && charItem.getAbility20UnlockedAt() == null) {
                charItem.setAbility20UnlockedAt(newLevelCapped);
                if (item.getAbility20() != null) {
                    newAbilities.add(item.getAbility20());
                }
            }

            charItemRepository.save(charItem);
            results.add(new ItemLevelUpResult(charItem.getItemName(), oldLevel, newLevelCapped, newAbilities));
        }

        return results;
    }

    /**
     * Get the current abilities for a character's growing item based on its level.
     */
    @Transactional(readOnly = true)
    public ItemAbilities getAbilities(Long characterId, Long growingItemId) {
        Dnd5eCharacterGrowingItem charItem = getCharacterGrowingItem(characterId, growingItemId);
        Dnd5eGrowingMagicItem item = getGrowingItem(growingItemId);

        ItemAbilities abilities = new ItemAbilities();
        abilities.setItemName(item.getName());
        abilities.setItemLevel(charItem.getItemLevel());
        abilities.setAttuned(charItem.isAttuned());

        // Always include ability 1
        if (item.getAbility1() != null) {
            abilities.setAbility1(item.getAbility1());
        }
        if (item.getStatBonus1() != null) {
            abilities.setStatBonus1(item.getStatBonus1());
        }

        // Include abilities based on level
        if (charItem.getItemLevel() >= 5) {
            if (item.getAbility5() != null) abilities.setAbility5(item.getAbility5());
            if (item.getStatBonus5() != null) abilities.setStatBonus5(item.getStatBonus5());
        }
        if (charItem.getItemLevel() >= 10) {
            if (item.getAbility10() != null) abilities.setAbility10(item.getAbility10());
            if (item.getStatBonus10() != null) abilities.setStatBonus10(item.getStatBonus10());
        }
        if (charItem.getItemLevel() >= 15) {
            if (item.getAbility15() != null) abilities.setAbility15(item.getAbility15());
            if (item.getStatBonus15() != null) abilities.setStatBonus15(item.getStatBonus15());
        }
        if (charItem.getItemLevel() >= 20) {
            if (item.getAbility20() != null) abilities.setAbility20(item.getAbility20());
            if (item.getStatBonus20() != null) abilities.setStatBonus20(item.getStatBonus20());
        }

        return abilities;
    }

    /**
     * Get the list of available categories.
     */
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return List.of("Weapon", "Armor", "Wondrous Item", "Ring", "Amulet", "Rod", "Staff", "Wand");
    }

    /**
     * Get the list of available rarities.
     */
    @Transactional(readOnly = true)
    public List<String> getRarities() {
        return List.of("Uncommon", "Rare", "Very Rare", "Legendary", "Artifact");
    }

    // ==================== Population ====================

    @Transactional
    public void populateGrowingItems() {
        // Weapon: Blade of Growth
        growingItemRepository.save(createItem(
                "Blade of Growth", "Weapon", "Rare", 20,
                "A sentient sword that grows in power with its wielder. The blade shimmers with an inner light that intensifies as the wielder gains experience.",
                "The sword grants a +1 bonus to attack and damage rolls.",
                "The sword gains the thundering property: on a hit, the target must succeed on a CON save or be pushed 10 feet.",
                "The sword grants a +2 bonus to attack and damage rolls. You can summon a blade of force as a bonus action.",
                "The sword grants a +3 bonus to attack and damage rolls. Once per turn, you can deal an extra 1d8 force damage.",
                "The sword becomes a legendary artifact. Once per long rest, you can unleash a wave of force in a 60-foot cone.",
                "+1 to attack and damage",
                "+1 to attack and damage, thundering",
                "+2 to attack and damage, summon blade",
                "+3 to attack and damage, extra force",
                "+3, legendary wave of force"
        ));

        // Armor: Shield of Ages
        growingItemRepository.save(createItem(
                "Shield of Ages", "Armor", "Rare", 20,
                "An ancient shield that adapts to protect its wearer. Runes glow along its surface, shifting and changing as the wearer grows stronger.",
                "The shield grants a +1 bonus to AC.",
                "Once per turn, you can impose disadvantage on one attack roll against you as a reaction.",
                "The shield grants a +2 bonus to AC. You have resistance to the damage type of the last attack that hit you.",
                "The shield grants a +3 bonus to AC. You can project a barrier that absorbs damage for an ally within 30 feet.",
                "The shield becomes impervious. Once per long rest, you can negate all damage from a single attack or spell.",
                "+1 to AC",
                "+1 to AC, reaction disadvantage",
                "+2 to AC, damage resistance",
                "+3 to AC, ally barrier",
                "+3 to AC, damage negation"
        ));

        // Wondrous Item: Amulet of Arcana
        growingItemRepository.save(createItem(
                "Amulet of Arcana", "Wondrous Item", "Very Rare", 20,
                "A crystalline amulet that channels raw magical energy. It pulses with power in rhythm with the wearer's heartbeat.",
                "You can cast one 1st-level spell from any school of magic once per day.",
                "You can cast one 2nd-level spell from any school of magic once per day, in addition to the 1st-level spell.",
                "You can cast one 3rd-level spell from any school of magic once per day. Your spell save DC increases by 1.",
                "You can cast one 4th-level spell from any school of magic once per day. You gain advantage on saves against spells.",
                "You can cast one 5th-level spell from any school of magic once per day. You can reroll one failed spell attack per day.",
                "1st-level spell once/day",
                "1st and 2nd-level spells",
                "Up to 3rd-level, +1 save DC",
                "Up to 4th-level, advantage on saves",
                "Up to 5th-level, reroll spell attack"
        ));

        // Ring: Ring of Vitality
        growingItemRepository.save(createItem(
                "Ring of Vitality", "Ring", "Uncommon", 20,
                "A golden ring that radiates warmth and vitality. It strengthens the wearer's body and spirit over time.",
                "You gain a +1 bonus to maximum hit points.",
                "At the start of your turn, you regain 1d4 hit points if you have less than half your hit points.",
                "You gain a +2 bonus to maximum hit points. You have advantage on saving throws against poison.",
                "You gain a +3 bonus to maximum hit points. Once per short rest, you can heal yourself for 2d10 hit points.",
                "You gain a +4 bonus to maximum hit points. Once per long rest, you can remove all exhaustion and cure all diseases.",
                "+1 max HP",
                "+1 max HP, regen 1d4",
                "+2 max HP, poison advantage",
                "+3 max HP, heal 2d10",
                "+4 max HP, cure all"
        ));

        // Staff: Staff of the Arcane
        growingItemRepository.save(createItem(
                "Staff of the Arcane", "Staff", "Legendary", 20,
                "A staff carved from the wood of an ancient treant, imbued with arcane energy. It grows more powerful as its master's magic deepens.",
                "You gain a +1 bonus to spell attack rolls. The staff has 10 charges for spellcasting.",
                "You gain a +1 bonus to spell attack rolls. The staff has 20 charges. You can cast magic missile as a bonus action.",
                "You gain a +2 bonus to spell attack rolls. The staff has 30 charges. You can cast fireball once per day.",
                "You gain a +3 bonus to spell attack rolls. The staff has unlimited charges for spells of 3rd level or lower.",
                "The staff becomes a conduit of raw power. You can cast any spell of 5th level or lower without expending a spell slot, once per day.",
                "+1 spell attack, 10 charges",
                "+1 spell attack, magic missile",
                "+2 spell attack, fireball",
                "+3 spell attack, unlimited low charges",
                "+3, cast any 5th-level spell free"
        ));

        // Rod: Rod of Leadership
        growingItemRepository.save(createItem(
                "Rod of Leadership", "Rod", "Rare", 20,
                "A rod of polished ebony topped with a ruby that glows when near allies. It enhances the wielder's presence and authority.",
                "You gain advantage on Charisma (Persuasion) checks.",
                "Creatures within 30 feet of you have advantage on saving throws against being frightened and charmed.",
                "You can cast command as a 1st-level spell once per day. Your Persuasion bonus extends to Intimidation.",
                "You can cast charm person and suggestion once per day each. Allies within 30 feet have a +1 bonus to attack rolls.",
                "You can dominate beast once per day. Once per long rest, inspire allies within 60 feet to gain temporary hit points equal to your level.",
                "Advantage on Persuasion",
                "Ally fear/charm resistance",
                "Command spell, Intimidation bonus",
                "Charm and suggestion, ally bonus",
                "Dominate beast, inspire allies"
        ));

        // Amulet: Amulet of the Feywild
        growingItemRepository.save(createItem(
                "Amulet of the Feywild", "Amulet", "Very Rare", 20,
                "An amulet crafted from living wood and starlight. It connects the wearer to the Feywild, granting fey powers.",
                "You can cast misty step once per long rest.",
                "You can pass through solid objects as if they were difficult terrain. You gain darkvision out to 60 feet.",
                "You can cast polymorph once per long rest. Your speed increases by 10 feet.",
                "You can cast conjure elemental once per long rest. You can spend a bonus action to become invisible for 1 minute.",
                "You can transport yourself and up to 10 willing creatures to the Feywild or back once per long rest. You have resistance to psychic damage.",
                "Misty step once/rest",
                "Phase through objects, darkvision",
                "Polymorph, +10 speed",
                "Conjure elemental, invisible",
                "Feywild transport, psychic resistance"
        ));

        // Wand: Wand of Fate
        growingItemRepository.save(createItem(
                "Wand of Fate", "Wand", "Legendary", 20,
                "A wand that bends probability to its wielder's will. Reality itself seems to warp around its tip.",
                "Once per day, you can reroll one d20 roll and keep the higher result.",
                "You can reroll two d20 rolls per day. Once per day, turn one natural 20 into a natural 1, or vice versa.",
                "You can reroll three d20 rolls per day. Once per day, you can force one creature within 60 feet to reroll a successful save.",
                "You can reroll any number of d20 rolls per day, up to your character level. You can alter fate on a 100-foot radius.",
                "You are the master of fate. Once per long rest, you can rewrite reality for 1 minute: all rolls in the area are rerolled.",
                "Reroll 1 die/day",
                "Reroll 2, alter nat 20/1",
                "Reroll 3, force reroll save",
                "Reroll up to level, 100-ft radius",
                "Rewrite reality for 1 minute"
        ));
    }

    private Dnd5eGrowingMagicItem createItem(String name, String category, String rarity,
                                              int maxLevel, String description,
                                              String ability1, String ability5, String ability10,
                                              String ability15, String ability20,
                                              String statBonus1, String statBonus5, String statBonus10,
                                              String statBonus15, String statBonus20) {
        Dnd5eGrowingMagicItem item = new Dnd5eGrowingMagicItem();
        item.setName(name);
        item.setCategory(category);
        item.setRarity(rarity);
        item.setMaxLevel(maxLevel);
        item.setDescription(description);
        item.setAbility1(ability1);
        item.setAbility5(ability5);
        item.setAbility10(ability10);
        item.setAbility15(ability15);
        item.setAbility20(ability20);
        item.setStatBonus1(statBonus1);
        item.setStatBonus5(statBonus5);
        item.setStatBonus10(statBonus10);
        item.setStatBonus15(statBonus15);
        item.setStatBonus20(statBonus20);
        return item;
    }

    private void verifyCharacterExists(Long characterId) {
        if (!characterRepository.existsById(characterId)) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
    }

    // ==================== DTOs ====================

    /**
     * Result of leveling up a growing item.
     */
    public static class ItemLevelUpResult {
        private final String itemName;
        private final int oldLevel;
        private final int newLevel;
        private final List<String> newAbilities;

        public ItemLevelUpResult(String itemName, int oldLevel, int newLevel, List<String> newAbilities) {
            this.itemName = itemName;
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.newAbilities = newAbilities;
        }

        public String getItemName() { return itemName; }
        public int getOldLevel() { return oldLevel; }
        public int getNewLevel() { return newLevel; }
        public List<String> getNewAbilities() { return newAbilities; }
    }

    /**
     * Current abilities of a character's growing item.
     */
    public static class ItemAbilities {
        private String itemName;
        private int itemLevel;
        private boolean isAttuned;
        private String ability1;
        private String statBonus1;
        private String ability5;
        private String statBonus5;
        private String ability10;
        private String statBonus10;
        private String ability15;
        private String statBonus15;
        private String ability20;
        private String statBonus20;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public int getItemLevel() { return itemLevel; }
        public void setItemLevel(int itemLevel) { this.itemLevel = itemLevel; }
        public boolean isAttuned() { return isAttuned; }
        public void setAttuned(boolean attuned) { isAttuned = attuned; }
        public String getAbility1() { return ability1; }
        public void setAbility1(String ability1) { this.ability1 = ability1; }
        public String getStatBonus1() { return statBonus1; }
        public void setStatBonus1(String statBonus1) { this.statBonus1 = statBonus1; }
        public String getAbility5() { return ability5; }
        public void setAbility5(String ability5) { this.ability5 = ability5; }
        public String getStatBonus5() { return statBonus5; }
        public void setStatBonus5(String statBonus5) { this.statBonus5 = statBonus5; }
        public String getAbility10() { return ability10; }
        public void setAbility10(String ability10) { this.ability10 = ability10; }
        public String getStatBonus10() { return statBonus10; }
        public void setStatBonus10(String statBonus10) { this.statBonus10 = statBonus10; }
        public String getAbility15() { return ability15; }
        public void setAbility15(String ability15) { this.ability15 = ability15; }
        public String getStatBonus15() { return statBonus15; }
        public void setStatBonus15(String statBonus15) { this.statBonus15 = statBonus15; }
        public String getAbility20() { return ability20; }
        public void setAbility20(String ability20) { this.ability20 = ability20; }
        public String getStatBonus20() { return statBonus20; }
        public void setStatBonus20(String statBonus20) { this.statBonus20 = statBonus20; }
    }
}
