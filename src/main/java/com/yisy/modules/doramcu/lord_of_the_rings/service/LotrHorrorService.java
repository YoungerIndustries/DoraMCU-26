package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDarkGift;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrFearDread;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrDarkGiftRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrFearDreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Horror mechanics.
 * <p>
 * Horror mechanics include dark gifts (corruption powers) and fear/dread tracking.
 * Dark gifts grant power at the cost of corruption. Fear/dread represents the
 * terror of facing Nazgûl, Shelob, and other horrors.
 * </p>
 */
@Service
public class LotrHorrorService {

    private static final Logger log = LoggerFactory.getLogger(LotrHorrorService.class);

    private final LotrDarkGiftRepository darkGiftRepository;
    private final LotrFearDreadRepository fearDreadRepository;

    public LotrHorrorService(LotrDarkGiftRepository darkGiftRepository,
                              LotrFearDreadRepository fearDreadRepository) {
        this.darkGiftRepository = darkGiftRepository;
        this.fearDreadRepository = fearDreadRepository;
    }

    // --- Dark Gifts ---

    /**
     * Get all dark gifts.
     */
    @Transactional(readOnly = true)
    public List<LotrDarkGift> findAllDarkGifts() {
        return darkGiftRepository.findAll();
    }

    /**
     * Get all active dark gifts.
     */
    @Transactional(readOnly = true)
    public List<LotrDarkGift> findActiveDarkGifts() {
        return darkGiftRepository.findAll().stream()
                .filter(LotrDarkGift::isActive)
                .toList();
    }

    /**
     * Get dark gift by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrDarkGift> findDarkGiftById(Long id) {
        return darkGiftRepository.findById(id);
    }

    /**
     * Find dark gifts by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrDarkGift> findDarkGiftsByName(String name) {
        return darkGiftRepository.findAll().stream()
                .filter(g -> g.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Create a new dark gift.
     */
    @Transactional
    public LotrDarkGift createDarkGift(String name, String description, int corruptionCost) {
        LotrDarkGift gift = new LotrDarkGift();
        gift.setName(name);
        gift.setDescription(description);
        gift.setCorruptionCost(corruptionCost);
        gift.setActive(true);

        LotrDarkGift saved = darkGiftRepository.save(gift);
        log.info("Created dark gift: {} (corruption cost: {})", saved.getName(), saved.getCorruptionCost());
        return saved;
    }

