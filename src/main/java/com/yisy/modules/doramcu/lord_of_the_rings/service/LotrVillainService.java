package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillain;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillainPower;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrVillainPowerRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrVillainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Villains.
 * <p>
 * Tracks the major antagonists of the Lord of the Rings galaxy, including
 * Sauron, Saruman, the Witch-king, and other dark lords. Villains have
 * threat levels, corruption, and special powers that drive the narrative.
 * </p>
 * <p>
 * Key mechanics:
 * - Full CRUD for villains with race, title, level, and abilities
 * - Threat level tracking (1-10) for narrative tension
 * - Corruption tracking (0-10) for dark influence
 * - Villain powers with attack bonuses, damage dice, and save DCs
 * - Damage tracking for villain encounters
 * </p>
 */
@Service
public class LotrVillainService {

    private static final Logger log = LoggerFactory.getLogger(LotrVillainService.class);

    private final LotrVillainRepository villainRepository;
    private final LotrVillainPowerRepository villainPowerRepository;

    public LotrVillainService(LotrVillainRepository villainRepository,
                               LotrVillainPowerRepository villainPowerRepository) {
        this.villainRepository = villainRepository;
        this.villainPowerRepository = villainPowerRepository;
    }

    // --- Villain CRUD ---

    /**
     * Get all villains.
     */
    @Transactional(readOnly = true)
    public List<LotrVillain> findAll() {
        return villainRepository.findAll();
    }

    /**
     * Get all active villains.
     */
    @Transactional(readOnly = true)
    public List<LotrVillain> findActiveVillains() {
        return villainRepository.findByIsActiveTrue();
    }

    /**
     * Get villain by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrVillain> findById(Long id) {
        return villainRepository.findById(id);
    }

    /**
     * Find villains by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrVillain> findByName(String name) {
        return villainRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Find villains by faction.
     */
    @Transactional(readOnly = true)
    public List<LotrVillain> findByFaction(String faction) {
        return villainRepository.findByFaction(faction);
    }

    /**
     * Create a new villain.
     */
    @Transactional
    public LotrVillain createVillain(String name, String race, String title, int level,
                                      String faction, String realm, String description,
                                      int threatLevel, int corruption) {
        LotrVillain villain = new LotrVillain();
        villain.setName(name);
        villain.setRace(race);
        villain.setTitle(title);
        villain.setLevel(level);
        villain.setFaction(faction);
        villain.setRealm(realm);
        villain.setDescription(description);
        villain.setThreatLevel(threatLevel);
        villain.setCorruption(corruption);
        villain.setActive(true);

        // Set ability scores based on race
        setRaceAbilityScores(villain, race);

        // Set HP and AC based on level
        int conMod = (villain.getConScore() - 10) / 2;
        int maxHp = level * 12 + (conMod * level);
        villain.setMaxHitPoints(maxHp);
        villain.setHitPoints(maxHp);
        villain.setArmorClass(10 + level / 2);

        LotrVillain saved = villainRepository.save(villain);
        log.info("Created villain: {} ({}, threat level {})", saved.getName(), saved.getTitle(), saved.getThreatLevel());
        return saved;
    }

    /**
     * Update an existing villain.
     */
    @Transactional
    public LotrVillain updateVillain(Long id, String name, String race, String title, int level,
                                      String faction, String realm, String description,
                                      int threatLevel, int corruption) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        villain.setName(name);
        villain.setRace(race);
        villain.setTitle(title);
        villain.setLevel(level);
        villain.setFaction(faction);
        villain.setRealm(realm);
        villain.setDescription(description);
        villain.setThreatLevel(threatLevel);
        villain.setCorruption(corruption);

        LotrVillain saved = villainRepository.save(villain);
        log.info("Updated villain: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a villain.
     */
    @Transactional
    public void deleteVillain(Long id) {
        if (!villainRepository.existsById(id)) {
            throw new IllegalArgumentException("Villain not found: " + id);
        }
        villainRepository.deleteById(id);
        log.info("Deleted villain: {}", id);
    }

    /**
     * Deactivate a villain (defeated but not deleted).
     */
    @Transactional
    public String deactivateVillain(Long id) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        villain.setActive(false);
        villainRepository.save(villain);

        return String.format("%s has been defeated and is no longer active.", villain.getName());
    }

    /**
     * Reactivate a villain.
     */
    @Transactional
    public String reactivateVillain(Long id) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        villain.setActive(true);
        villain.setHitPoints(villain.getMaxHitPoints());
        villainRepository.save(villain);

