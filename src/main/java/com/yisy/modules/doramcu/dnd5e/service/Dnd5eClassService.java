package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.*;
import com.yisy.modules.doramcu.dnd5e.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing D&D 5e Classes (2024 PHB).
 * <p>
 * Classes define a character's combat and roleplaying abilities, including
 * hit dice, primary ability scores, proficiencies, and spellcasting capabilities.
 * </p>
 * <p>
 * Key mechanics:
 * - 15 PHB classes with unique hit dice, proficiencies, and features
 * - Spellcasting types: Full caster, Half caster, Pact magician, Eldritch Burst
 * - Subclasses chosen at level 2 (or later)
 * - Multiclassing support with spellcaster level calculation
 * - Class features progress from level 1 to 20
 * </p>
 */
@Service
public class Dnd5eClassService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eClassService.class);

    private static final int EXPECTED_CLASS_COUNT_MIN = 12;

    private final Dnd5eClassRepository classRepository;
    private final Dnd5eClassFeatureRepository classFeatureRepository;
    private final Dnd5eSubclassRepository subclassRepository;
    private final Dnd5eCharacterClassRepository characterClassRepository;
    private final Dnd5eCharacterRepository characterRepository;
    private boolean initialized = false;

    public Dnd5eClassService(Dnd5eClassRepository classRepository,
                              Dnd5eClassFeatureRepository classFeatureRepository,
                              Dnd5eSubclassRepository subclassRepository,
                              Dnd5eCharacterClassRepository characterClassRepository,
                              Dnd5eCharacterRepository characterRepository) {
        this.classRepository = classRepository;
        this.classFeatureRepository = classFeatureRepository;
        this.subclassRepository = subclassRepository;
        this.characterClassRepository = characterClassRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeClassDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = classRepository.count();
        if (existingCount >= EXPECTED_CLASS_COUNT_MIN) {
            log.info("D&D 5e class database already populated with {} classes", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e class database...");
        populateClasses();
        populateSubclasses();
        populateClassFeatures();
        initialized = true;
        log.info("D&D 5e class database initialized: {} classes, {} subclasses, {} features",
                classRepository.count(), subclassRepository.count(), classFeatureRepository.count());
    }

    // ==================== Class CRUD ====================

    @Transactional(readOnly = true)
    public Dnd5eClass getClassByName(String name) {
        return classRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + name));
    }

    @Transactional(readOnly = true)
    public Dnd5eClass getClass(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClass> listClasses() {
        return classRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClass> listClassesByHitDie(String hitDie) {
        return classRepository.findByHitDieAndActiveTrue(hitDie);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClass> listClassesByPrimaryAbility(String primaryAbility) {
        return classRepository.findByPrimaryAbilityAndActiveTrue(primaryAbility);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClass> listSpellcastingClasses() {
        return classRepository.findByHasSpellcastingTrueAndActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClass> listClassesBySpellcastingType(String spellcastingType) {
        return classRepository.findBySpellcastingTypeAndActiveTrue(spellcastingType);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClass> searchClasses(String keyword) {
        return classRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Dnd5eClass createClass(Dnd5eClass cls) {
        cls.setId(null);
        return classRepository.save(cls);
    }

    @Transactional
    public Dnd5eClass updateClass(Long id, Dnd5eClass cls) {
        Dnd5eClass existing = getClass(id);
        existing.setName(cls.getName());
        existing.setDescription(cls.getDescription());
        existing.setHitDie(cls.getHitDie());
        existing.setPrimaryAbility(cls.getPrimaryAbility());
        existing.setArmorProficiencies(cls.getArmorProficiencies());
        existing.setWeaponProficiencies(cls.getWeaponProficiencies());
        existing.setToolProficiencies(cls.getToolProficiencies());
        existing.setSavingThrowProficiencies(cls.getSavingThrowProficiencies());
        existing.setHasSpellcasting(cls.isHasSpellcasting());
        existing.setSpellcastingType(cls.getSpellcastingType());
        existing.setSpellcastingAbility(cls.getSpellcastingAbility());
        existing.setMaxSpellLevel(cls.getMaxSpellLevel());
        existing.setSubclasses(cls.getSubclasses());
        existing.setActive(cls.isActive());
        return classRepository.save(existing);
    }

    @Transactional
    public void deleteClass(Long id) {
        Dnd5eClass existing = getClass(id);
        existing.setActive(false);
        classRepository.save(existing);
    }

    // ==================== Subclass CRUD ====================

    @Transactional(readOnly = true)
    public Dnd5eSubclass getSubclassByName(String name) {
        return subclassRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Subclass not found: " + name));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eSubclass> listSubclasses() {
        return subclassRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eSubclass> listSubclassesByClass(String className) {
        return subclassRepository.findByClassNameAndActiveTrue(className);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eSubclass> searchSubclasses(String keyword) {
        return subclassRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Dnd5eSubclass createSubclass(Dnd5eSubclass subclass) {
        subclass.setId(null);
        return subclassRepository.save(subclass);
    }

    @Transactional
    public void deleteSubclass(Long id) {
        Dnd5eSubclass existing = subclassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subclass not found: " + id));
        existing.setActive(false);
        subclassRepository.save(existing);
    }

    // ==================== Class Feature CRUD ====================

    @Transactional(readOnly = true)
    public List<Dnd5eClassFeature> listFeaturesByClass(String className) {
        return classFeatureRepository.findByClassNameAndActiveTrue(className);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClassFeature> listFeaturesByClassAndLevel(String className, int level) {
        return classFeatureRepository.findByClassNameAndLevelAndActiveTrue(className, level);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClassFeature> listFeaturesBySubclass(String className, String subclass) {
        return classFeatureRepository.findByClassNameAndSubclassAndActiveTrue(className, subclass);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClassFeature> listFeaturesUpToLevel(String className, int level) {
        return classFeatureRepository.findFeaturesUpToLevel(className, level);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClassFeature> listSubclassFeaturesUpToLevel(String className, String subclass, int level) {
        return classFeatureRepository.findSubclassFeaturesUpToLevel(className, subclass, level);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eClassFeature> searchFeatures(String keyword) {
        return classFeatureRepository.searchByName(keyword);
    }

    @Transactional
    public Dnd5eClassFeature createFeature(Dnd5eClassFeature feature) {
        feature.setId(null);
        return classFeatureRepository.save(feature);
    }

    @Transactional
    public void deleteFeature(Long id) {
        Dnd5eClassFeature existing = classFeatureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + id));
        existing.setActive(false);
        classFeatureRepository.save(existing);
    }

    // ==================== Character Class Management ====================

    @Transactional
    public Dnd5eCharacterClass addClassToCharacter(Long characterId, String className) {
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        if (characterClassRepository.existsByCharacterIdAndClassNameAndActiveTrue(characterId, className)) {
            throw new IllegalArgumentException("Character already has class: " + className);
        }

        // Verify class exists
        getClassByName(className);

        Dnd5eCharacterClass charClass = new Dnd5eCharacterClass();
        charClass.setCharacterId(characterId);
        charClass.setCharacterName(character.getName());
        charClass.setClassName(className);
        charClass.setLevel(1);

        // Update character's primary class string
        List<Dnd5eCharacterClass> existingClasses = characterClassRepository.findByCharacterIdAndActiveTrue(characterId);
        if (existingClasses.isEmpty()) {
            character.setCharacterClass(className);
        } else {
            character.setCharacterClass(existingClasses.stream()
                    .map(Dnd5eCharacterClass::getClassName)
                    .collect(Collectors.joining("/")) + "/" + className);
        }
        characterRepository.save(character);

        return characterClassRepository.save(charClass);
    }

    @Transactional
    public Dnd5eCharacterClass levelUpClass(Long characterId, String className, int newLevel) {
        Dnd5eCharacterClass charClass = characterClassRepository
                .findByCharacterIdAndClassNameAndActiveTrue(characterId, className)
                .orElseThrow(() -> new IllegalArgumentException("Class not found for character: " + className));

        newLevel = Math.max(1, Math.min(20, newLevel));
        charClass.setLevel(newLevel);

        // Update character's total level
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        List<Dnd5eCharacterClass> allClasses = characterClassRepository.findByCharacterIdAndActiveTrue(characterId);
        int totalLevel = allClasses.stream().mapToInt(Dnd5eCharacterClass::getLevel).sum();
        character.setLevel(totalLevel);
        characterRepository.save(character);

        return characterClassRepository.save(charClass);
    }

    @Transactional
    public Dnd5eCharacterClass setSubclass(Long characterId, String className, String subclass) {
        Dnd5eCharacterClass charClass = characterClassRepository
                .findByCharacterIdAndClassNameAndActiveTrue(characterId, className)
                .orElseThrow(() -> new IllegalArgumentException("Class not found for character: " + className));

        // Verify subclass exists for this class
        Dnd5eSubclass sub = subclassRepository.findByName(subclass)
                .orElseThrow(() -> new IllegalArgumentException("Subclass not found: " + subclass));
        if (!sub.getClassName().equals(className)) {
            throw new IllegalArgumentException("Subclass " + subclass + " is not for class " + className);
        }

        charClass.setSubclass(subclass);
        return characterClassRepository.save(charClass);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCharacterClass> listCharacterClasses(Long characterId) {
        return characterClassRepository.findByCharacterIdAndActiveTrue(characterId);
    }

    @Transactional
    public void removeClassFromCharacter(Long characterId, String className) {
        Dnd5eCharacterClass charClass = characterClassRepository
                .findByCharacterIdAndClassNameAndActiveTrue(characterId, className)
                .orElseThrow(() -> new IllegalArgumentException("Class not found for character: " + className));
        charClass.setActive(false);
        characterClassRepository.save(charClass);

        // Update character's class string
        Dnd5eCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        List<Dnd5eCharacterClass> remaining = characterClassRepository.findByCharacterIdAndActiveTrue(characterId);
        if (remaining.isEmpty()) {
            character.setCharacterClass("None");
        } else {
            character.setCharacterClass(remaining.stream()
                    .map(Dnd5eCharacterClass::getClassName)
                    .collect(Collectors.joining("/")));
        }
        characterRepository.save(character);
    }

    // ==================== Spellcaster Level Calculation ====================

    /**
     * Calculate effective spellcaster level for multiclass characters.
     * Follows PHB multiclassing rules for spellcaster levels.
     *
     * Full casters: Bard, Cleric, Druid, Sorcerer, Wizard
     * Half casters: Paladin, Ranger
     * Pact: Warlock (special rules)
     * Eldritch Burst: Artificer (levels 2,3,5,6,7,8,9,10 count)
     */
    @Transactional(readOnly = true)
    public int calculateSpellcasterLevel(Long characterId) {
        List<Dnd5eCharacterClass> charClasses = characterClassRepository.findByCharacterIdAndActiveTrue(characterId);
        int fullCasterLevels = 0;
        int halfCasterLevels = 0;
        int thirdCasterLevels = 0; // Artificer

        for (Dnd5eCharacterClass cc : charClasses) {
            Dnd5eClass cls = classRepository.findByName(cc.getClassName()).orElse(null);
            if (cls == null) continue;

            String type = cls.getSpellcastingType();
            if ("Full".equals(type) || "Spellcasting".equals(type)) {
                fullCasterLevels += cc.getLevel();
            } else if ("Half".equals(type)) {
                halfCasterLevels += cc.getLevel();
            } else if ("Eldritch Burst".equals(type)) {
                thirdCasterLevels += cc.getLevel();
            }
            // Warlock (Pact) doesn't contribute to multiclass spellcaster level
        }

        int effectiveLevel = fullCasterLevels;
        effectiveLevel += Math.max(0, halfCasterLevels / 2);
        effectiveLevel += Math.max(0, thirdCasterLevels / 3);

        return effectiveLevel;
    }

    // ==================== Population ====================

    private void populateClasses() {
        // Barbarian
        saveClass(new Dnd5eClass("Barbarian",
                "A fierce warrior who fights in a frenzied rage, using adrenaline and fury to overwhelm enemies.",
                "d12", "STR", null, "Simple weapons, Martial weapons", null,
                "Strength, Constitution", false, null, "None", 0,
                "Path of the Berserker, Path of the Totem Warrior, Path of the Ancestral Guardian, Path of the Beast"));

        // Bard
        saveClass(new Dnd5eClass("Bard",
                "A charismatic performer who uses music and magic to inspire allies and undermine foes.",
                "d8", "CHA", "Light armor", "Simple weapons, Hand crossbows, Longswords, Rapiers, Shortswords",
                "Three musical instruments of your choice", "Dexterity, Constitution", true, "Full", "CHA", 9,
                "College of Lore, College of Valor, College of Creation, College of Whispers"));

        // Cleric
        saveClass(new Dnd5eClass("Cleric",
                "A divine priest who channels the power of the gods to heal allies and smite enemies.",
                "d8", "WIS", "Light armor, Medium armor, Shields", "Simple weapons", null,
                "Wisdom, Charisma", true, "Full", "WIS", 9,
                "Life Domain, Light Domain, Knowledge Domain, Nature Domain, Tempest Domain, Trickery Domain, War Domain"));

        // Druid
        saveClass(new Dnd5eClass("Druid",
                "A priest of the Old Faith, wielding the powers of nature and wild shapes.",
                "d8", "WIS", "Light armor, Medium armor, Shields", "Clubs, Daggers, Darts, Javelins, Maces, Quarterstaffs, Scimitars, Sickle, Slings, Spears",
                "Herbalism kit", "Intelligence, Wisdom", true, "Full", "WIS", 9,
                "Circle of the Land, Circle of the Moon, Circle of Dreams, Circle of the Wildfire"));

        // Fighter
        saveClass(new Dnd5eClass("Fighter",
                "A master of martial combat, skilled with weapons and armor.",
                "d10", "STR", "All armor, Shields", "Simple weapons, Martial weapons", null,
                "Strength, Constitution", false, null, "None", 0,
                "Champion, Battle Master, Eldritch Knight, Arcane Archer, Rune Knight"));

        // Monk
        saveClass(new Dnd5eClass("Monk",
                "A martial artist who harnesses the mystical energy flow called ki.",
                "d8", "DEX", null, "Simple weapons, Short swords", "One type of artisan's tools or one musical instrument",
                "Dexterity, Wisdom", false, null, "None", 0,
                "Way of Open Hand, Way of Shadow, Way of the Four Elements, Way of Mercy"));

        // Paladin
        saveClass(new Dnd5eClass("Paladin",
                "A holy warrior who combines martial prowess with divine magic to fight evil.",
                "d10", "STR", "All armor, Shields", "Simple weapons, Martial weapons", "Insight, Medicine",
                "Wisdom, Charisma", true, "Half", "CHA", 5,
                "Oath of Devotion, Oath of the Ancients, Oath of Vengeance, Oath of Conquest"));

        // Ranger
        saveClass(new Dnd5eClass("Ranger",
                "A warrior who hunts the brutal monsters of the world, using stealth and spellcraft.",
                "d10", "DEX", "Light armor, Medium armor, Shields", "Simple weapons, Martial weapons", "Athletics, Survival",
                "Strength, Dexterity", true, "Half", "WIS", 5,
                "Hunter, Beast Master, Gloom Stalker, Swarmkeeper"));

        // Rogue
        saveClass(new Dnd5eClass("Rogue",
                "A scoundrel who uses stealth and trickery to get ahead in a dangerous world.",
                "d8", "DEX", null, "Simple weapons, Hand crossbows, Longswords, Rapiers, Shortswords", "Thieves' tools",
                "Dexterity, Intelligence", false, null, "None", 0,
                "Thief, Assassin, Arcane Trickster, Phantom, Soulknife"));

        // Sorcerer
        saveClass(new Dnd5eClass("Sorcerer",
                "A magic user who draws on innate magic from within their own being.",
                "d6", "CHA", null, "Daggers, Darts, Slings, Quarterstaffs, Light crossbows", null,
                "Constitution, Charisma", true, "Full", "CHA", 9,
                "Draconic Bloodline, Wild Magic, Shadow Magic, Aberrant Mind"));

        // Warlock
        saveClass(new Dnd5eClass("Warlock",
                "A wielder of magic that derives from a deal with an extraplanar entity.",
                "d8", "CHA", "Light armor", "Simple weapons", null,
                "Wisdom, Charisma", true, "Pact", "CHA", 5,
                "The Archfey, The Fiend, The Great Old One, The Hexblade"));

        // Wizard
        saveClass(new Dnd5eClass("Wizard",
                "A scholarly magic-user who can bend the forces of magic to their will.",
                "d6", "INT", null, "Daggers, Darts, Slings, Quarterstaffs, Light crossbows", "Arcana",
                "Intelligence, Wisdom", true, "Full", "INT", 9,
                "School of Evocation, School of Abjuration, School of Conjuration, School of Divination, " +
                "School of Enchantment, School of Illusion, School of Necromancy, School of Transmutation"));

        // Artificer (TCE)
        saveClass(new Dnd5eClass("Artificer",
                "A mystical tinkerer who uses magic to infuse items with power.",
                "d8", "INT", "Light armor", "Simple weapons, Bows", "Thieves' tools, One type of artisan's tools",
                "Constitution, Intelligence", true, "Eldritch Burst", "INT", 5,
                "Alchemist, Armorer, Artillerist, Battle Smith"));

        // Blood Hunter (GGR)
        saveClass(new Dnd5eClass("Blood Hunter",
                "A hunter who uses mutagenic blood magic to fight the undead and aberrations.",
                "d10", "STR", "Light armor, Shields", "Simple weapons, Martial weapons", "One type of artisan's tools",
                "Constitution, Intelligence", false, null, "None", 0,
                "Order of the Ghost, Order of the Lycan, Order of the Mutant, Order of the Vengeance"));
    }

    private Dnd5eClass saveClass(Dnd5eClass cls) {
        if (!classRepository.existsByName(cls.getName())) {
            cls.setId(null);
            cls.setActive(true);
            return classRepository.save(cls);
        }
        return cls;
    }

    private void populateSubclasses() {
        // Barbarian subclasses
        saveSubclass("Path of the Berserker", "Barbarian",
                "A barbarian who embraces the fury of battle, able to go into a frenzy and ignore pain.",
                "Level3: Frenzy | Level6: Mindless Rage | Level10: Intimidating Presence | Level14: Retaliation");
        saveSubclass("Path of the Totem Warrior", "Barbarian",
                "A barbarian who draws strength from a spiritual connection to a totem animal.",
                "Level3: Spirit Seeker, Totem Spirit | Level6: Aspect of the Beast | Level10: Magical Totem | Level14: Totem Warrior");
        saveSubclass("Path of the Ancestral Guardian", "Barbarian",
                "A barbarian who protects allies with the spirits of ancestors.",
                "Level3: Ancestral Protectors | Level6: Ancestral Command | Level10: Spirit Speaks | Level14: Ancestral Protectors Improvement");
        saveSubclass("Path of the Beast", "Barbarian",
                "A barbarian who mimics the deadly abilities of beasts.",
                "Level3: Primal Knowledge, Bestial Soul | Level6: Bestial Movement | Level10: Bestial Instinct | Level14: Claws of the Beast");

        // Fighter subclasses
        saveSubclass("Champion", "Fighter",
                "A fighter who focuses on raw combat prowess, improving basic attacks.",
                "Level3: Improved Critical | Level5: Remarkable Athlete | Level7: Additional Fighting Style | Level10: Superior Critical | Level15: Critical Strike | Level18: Survivor");
        saveSubclass("Battle Master", "Fighter",
                "A fighter who studies combat techniques to gain tactical advantages.",
                "Level3: Combat Superiority, Battle Master Maneuvers | Level7: Additional Maneuvers, Student of War | Level10: Know Your Enemy | Level15: Improved Combat Superiority | Level18: Relentless");
        saveSubclass("Eldritch Knight", "Fighter",
                "A fighter who combines martial training with arcane magic.",
                "Level3: Weapon Bond, War Magic | Level5: Evocation Wizard Spells | Level7: War Magic Improvement | Level10: Arcane Charge | Level15: Eldritch Strike | Level18: Improved War Magic");

        // Rogue subclasses
        saveSubclass("Thief", "Rogue",
                "A rogue who specializes in stealing and quick thinking.",
                "Level3: Fast Hands, Second-Story Work | Level9: Supreme Sneak | Level13: Use Magic Device | Level17: Thief's Reflexes");
        saveSubclass("Assassin", "Rogue",
                "A rogue who kills with precision and stealth.",
                "Level3: Bonus Proficiencies, Assassinate | Level9: Sneak Attack Improvement | Level13: Death Strike | Level17: Impostor");
        saveSubclass("Arcane Trickster", "Rogue",
                "A rogue who uses illusion and enchantment magic alongside thievery.",
                "Level3: Spellcasting, Mage Hand Legerdemain | Level9: Magical Ambush | Level13: Versatile Trickster | Level17: Spell Thief");

        // Wizard subclasses
        saveSubclass("School of Evocation", "Wizard",
                "A wizard who specializes in destructive magic: fire, cold, lightning, and force.",
                "Level2: Evocation Savant, Sculpt Spells | Level6: Potent Cantrip | Level10: Empowered Evocation | Level14: Overchannel");
        saveSubclass("School of Abjuration", "Wizard",
                "A wizard who focuses on protective magic and warding.",
                "Level2: Abjuration Savant, Arcane Ward | Level6: Improved Abjuration | Level10: Projected Ward | Level14: Spell Resistance");
        saveSubclass("School of Conjuration", "Wizard",
                "A wizard who manipulates the planes to summon creatures and objects.",
                "Level2: Conjuration Savant, Bonus Proficiency | Level6: Minor Conjuration | Level10: Overchannel | Level14: Step Through Shadows");
        saveSubclass("School of Divination", "Wizard",
                "A wizard who peeks into the future to gain advantage.",
                "Level2: Divination Savant, Portent | Level6: Expert Divination | Level10: The Third Eye | Level14: Greater Portent");
        saveSubclass("School of Enchantment", "Wizard",
                "A wizard who controls minds and influences emotions.",
                "Level2: Enchantment Savant, Hypnotic Gaze | Level6: Advanced Hypnotic Gaze | Level10: Time Stop Mastery | Level14: Modify Memory");
        saveSubclass("School of Illusion", "Wizard",
                "A wizard who creates false sights, sounds, and sensations.",
                "Level2: Illusion Savant, Improved Minor Illusion | Level6: Malleable Illusions | Level10: Illusory Self | Level14: Illusory Reality");
        saveSubclass("School of Necromancy", "Wizard",
                "A wizard who commands death energy and undead.",
                "Level2: Necromancy Savant, Undead Thrall | Level6: Grave Taint | Level10: Command Undead | Level14: Inured to Undead");
        saveSubclass("School of Transmutation", "Wizard",
                "A wizard who changes the nature of reality itself.",
                "Level2: Transmutation Savant, Minor Alchemy | Level6: Transmuter's Stone | Level10: Shapechanger | Level14: Master Transmuter");

        // Cleric subclasses
        saveSubclass("Life Domain", "Cleric",
                "A cleric who focuses on healing and protecting the living.",
                "Level1: Bonus Proficiency, Disciple of Life | Level2: Channel Divinity: Preserve Life | Level6: Divine Strike | Level8: Blessing of Nature | Level17: Supreme Healing");
        saveSubclass("Light Domain", "Cleric",
                "A cleric who wields radiant energy to smite foes.",
                "Level1: Weapon Proficiencies, Warding Flare | Level2: Channel Divinity: Radiance of the Dawn | Level6: Radiant Wrath | Level8: Coruscating Seal | Level17: Sunlight Radiance");
        saveSubclass("Tempest Domain", "Cleric",
                "A cleric who commands the fury of storms.",
                "Level1: Weapon Proficiencies, Wrath of the Storm | Level2: Channel Divinity: Destructive Wave | Level6: Thunderbolt Strike | Level8: Ascendant Step | Level17: Wind Walk");

        // Druid subclasses
        saveSubclass("Circle of the Land", "Druid",
                "A druid who draws magic from the natural features of the land.",
                "Level2: Natural Recovery, Regional Explorer | Level6: Land's Stride | Level10: Nature's Ward | Level14: Nature's Presence");
        saveSubclass("Circle of the Moon", "Druid",
                "A druid who masters wild shape, becoming powerful beasts.",
                "Level2: Combat Wild Shape, Circle Forms | Level6: Primal Strike | Level10: Elemental Wild Shape | Level14: Thousand Forms");

        // Paladin subclasses
        saveSubclass("Oath of Devotion", "Paladin",
                "A paladin who upholds the highest ideals of good and honor.",
                "Level3: Oath Spells, Holy Weapon, Sacred Weapon | Level7: Aura of Devotion | Level15: Aura of Wisdom | Level20: Aura of Divine Strength");
        saveSubclass("Oath of Vengeance", "Paladin",
                "A paladin who hunts down those who commit atrocities.",
                "Level3: Oath Spells, Abjure Enemy, Vow of Enmity | Level7: Aura of Wrath | Level15: Aura of Victory | Level20: Soul of Vengeance");

        // Ranger subclasses
        saveSubclass("Hunter", "Ranger",
                "A ranger who specializes in hunting specific types of foes.",
                "Level3: Hunter's Prey | Level7: Defensive Tactics | Level11: Multiattack | Level15: Ultra Kill");
        saveSubclass("Beast Master", "Ranger",
                "A ranger who forms a bond with an animal companion.",
                "Level3: Ranger's Companion | Level7: Beast's Sense | Level11: Exceptional Training | Level15: Feral Senses");

        // Sorcerer subclasses
        saveSubclass("Draconic Bloodline", "Sorcerer",
                "A sorcerer whose magic comes from a dragon ancestor.",
                "Level1: Dragon Ancestor, Draconic Resilience | Level6: Elementals Soul | Level14: Dragon Wings | Level18: Elemental Mastery");
        saveSubclass("Wild Magic", "Sorcerer",
                "A sorcerer whose magic is chaotic and unpredictable.",
                "Level1: Wild Magic Surge, Bounded Wild Magic | Level3: Wild Magic Spells | Level6: Controlled Chaos | Level14: Wild Magic Mastery");

        // Warlock subclasses
        saveSubclass("The Archfey", "Warlock",
                "A warlock who serves a fey patron from the Feywild.",
                "Level1: Otherworldly Patron, Fey Presence | Level2: Dark One's Blessing | Level6: Misty Escape | Level14: Fae Wards");
        saveSubclass("The Fiend", "Warlock",
                "A warlock who made a pact with a fiend from the Lower Planes.",
                "Level1: Otherworldly Patron, Dark One's Blessing | Level2: Dark One's Own Luck | Level6: Fiendish Vigor | Level14: Hurl Through Hell");
        saveSubclass("The Hexblade", "Warlock",
                "A warlock who wields a cursed blade blessed by the Shadowfell.",
                "Level1: Hexblade's Curse, Curse of the Hexblade | Level2: Hexblade's Curse Improvement | Level6: Accursed Specter | Level14: Master of Hexes");

        // Monk subclasses
        saveSubclass("Way of Open Hand", "Monk",
                "A monk who masters unarmed combat techniques.",
                "Level3: Open Hand Technique | Level6: Rising Sun Kick | Level11: Quivering Palm | Level17: Perfection");
        saveSubclass("Way of Shadow", "Monk",
                "A monk who uses stealth and shadow magic.",
                "Level3: Shadow Arts | Level6: Shadow Step | Level11: Cloud of Darkness | Level17: Shadow Stealth");

        // Bard subclasses
        saveSubclass("College of Lore", "Bard",
                "A bard who seeks forbidden knowledge through songs and stories.",
                "Level3: Cutting Words, Bonus Canntrips | Level6: Countercharm | Level14: Peerless Skill");
        saveSubclass("College of Valor", "Bard",
                "A bard who protects allies in battle with inspiring songs.",
                "Level3: Combat Inspiration, Extra Attack | Level6: Battle Magic | Level14: Restful Brevity");

        // Artificer subclasses
        saveSubclass("Alchemist", "Artificer",
                "An artificer who creates magical potions and elixirs.",
                "Level3: Chemical Mastery, Restorative Reagents | Level5: Alchemical Savant | Level7: Transmuter | Level9: Panacea");
        saveSubclass("Armorer", "Artificer",
                "An artificer who creates magical armor with integrated weapons.",
                "Level3: Arcane Armor, Arcane Jolt | Level5: Heavy Armor Protocol | Level7: Ejector Blast | Level9: Holistic Protector");
        saveSubclass("Artillerist", "Artificer",
                "An artificer who creates magical siege weapons and turrets.",
                "Level3: Arcane Firearm, Eldritch Cannon | Level5: Abrupt Smite | Level7: Eldritch Cannon Improvement | Level9: Storm's Edge");
        saveSubclass("Battle Smith", "Artificer",
                "An artificer who creates a steel defender construct.",
                "Level3: Steel Defender, Arcane Jolt | Level5: Extra Attack | Level7: Repelling Blast | Level9: Extra Arcane Properties");

        // Blood Hunter subclasses
        saveSubclass("Order of the Ghost", "Blood Hunter",
                "A blood hunter who uses spectral magic to fight the undead.",
                "Level1: Hemocraft, Order of the Ghost | Level3: Haunting, Spectral Hand | Level6: Deathless | Level7: Spectral Hand Improvement | Level15: Deathless Improvement");
        saveSubclass("Order of the Lycan", "Blood Hunter",
                "A blood hunter who transforms into a werewolf-like creature.",
                "Level1: Hemocraft, Order of the Lycan | Level3: Lycanthropy | Level6: Lycanthropy Improvement | Level7: Lycanthropy Improvement 2 | Level15: Lycanthropy Mastery");
    }

    private Dnd5eSubclass saveSubclass(String name, String className, String description, String featureSummary) {
        if (!subclassRepository.existsByName(name)) {
            Dnd5eSubclass sub = new Dnd5eSubclass();
            sub.setName(name);
            sub.setClassName(className);
            sub.setDescription(description);
            sub.setFeatureSummary(featureSummary);
            sub.setActive(true);
            return subclassRepository.save(sub);
        }
        return null;
    }

    private void populateClassFeatures() {
        // Barbarian features
        saveFeature("Barbarian", null, 1, "Rage", "You can rage for 1 minute as a bonus action. While raging, you have advantage on STR checks and saves, +2 melee damage, and resistance to bludgeoning/piercing/slashing damage. You can rage twice per long rest, gaining more uses as you level.", true, 0);
        saveFeature("Barbarian", null, 1, "Unarmored Defense", "While not wearing armor, your AC = 10 + DEX modifier + CON modifier.", false, 0);
        saveFeature("Barbarian", null, 2, "Reckless Attack", "At the start of your turn, you can go reckless. You have advantage on melee STR attacks, and attackers have advantage on attacks against you until your next turn.", false, 0);
        saveFeature("Barbarian", null, 2, "Danger Sense", "You have advantage on DEX saving throws against effects you can see.", false, 0);
        saveFeature("Barbarian", null, 5, "Extra Attack", "You can attack twice, instead of once, whenever you take the Attack action on your turn.", false, 0);
        saveFeature("Barbarian", null, 5, "Fast Movement", "Your speed increases by 10 feet while you aren't wearing heavy armor.", false, 0);
        saveFeature("Barbarian", null, 7, "Feral Instinct", "You fight with heightened senses. You have advantage on initiative rolls and can't be surprised while conscious. When an attacker is within 5 feet of you, that attacker has disadvantage on attack rolls against you, and you have advantage on attack rolls against that attacker.", false, 0);
        saveFeature("Barbarian", null, 9, "Brutal Critical", "When you score a critical hit, you can roll one additional damage die. This die increases at higher levels.", false, 0);
        saveFeature("Barbarian", null, 11, "Persistent Rage", "Your rage can last up to 10 minutes. It ends early only if you are knocked unconscious or if you choose to end it.", false, 0);
        saveFeature("Barbarian", null, 15, "Relentless Rage", "When you take damage that reduces you to 0 HP, you can drop to 1 HP instead. Once you use this ability, you can't use it again until you finish a short or long rest.", false, 0);
        saveFeature("Barbarian", null, 18, "Indomitable Might", "You ignore strength score maximums. If your rage would raise your Strength above 20, it can go as high as 30.", false, 0);
        saveFeature("Barbarian", null, 20, "Primal Champion", "Your STR and CON increase by 4. Your maximum for those scores is now 24.", false, 0);

        // Fighter features
        saveFeature("Fighter", null, 1, "Fighting Style", "You adopt a particular style of fight. Options: Archery, Defense, Dueling, Great Weapon Fighting, Protection, Two-Weapon Fighting.", false, 0);
        saveFeature("Fighter", null, 1, "Second Wind", "As a bonus action, you can recover hit points equal to 1d10 + your fighter level. Once you use this feature, you must finish a short or long rest to use it again.", true, 0);
        saveFeature("Fighter", null, 2, "Action Surge", "You can take one additional action on your turn. Once you use this feature, you must finish a short or long rest to use it again. At higher levels, you can use it twice before a rest.", true, 1);
        saveFeature("Fighter", null, 3, "Martial Archetype", "You choose an archetype, such as Champion, Battle Master, or Eldritch Knight.", false, 0);
        saveFeature("Fighter", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Fighter", null, 5, "Extra Attack", "You can attack twice, instead of once, whenever you take the Attack action on your turn.", false, 0);
        saveFeature("Fighter", null, 7, "Indomitable", "When you roll initiative and have no uses of Second Wind remaining, you can reroll the d20 and use the new roll. You can use this feature once per long rest.", false, 0);
        saveFeature("Fighter", null, 9, "Additional Fighting Style", "You can choose a second option from the Fighting Style feature.", false, 0);
        saveFeature("Fighter", null, 10, "Extra Attack (2)", "You can attack three times whenever you take the Attack action.", false, 0);
        saveFeature("Fighter", null, 11, "Second Wind (2)", "You can use Second Wind twice before a rest.", true, 2);
        saveFeature("Fighter", null, 14, "Action Surge (2)", "You can use Action Surge twice before a rest.", true, 2);
        saveFeature("Fighter", null, 15, "Indomitable (2)", "You can use Indomitable twice per long rest.", false, 0);
        saveFeature("Fighter", null, 18, "Extra Attack (3)", "You can attack four times whenever you take the Attack action.", false, 0);
        saveFeature("Fighter", null, 19, "Indomitable (3)", "You can use Indomitable three times per long rest.", false, 0);
        saveFeature("Fighter", null, 20, "Survivor", "At the start of each of your turns, you regain 1d10 hit points if you have no more than half of your hit points left. This healing doesn't occur if you have 0 hit points or are resting.", false, 0);

        // Rogue features
        saveFeature("Rogue", null, 1, "Expertise", "Choose two skill proficiencies. Your proficiency bonus is doubled for any ability check using the chosen proficiencies.", false, 0);
        saveFeature("Rogue", null, 1, "Sneak Attack", "Once per turn, you can deal an extra 1d6 damage to one creature when you have advantage on the attack roll or when another enemy is within 5 feet of the target. Damage increases as you gain levels.", false, 0);
        saveFeature("Rogue", null, 1, "Thieves' Cant", "You know thieves' cant, a secret mix of dialect, jargon, and code.", false, 0);
        saveFeature("Rogue", null, 2, "Cunning Action", "You can take a bonus action on each of your turns to take the Dash, Disengage, or Hide action.", false, 0);
        saveFeature("Rogue", null, 3, "Roguish Archetype", "You choose an archetype, such as Thief, Assassin, or Arcane Trickster.", false, 0);
        saveFeature("Rogue", null, 5, "Uncanny Dodge", "When an attacker that you can see hits you with an attack, you use your reaction to halve the damage.", false, 0);
        saveFeature("Rogue", null, 7, "Evasion", "When subjected to an effect that allows a DEX saving throw to take no damage, you take no damage on a success and half damage on a failure.", false, 0);
        saveFeature("Rogue", null, 10, "Reliable Talent", "When you have advantage on an ability check using your proficiency bonus, you can treat a d20 roll of 9 or lower on that check as a 10.", false, 0);
        saveFeature("Rogue", null, 11, "Blindsense", "If you are conscious and haven't been blinded, you can determine the location of any hidden creature within 10 feet of you.", false, 0);
        saveFeature("Rogue", null, 14, "Slippery Mind", "You have advantage on WIS saving throws against being charmed or frightened.", false, 0);
        saveFeature("Rogue", null, 15, "Elusive", "You are never at disadvantage when attacking a creature that has advantage on the attack roll against you.", false, 0);
        saveFeature("Rogue", null, 18, "Stroke of Luck", "When you roll initiative and have no uses of Cunning Action remaining, you can reroll the d20 and use the new roll. You can use this feature once per long rest.", false, 0);
        saveFeature("Rogue", null, 20, "Sneak Attack (Always)", "You always have advantage on attack rolls against creatures that are within 5 feet of a friendly creature.", false, 0);

        // Wizard features
        saveFeature("Wizard", null, 1, "Spellcasting", "You have learned spells that can be cast using the spellcasting rules. You prepare spells from your spellbook using INT + wizard level.", false, 0);
        saveFeature("Wizard", null, 2, "Arcane Recovery", "At the end of a rest, you can recover some spell slots. The combined level of the slots can't exceed half your wizard level (rounded up), and no slot can be higher than 3rd level.", true, 1);
        saveFeature("Wizard", null, 3, "Arcane Tradition", "You choose an arcane tradition, such as School of Evocation or School of Abjuration.", false, 0);
        saveFeature("Wizard", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Wizard", null, 6, "Spell Mastery", "Choose a 1st-level wizard spell and a 2nd-level wizard spell. You can cast them at their lowest level without expending a spell slot. You must finish a long rest to change spells.", false, 0);
        saveFeature("Wizard", null, 10, "Signature Spells", "Choose two 3rd-level wizard spells. You always have them prepared and can cast them once each at 3rd level without a spell slot. They reset on a short or long rest.", true, 2);
        saveFeature("Wizard", null, 14, "Spell Mastery (Higher)", "You can choose two 3rd-level spells and two 4th-level spells for Spell Mastery.", false, 0);
        saveFeature("Wizard", null, 18, "Signature Spells (Higher)", "Your Signature Spells are now 4th-level spells, castable at 5th level.", false, 0);
        saveFeature("Wizard", null, 20, "Spell Mastery (Ultimate)", "You can choose any number of wizard spells you know for Spell Mastery, and you can cast them at will without expending spell slots.", false, 0);

        // Cleric features
        saveFeature("Cleric", null, 1, "Spellcasting", "You can cast cleric spells using WIS as your spellcasting ability.", false, 0);
        saveFeature("Cleric", null, 1, "Divine Domain", "Choose a divine domain, such as Life, Light, or Tempest. It grants domain spells and features.", false, 0);
        saveFeature("Cleric", null, 2, "Channel Divinity", "You can channel divine energy to fuel magical effects. You can use this feature once per long rest.", false, 0);
        saveFeature("Cleric", null, 5, "Destroy Undead", "When an undead fails a saving throw against your cleric spell, the creature is instantly destroyed if its challenge rating is within your threshold.", false, 0);
        saveFeature("Cleric", null, 6, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Cleric", null, 8, "Divine Intervention", "You can call on your deity to intervene. Roll d100; on a roll equal to or less than your cleric level, your deity acts. Once used, you must finish a long rest.", false, 0);
        saveFeature("Cleric", null, 10, "Destroy Undead (Higher)", "You can destroy undead of higher challenge rating.", false, 0);
        saveFeature("Cleric", null, 17, "Destroy Undead (Higher)", "You can destroy undead of even higher challenge rating.", false, 0);
        saveFeature("Cleric", null, 18, "Divine Intervention (Improved)", "Your Divine Intervention threshold improves.", false, 0);
        saveFeature("Cleric", null, 20, "Divine Intervention (Automatic)", "Your deity automatically intervenes. Choose one effect: creatures fail saves against you, you regain HP and are healed, allies succeed on saves, or enemies fail saves against you.", false, 0);

        // Druid features
        saveFeature("Druid", null, 1, "Druidic", "You know Druidic, the secret language of druids.", false, 0);
        saveFeature("Druid", null, 1, "Spellcasting", "You can cast druid spells using WIS as your spellcasting ability.", false, 0);
        saveFeature("Druid", null, 2, "Wild Shape", "You can use your action to magically assume the shape of a beast. You can use this feature twice per long rest.", false, 0);
        saveFeature("Druid", null, 2, "Druid Circle", "Choose a druid circle, such as Circle of the Land or Circle of the Moon.", false, 0);
        saveFeature("Druid", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Druid", null, 6, "Timeless Body", "You age more slowly. You gain no physical frailty of old age.", false, 0);
        saveFeature("Druid", null, 8, "Untamed Wilds", "You can cast Wild Shape as a bonus action.", false, 0);
        saveFeature("Druid", null, 10, "Beast Sense", "You can see, hear, and smell anything within 1 mile that any beast can perceive.", false, 0);
        saveFeature("Druid", null, 14, "Commune with Nature", "You can spend 1 minute communing with nature to learn about terrain, creatures, and settlements within 3 miles.", false, 0);
        saveFeature("Druid", null, 18, "Treespeech", "You can communicate slowly with plants.", false, 0);

        // Paladin features
        saveFeature("Paladin", null, 1, "Divine Sense", "As an action, you can detect celestial, fiends, or undead within 60 feet. You can use this feature a number of times equal to 1 + your CHA modifier per long rest.", false, 0);
        saveFeature("Paladin", null, 1, "Lay on Hands", "You have a pool of healing power equal to your paladin level x 5. As an action, you can spend from the pool to restore HP. The pool recharges on a long rest.", false, 0);
        saveFeature("Paladin", null, 2, "Fighting Style", "You adopt a particular style of fight.", false, 0);
        saveFeature("Paladin", null, 2, "Spellcasting", "You can cast paladin spells using CHA as your spellcasting ability.", false, 0);
        saveFeature("Paladin", null, 3, "Divine Smite", "When you hit a creature with a melee weapon attack, you can expend a spell slot to deal radiant damage. Damage increases by spell slot level.", false, 0);
        saveFeature("Paladin", null, 3, "Oath", "You swear an oath, such as Oath of Devotion or Oath of Vengeance.", false, 0);
        saveFeature("Paladin", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Paladin", null, 5, "Extra Attack", "You can attack twice, instead of once, whenever you take the Attack action.", false, 0);
        saveFeature("Paladin", null, 6, "Aura of Protection", "You and friendly creatures within 10 feet gain your proficiency bonus to all saving throws.", false, 0);
        saveFeature("Paladin", null, 7, "Aura of Courage", "You and friendly creatures within 10 feet have advantage on WIS saving throws against being frightened.", false, 0);
        saveFeature("Paladin", null, 10, "Aura Range", "Your auras extend to 30 feet.", false, 0);
        saveFeature("Paladin", null, 11, "Improved Divine Smite", "Your melee weapon attacks deal extra radiant damage on a hit.", false, 0);
        saveFeature("Paladin", null, 14, "Aura of Life", "You and friendly creatures within 10 feet regain extra hit points when healed.", false, 0);
        saveFeature("Paladin", null, 18, "Aura of Glory", "You and friendly creatures within 10 feet gain temporary hit points when you use Action Surge or similar features.", false, 0);
        saveFeature("Paladin", null, 20, "Aura Improvement", "Your auras extend to 60 feet and affect you even when you're unconscious.", false, 0);

        // Ranger features
        saveFeature("Ranger", null, 1, "Favored Enemy", "You have advantage on wisdom (Survival) checks to track your favored enemies, and you can learn more about them.", false, 0);
        saveFeature("Ranger", null, 1, "Natural Explorer", "You are exceptionally skilled at navigating certain terrain types.", false, 0);
        saveFeature("Ranger", null, 2, "Fighting Style", "You adopt a particular style of fight.", false, 0);
        saveFeature("Ranger", null, 2, "Spellcasting", "You can cast ranger spells using WIS as your spellcasting ability.", false, 0);
        saveFeature("Ranger", null, 3, "Ranger Archetype", "You choose an archetype, such as Hunter or Beast Master.", false, 0);
        saveFeature("Ranger", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Ranger", null, 5, "Extra Attack", "You can attack twice, instead of once, whenever you take the Attack action.", false, 0);
        saveFeature("Ranger", null, 8, "Land's Stride", "You can move through nonmagical difficult terrain without penalty. You and your companions have advantage on checks to resist magic that hinder movement through such terrain.", false, 0);
        saveFeature("Ranger", null, 10, "Hide in Plain Sight", "You can cast hide as a bonus action.", false, 0);
        saveFeature("Ranger", null, 11, "Vanish", "You can cast hide as a bonus action.", false, 0);
        saveFeature("Ranger", null, 14, "Feral Senses", "You have truesight out to 30 feet.", false, 0);
        saveFeature("Ranger", null, 15, "Feral Senses", "You have truesight out to 30 feet.", false, 0);
        saveFeature("Ranger", null, 18, "Foe Slayer", "When you make an attack roll, you can add a d6 to the roll.", false, 0);

        // Sorcerer features
        saveFeature("Sorcerer", null, 1, "Spellcasting", "You can cast sorcerer spells using CHA as your spellcasting ability.", false, 0);
        saveFeature("Sorcerer", null, 1, "Sorcerous Origin", "Choose a sorcerous origin, such as Draconic Bloodline or Wild Magic.", false, 0);
        saveFeature("Sorcerer", null, 2, "Font of Magic", "You have a pool of sorcery points. You can expend them to create spell slots or convert spell slots to sorcery points.", false, 0);
        saveFeature("Sorcerer", null, 3, "Metamagic", "You can alter spells as you cast them. Options: Careful Spell, Empowered Spell, Extended Spell, Heightened Spell, Quickened Spell, Subtle Spell, Twinned Spell.", false, 0);
        saveFeature("Sorcerer", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Sorcerer", null, 6, "Sorcerous Restoration", "When you finish a short or long rest, you regain sorcery points equal to your CHA modifier.", true, 1);
        saveFeature("Sorcerer", null, 12, "Sorcerous Restoration (Improved)", "You regain sorcery points equal to your CHA modifier x 2.", true, 1);
        saveFeature("Sorcerer", null, 14, "Sorcerous Restoration (Greater)", "You regain sorcery points equal to your CHA modifier x 3.", true, 1);
        saveFeature("Sorcerer", null, 16, "Sorcerous Restoration (Supreme)", "You regain sorcery points equal to your CHA modifier x 4.", true, 1);
        saveFeature("Sorcerer", null, 18, "Careful Metamagic", "When you use Careful Spell, you can use a lower-level spell slot.", false, 0);
        saveFeature("Sorcerer", null, 20, "Superior Metamagic", "You can use two Metamagic options on the same spell.", false, 0);

        // Warlock features
        saveFeature("Warlock", null, 1, "Otherworldly Patron", "Choose an otherworldly patron, such as The Archfey, The Fiend, or The Hexblade.", false, 0);
        saveFeature("Warlock", null, 1, "Pact Magic", "You can cast warlock spells using CHA as your spellcasting ability. You always prepare all warlock spells you know.", false, 0);
        saveFeature("Warlock", null, 2, "Eldritch Invocations", "You learn eldritch invocations, short warlock spells that grant lasting or instant effects.", false, 0);
        saveFeature("Warlock", null, 3, "Pact Boon", "You gain a pact boon: Pact of the Chain, Pact of the Blade, Pact of the Tome, or Pact of the Talisman.", false, 0);
        saveFeature("Warlock", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Warlock", null, 5, "Mystic Arcanum", "You gain a 6th-level spell slot that can be used only for your warlock spells.", false, 0);
        saveFeature("Warlock", null, 6, "Eldritch Master", "You can learn two additional warlock cantrips.", false, 0);
        saveFeature("Warlock", null, 7, "Mystic Arcanum (7th)", "You gain a 7th-level spell slot for warlock spells.", false, 0);
        saveFeature("Warlock", null, 8, "Eldritch Invocations (More)", "You learn additional eldritch invocations.", false, 0);
        saveFeature("Warlock", null, 9, "Mystic Arcanum (8th)", "You gain an 8th-level spell slot for warlock spells.", false, 0);
        saveFeature("Warlock", null, 10, "Eldritch Invocations (More)", "You learn additional eldritch invocations.", false, 0);
        saveFeature("Warlock", null, 11, "Mystic Arcanum (9th)", "You gain a 9th-level spell slot for warlock spells.", false, 0);
        saveFeature("Warlock", null, 12, "Eldritch Invocations (More)", "You learn additional eldritch invocations.", false, 0);
        saveFeature("Warlock", null, 15, "Mystic Arcanum (Improved)", "Your 6th-level Mystic Arcanum slot becomes a 7th-level slot.", false, 0);
        saveFeature("Warlock", null, 17, "Mystic Arcanum (Improved)", "Your 7th-level Mystic Arcanum slot becomes an 8th-level slot.", false, 0);
        saveFeature("Warlock", null, 19, "Mystic Arcanum (Improved)", "Your 8th-level Mystic Arcanum slot becomes a 9th-level slot.", false, 0);
        saveFeature("Warlock", null, 20, "Eldritch Master (Improved)", "You learn one additional warlock cantrip.", false, 0);

        // Monk features
        saveFeature("Monk", null, 1, "Unarmored Defense", "While not wearing armor or using a shield, your AC = 10 + DEX modifier + WIS modifier.", false, 0);
        saveFeature("Monk", null, 1, "Martial Arts", "You can use DEX for melee unarmed attacks. Your unarmed strike deals 1d4 bludgeoning damage. You can roll a d4 when you use your bonus action to make an unarmed strike.", false, 0);
        saveFeature("Monk", null, 2, "Ki", "You can spend ki points to fuel various features. You regain ki points on a short or long rest.", true, 0);
        saveFeature("Monk", null, 2, "Unarmored Movement", "Your speed increases by 10 feet while not wearing armor or shields.", false, 0);
        saveFeature("Monk", null, 3, "Monk Archetype", "Choose a monk archetype, such as Way of Open Hand or Way of Shadow.", false, 0);
        saveFeature("Monk", null, 3, "Deflect Missiles", "As a reaction, you can deflect a ranged weapon attack. You take no damage and can catch the projectile.", false, 0);
        saveFeature("Monk", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Monk", null, 5, "Slow Fall", "When you fall, you can use your reaction to reduce your falling damage by 5 per monk level.", false, 0);
        saveFeature("Monk", null, 6, "Stunning Strike", "When you hit a creature with a melee weapon attack, you can spend 1 ki point to attempt to stun it.", false, 0);
        saveFeature("Monk", null, 7, "Evasion", "When subjected to an effect that allows a DEX saving throw, you take no damage on a success and half on a failure.", false, 0);
        saveFeature("Monk", null, 9, "Stillness of Mind", "As an action, you can end one effect on yourself that is causing you to be charmed or frightened.", false, 0);
        saveFeature("Monk", null, 11, "Purity of Body", "You are immune to disease and poison.", false, 0);
        saveFeature("Monk", null, 13, "Tongue of Sun and Moon", "You can understand and communicate with any intelligent creature.", false, 0);
        saveFeature("Monk", null, 14, "Diamond Soul", "You have advantage on saving throws against spells and magical effects. You gain resistance to damage from spells.", false, 0);
        saveFeature("Monk", null, 15, "Timeless Body", "You age more slowly and no longer need food, water, or air to survive.", false, 0);
        saveFeature("Monk", null, 18, "Empty Body", "You can cast pass without trace and invisibility. You can also assume the form of a creature.", false, 0);
        saveFeature("Monk", null, 20, "Perfect Self", "When you roll to initiative, you can enter a state of perfect awareness. You gain blindsight 30 feet, can use your reaction to impose disadvantage on attack rolls against you, and can make an unarmed strike as a reaction.", false, 0);

        // Bard features
        saveFeature("Bard", null, 1, "Spellcasting", "You can cast bard spells using CHA as your spellcasting ability.", false, 0);
        saveFeature("Bard", null, 1, "Bardic Inspiration", "As a bonus action, you can inspire a creature. It gains a d6 to add to one ability check, attack roll, or saving throw. You can use this a number of times equal to your CHA modifier per long rest.", false, 0);
        saveFeature("Bard", null, 2, "Jack of All Trades", "You can add half your proficiency bonus (rounded down) to any ability check you don't already add your proficiency bonus to.", false, 0);
        saveFeature("Bard", null, 3, "Bard College", "Choose a bard college, such as College of Lore or College of Valor.", false, 0);
        saveFeature("Bard", null, 3, "Song of Rest", "When you use a bonus action to inspire a creature with Bardic Inspiration, you can also restore hit points to it.", false, 0);
        saveFeature("Bard", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Bard", null, 5, "Expertise", "Choose two skill proficiencies from your bard proficiencies. Your proficiency bonus is doubled for any ability check using the chosen proficiencies.", false, 0);
        saveFeature("Bard", null, 6, "Countercharm", "You can use your action to create an anti-magic field. Creatures within 10 feet have advantage on WIS saving throws against being charmed or frightened.", false, 0);
        saveFeature("Bard", null, 10, "Magical Secrets", "You learn two spells from any class. They count as bard spells for you.", false, 0);
        saveFeature("Bard", null, 14, "Magical Secrets (2)", "You learn two more spells from any class.", false, 0);
        saveFeature("Bard", null, 15, "Magical Secrets (3)", "You learn two more spells from any class.", false, 0);
        saveFeature("Bard", null, 18, "Magical Secrets (4)", "You learn two more spells from any class.", false, 0);
        saveFeature("Bard", null, 20, "Superior Inspiration", "When you roll initiative and have no uses of Bardic Inspiration remaining, you can reroll the d20 and use the new roll. You can use this feature once per long rest.", false, 0);

        // Artificer features
        saveFeature("Artificer", null, 1, "Tool Proficiency", "You gain proficiency with one type of artisan's tools.", false, 0);
        saveFeature("Artificer", null, 1, "Spellcasting", "You can cast artificer spells using INT as your spellcasting ability.", false, 0);
        saveFeature("Artificer", null, 2, "Infuse Item", "You can imbue an item with magic. You can infuse up to two items at a time, increasing as you level.", false, 0);
        saveFeature("Artificer", null, 3, "Artificer Specialty", "Choose an artificer specialty, such as Alchemist, Armorer, Artillerist, or Battle Smith.", false, 0);
        saveFeature("Artificer", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Artificer", null, 5, "Tool Expertise", "Choose two tools you have proficiency with. Your proficiency bonus is doubled for ability checks using them.", false, 0);
        saveFeature("Artificer", null, 6, "Flash of Genius", "When a creature makes an attack roll or ability check, you can use your reaction to add a d4 to the roll.", false, 0);
        saveFeature("Artificer", null, 7, "Staff of Defense", "When you hold a staff, you can add your proficiency bonus to your AC.", false, 0);
        saveFeature("Artificer", null, 8, "Magic Item Creation", "You can create magic items.", false, 0);
        saveFeature("Artificer", null, 9, "Extended Infusion", "You can infuse up to four items at a time.", false, 0);
        saveFeature("Artificer", null, 10, "Effectual Infusions", "Your infusions are more effective.", false, 0);
        saveFeature("Artificer", null, 11, "Soul of Artifice", "You can cast animate objects more effectively.", false, 0);
        saveFeature("Artificer", null, 12, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Artificer", null, 14, "Magical Inspiration", "You can use your action to create a magical effect.", false, 0);
        saveFeature("Artificer", null, 16, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Artificer", null, 18, "Master of Invention", "You can infuse up to six items at a time.", false, 0);
        saveFeature("Artificer", null, 20, "Artificer's Apex", "You gain powerful abilities based on your specialty.", false, 0);

        // Blood Hunter features
        saveFeature("Blood Hunter", null, 1, "Order of the Ghost", "You belong to the Order of the Ghost, using spectral magic.", false, 0);
        saveFeature("Blood Hunter", null, 1, "Hemocraft", "You can spend hit points to fuel hemocraft techniques.", false, 0);
        saveFeature("Blood Hunter", null, 1, "Mutagen", "You imbue yourself with a mutagen that grants you resistance to one damage type.", false, 0);
        saveFeature("Blood Hunter", null, 2, "Blood Malediction", "You can curse a creature, dealing extra necrotic damage and preventing it from regenerating HP.", false, 0);
        saveFeature("Blood Hunter", null, 3, "Haunting", "You can spend hit points to create a spectral hand that can grapple or push creatures.", false, 0);
        saveFeature("Blood Hunter", null, 4, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Blood Hunter", null, 5, "Extra Attack", "You can attack twice, instead of once, whenever you take the Attack action.", false, 0);
        saveFeature("Blood Hunter", null, 6, "Deathless", "You gain resistance to necrotic and radiant damage.", false, 0);
        saveFeature("Blood Hunter", null, 7, "Spectral Hand Improvement", "Your spectral hand is more powerful.", false, 0);
        saveFeature("Blood Hunter", null, 8, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Blood Hunter", null, 10, "Blood Feud", "You can curse multiple creatures.", false, 0);
        saveFeature("Blood Hunter", null, 11, "Haunting Improvement", "Your haunting is more powerful.", false, 0);
        saveFeature("Blood Hunter", null, 12, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Blood Hunter", null, 14, "Deathless Improvement", "Your resistance to necrotic and radiant damage improves.", false, 0);
        saveFeature("Blood Hunter", null, 15, "Spectral Hand Mastery", "Your spectral hand can now attack.", false, 0);
        saveFeature("Blood Hunter", null, 16, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Blood Hunter", null, 18, "Blood Hunter's Prowess", "You gain powerful hemocraft abilities.", false, 0);
        saveFeature("Blood Hunter", null, 19, "Ability Score Improvement", "Increase one ability score by 2, or two by 1. You can instead take a feat.", false, 0);
        saveFeature("Blood Hunter", null, 20, "Order's End", "You gain the ultimate power of your order.", false, 0);
    }

    private Dnd5eClassFeature saveFeature(String className, String subclass, int level, String name, String description, boolean shortRestReset, int usesPerLongRest) {
        Dnd5eClassFeature feature = new Dnd5eClassFeature();
        feature.setClassName(className);
        feature.setSubclass(subclass);
        feature.setLevel(level);
        feature.setName(name);
        feature.setDescription(description);
        feature.setShortRestReset(shortRestReset);
        feature.setUsesPerLongRest(usesPerLongRest);
        feature.setActive(true);
        return classFeatureRepository.save(feature);
    }
}
