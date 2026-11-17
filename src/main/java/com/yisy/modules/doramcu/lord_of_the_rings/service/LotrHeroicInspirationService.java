package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrHeroicInspiration;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrHeroicInspirationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Heroic Inspiration.
 * <p>
 * Heroic Inspiration points represent a character's heroic deeds and
 * narrative importance. Points can be spent to reroll dice or gain
 * special benefits.
 * </p>
 */
@Service
public class LotrHeroicInspirationService {

    private static final Logger log = LoggerFactory.getLogger(LotrHeroicInspirationService.class);

    private final LotrHeroicInspirationRepository inspirationRepository;

    public LotrHeroicInspirationService(LotrHeroicInspirationRepository inspirationRepository) {
        this.inspirationRepository = inspirationRepository;
    }

    /**
     * Get all heroic inspiration records.
     */
    @Transactional(readOnly = true)
    public List<LotrHeroicInspiration> findAll() {
        return inspirationRepository.findAll();
    }

    /**
     * Get heroic inspiration for a character.
     */
    @Transactional(readOnly = true)
    public Optional<LotrHeroicInspiration> findByCharacterId(Long characterId) {
        return inspirationRepository.findAll().stream()
                .filter(hi -> hi.getCharacterId().equals(characterId) && hi.isActive())
                .findFirst();
    }

    /**
     * Get heroic inspiration by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrHeroicInspiration> findById(Long id) {
        return inspirationRepository.findById(id);
    }

    /**
     * Create or get heroic inspiration for a character.
     */
    @Transactional
    public LotrHeroicInspiration createOrUpdate(Long characterId, int maxPoints) {
        Optional<LotrHeroicInspiration> existing = findByCharacterId(characterId);
        if (existing.isPresent()) {
            LotrHeroicInspiration hi = existing.get();
            hi.setMaxPoints(maxPoints);
            return inspirationRepository.save(hi);
        }

        LotrHeroicInspiration hi = new LotrHeroicInspiration();
        hi.setCharacterId(characterId);
        hi.setPoints(0);
        hi.setMaxPoints(maxPoints);
        hi.setActive(true);

        LotrHeroicInspiration saved = inspirationRepository.save(hi);
        log.info("Created heroic inspiration for character {}", characterId);
        return saved;
    }

    /**
     * Award heroic inspiration points.
     */
    @Transactional
    public String awardPoints(Long characterId, int points) {
        LotrHeroicInspiration hi = getOrCreateInspiration(characterId);

        int newPoints = Math.min(hi.getMaxPoints(), hi.getPoints() + points);
        int awarded = newPoints - hi.getPoints();
        hi.setPoints(newPoints);
        inspirationRepository.save(hi);

        return String.format("Awarded %d heroic inspiration points. Total: %d/%d",
                awarded, hi.getPoints(), hi.getMaxPoints());
    }

    /**
     * Spend heroic inspiration points for a reroll.
     */
    @Transactional
    public String spendPoints(Long characterId, int points) {
        LotrHeroicInspiration hi = getOrCreateInspiration(characterId);

        if (hi.getPoints() < points) {
            return "Not enough heroic inspiration points!";
        }

        hi.setPoints(hi.getPoints() - points);
        inspirationRepository.save(hi);

        return String.format("Spent %d heroic inspiration points. Remaining: %d/%d",
                points, hi.getPoints(), hi.getMaxPoints());
    }

    /**
     * Reset heroic inspiration points.
     */
    @Transactional
    public String resetPoints(Long characterId) {
        LotrHeroicInspiration hi = getOrCreateInspiration(characterId);

        int previous = hi.getPoints();
        hi.setPoints(0);
        inspirationRepository.save(hi);

        return String.format("Reset %d heroic inspiration points.", previous);
    }

    /**
     * Delete heroic inspiration.
     */
    @Transactional
    public void deleteInspiration(Long id) {
        if (!inspirationRepository.existsById(id)) {
            throw new IllegalArgumentException("Heroic inspiration not found: " + id);
        }
        inspirationRepository.deleteById(id);
        log.info("Deleted heroic inspiration: {}", id);
    }

    /**
     * Deactivate heroic inspiration.
     */
    @Transactional
    public void deactivateInspiration(Long id) {
        LotrHeroicInspiration hi = inspirationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Heroic inspiration not found: " + id));
        hi.setActive(false);
        inspirationRepository.save(hi);
        log.info("Deactivated heroic inspiration: {}", id);
    }

    // --- Helper Methods ---

    private LotrHeroicInspiration getOrCreateInspiration(Long characterId) {
        Optional<LotrHeroicInspiration> existing = findByCharacterId(characterId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LotrHeroicInspiration hi = new LotrHeroicInspiration();
        hi.setCharacterId(characterId);
        hi.setPoints(0);
        hi.setMaxPoints(3);
        hi.setActive(true);
        return inspirationRepository.save(hi);
    }
}