    /**
     * Update an existing dark gift.
     */
    @Transactional
    public LotrDarkGift updateDarkGift(Long id, String name, String description, int corruptionCost) {
        LotrDarkGift gift = darkGiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dark gift not found: " + id));

        gift.setName(name);
        gift.setDescription(description);
        gift.setCorruptionCost(corruptionCost);

        LotrDarkGift saved = darkGiftRepository.save(gift);
        log.info("Updated dark gift: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a dark gift.
     */
    @Transactional
    public void deleteDarkGift(Long id) {
        if (!darkGiftRepository.existsById(id)) {
            throw new IllegalArgumentException("Dark gift not found: " + id);
        }
        darkGiftRepository.deleteById(id);
        log.info("Deleted dark gift: {}", id);
    }

    // --- Fear/Dread ---

    /**
     * Get all fear/dread records.
     */
    @Transactional(readOnly = true)
    public List<LotrFearDread> findAllFearDread() {
        return fearDreadRepository.findAll();
    }

    /**
     * Get fear/dread for a character.
     */
    @Transactional(readOnly = true)
    public Optional<LotrFearDread> findByCharacterId(Long characterId) {
        return fearDreadRepository.findAll().stream()
                .filter(fd -> fd.getCharacterId().equals(characterId) && fd.isActive())
                .findFirst();
    }

    /**
     * Get fear/dread by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrFearDread> findFearDreadById(Long id) {
        return fearDreadRepository.findById(id);
    }

    /**
     * Create or get fear/dread for a character.
     */
    @Transactional
    public LotrFearDread createOrUpdateFearDread(Long characterId, int maxLevel) {
        Optional<LotrFearDread> existing = findByCharacterId(characterId);
        if (existing.isPresent()) {
            LotrFearDread fd = existing.get();
            fd.setMaxLevel(maxLevel);
            return fearDreadRepository.save(fd);
        }

        LotrFearDread fd = new LotrFearDread();
        fd.setCharacterId(characterId);
        fd.setLevel(0);
        fd.setMaxLevel(maxLevel);
        fd.setActive(true);

        LotrFearDread saved = fearDreadRepository.save(fd);
        log.info("Created fear/dread for character {}", characterId);
        return saved;
    }

    /**
     * Increase fear/dread level.
     */
    @Transactional
    public String increaseFear(Long characterId, int amount) {
        LotrFearDread fd = getOrCreateFearDread(characterId);

        int newLevel = Math.min(fd.getMaxLevel(), fd.getLevel() + amount);
        int added = newLevel - fd.getLevel();
        fd.setLevel(newLevel);
        fearDreadRepository.save(fd);

        if (newLevel >= fd.getMaxLevel()) {
            return String.format("Fear maxed out! +{} levels. The character is paralyzed by terror!", added);
        }
        return String.format("Fear increases by {}. Level: {}/{}", added, fd.getLevel(), fd.getMaxLevel());
    }

    /**
     * Decrease fear/dread level.
     */
    @Transactional
    public String decreaseFear(Long characterId, int amount) {
        LotrFearDread fd = getOrCreateFearDread(characterId);

        int newLevel = Math.max(0, fd.getLevel() - amount);
        int reduced = fd.getLevel() - newLevel;
        fd.setLevel(newLevel);
        fearDreadRepository.save(fd);

        return String.format("Fear decreases by {}. Level: {}/{}", reduced, fd.getLevel(), fd.getMaxLevel());
    }

    /**
     * Reset fear/dread.
     */
    @Transactional
    public String resetFear(Long characterId) {
        LotrFearDread fd = getOrCreateFearDread(characterId);

        int previous = fd.getLevel();
        fd.setLevel(0);
        fearDreadRepository.save(fd);

        return String.format("Fear reset from {} to 0.", previous);
    }

    /**
     * Delete fear/dread record.
     */
    @Transactional
    public void deleteFearDread(Long id) {
        if (!fearDreadRepository.existsById(id)) {
            throw new IllegalArgumentException("Fear/dread not found: " + id);
        }
        fearDreadRepository.deleteById(id);
        log.info("Deleted fear/dread: {}", id);
    }

    /**
     * Initialize the horror database with LOTR-themed dark gifts.
     */
    @Transactional
    public void initializeHorrorDatabase() {
        if (!darkGiftRepository.findAll().isEmpty()) {
            log.info("Horror database already initialized, skipping.");
            return;
        }

        createDarkGift("Shadow Walk",
                "Merge with shadows, becoming invisible for 1 minute.", 2);
        createDarkGift("Dark Fire",
                "Cast black flame that deals 3d8 necrotic damage.", 1);
        createDarkGift("Mind Domination",
                "Attempt to control a target's mind. DC 15 Cha save.", 3);
        createDarkGift("Ring's Power",
                "Use the Ring's power to dominate weaker wills. +1 corruption per use.", 2);
        createDarkGift("Wraith Form",
                "Transform into a wraith-like being. Gain resistance to non-magical damage.", 4);
        createDarkGift("Nazgûl's Blade",
                "Summon a Morgul blade that infects wounds with darkness.", 3);
        createDarkGift("Shelob's Venom",
                "Inject paralyzing venom. Target must succeed on DC 14 Con save or be paralyzed.", 2);
        createDarkGift("Balrog's Flame",
                "Unleash hellish fire. 4d10 fire damage in a 15-foot cone.", 5);

        log.info("Initialized horror database with {} LOTR dark gifts.", darkGiftRepository.count());
    }

    // --- Helper Methods ---

    private LotrFearDread getOrCreateFearDread(Long characterId) {
        Optional<LotrFearDread> existing = findByCharacterId(characterId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LotrFearDread fd = new LotrFearDread();
        fd.setCharacterId(characterId);
        fd.setLevel(0);
        fd.setMaxLevel(10);
        fd.setActive(true);
        return fearDreadRepository.save(fd);
    }
}
