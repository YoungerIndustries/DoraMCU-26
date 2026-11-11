package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBackground;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterBackground;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eBackgroundRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterBackgroundRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing D&D 5e Backgrounds (2024 PHB).
 * <p>
 * Backgrounds represent a character's origins and upbringing, providing
 * roleplaying hooks and mechanical benefits. The 2024 PHB revised backgrounds
 * to include feature slots that allow more customization.
 * </p>
 * <p>
 * Key mechanics:
 * - Skill Proficiencies: Each background grants 2 skill proficiencies
 * - Tool Proficiencies: Some backgrounds grant tool or language proficiencies
 * - Feature Slots: 2024 PHB backgrounds have feature slots with multiple options
 * - Feature: The main mechanical benefit of the background
 * - Equipment: Starting equipment provided by the background
 * </p>
 */
@Service
public class Dnd5eBackgroundService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eBackgroundService.class);

    private static final int EXPECTED_BACKGROUND_COUNT_MIN = 15;

    private final Dnd5eBackgroundRepository backgroundRepository;
    private final Dnd5eCharacterBackgroundRepository characterBackgroundRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private boolean initialized = false;

    public Dnd5eBackgroundService(Dnd5eBackgroundRepository backgroundRepository,
                                   Dnd5eCharacterBackgroundRepository characterBackgroundRepository,
                                   Dnd5eCharacterRepository characterRepository) {
        this.backgroundRepository = backgroundRepository;
        this.characterBackgroundRepository = characterBackgroundRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeBackgroundDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = backgroundRepository.count();
        if (existingCount >= EXPECTED_BACKGROUND_COUNT_MIN) {
            log.info("D&D 5e background database already populated with {} backgrounds", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e background database...");
        populateBackgrounds();
        initialized = true;
        log.info("D&D 5e background database initialized: {} backgrounds", backgroundRepository.count());
    }

    // ==================== Background CRUD ====================

    /**
     * Get a background by name.
     */
    @Transactional(readOnly = true)
    public Dnd5eBackground getBackgroundByName(String name) {
        return backgroundRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Background not found: " + name));
    }

    /**
     * Get a background by ID.
     */
    @Transactional(readOnly = true)
    public Dnd5eBackground getBackground(Long id) {
        return backgroundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Background not found: " + id));
    }

    /**
     * List all available backgrounds.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eBackground> listBackgrounds() {
        return backgroundRepository.findByActiveTrue();
    }

    /**
     * List all backgrounds (including inactive).
     */
    @Transactional(readOnly = true)
    public List<Dnd5eBackground> listAllBackgrounds() {
        return backgroundRepository.findAll();
    }

    /**
     * List backgrounds by category.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eBackground> listBackgroundsByCategory(String category) {
        return backgroundRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * List backgrounds that grant a specific skill proficiency.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eBackground> listBackgroundsBySkill(String skill) {
        return backgroundRepository.findBySkillProficiency(skill);
    }

    /**
     * Search backgrounds by keyword in name or description.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eBackground> searchBackgrounds(String keyword) {
        return backgroundRepository.searchByKeyword(keyword);
    }

    /**
     * List distinct categories.
     */
    @Transactional(readOnly = true)
    public List<String> listCategories() {
        return listBackgrounds().stream()
                .map(Dnd5eBackground::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Create a new background definition.
     */
    @Transactional
    public Dnd5eBackground createBackground(String name, String description, String category,
                                             String skillProficiencies, String toolProficiencies,
                                             String featureSlots, String feature,
                                             String featureDescription, String equipment) {
        if (backgroundRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Background already exists: " + name);
        }

        Dnd5eBackground background = new Dnd5eBackground();
        background.setName(name);
        background.setDescription(description);
        background.setCategory(category != null ? category : "Common");
        background.setSkillProficiencies(skillProficiencies != null ? skillProficiencies : "");
        background.setToolProficiencies(toolProficiencies);
        background.setFeatureSlots(featureSlots);
        background.setFeature(feature != null ? feature : "");
        background.setFeatureDescription(featureDescription);
        background.setEquipment(equipment);
        background.setActive(true);

        Dnd5eBackground saved = backgroundRepository.save(background);
        log.info("Created background: {}", name);
        return saved;
    }

    /**
     * Update a background.
     */
    @Transactional
    public Dnd5eBackground updateBackground(Long id, String name, String description, String category,
                                             String skillProficiencies, String toolProficiencies,
                                             String featureSlots, String feature,
                                             String featureDescription, String equipment) {
        Dnd5eBackground background = getBackground(id);

        if (name != null) background.setName(name);
        if (description != null) background.setDescription(description);
        if (category != null) background.setCategory(category);
        if (skillProficiencies != null) background.setSkillProficiencies(skillProficiencies);
        if (toolProficiencies != null) background.setToolProficiencies(toolProficiencies);
        if (featureSlots != null) background.setFeatureSlots(featureSlots);
        if (feature != null) background.setFeature(feature);
        if (featureDescription != null) background.setFeatureDescription(featureDescription);
        if (equipment != null) background.setEquipment(equipment);

        return backgroundRepository.save(background);
    }

    /**
     * Delete a background definition.
     */
    @Transactional
    public void deleteBackground(String name) {
        Dnd5eBackground background = getBackgroundByName(name);
        backgroundRepository.delete(background);
        log.info("Deleted background: {}", name);
    }

    /**
     * Toggle a background's active state.
     */
    @Transactional
    public Dnd5eBackground toggleActive(Long id, boolean active) {
        Dnd5eBackground background = getBackground(id);
        background.setActive(active);
        return backgroundRepository.save(background);
    }

    // ==================== Character Background Management ====================

    /**
     * Assign a background to a character.
     * Updates the character's background field and creates the binding.
     */
    @Transactional
    public Dnd5eCharacterBackground assignBackgroundToCharacter(Long characterId, String backgroundName,
                                                                  String featureSelections) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eBackground background = getBackgroundByName(backgroundName);

        // Check if character already has this background
        if (characterBackgroundRepository.existsByCharacterIdAndBackgroundNameAndActiveTrue(characterId, backgroundName)) {
            throw new IllegalArgumentException("Character already has this background: " + backgroundName);
        }

        // Update character's background field
        character.setBackground(backgroundName);
        characterRepository.save(character);

        // Create the character-background association
        Dnd5eCharacterBackground characterBackground = new Dnd5eCharacterBackground();
        characterBackground.setCharacterId(characterId);
        characterBackground.setCharacterName(character.getName());
        characterBackground.setBackgroundName(backgroundName);
        characterBackground.setFeatureSelections(featureSelections);
        characterBackground.setActive(true);

        Dnd5eCharacterBackground saved = characterBackgroundRepository.save(characterBackground);
        log.info("Assigned background '{}' to '{}'", backgroundName, character.getName());
        return saved;
    }

    /**
     * Remove a background from a character.
     */
    @Transactional
    public void removeBackgroundFromCharacter(Long characterId, String backgroundName) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        Dnd5eCharacterBackground characterBackground = characterBackgroundRepository
                .findByCharacterIdAndBackgroundNameAndActiveTrue(characterId, backgroundName)
                .orElseThrow(() -> new IllegalArgumentException("Character does not have this background: " + backgroundName));

        characterBackground.setActive(false);
        characterBackgroundRepository.save(characterBackground);

        // Clear character's background field if it matches
        if (backgroundName.equals(character.getBackground())) {
            character.setBackground(null);
            characterRepository.save(character);
        }

        log.info("Removed background '{}' from '{}'", backgroundName, character.getName());
    }

    /**
     * Update feature slot selections for a character's background.
     */
    @Transactional
    public Dnd5eCharacterBackground updateFeatureSelections(Long characterId, String backgroundName,
                                                              String featureSelections) {
        Dnd5eCharacterBackground characterBackground = characterBackgroundRepository
                .findByCharacterIdAndBackgroundNameAndActiveTrue(characterId, backgroundName)
                .orElseThrow(() -> new IllegalArgumentException("Character does not have this background: " + backgroundName));

        characterBackground.setFeatureSelections(featureSelections);
        return characterBackgroundRepository.save(characterBackground);
    }

    /**
     * Get all backgrounds assigned to a character.
     */
    @Transactional(readOnly = true)
    public List<Dnd5eCharacterBackground> listCharacterBackgrounds(Long characterId) {
        return characterBackgroundRepository.findByCharacterId(characterId);
    }

    /**
     * Get the active background for a character.
     */
    @Transactional(readOnly = true)
    public Dnd5eCharacterBackground getActiveCharacterBackground(Long characterId) {
        List<Dnd5eCharacterBackground> backgrounds = characterBackgroundRepository
                .findByCharacterIdAndActiveTrue(characterId);
        if (backgrounds.isEmpty()) {
            return null;
        }
        return backgrounds.get(0);
    }

    /**
     * Get the skill proficiencies granted by a background as a list.
     */
    @Transactional(readOnly = true)
    public List<String> getSkillProficiencies(String backgroundName) {
        Dnd5eBackground background = getBackgroundByName(backgroundName);
        if (background.getSkillProficiencies() == null || background.getSkillProficiencies().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(background.getSkillProficiencies().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Get the tool proficiencies granted by a background as a list.
     */
    @Transactional(readOnly = true)
    public List<String> getToolProficiencies(String backgroundName) {
        Dnd5eBackground background = getBackgroundByName(backgroundName);
        if (background.getToolProficiencies() == null || background.getToolProficiencies().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(background.getToolProficiencies().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Parse feature slots from a background's feature slots string.
     * Returns a list of slot names and their options.
     */
    @Transactional(readOnly = true)
    public List<String> getFeatureSlotOptions(String backgroundName) {
        Dnd5eBackground background = getBackgroundByName(backgroundName);
        if (background.getFeatureSlots() == null || background.getFeatureSlots().isEmpty()) {
            return List.of();
        }
        // Parse "Slot1: OptionA, OptionB | Slot2: OptionC, OptionD" format
        List<String> allOptions = new ArrayList<>();
        String[] slots = background.getFeatureSlots().split("\\|");
        for (String slot : slots) {
            String[] parts = slot.split(":");
            if (parts.length > 1) {
                String[] options = parts[1].split(",");
                for (String option : options) {
                    allOptions.add(option.trim());
                }
            }
        }
        return allOptions;
    }

    // ==================== Background Database Population ====================

    @Transactional
    public void populateBackgrounds() {
        List<Dnd5eBackground> backgrounds = new ArrayList<>();

        // --- COMMON BACKGROUNDS (2024 PHB) ---

        backgrounds.add(newBackground("Acolyte", "Common",
                "For as long as you can remember, you have been devoted to the worship of a deity or pantheon. " +
                "You live in a temple or serve as a missionary, healer, or preacher in the world.",
                "Insight, Religion",
                "Herbalism kit",
                "Sheltered: You have access to a temple network | Missionary: You can find work in temples",
                "Sheltered",
                "You can always find free shelter at a temple, shrine, or other establishment of your order. " +
                "You can also call upon the clergy for assistance with legal matters or funerals for your allies.",
                "Holy symbol, Prayer book, 5 sheets of parchment, Ink pen, Player's choice of 5 common items"));

        backgrounds.add(newBackground("Charlatan", "Common",
                "You are an accomplished scammer, con artist, or grifter. You have spent years perfecting " +
                "the art of deception, creating false identities, and pulling off elaborate cons.",
                "Deception, Sleight of Hand",
                "Disguise kit, Forgery kit",
                "Con Artist: You can create false identities | Grifter: You have a network of fences",
                "Con Artist",
                "You have created a second identity with documents, costumes, and disguises. " +
                "You can pass yourself off as an established, respectable person.",
                "False documents, Disguise kit, Tools of the con of your choice, 15 gp"));

        backgrounds.add(newBackground("Criminal", "Common",
                "You are an experienced criminal with a history of breaking the law. You have spent years " +
                "learning the tricks of the trade and building contacts in the underworld.",
                "Deception, Stealth",
                "Gaming set, Thief's tools",
                "Criminal Contact: You have a network of contacts | Crime Boss: You command a criminal organization",
                "Criminal Contact",
                "You have a reliable and trustworthy contact who acts as your liaison to a network of other criminals. " +
                "You know how to get messages to and from your contact, even over great distances.",
                "Crowbar, Dark clothing with hood, 15 gp"));

        backgrounds.add(newBackground("Entertainer", "Common",
                "You thrive in front of an audience. You have spent your life performing, whether as a " +
                "actor, singer, dancer, juggler, or teller of tales. You charm crowds and make a living from your art.",
                "Acrobatics, Performance",
                "Disguise kit, Musical instrument",
                "Fan Favorite: You can draw a crowd | Critic's Friend: You impress the elite",
                "Fan Favorite",
                "You can put on a show that includes daring stunts, inspiring songs, hilarious skits, " +
                "or romantic sonnets. When you perform, you can inspire or distract your audience.",
                "Musical instrument, Favor of an admirer, Costume, 15 gp"));

        backgrounds.add(newBackground("Folk Hero", "Common",
                "You came of age on a farm or in a humble village, and you never quite shed your simple roots. " +
                "When evil came to your homeland, you stood valiantly against it, and your tale has been told by storytellers ever since.",
                "Animal Handling, Survival",
                "Artisan's tools, Vehicle (land)",
                "Rustic Hospitality: Common folk offer you food and shelter | Heroic Origin: You have a legendary origin story",
                "Rustic Hospitality",
                "You command the respect of those who share your heritage, as you are honored for your deeds against a great evil. " +
                "Common folk will do anything they can to help you, though they may ask for a favor in return.",
                "Artisan's tools, Shovel, Iron pot, Common clothes, 10 gp"));

        backgrounds.add(newBackground("Guild Artisan", "Common",
                "You are a member of an artisans' guild, skilled in a particular craft. Whether you work " +
                "with metal, wood, stone, or cloth, your craft is your livelihood and your pride.",
                "History, Persuasion",
                "Artisan's tools, Gaming set",
                "Guild Membership: You can find work and lodging through the guild | Master Craftsman: Your work is renowned",
                "Guild Membership",
                "As a guild member, you can secure employment and lodging in guild halls throughout the land. " +
                "Other guild members are likely to provide you with materials for your craft at a discount.",
                "Artisan's tools, Letter of introduction from your guild, Traveler's clothes, 15 gp"));

        backgrounds.add(newBackground("Hermit", "Common",
                "You lived in seclusion -- either in a hermitage or simply by avoiding people -- for a formative " +
                "part of your life. Your time in solitude led to an important revelation, and you emerged from " +
                "your retreat ready to share its impact with the world.",
                "Medicine, Religion",
                "Herbalism kit, One language",
                "Discovery: You gained a profound insight | Contemplation: You have deep wisdom",
                "Discovery",
                "Your time in seclusion resulted in a discovery that you have shared with the world. " +
                "You can find places to conduct your research, including basic supplies and labors.",
                "Scroll case full of notes from your time of seclusion, Herbalism kit, Winter blanket, Common clothes, 5 gp"));

        backgrounds.add(newBackground("Noble", "Common",
                "You understand wealth, power, and privilege. You carried yourself with style and grace, " +
                "and even the basest commoner recognizes that you are cut from a different cloth. " +
                "You had an aristocratic parent or mentor who took pains to ensure you received the best education.",
                "History, Persuasion",
                "Gaming set, One language",
                "Boastful: You demand the best | Benevolent: You use your wealth to help others",
                "Boastful",
                "You carry an air of authority that commands respect. You can secure an audience with a local " +
                "leader if you present a formal letter of introduction. Nobles and commoners alike defer to your status.",
                "Fine clothes, Signet ring, Scroll of pedigree, 25 gp"));

        backgrounds.add(newBackground("Outlander", "Common",
                "You grew up in the wilds, far from civilization and the comforts of town and technology. " +
                "You have witnessed the migration of herds the size of the seasons and survived weather " +
                "beyond the extremes of civilization's experience.",
                "Athletics, Survival",
                "One musical instrument, Vehicle (land)",
                "Wanderer: You can find food and shelter in the wild | Starwalker: You navigate by the stars",
                "Wanderer",
                "You are accustomed to the hard life of the wild. You can find food and fresh water for yourself " +
                "and up to five other people, provided that the land bears vegetables, fruit, game, and fish.",
                "Staff, Hunting trap, Trophy from an animal, Traveler's clothes, 10 gp"));

        backgrounds.add(newBackground("Sage", "Common",
                "You spent years studying the secrets of the natural and unnatural world. You pored over " +
                "dusty tomes, engaged in intense discussions with your peers, and conducted experiments " +
                "in hopes of advancing the world's knowledge.",
                "Arcana, History",
                "Two languages",
                "Researcher: You can find libraries and scholars | Savant: You have encyclopedic knowledge",
                "Researcher",
                "When you need to learn information that can be found in libraries -- information that has " +
                "been compiled in books and scrolls -- you can learn that information and any skill proficiencies " +
                "associated with it by spending time studying in a library.",
                "Bottle of black ink, Quill, Small note, Letter from a dead colleague, Common clothes, 10 gp"));

        // --- RARE BACKGROUNDS ---

        backgrounds.add(newBackground("Soldier", "Rare",
                "War was never far from where you lived, and when it came calling, you answered. " +
                "You served in a structured military unit, learning the discipline of formation fighting " +
                "and the camaraderie of soldiers.",
                "Athletics, Intimidation",
                "Gaming set, Vehicle (land)",
                "Military Rank: You can requisition supplies from military outposts | Veteran: You have combat experience",
                "Military Rank",
                "By virtue of your time in the military, you can secure free lodging and a meal at an active military " +
                "outpost, such as a fort, camp, or garrison, for yourself and your allies.",
                "Insignia of rank, Trophy taken from a fallen enemy, Bone wedges, Uniform, 10 gp"));

        backgrounds.add(newBackground("Urchin", "Rare",
                "You grew up on the streets, learning to survive by your wits. You know the hidden paths " +
                "and secret haunts of the city, and you have a network of street contacts who can help you.",
                "Sleight of Hand, Stealth",
                "Gaming set, Thief's tools",
                "City Secrets: You know the hidden paths of the city | Street Rat: You can find shelter in the slums",
                "City Secrets",
                "You know the secret paths and hidden lands of your city. You can move throughout a city quickly, " +
                "and you can find shelter and food in the city's underbelly.",
                "Small knife, Map of the city, Pet mouse, Token to remember parents by, Common clothes, 10 gp"));

        // --- EXOTIC BACKGROUNDS ---

        backgrounds.add(newBackground("Agent of the Guild", "Exotic",
                "You are a spy, assassin, or information broker for a powerful guild or organization. " +
                "Your skills in deception and stealth are unmatched, and you have access to a vast network of informants.",
                "Deception, Investigation",
                "Disguise kit, Forgery kit",
                "Guild Network: You have access to the guild's resources | Shadow Operative: You can disappear at will",
                "Guild Network",
                "You can call upon the guild for assistance, including safe houses, forged documents, " +
                "and information about targets or locations.",
                "Disguise kit, Forgery kit, Signet ring of the guild, 50 gp"));

        backgrounds.add(newBackground("Ship's Captain", "Exotic",
                "You are a seasoned captain of a sea vessel, experienced in navigation, combat, and trade. " +
                "Your ship is your home, and your crew looks to you for leadership.",
                "Athletics, Perception",
                "Navigator's tools, Vehicle (water)",
                "Ship's Captain: You command a vessel and crew | Trade Route: You know the sea lanes",
                "Ship's Captain",
                "You command a ship and a crew of at least a dozen sailors. You can call upon your crew " +
                "for assistance, and your ship can transport goods and passengers across the seas.",
                "Navigator's tools, Ship's log, Map of trade routes, Fine clothes, 50 gp"));

        backgrounds.add(newBackground("Spy", "Exotic",
                "You are a trained spy and infiltrator, skilled in gathering intelligence and operating " +
                "in the shadows. You have been trained by a powerful organization or nation.",
                "Stealth, Deception",
                "Disguise kit, Forgery kit",
                "Handler: You have a handler who provides missions | Safe House: You know hidden safe houses",
                "Handler",
                "You have a handler who provides you with missions, resources, and extraction plans. " +
                "Your handler can arrange for safe passage, forged documents, and other resources.",
                "Disguise kit, Forgery kit, Cipher device, 50 gp"));

        backgrounds.add(newBackground("Urban Bounty Hunter", "Exotic",
                "You are a bounty hunter who tracks down fugitives in urban environments. " +
                "You know the streets, the underworld, and the legal system well enough to catch your marks.",
                "Athletics, Intimidation",
                "Gaming set, One tool",
                "Network: You have a network of informants | Tracker: You can follow any trail",
                "Network",
                "You have a network of informants who can provide information about fugitives, " +
                "their whereabouts, and their associates.",
                "Manacles, Gaming set, Disguise kit, 25 gp"));

        backgrounds.add(newBackground("Face", "Exotic",
                "You are a master of social manipulation, able to charm your way into any situation. " +
                "Your beauty, charisma, or social grace opens doors that would otherwise remain closed.",
                "Deception, Persuasion",
                "Disguise kit, Gaming set",
                "Social Grace: You can enter high society | Silver Tongue: You can talk your way out of anything",
                "Social Grace",
                "You can enter high society and gain access to exclusive events, gatherings, and information. " +
                "Nobles and wealthy patrons are drawn to your charm and grace.",
                "Disguise kit, Fine clothes, Jeweled pin, 50 gp"));

        backgrounds.add(newBackground("Haunted One", "Exotic",
                "You were haunted by something terrible -- a vision, a nightmare, a demonic presence. " +
                "This experience changed you, giving you insight into the darker aspects of the world.",
                "Insight, Survival",
                "One language",
                "Rage: You can fly into a rage | Warding: You can ward against evil",
                "Rage",
                "You can fly into a rage, gaining advantage on Strength checks and saving throws " +
                "but disadvantage on Intelligence and Wisdom saving throws.",
                "Amulet of evil origin, Journal, Common clothes, 10 gp"));

        // Populate
        for (Dnd5eBackground bg : backgrounds) {
            if (!backgroundRepository.existsByName(bg.getName())) {
                backgroundRepository.save(bg);
            }
        }
        log.info("Populated {} backgrounds", backgrounds.size());
    }

    // ==================== Helper Methods ====================

    private Dnd5eBackground newBackground(String name, String category, String description,
                                           String skillProficiencies, String toolProficiencies,
                                           String featureSlots, String feature,
                                           String featureDescription, String equipment) {
        Dnd5eBackground bg = new Dnd5eBackground();
        bg.setName(name);
        bg.setCategory(category);
        bg.setDescription(description);
        bg.setSkillProficiencies(skillProficiencies);
        bg.setToolProficiencies(toolProficiencies);
        bg.setFeatureSlots(featureSlots);
        bg.setFeature(feature);
        bg.setFeatureDescription(featureDescription);
        bg.setEquipment(equipment);
        bg.setActive(true);
        return bg;
    }
}