        return String.format("%s has returned! The darkness grows.", villain.getName());
    }

    // --- Villain Combat ---

    /**
     * Damage a villain in combat.
     */
    @Transactional
    public String damageVillain(Long id, int damage) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        int currentHp = villain.getHitPoints();
        int newHp = Math.max(0, currentHp - damage);
        villain.setHitPoints(newHp);
        villainRepository.save(villain);

        if (newHp <= 0) {
            return String.format("%s has been defeated! The light prevails.", villain.getName());
        }

        int hpPercent = (newHp * 100) / villain.getMaxHitPoints();
        if (hpPercent <= 25) {
            return String.format("%s is critically wounded! HP: %d/%d. The villain fights desperately.",
                    villain.getName(), newHp, villain.getMaxHitPoints());
        } else if (hpPercent <= 50) {
            return String.format("%s is badly wounded. HP: %d/%d. Dark power surges.",
                    villain.getName(), newHp, villain.getMaxHitPoints());
        }
        return String.format("%s takes %d damage. HP: %d/%d", villain.getName(), damage, newHp, villain.getMaxHitPoints());
    }

    /**
     * Heal a villain.
     */
    @Transactional
    public String healVillain(Long id, int healing) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        int currentHp = villain.getHitPoints();
        int newHp = Math.min(villain.getMaxHitPoints(), currentHp + healing);
        villain.setHitPoints(newHp);
        villainRepository.save(villain);

        return String.format("%s is darkly healed. HP: %d/%d", villain.getName(), newHp, villain.getMaxHitPoints());
    }

    /**
     * Increase villain's corruption.
     */
    @Transactional
    public String increaseCorruption(Long id, int amount) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        int newCorruption = Math.min(10, villain.getCorruption() + amount);
        villain.setCorruption(newCorruption);
        villainRepository.save(villain);

        if (newCorruption >= 10) {
            return String.format("%s is fully consumed by darkness!", villain.getName());
        }
        return String.format("%s's corruption deepens to %d/10.", villain.getName(), newCorruption);
    }

    /**
     * Increase villain's threat level.
     */
    @Transactional
    public String increaseThreatLevel(Long id, int amount) {
        LotrVillain villain = villainRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + id));

        int newThreat = Math.min(10, villain.getThreatLevel() + amount);
        villain.setThreatLevel(newThreat);
        villainRepository.save(villain);

        if (newThreat >= 10) {
            return String.format("%s's threat is at maximum! All of Middle-earth trembles.", villain.getName());
        }
        return String.format("%s's threat level rises to %d/10.", villain.getName(), newThreat);
    }

    // --- Villain Powers ---

    /**
     * Get all powers for a villain.
     */
    @Transactional(readOnly = true)
    public List<LotrVillainPower> getVillainPowers(Long villainId) {
        return villainPowerRepository.findAll().stream()
                .filter(p -> p.getVillainId().equals(villainId) && p.isActive())
                .toList();
    }

    /**
     * Create a villain power.
     */
    @Transactional
    public LotrVillainPower createVillainPower(Long villainId, String name, String type,
                                                String description, int attackBonus,
                                                String damageDice, String damageType,
                                                String saveAbility, int saveDc) {
        LotrVillain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new IllegalArgumentException("Villain not found: " + villainId));

        LotrVillainPower power = new LotrVillainPower();
        power.setVillainId(villainId);
        power.setName(name);
        power.setType(type);
        power.setDescription(description);
        power.setAttackBonus(attackBonus);
        power.setDamageDice(damageDice);
        power.setDamageType(damageType);
        power.setSaveAbility(saveAbility);
        power.setSaveDc(saveDc);
        power.setActive(true);

        LotrVillainPower saved = villainPowerRepository.save(power);
        log.info("Created villain power: {} for {}", saved.getName(), villain.getName());
        return saved;
    }

    /**
     * Update a villain power.
     */
    @Transactional
    public LotrVillainPower updateVillainPower(Long powerId, String name, String type,
                                                String description, int attackBonus,
                                                String damageDice, String damageType,
                                                String saveAbility, int saveDc) {
        LotrVillainPower power = villainPowerRepository.findById(powerId)
                .orElseThrow(() -> new IllegalArgumentException("Villain power not found: " + powerId));

        power.setName(name);
        power.setType(type);
        power.setDescription(description);
        power.setAttackBonus(attackBonus);
        power.setDamageDice(damageDice);
        power.setDamageType(damageType);
        power.setSaveAbility(saveAbility);
        power.setSaveDc(saveDc);

        LotrVillainPower saved = villainPowerRepository.save(power);
        log.info("Updated villain power: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a villain power.
     */
    @Transactional
    public void deleteVillainPower(Long powerId) {
        if (!villainPowerRepository.existsById(powerId)) {
            throw new IllegalArgumentException("Villain power not found: " + powerId);
        }
        villainPowerRepository.deleteById(powerId);
        log.info("Deleted villain power: {}", powerId);
    }

    /**
     * Deactivate a villain power.
     */
    @Transactional
    public void deactivateVillainPower(Long powerId) {
        LotrVillainPower power = villainPowerRepository.findById(powerId)
                .orElseThrow(() -> new IllegalArgumentException("Villain power not found: " + powerId));
        power.setActive(false);
        villainPowerRepository.save(power);
        log.info("Deactivated villain power: {}", power.getName());
    }

    /**
     * Initialize the villain database with LOTR-themed antagonists.
     */
    @Transactional
    public void initializeVillainDatabase() {
        if (!villainRepository.findAll().isEmpty()) {
            log.info("Villain database already initialized, skipping.");
            return;
        }

        // Sauron
        LotrVillain sauron = createVillain("Sauron", "Maia", "Dark Lord", 20,
                "Mordor", "Mordor", "The Dark Lord of Mordor, creator of the One Ring.", 10, 10);
        createVillainPower(sauron.getId(), "Eye of Sauron", "Spell",
                "The all-seeing Eye scans for enemies. Target must succeed on a DC 20 Wis save or be frightened.",
                0, null, null, "Wis", 20);
        createVillainPower(sauron.getId(), "Flame of Udûn", "Spell",
                "Dark fire erupts from the ground. 3d10 fire damage, DC 18 Dex save for half.",
                0, "3d10", "fire", "Dex", 18);
        createVillainPower(sauron.getId(), "Dominate Will", "Spell",
                "Attempt to control a target's mind. DC 20 Cha save or charmed for 1 minute.",
                0, null, null, "Cha", 20);

        // Saruman
        LotrVillain saruman = createVillain("Saruman", "Maia", "White Wizard", 18,
                "Isengard", "Isengard", "Once chief of the Istari, now corrupted by the One Ring.", 8, 8);
        createVillainPower(saruman.getId(), "Staff Strike", "Weapon",
                "Saruman strikes with his enchanted staff. +12 to hit, 2d8+4 force damage.",
                12, "2d8+4", "force", "Dex", 17);
        createVillainPower(saruman.getId(), "Voice of Command", "Spell",
                "Saruman's voice compels obedience. DC 17 Cha save or paralyzed for 1 round.",
                0, null, null, "Cha", 17);
        createVillainPower(saruman.getId(), "Lightning Bolt", "Spell",
                "A bolt of lightning strikes. 4d6 lightning damage, DC 17 Dex save for half.",
                0, "4d6", "lightning", "Dex", 17);

        // Witch-king
        LotrVillain witchKing = createVillain("Witch-king", "Maia", "Lord of the Nazgûl", 15,
                "Nazgûl", "Mordor", "Captain of Sauron's servants, the Dead King of Angmar.", 9, 9);
        createVillainPower(witchKing.getId(), "Black Blade", "Weapon",
                "The Witch-king's cursed blade. +10 to hit, 2d10+3 necrotic damage.",
                10, "2d10+3", "necrotic", "Dex", 16);
        createVillainPower(witchKing.getId(), "Fear Aura", "Spell",
                "Terrifying aura radiates. DC 16 Wis save or frightened for 1 minute.",
                0, null, null, "Wis", 16);
        createVillainPower(witchKing.getId(), "Morgul Knife", "Weapon",
                "Poisoned blade that turns the wounded into wraiths. +9 to hit, 1d8+2 piercing + poison.",
                9, "1d8+2", "poison", "Con", 15);

        // Wormtongue
        LotrVillain wormtongue = createVillain("Wormtongue", "Man", "Traitor", 6,
                "Isengard", "Rohan", "Gríma Wormtongue, corrupt advisor to Théoden.", 4, 6);
        createVillainPower(wormtongue.getId(), "Whispered Lies", "Spell",
                "Poison the target's mind with doubt. DC 13 Cha save or disadvantaged on next check.",
                0, null, null, "Cha", 13);
        createVillainPower(wormtongue.getId(), "Poison Dagger", "Weapon",
                "Sneaky attack with poisoned blade. +7 to hit, 1d4+2 piercing + 1d4 poison.",
                7, "1d4+2", "poison", "Dex", 13);

        // Gothmog
        LotrVillain gothmog = createVillain("Gothmog", "Orc", "Lord of Balrogs", 12,
                "Mordor", "Moria", "Lord of the Balrogs, commander of Mordor's forces.", 7, 7);
        createVillainPower(gothmog.getId(), "Orc War Axe", "Weapon",
                "Massive axe swing. +8 to hit, 2d8+3 slashing damage.",
                8, "2d8+3", "slashing", "Dex", 15);
        createVillainPower(gothmog.getId(), "Command Minions", "Spell",
                "Summon 2d4 Orc Grunts to aid in combat.",
                0, null, null, null, 0);

        log.info("Initialized villain database with {} LOTR villains.", villainRepository.count());
    }

    // --- Helper Methods ---

    private void setRaceAbilityScores(LotrVillain villain, String race) {
        switch (race) {
            case "Maia":
                villain.setIntScore(18);
                villain.setWisScore(16);
                villain.setChaScore(18);
                break;
            case "Man":
                villain.setStrScore(14);
                villain.setChaScore(16);
                break;
            case "Orc":
                villain.setStrScore(18);
                villain.setConScore(16);
                break;
            case "Elf":
                villain.setDexScore(16);
                villain.setIntScore(16);
                villain.setChaScore(14);
                break;
            default:
                break;
        }
    }
}
