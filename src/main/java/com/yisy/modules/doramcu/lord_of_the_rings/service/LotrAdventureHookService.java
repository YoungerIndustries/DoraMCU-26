package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrAdventureHook;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrAdventureHookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Adventure Hooks.
 * <p>
 * Adventure hooks provide quest starters and narrative triggers.
 * They are categorized by type and have level ranges.
 * </p>
 */
@Service
public class LotrAdventureHookService {

    private static final Logger log = LoggerFactory.getLogger(LotrAdventureHookService.class);

    private final LotrAdventureHookRepository adventureHookRepository;
    private final Random random = new Random();

    public LotrAdventureHookService(LotrAdventureHookRepository adventureHookRepository) {
        this.adventureHookRepository = adventureHookRepository;
    }

    /**
     * Get all adventure hooks.
     */
    @Transactional(readOnly = true)
    public List<LotrAdventureHook> findAll() {
        return adventureHookRepository.findAll();
    }

    /**
     * Get all active adventure hooks.
     */
    @Transactional(readOnly = true)
    public List<LotrAdventureHook> findActiveHooks() {
        return adventureHookRepository.findAll().stream()
                .filter(LotrAdventureHook::isActive)
                .toList();
    }

    /**
     * Get adventure hook by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrAdventureHook> findById(Long id) {
        return adventureHookRepository.findById(id);
    }

    /**
     * Find adventure hooks by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrAdventureHook> findByName(String name) {
        return adventureHookRepository.findAll().stream()
                .filter(h -> h.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find adventure hooks by category.
     */
    @Transactional(readOnly = true)
    public List<LotrAdventureHook> findByCategory(String category) {
        return adventureHookRepository.findAll().stream()
                .filter(h -> category.equals(h.getCategory()))
                .toList();
    }

    /**
     * Find adventure hooks suitable for a character level.
     */
    @Transactional(readOnly = true)
    public List<LotrAdventureHook> findByLevel(int level) {
        return adventureHookRepository.findAll().stream()
                .filter(h -> level >= h.getMinLevel() && level <= h.getMaxLevel() && h.isActive())
                .toList();
    }

    /**
     * Get a random adventure hook suitable for a character level.
     */
    @Transactional(readOnly = true)
    public Optional<LotrAdventureHook> getRandomHook(int level) {
        List<LotrAdventureHook> hooks = findByLevel(level);
        if (hooks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(hooks.get(random.nextInt(hooks.size())));
    }

    /**
     * Create a new adventure hook.
     */
    @Transactional
    public LotrAdventureHook createAdventureHook(String name, String description,
                                                   String category, int minLevel, int maxLevel) {
        LotrAdventureHook hook = new LotrAdventureHook();
        hook.setName(name);
        hook.setDescription(description);
        hook.setCategory(category);
        hook.setMinLevel(minLevel);
        hook.setMaxLevel(maxLevel);
        hook.setActive(true);

        LotrAdventureHook saved = adventureHookRepository.save(hook);
        log.info("Created adventure hook: {} ({})", saved.getName(), saved.getCategory());
        return saved;
    }

    /**
     * Update an existing adventure hook.
     */
    @Transactional
    public LotrAdventureHook updateAdventureHook(Long id, String name, String description,
                                                   String category, int minLevel, int maxLevel) {
        LotrAdventureHook hook = adventureHookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Adventure hook not found: " + id));

        hook.setName(name);
        hook.setDescription(description);
        hook.setCategory(category);
        hook.setMinLevel(minLevel);
        hook.setMaxLevel(maxLevel);

        LotrAdventureHook saved = adventureHookRepository.save(hook);
        log.info("Updated adventure hook: {}", saved.getName());
        return saved;
    }

    /**
     * Delete an adventure hook.
     */
    @Transactional
    public void deleteAdventureHook(Long id) {
        if (!adventureHookRepository.existsById(id)) {
            throw new IllegalArgumentException("Adventure hook not found: " + id);
        }
        adventureHookRepository.deleteById(id);
        log.info("Deleted adventure hook: {}", id);
    }

    /**
     * Initialize the adventure hook database with LOTR-themed hooks.
     */
    @Transactional
    public void initializeAdventureHookDatabase() {
        if (!adventureHookRepository.findAll().isEmpty()) {
            log.info("Adventure hook database already initialized, skipping.");
            return;
        }

        createAdventureHook("The One Ring",
                "A mysterious ring has been found. Its power is tempting, but dangerous.",
                "Epic", 1, 20);
        createAdventureHook("Orc Ambush",
                "Orcs attack a caravan. The survivors need help reaching safety.",
                "Combat", 1, 5);
        createAdventureHook("The Lost Mine",
                "Dwarves seek to reclaim a lost mine in the Misty Mountains.",
                "Exploration", 3, 8);
        createAdventureHook("The Nazgûl's Hunt",
                "The Ringwraiths search for the Ring. Help the Ring-bearer escape.",
                "Epic", 5, 15);
        createAdventureHook("Entmoot",
                "The Ents gather to discuss the fate of Fangorn. Can you persuade them?",
                "Diplomacy", 5, 10);
        createAdventureHook("Siege of Minas Tirith",
                "Mordor's army approaches. Help defend the White City.",
                "Combat", 8, 15);
        createAdventureHook("Journey to Mordor",
                "Infiltrate Mordor and destroy the One Ring in Mount Doom.",
                "Epic", 10, 20);
        createAdventureHook("The Shire in Peril",
                "Saruman's forces have invaded the Shire. Restore peace to the homeland.",
                "Combat", 8, 12);
        createAdventureHook("Riddles in the Dark",
                "Explore the Mines of Moria and escape the Balrog.",
                "Exploration", 5, 10);
        createAdventureHook("The Watchful Void",
                "Sail to the Undying Lands and face the powers that guard the West.",
                "Epic", 15, 20);
        createAdventureHook("Palantír Recovery",
                "Recover a stolen Palantír before Sauron can use it.",
                "Quest", 3, 8);
        createAdventureHook("The Heir of Numenor",
                "Help Aragorn claim his birthright and unite the Free Peoples.",
                "Diplomacy", 10, 15);

        log.info("Initialized adventure hook database with {} LOTR hooks.", adventureHookRepository.count());
    }
}
