package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrFeat;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrFeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Feats.
 * <p>
 * Feats provide special abilities and bonuses. LOTR feats include Ring Resistance,
 * Ent Speech, Elven Sight, and other unique powers.
 * </p>
 */
@Service
public class LotrFeatService {

    private static final Logger log = LoggerFactory.getLogger(LotrFeatService.class);

    private final LotrFeatRepository featRepository;

    public LotrFeatService(LotrFeatRepository featRepository) {
        this.featRepository = featRepository;
    }

    /**
     * Get all feats.
     */
    @Transactional(readOnly = true)
    public List<LotrFeat> findAll() {
        return featRepository.findAll();
    }

    /**
     * Get all active feats.
     */
    @Transactional(readOnly = true)
    public List<LotrFeat> findActiveFeats() {
        return featRepository.findAll().stream()
                .filter(LotrFeat::isActive)
                .toList();
    }

    /**
     * Get feat by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrFeat> findById(Long id) {
        return featRepository.findById(id);
    }

    /**
     * Find feats by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrFeat> findByName(String name) {
        return featRepository.findAll().stream()
                .filter(f -> f.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Create a new feat.
     */
    @Transactional
    public LotrFeat createFeat(String name, String description, String prerequisite, String abilityIncrease) {
        LotrFeat feat = new LotrFeat();
        feat.setName(name);
        feat.setDescription(description);
        feat.setPrerequisite(prerequisite);
        feat.setAbilityIncrease(abilityIncrease);
        feat.setActive(true);

        LotrFeat saved = featRepository.save(feat);
        log.info("Created feat: {}", saved.getName());
        return saved;
    }

    /**
     * Update an existing feat.
     */
    @Transactional
    public LotrFeat updateFeat(Long id, String name, String description, String prerequisite, String abilityIncrease) {
        LotrFeat feat = featRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feat not found: " + id));

        feat.setName(name);
        feat.setDescription(description);
        feat.setPrerequisite(prerequisite);
        feat.setAbilityIncrease(abilityIncrease);

        LotrFeat saved = featRepository.save(feat);
        log.info("Updated feat: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a feat.
     */
    @Transactional
    public void deleteFeat(Long id) {
        if (!featRepository.existsById(id)) {
            throw new IllegalArgumentException("Feat not found: " + id);
        }
        featRepository.deleteById(id);
        log.info("Deleted feat: {}", id);
    }

    /**
     * Initialize the feat database with LOTR-themed feats.
     */
    @Transactional
    public void initializeFeatDatabase() {
        if (!featRepository.findAll().isEmpty()) {
            log.info("Feat database already initialized, skipping.");
            return;
        }

        createFeat("Ring Resistance",
                "You have resisted the pull of the Ring. Advantage on saves against corruption and charm effects.",
                "None", "None");

        createFeat("Ent Speech",
                "You can communicate with Ents. They regard you with patience, though slowly.",
                "Elf or Maia race", "None");

        createFeat("Elven Sight",
                "Your eyes see beyond the normal. Advantage on Perception checks and darkvision out to 60 feet.",
                "Elf race", "None");

        createFeat("Dwarven Toughness",
                "You are hardy as stone. +1 maximum hit points.",
                "Dwarf race", "None");

        createFeat("Hobbit Luck",
                "You are unusually lucky. Once per long rest, reroll any d20 roll.",
                "Hobbit race", "None");

        createFeat("Mark of the Ring",
                "The Ring has marked you. You can turn invisible as a bonus action, but gain 1 corruption.",
                "Corruption >= 3", "None");

        createFeat("Warden of the West",
                "You are a protector of the Free Peoples. +2 to AC against enemies of Mordor.",
                "Free Peoples Alliance faction", "None");

        createFeat("Master Archer",
                "Your bow never misses. +2 to attack rolls with ranged weapons.",
                "Ranger or Scout calling", "+1 Dex");

        createFeat("Wizard's Ward",
                "You can cast Shield once per long rest without expending a spell slot.",
                "Mage calling", "+1 Int");

        createFeat("Shield of the Valar",
                "Divine protection guards you. Once per rest, gain resistance to all damage for 1 round.",
                "Healer calling", "+1 Wis");

        createFeat("Beast Speaker",
                "You can communicate with animals and beasts. Advantage on Animal Handling checks.",
                "None", "None");

        createFeat("Shadow Walker",
                "You move through darkness unseen. Advantage on Stealth checks in dim light or darkness.",
                "None", "+1 Dex");

        log.info("Initialized feat database with {} LOTR feats.", featRepository.count());
    }
}
