package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrExhaustion;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrExhaustionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Exhaustion.
 * <p>
 * Exhaustion represents the cumulative effects of hardship, injury, and fatigue.
 * Each level imposes penalties on speed and ability checks. LOTR exhaustion
 * is themed around the perils of the journey to Mount Doom.
 * </p>
 */
@Service
public class LotrExhaustionService {

    private static final Logger log = LoggerFactory.getLogger(LotrExhaustionService.class);

    private final LotrExhaustionRepository exhaustionRepository;

    public LotrExhaustionService(LotrExhaustionRepository exhaustionRepository) {
        this.exhaustionRepository = exhaustionRepository;
    }

    /**
     * Get all exhaustion levels.
     */
    @Transactional(readOnly = true)
    public List<LotrExhaustion> findAll() {
        return exhaustionRepository.findAll();
    }

    /**
     * Get exhaustion level by level number.
     */
    @Transactional(readOnly = true)
    public Optional<LotrExhaustion> findByLevel(int level) {
        return exhaustionRepository.findAll().stream()
                .filter(e -> e.getLevel() == level && e.isActive())
                .findFirst();
    }

    /**
     * Get exhaustion by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrExhaustion> findById(Long id) {
        return exhaustionRepository.findById(id);
    }

    /**
     * Get the speed penalty for a given exhaustion level.
     */
    @Transactional(readOnly = true)
    public int getSpeedPenalty(int level) {
        return exhaustionRepository.findAll().stream()
                .filter(e -> e.getLevel() == level && e.isActive())
                .findFirst()
                .map(LotrExhaustion::getSpeedPenalty)
                .orElse(0);
    }

    /**
     * Check if a given exhaustion level imposes disadvantage.
     */
    @Transactional(readOnly = true)
    public boolean hasDisadvantage(int level) {
        return exhaustionRepository.findAll().stream()
                .filter(e -> e.getLevel() == level && e.isActive())
                .findFirst()
                .map(LotrExhaustion::isDisadvantage)
                .orElse(false);
    }

    /**
     * Create a new exhaustion level.
     */
    @Transactional
    public LotrExhaustion createExhaustionLevel(int level, String name, String description,
                                                  int speedPenalty, boolean disadvantage) {
        LotrExhaustion exhaustion = new LotrExhaustion();
        exhaustion.setLevel(level);
        exhaustion.setName(name);
        exhaustion.setDescription(description);
        exhaustion.setSpeedPenalty(speedPenalty);
        exhaustion.setDisadvantage(disadvantage);
        exhaustion.setActive(true);

        LotrExhaustion saved = exhaustionRepository.save(exhaustion);
        log.info("Created exhaustion level {}: {}", saved.getLevel(), saved.getName());
        return saved;
    }

    /**
     * Update an existing exhaustion level.
     */
    @Transactional
    public LotrExhaustion updateExhaustionLevel(Long id, String name, String description,
                                                  int speedPenalty, boolean disadvantage) {
        LotrExhaustion exhaustion = exhaustionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exhaustion level not found: " + id));

        exhaustion.setName(name);
        exhaustion.setDescription(description);
        exhaustion.setSpeedPenalty(speedPenalty);
        exhaustion.setDisadvantage(disadvantage);

        LotrExhaustion saved = exhaustionRepository.save(exhaustion);
        log.info("Updated exhaustion level {}: {}", saved.getLevel(), saved.getName());
        return saved;
    }

    /**
     * Delete an exhaustion level.
     */
    @Transactional
    public void deleteExhaustionLevel(Long id) {
        if (!exhaustionRepository.existsById(id)) {
            throw new IllegalArgumentException("Exhaustion level not found: " + id);
        }
        exhaustionRepository.deleteById(id);
        log.info("Deleted exhaustion level: {}", id);
    }

    /**
     * Get a description of the effects at a given exhaustion level.
     */
    @Transactional(readOnly = true)
    public String getExhaustionEffects(int level) {
        LotrExhaustion exhaustion = exhaustionRepository.findAll().stream()
                .filter(e -> e.getLevel() == level && e.isActive())
                .findFirst()
                .orElse(null);

        if (exhaustion == null) {
            return "No exhaustion effects at level " + level;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(exhaustion.getName()).append(": ").append(exhaustion.getDescription());
        if (exhaustion.getSpeedPenalty() > 0) {
            sb.append(String.format(" Speed reduced by %d feet.", exhaustion.getSpeedPenalty()));
        }
        if (exhaustion.isDisadvantage()) {
            sb.append(" Disadvantage on ability checks.");
        }

        return sb.toString();
    }

    /**
     * Initialize the exhaustion database with LOTR-themed levels.
     */
    @Transactional
    public void initializeExhaustionDatabase() {
        if (!exhaustionRepository.findAll().isEmpty()) {
            log.info("Exhaustion database already initialized, skipping.");
            return;
        }

        createExhaustionLevel(1, "Tired",
                "The journey weighs on you. You feel the strain of the road.",
                0, false);
        createExhaustionLevel(2, "Weary",
                "Your limbs feel heavy. The Ring seems heavier than usual.",
                -10, false);
        createExhaustionLevel(3, "Exhausted",
                "Dark thoughts intrude. You struggle to focus.",
                -10, true);
        createExhaustionLevel(4, "Debilitated",
                "Your body rebels against the journey. Every step is agony.",
                -20, true);
        createExhaustionLevel(5, "Near Death",
                "You can barely stand. The shadows close in.",
                -30, true);
        createExhaustionLevel(6, "Dying",
                "Death beckons. Without rest, you will not survive.",
                -30, true);

        log.info("Initialized exhaustion database with {} LOTR levels.", exhaustionRepository.count());
    }
}
