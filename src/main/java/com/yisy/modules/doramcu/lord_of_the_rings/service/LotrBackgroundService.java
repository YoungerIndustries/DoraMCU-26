package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBackground;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrBackgroundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Backgrounds.
 * <p>
 * Backgrounds provide skill proficiencies, tool proficiencies, languages,
 * and lore-based traits. Examples include Ranger of the North, Steward of Gondor,
 * and Hobbit of the Shire.
 * </p>
 */
@Service
public class LotrBackgroundService {

    private static final Logger log = LoggerFactory.getLogger(LotrBackgroundService.class);

    private final LotrBackgroundRepository backgroundRepository;

    public LotrBackgroundService(LotrBackgroundRepository backgroundRepository) {
        this.backgroundRepository = backgroundRepository;
    }

    /**
     * Get all backgrounds.
     */
    @Transactional(readOnly = true)
    public List<LotrBackground> findAll() {
        return backgroundRepository.findAll();
    }

    /**
     * Get all active backgrounds.
     */
    @Transactional(readOnly = true)
    public List<LotrBackground> findActiveBackgrounds() {
        return backgroundRepository.findAll().stream()
                .filter(LotrBackground::isActive)
                .toList();
    }

    /**
     * Get background by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrBackground> findById(Long id) {
        return backgroundRepository.findById(id);
    }

    /**
     * Find backgrounds by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrBackground> findByName(String name) {
        return backgroundRepository.findAll().stream()
                .filter(b -> b.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Create a new background.
     */
    @Transactional
    public LotrBackground createBackground(String name, String description,
                                            String skillProficiency1, String skillProficiency2,
                                            String toolProficiency, String language,
                                            String featureName, String featureDescription) {
        LotrBackground bg = new LotrBackground();
        bg.setName(name);
        bg.setDescription(description);
        bg.setSkillProficiency1(skillProficiency1);
        bg.setSkillProficiency2(skillProficiency2);
        bg.setToolProficiency(toolProficiency);
        bg.setLanguage(language);
        bg.setFeatureName(featureName);
        bg.setFeatureDescription(featureDescription);
        bg.setActive(true);

        LotrBackground saved = backgroundRepository.save(bg);
        log.info("Created background: {}", saved.getName());
        return saved;
    }

    /**
     * Update an existing background.
     */
    @Transactional
    public LotrBackground updateBackground(Long id, String name, String description,
                                            String skillProficiency1, String skillProficiency2,
                                            String toolProficiency, String language,
                                            String featureName, String featureDescription) {
        LotrBackground bg = backgroundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Background not found: " + id));

        bg.setName(name);
        bg.setDescription(description);
        bg.setSkillProficiency1(skillProficiency1);
        bg.setSkillProficiency2(skillProficiency2);
        bg.setToolProficiency(toolProficiency);
        bg.setLanguage(language);
        bg.setFeatureName(featureName);
        bg.setFeatureDescription(featureDescription);

        LotrBackground saved = backgroundRepository.save(bg);
        log.info("Updated background: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a background.
     */
    @Transactional
    public void deleteBackground(Long id) {
        if (!backgroundRepository.existsById(id)) {
            throw new IllegalArgumentException("Background not found: " + id);
        }
        backgroundRepository.deleteById(id);
        log.info("Deleted background: {}", id);
    }

    /**
     * Initialize the background database with LOTR-themed backgrounds.
     */
    @Transactional
    public void initializeBackgroundDatabase() {
        if (!backgroundRepository.findAll().isEmpty()) {
            log.info("Background database already initialized, skipping.");
            return;
        }

        createBackground("Ranger of the North",
                "A wanderer of the northern lands, skilled in tracking and survival.",
                "Survival", "Perception", "Navigator's tools", "Sindarin",
                "Wanderer's Insight", "Advantage on Survival checks in wilderness.");

        createBackground("Steward of Gondor",
                "A noble of Gondor, trained in governance and warfare.",
                "History", "Persuasion", "Calligrapher's supplies", "Adûnaic",
                "Noble Bearing", "Advantage on Persuasion checks with nobles.");

        createBackground("Hobbit of the Shire",
                "A simple folk of the Shire, content with peace and quiet.",
                "Nature", "Persuasion", "Cook's utensils", "Westron",
                "Hobbit Luck", "Once per long rest, reroll a d20 roll.");

        createBackground("Elf of Rivendell",
                "A resident of Imladris, keeper of ancient lore.",
                "History", "Medicine", "Musical instrument", "Quenya",
                "Elven Resilience", "Advantage on saving throws against poison.");

        createBackground("Dwarf of the Mountains",
                "A craftsman and warrior of the dwarf kingdoms.",
                "History", "Insight", "Mason's tools", "Dwarvish",
                "Stonecunning", "Double proficiency bonus on History checks about stone structures.");

        createBackground("Rohirrim Rider",
                "A horse-lord of Rohan, skilled in riding and combat.",
                "Animal Handling", "Athletics", "Rider's weapons", "Westron",
                "Horse Lord", "Advantage on checks involving horses.");

        createBackground("Outlaw of the Wild",
                "A fugitive living outside the law, skilled in stealth.",
                "Stealth", "Deception", "Thieves' tools", "Common tongues",
                "Criminal Contact", "Access to a network of informants.");

        createBackground("Scholar of Lore",
                "A learned individual, versed in the histories of Middle-earth.",
                "Arcana", "History", "Calligrapher's supplies", "Multiple languages",
                "Researcher", "Can find information about ancient artifacts and places.");

        log.info("Initialized background database with {} LOTR backgrounds.", backgroundRepository.count());
    }
}
