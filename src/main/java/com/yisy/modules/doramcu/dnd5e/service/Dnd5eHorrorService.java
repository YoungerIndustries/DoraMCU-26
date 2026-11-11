package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDarkGift;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFearDread;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eDarkGiftRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eFearDreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing D&D 5e Horror mechanics from Ravenloft.
 * <p>
 * This service handles the Domains of Dread mechanics including:
 * - Dark Gifts: Horror-themed abilities with benefits and curses
 * - Fear Level: A character's current fear state (0-6)
 * - Dread Level: Long-term corruption by the Domain (0-100)
 * - Darklord Powers: Abilities of the rulers of each Domain
 * - Fear Checks: d20 tests to resist fear effects
 * </p>
 * <p>
 * Key mechanics:
 * - Fear Level 0: No effect
 * - Fear Level 1-2: Disadvantage on d20 tests
 * - Fear Level 3-4: Cannot take reactions
 * - Fear Level 5-6: Paralyzed with fear, cannot act
 * - Dread increases when encountering horror, failing fear checks
 * - Dread decreases when succeeding on fear checks or leaving the domain
 * - Dark Gifts provide powerful abilities but increase dread
 * </p>
 */
@Service
public class Dnd5eHorrorService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eHorrorService.class);
    private static final int EXPECTED_DARK_GIFT_COUNT_MIN = 10;
    private static final Random random = new Random();

    private final Dnd5eDarkGiftRepository darkGiftRepository;
    private final Dnd5eFearDreadRepository fearDreadRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private boolean initialized = false;

    public Dnd5eHorrorService(Dnd5eDarkGiftRepository darkGiftRepository,
                              Dnd5eFearDreadRepository fearDreadRepository,
                              Dnd5eCharacterRepository characterRepository) {
        this.darkGiftRepository = darkGiftRepository;
        this.fearDreadRepository = fearDreadRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeHorrorDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = darkGiftRepository.count();
        if (existingCount >= EXPECTED_DARK_GIFT_COUNT_MIN) {
            log.info("D&D 5e horror database already populated with {} dark gifts", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e horror database...");
        populateDarkGifts();
        initialized = true;
        log.info("D&D 5e horror database initialized: {} dark gifts", darkGiftRepository.count());
    }

    // ==================== Dark Gift CRUD ====================

    /**
     * Get a Dark Gift by name.
     */
    @Transactional(readOnly = true)
    public Dnd5eDarkGift getDarkGiftByName(String name) {
        return darkGiftRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Dark Gift not found: " + name));
    }

    /**
     * Get a Dark Gift by ID.
     */
    @Transactional(readOnly = true)
    public Dnd5eDarkGift getDarkGift(Long id) {
        return darkGiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dark Gift not found: " + id));
    }

    /**
     * List all available Dark Gifts.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eDarkGift> listDarkGifts() {
        return darkGiftRepository.findAll();
    }

    /**
     * List Dark Gifts by category.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eDarkGift> listDarkGiftsByCategory(String category) {
        return darkGiftRepository.findByCategory(category);
    }

    /**
     * List Dark Gifts by associated Darklord.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eDarkGift> listDarkGiftsByDarklord(String darklord) {
        return darkGiftRepository.findByDarklord(darklord);
    }

    /**
     * List Dark Gifts available at or below a character's level.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eDarkGift> listDarkGiftsByLevel(int level) {
        return darkGiftRepository.findByMinLevelLessThanEqual(level);
    }

    /**
     * Search Dark Gifts by name.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eDarkGift> searchDarkGifts(String searchTerm) {
        return darkGiftRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Create a new Dark Gift.
     */
    @Transactional
    public Dnd5eDarkGift createDarkGift(String name, int minLevel, String category,
                                         String benefit, String curse, String darklord,
                                         boolean repeatable, String description) {
        Dnd5eDarkGift gift = new Dnd5eDarkGift();
        gift.setName(name);
        gift.setMinLevel(minLevel);
        gift.setCategory(category);
        gift.setBenefit(benefit);
        gift.setCurse(curse);
        gift.setDarklord(darklord);
        gift.setRepeatable(repeatable);
        gift.setDescription(description);
        return darkGiftRepository.save(gift);
    }

    /**
     * Delete a Dark Gift by ID.
     */
    @Transactional
    public void deleteDarkGift(Long id) {
        darkGiftRepository.deleteById(id);
    }

    // ==================== Fear & Dread Management ====================

    /**
     * Get or create fear/dread state for a character.
     */
    @Transactional
    public Dnd5eFearDread getOrCreateFearDread(Long characterId) {
        return fearDreadRepository.findByCharacterId(characterId)
                .orElseGet(() -> {
                    Dnd5eCharacter character = characterRepository.findById(characterId)
                            .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
                    Dnd5eFearDread fearDread = new Dnd5eFearDread();
                    fearDread.setCharacterId(characterId);
                    fearDread.setCharacterName(character.getName());
                    return fearDreadRepository.save(fearDread);
                });
    }

    /**
     * Get fear/dread state for a character.
     */
    @Transactional(readOnly = true)
    public Dnd5eFearDread getFearDread(Long characterId) {
        return fearDreadRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Fear/Dread not found for character: " + characterId));
    }

    /**
     * List all fear/dread states.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFearDread> listAllFearDread() {
        return fearDreadRepository.findAll();
    }

    /**
     * List characters in a specific Domain of Dread.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFearDread> listByDomain(String domain) {
        return fearDreadRepository.findByDomain(domain);
    }

    /**
     * List frightened characters.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eFearDread> listFrightenedCharacters() {
        return fearDreadRepository.findByIsFrightenedTrue();
    }

    /**
     * Set the current domain for a character.
     */
    @Transactional
    public Dnd5eFearDread setDomain(Long characterId, String domain, String darklord) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setDomain(domain);
        fearDread.setDarklord(darklord);
        return fearDreadRepository.save(fearDread);
    }

    // ==================== Fear Mechanics ====================

    /**
     * Make a fear check for a character.
     * <p>
     * Fear checks use WIS saving throw vs DC 15.
     * Success reduces fear level by 1 and dread by 5.
     * Failure increases fear level by 1 and dread by 10.
     * </p>
     *
     * @param characterId the character's ID
     * @param wisModifier the character's WIS modifier
     * @param proficient  whether the character has proficiency in WIS saves
     * @param profBonus   the character's proficiency bonus
     * @return the result of the fear check
     */
    @Transactional
    public FearCheckResult makeFearCheck(Long characterId, int wisModifier, boolean proficient, int profBonus) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);

        int roll = random.nextInt(20) + 1;
        int total = roll + wisModifier + (proficient ? profBonus : 0);
        int dc = 15;
        boolean success = total >= dc;

        // Update fear level
        if (success) {
            fearDread.setFearLevel(Math.max(0, fearDread.getFearLevel() - 1));
            fearDread.setFearChecksSucceeded(fearDread.getFearChecksSucceeded() + 1);
            // Reduce dread on success
            fearDread.setDreadLevel(Math.max(0, fearDread.getDreadLevel() - 5));
        } else {
            fearDread.setFearLevel(fearDread.getFearLevel() + 1);
            fearDread.setFearChecksFailed(fearDread.getFearChecksFailed() + 1);
            // Increase dread on failure
            fearDread.setDreadLevel(fearDread.getDreadLevel() + 10);
        }

        // Update frightened condition based on fear level
        fearDread.setFrightened(fearDread.getFearLevel() >= 3);

        fearDreadRepository.save(fearDread);

        return new FearCheckResult(roll, total, dc, success, fearDread.getFearLevel(), fearDread.getDreadLevel());
    }

    /**
     * Increase a character's fear level.
     */
    @Transactional
    public Dnd5eFearDread increaseFear(Long characterId, int amount) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setFearLevel(fearDread.getFearLevel() + amount);
        fearDread.setFrightened(fearDread.getFearLevel() >= 3);
        return fearDreadRepository.save(fearDread);
    }

    /**
     * Decrease a character's fear level.
     */
    @Transactional
    public Dnd5eFearDread decreaseFear(Long characterId, int amount) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setFearLevel(fearDread.getFearLevel() - amount);
        fearDread.setFrightened(fearDread.getFearLevel() >= 3);
        return fearDreadRepository.save(fearDread);
    }

    /**
     * Clear all fear for a character (e.g., after a short rest).
     */
    @Transactional
    public Dnd5eFearDread clearFear(Long characterId) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setFearLevel(0);
        fearDread.setFrightened(false);
        return fearDreadRepository.save(fearDread);
    }

    /**
     * Get the d20 penalty imposed by the current fear level.
     * Fear level imposes -1 to d20 tests per level.
     */
    @Transactional(readOnly = true)
    public int getFearPenalty(Long characterId) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        return -fearDread.getFearLevel();
    }

    /**
     * Get the effects of the current fear level.
     */
    @Transactional(readOnly = true)
    public String getFearEffects(Long characterId) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        int level = fearDread.getFearLevel();
        if (level == 0) return "No fear effects";
        if (level <= 2) return "Disadvantage on d20 tests";
        if (level <= 4) return "Disadvantage on d20 tests, cannot take reactions";
        return "Paralyzed with fear, cannot act, move, or speak";
    }

    // ==================== Dread Mechanics ====================

    /**
     * Increase a character's dread level.
     */
    @Transactional
    public Dnd5eFearDread increaseDread(Long characterId, int amount, String reason) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setDreadLevel(fearDread.getDreadLevel() + amount);

        // Check for dread threshold effects
        String effects = checkDreadThresholds(fearDread);
        fearDread.setDreadEffects(effects);

        return fearDreadRepository.save(fearDread);
    }

    /**
     * Decrease a character's dread level (e.g., leaving the domain, defeating a horror).
     */
    @Transactional
    public Dnd5eFearDread decreaseDread(Long characterId, int amount) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setDreadLevel(fearDread.getDreadLevel() - amount);

        // Update dread effects
        String effects = checkDreadThresholds(fearDread);
        fearDread.setDreadEffects(effects);

        return fearDreadRepository.save(fearDread);
    }

    /**
     * Clear all dread for a character (e.g., permanently leaving the Domains of Dread).
     */
    @Transactional
    public Dnd5eFearDread clearDread(Long characterId) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        fearDread.setDreadLevel(0);
        fearDread.setDreadEffects(null);
        return fearDreadRepository.save(fearDread);
    }

    /**
     * Check what dread threshold effects have triggered.
     */
    private String checkDreadThresholds(Dnd5eFearDread fearDread) {
        int dread = fearDread.getDreadLevel();
        List<String> effects = new ArrayList<>();

        if (dread >= 10) effects.add("Nightmares and disturbing visions");
        if (dread >= 25) effects.add("Physical changes: pale skin, dark circles");
        if (dread >= 50) effects.add("Mental changes: paranoia, distrust");
        if (dread >= 75) effects.add("Supernatural changes: shadowy aura, cold touch");
        if (dread >= 90) effects.add("Transformation: becoming part of the Domain");

        if (effects.isEmpty()) return null;
        return String.join("; ", effects);
    }

    /**
     * Get the dread threshold effects for a character.
     */
    @Transactional(readOnly = true)
    public String getDreadEffects(Long characterId) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        return fearDread.getDreadEffects();
    }

    /**
     * Get the dread level description.
     */
    @Transactional(readOnly = true)
    public String getDreadDescription(Long characterId) {
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);
        int dread = fearDread.getDreadLevel();

        if (dread == 0) return "No corruption";
        if (dread < 10) return "Slightly unsettled";
        if (dread < 25) return "Haunted by nightmares";
        if (dread < 50) return "Visibly affected by the Domain";
        if (dread < 75) return "Deeply corrupted by the Domain";
        if (dread < 90) return "Becoming part of the Domain";
        return "On the verge of transformation";
    }

    // ==================== Dark Gift Assignment ====================

    /**
     * Acquire a Dark Gift for a character.
     * <p>
     * Acquiring a Dark Gift increases dread by 10 and applies the gift's curse.
     * </p>
     */
    @Transactional
    public Dnd5eFearDread acquireDarkGift(Long characterId, String giftName) {
        Dnd5eDarkGift gift = getDarkGiftByName(giftName);
        Dnd5eFearDread fearDread = getOrCreateFearDread(characterId);

        // Acquiring a Dark Gift increases dread
        fearDread.setDreadLevel(fearDread.getDreadLevel() + 10);

        // Update dread effects
        String effects = checkDreadThresholds(fearDread);
        if (effects != null) {
            effects = effects + "; Dark Gift curse: " + gift.getCurse();
        } else {
            effects = "Dark Gift curse: " + gift.getCurse();
        }
        fearDread.setDreadEffects(effects);

        return fearDreadRepository.save(fearDread);
    }

    /**
     * Get the list of available Dark Gift categories.
     */
    @Transactional(readOnly = true)
    public List<String> getDarkGiftCategories() {
        return List.of("Undead", "Fiend", "Aberration", "Fey", "Beast");
    }

    /**
     * Get the list of known Darklords.
     */
    @Transactional(readOnly = true)
    public List<String> getDarklords() {
        return List.of("Strahd", "Azalin", "Vecna", "Sorren", "Arkhan", "Zutha", "Lamordia", "Dementrev");
    }

    /**
     * Get the list of known Domains of Dread.
     */
    @Transactional(readOnly = true)
    public List<String> getDomains() {
        return List.of("Barovia", "Ravenloft", "Ettin", "Tharizdun", "Harmach",
                "Dementlieu", "Isle of Dread", "Fen'Harel", "Lamordia", "Menagerie");
    }

    // ==================== Population ====================

    @Transactional
    public void populateDarkGifts() {
        // Undead Dark Gifts
        darkGiftRepository.save(createGift("Bone Armor", 1, "Undead",
                "Your bones harden, granting AC 13 + DEX modifier",
                "You have disadvantage on Charisma (Persuasion) checks",
                null, false,
                "Your skeleton becomes visible through your skin, granting natural armor."));

        darkGiftRepository.save(createGift("Life Drain", 3, "Undead",
                "As an action, drain a creature's life for 2d6 necrotic damage; you regain HP equal to half the damage",
                "You cannot regain hit points from healing magic",
                "Strahd", false,
                "You can drain the life force from living creatures to sustain yourself."));

        darkGiftRepository.save(createGift("Undead Fortitude", 5, "Undead",
                "When damage reduces you to 0 HP, you drop to 1 HP instead if you have 10+ HP before the damage",
                "Sunlight imposes disadvantage on your attack rolls",
                null, false,
                "Death cannot easily claim you, as you are already touched by undeath."));

        // Fiend Dark Gifts
        darkGiftRepository.save(createGift("Hellish Rebuke Aura", 2, "Fiend",
                "Creatures that hit you with melee attacks take 1d6 fire damage",
                "You emit a faint sulfurous smell; beasts flee from you",
                null, false,
                "Your body radiates hellish energy that burns those who strike you."));

        darkGiftRepository.save(createGift("Infernal Speech", 1, "Fiend",
                "You can speak, read, and write Infernal. You gain advantage on INT (Intimidation) checks",
                "You have disadvantage on WIS (Insight) checks",
                null, false,
                "The language of the Nine Hells flows through your veins."));

        darkGiftRepository.save(createGift("Devil's Bargain", 7, "Fiend",
                "Once per long rest, reroll any d20 test and keep the higher result",
                "A fiendish patron demands a favor once per month; refusing imposes dread +20",
                null, false,
                "You have struck a bargain with a powerful fiend who grants you power at a price."));

        // Aberration Dark Gifts
        darkGiftRepository.save(createGift("Eldritch Sight", 2, "Aberration",
                "You have darkvision out to 60 feet. You can see invisible creatures within 30 feet",
                "You have disadvantage on saving throws against being charmed",
                null, false,
                "Your eyes have adapted to see things that others cannot."));

        darkGiftRepository.save(createGift("Mind Flay", 4, "Aberration",
                "As an action, deal 2d6 psychic damage to a creature within 30 feet (WIS save for half)",
                "You hear whispers in your mind constantly; disadvantage on concentration saves",
                null, false,
                "You can project psychic energy to assault the minds of your foes."));

        // Fey Dark Gifts
        darkGiftRepository.save(createGift("Fey Step", 3, "Fey",
                "As a bonus action, teleport up to 30 feet to an unoccupied space you can see",
                "You are drawn to beauty and have disadvantage on saves against enchantment spells",
                "Lamordia", false,
                "You can step through the Feywild to appear elsewhere."));

        darkGiftRepository.save(createGift("Enchanting Presence", 1, "Fey",
                "You gain advantage on Charisma checks to influence humanoids",
                "Humanoids have advantage on saves against your spells",
                null, false,
                "Your presence is unnaturally compelling, drawing others to you."));

        // Beast Dark Gifts
        darkGiftRepository.save(createGift("Beast Sense", 1, "Beast",
                "You have advantage on WIS (Perception) checks that rely on hearing or smell",
                "You have disadvantage on INT (Investigation) checks",
                null, false,
                "Your senses have sharpened to those of a predator."));

        darkGiftRepository.save(createGift("Claws of the Beast", 4, "Beast",
                "You gain unarmed strikes that deal 1d6 slashing damage + STR modifier",
                "You cannot wear gloves or gauntlets",
                null, false,
                "Your hands transform into deadly claws when you wish."));

        // Repeatable Dark Gifts
        darkGiftRepository.save(createGift("Shadow Walk", 2, "Aberration",
                "You can move through shadows, becoming invisible while in dim light or darkness",
                "You cannot benefit from sunlight; it deals 1d4 necrotic damage to you at dawn",
                null, true,
                "You can merge with shadows and move between them."));

        darkGiftRepository.save(createGift("Curse of the Damned", 5, "Undead",
                "Once per short rest, curse a creature within 60 feet; they have disadvantage on d20 tests until the end of your next turn",
                "All creatures within 5 feet of you have disadvantage on their saving throws",
                null, true,
                "Your curse spreads to those around you, affecting friend and foe alike."));
    }

    private Dnd5eDarkGift createGift(String name, int minLevel, String category,
                                      String benefit, String curse, String darklord,
                                      boolean repeatable, String description) {
        Dnd5eDarkGift gift = new Dnd5eDarkGift();
        gift.setName(name);
        gift.setMinLevel(minLevel);
        gift.setCategory(category);
        gift.setBenefit(benefit);
        gift.setCurse(curse);
        gift.setDarklord(darklord);
        gift.setRepeatable(repeatable);
        gift.setDescription(description);
        return gift;
    }

    // ==================== DTOs ====================

    /**
     * Result of a fear check.
     */
    public static class FearCheckResult {
        private final int roll;
        private final int total;
        private final int dc;
        private final boolean success;
        private final int fearLevel;
        private final int dreadLevel;

        public FearCheckResult(int roll, int total, int dc, boolean success, int fearLevel, int dreadLevel) {
            this.roll = roll;
            this.total = total;
            this.dc = dc;
            this.success = success;
            this.fearLevel = fearLevel;
            this.dreadLevel = dreadLevel;
        }

        public int getRoll() { return roll; }
        public int getTotal() { return total; }
        public int getDc() { return dc; }
        public boolean isSuccess() { return success; }
        public int getFearLevel() { return fearLevel; }
        public int getDreadLevel() { return dreadLevel; }
    }
}
