package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCalling;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCallingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Callings.
 * <p>
 * Callings define a character's role and abilities in the Lord of the Rings RPG.
 * Based on the LOTR CODA RPG system, callings include Scout, Warrior, Ranger,
 * Bard, Mage, and Healer. Each calling has a hit die, primary ability, and
 * special powers.
 * </p>
 * <p>
 * Key mechanics:
 * - Full CRUD for callings
 * - Hit die determines HP per level
 * - Primary ability for skill checks
 * - Armor and weapon proficiencies
 * - Power descriptions for special abilities
 * </p>
 */
@Service
public class LotrCallingService {

    private static final Logger log = LoggerFactory.getLogger(LotrCallingService.class);

    private final LotrCallingRepository callingRepository;

    public LotrCallingService(LotrCallingRepository callingRepository) {
        this.callingRepository = callingRepository;
    }

    /**
     * Get all callings.
     */
    @Transactional(readOnly = true)
    public List<LotrCalling> findAll() {
        return callingRepository.findAll();
    }

    /**
     * Get calling by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrCalling> findById(Long id) {
        return callingRepository.findById(id);
    }

    /**
     * Get calling by name.
     */
    @Transactional(readOnly = true)
    public Optional<LotrCalling> findByName(String name) {
        return callingRepository.findByName(name);
    }

    /**
     * Create a new calling.
     */
    @Transactional
    public LotrCalling createCalling(String name, String description, String hitDie,
                                      String primaryAbility, String armorProficiency,
                                      String weaponProficiency, String skillSlots1,
                                      String powerDescription) {
        LotrCalling calling = new LotrCalling();
        calling.setName(name);
        calling.setDescription(description);
        calling.setHitDie(hitDie);
        calling.setPrimaryAbility(primaryAbility);
        calling.setArmorProficiency(armorProficiency);
        calling.setWeaponProficiency(weaponProficiency);
        calling.setSkillSlots1(skillSlots1);
        calling.setPowerDescription(powerDescription);

        LotrCalling saved = callingRepository.save(calling);
        log.info("Created calling: {} ({})", saved.getName(), saved.getHitDie());
        return saved;
    }

    /**
     * Update an existing calling.
     */
    @Transactional
    public LotrCalling updateCalling(Long id, String name, String description, String hitDie,
                                      String primaryAbility, String armorProficiency,
                                      String weaponProficiency, String skillSlots1,
                                      String powerDescription) {
        LotrCalling calling = callingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Calling not found: " + id));

        calling.setName(name);
        calling.setDescription(description);
        calling.setHitDie(hitDie);
        calling.setPrimaryAbility(primaryAbility);
        calling.setArmorProficiency(armorProficiency);
        calling.setWeaponProficiency(weaponProficiency);
        calling.setSkillSlots1(skillSlots1);
        calling.setPowerDescription(powerDescription);

        LotrCalling saved = callingRepository.save(calling);
        log.info("Updated calling: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a calling.
     */
    @Transactional
    public void deleteCalling(Long id) {
        if (!callingRepository.existsById(id)) {
            throw new IllegalArgumentException("Calling not found: " + id);
        }
        callingRepository.deleteById(id);
        log.info("Deleted calling: {}", id);
    }

    /**
     * Get HP for a calling at a given level.
     */
    public int getHpForLevel(String callingName, int level) {
        LotrCalling calling = callingRepository.findByName(callingName).orElse(null);
        if (calling == null) {
            return level * 8; // Default d8
        }

        int hitDie = parseHitDie(calling.getHitDie());
        return level * hitDie;
    }

    /**
     * Get the primary ability modifier for a calling.
     */
    public String getPrimaryAbility(String callingName) {
        LotrCalling calling = callingRepository.findByName(callingName).orElse(null);
        return calling != null ? calling.getPrimaryAbility() : "Str";
    }

    /**
     * Initialize the calling database with LOTR-themed callings.
     */
    @Transactional
    public void initializeCallingDatabase() {
        if (!callingRepository.findAll().isEmpty()) {
            log.info("Calling database already initialized, skipping.");
            return;
        }

        // Scout
        createCalling("Scout",
                "Swift and stealthy, scouts excel at exploration and survival. They move silently through the wilds of Middle-earth.",
                "d6", "Dex", "Light armor", "Simple weapons, bows",
                "Athletics, Perception, Stealth, Survival",
                "Swift Stride: Move through difficult terrain without penalty. " +
                "Eagle Eye: Advantage on Perception checks. " +
                "Ambush: +2 to attack rolls when attacking from hiding.");

        // Warrior
        createCalling("Warrior",
                "Trained in combat, warriors are the backbone of any army. From Rohirrim cavalry to Gondorian soldiers.",
                "d10", "Str", "All armor, shields", "All weapons",
                "Athletics, Intimidation, Perception",
                "Second Wind: Once per rest, regain 1d10+level hit points. " +
                "Weapon Mastery: +1 to attack and damage with favored weapon. " +
                "Shield Bearer: +1 AC when using a shield.");

        // Ranger
        createCalling("Ranger",
                "Rangers of the North and Ithilien combine combat prowess with tracking and survival skills.",
                "d8", "Dex", "Light and medium armor", "All weapons, bows",
                "Athletics, Perception, Stealth, Survival, Tracking",
                "Favored Enemy: +2 to attack rolls and damage against a chosen enemy type. " +
                "Natural Hunter: Advantage on Survival checks in natural environments. " +
                "Tracker: Can follow trails even in difficult conditions.");

        // Bard
        createCalling("Bard",
                "Inspiring performers who use song and story to bolster allies and confound enemies. Like the bards of Rivendell.",
                "d8", "Cha", "Light armor", "Simple weapons, musical instruments",
                "Performance, Persuasion, Deception, Lore",
                "Inspiring Song: Allies within 30 feet gain +1 to attack rolls. " +
                "Countersong: Can interrupt enemy spells with music. " +
                "Jack of All Trades: Proficiency bonus divided by 2 to any skill check without proficiency.");

        // Mage
        createCalling("Mage",
                "Wielders of arcane power, from the wizards of the West to the sorcerers of Rhûn.",
                "d6", "Int", "None", "Wands, staves, daggers",
                "Arcana, History, Investigation, Religion",
                "Spellcasting: Cast spells using Intelligence. " +
                "Arcane Recovery: Once per rest, recover spell slots. " +
                "Spell Sniper: +1 to spell attack rolls, double spell range.");

        // Healer
        createCalling("Healer",
                "Skilled in medicine and divine healing, healers keep allies alive in the darkest times.",
                "d8", "Wis", "Light armor", "Maces, staffs, healing tools",
                "Medicine, Insight, Religion, Persuasion",
                "Healing Hands: Use Wisdom to heal allies as an action. " +
                "Divine Intervention: Once per long rest, call for divine aid. " +
                "Blessing: Allies within 30 feet have advantage on saving throws.");

        log.info("Initialized calling database with {} LOTR callings.", callingRepository.count());
    }

    // --- Helper Methods ---

    private int parseHitDie(String hitDie) {
        if (hitDie == null) return 8;
        try {
            return Integer.parseInt(hitDie.replace("d", ""));
        } catch (NumberFormatException e) {
            return 8;
        }
    }
}
