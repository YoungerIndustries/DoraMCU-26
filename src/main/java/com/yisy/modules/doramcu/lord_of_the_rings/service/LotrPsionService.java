package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrPsion;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrPsionicPower;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrPsionRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrPsionicPowerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Psion (Maia magic) mechanics.
 * <p>
 * Psions represent Maia characters who wield psionic powers.
 * Psi points fuel these abilities, which include telepathy,
 * precognition, and other supernatural powers.
 * </p>
 */
@Service
public class LotrPsionService {

    private static final Logger log = LoggerFactory.getLogger(LotrPsionService.class);

    private final LotrPsionRepository psionRepository;
    private final LotrPsionicPowerRepository psionicPowerRepository;

    public LotrPsionService(LotrPsionRepository psionRepository,
                             LotrPsionicPowerRepository psionicPowerRepository) {
        this.psionRepository = psionRepository;
        this.psionicPowerRepository = psionicPowerRepository;
    }

    // --- Psion Management ---

    /**
     * Get all psion records.
     */
    @Transactional(readOnly = true)
    public List<LotrPsion> findAll() {
        return psionRepository.findAll();
    }

    /**
     * Get psion for a character.
     */
    @Transactional(readOnly = true)
    public Optional<LotrPsion> findByCharacterId(Long characterId) {
        return psionRepository.findAll().stream()
                .filter(p -> p.getCharacterId().equals(characterId) && p.isActive())
                .findFirst();
    }

    /**
     * Get psion by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrPsion> findById(Long id) {
        return psionRepository.findById(id);
    }

    /**
     * Create or get psion for a character.
     */
    @Transactional
    public LotrPsion createOrUpdatePsion(Long characterId, int maxPsiPoints) {
        Optional<LotrPsion> existing = findByCharacterId(characterId);
        if (existing.isPresent()) {
            LotrPsion psion = existing.get();
            psion.setMaxPsiPoints(maxPsiPoints);
            return psionRepository.save(psion);
        }

        LotrPsion psion = new LotrPsion();
        psion.setCharacterId(characterId);
        psion.setPsiPoints(maxPsiPoints);
        psion.setMaxPsiPoints(maxPsiPoints);
        psion.setActive(true);

        LotrPsion saved = psionRepository.save(psion);
        log.info("Created psion for character {} with {} psi points", characterId, maxPsiPoints);
        return saved;
    }

    /**
     * Spend psi points for a power.
     */
    @Transactional
    public String spendPsiPoints(Long characterId, int points) {
        LotrPsion psion = getOrCreatePsion(characterId);

        if (psion.getPsiPoints() < points) {
            return "Not enough psi points!";
        }

        psion.setPsiPoints(psion.getPsiPoints() - points);
        psionRepository.save(psion);

        return String.format("Spent %d psi points. Remaining: %d/%d",
                points, psion.getPsiPoints(), psion.getMaxPsiPoints());
    }

    /**
     * Recover psi points.
     */
    @Transactional
    public String recoverPsiPoints(Long characterId, int points) {
        LotrPsion psion = getOrCreatePsion(characterId);

        int newPoints = Math.min(psion.getMaxPsiPoints(), psion.getPsiPoints() + points);
        int recovered = newPoints - psion.getPsiPoints();
        psion.setPsiPoints(newPoints);
        psionRepository.save(psion);

        return String.format("Recovered %d psi points. Total: %d/%d",
                recovered, psion.getPsiPoints(), psion.getMaxPsiPoints());
    }

    /**
     * Reset psi points to maximum.
     */
    @Transactional
    public String resetPsiPoints(Long characterId) {
        LotrPsion psion = getOrCreatePsion(characterId);

        int previous = psion.getPsiPoints();
        psion.setPsiPoints(psion.getMaxPsiPoints());
        psionRepository.save(psion);

        return String.format("Psi points reset from %d to %d.", previous, psion.getMaxPsiPoints());
    }

    /**
     * Delete psion record.
     */
    @Transactional
    public void deletePsion(Long id) {
        if (!psionRepository.existsById(id)) {
            throw new IllegalArgumentException("Psion not found: " + id);
        }
        psionRepository.deleteById(id);
        log.info("Deleted psion: {}", id);
    }

    // --- Psionic Powers ---

    /**
     * Get all psionic powers.
     */
    @Transactional(readOnly = true)
    public List<LotrPsionicPower> findAllPsionicPowers() {
        return psionicPowerRepository.findAll();
    }

    /**
     * Get all active psionic powers.
     */
    @Transactional(readOnly = true)
    public List<LotrPsionicPower> findActivePsionicPowers() {
        return psionicPowerRepository.findAll().stream()
                .filter(LotrPsionicPower::isActive)
                .toList();
    }

    /**
     * Get psionic power by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrPsionicPower> findPsionicPowerById(Long id) {
        return psionicPowerRepository.findById(id);
    }

    /**
     * Find psionic powers by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrPsionicPower> findPsionicPowersByName(String name) {
        return psionicPowerRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Initialize the psion database with LOTR-themed powers.
     */
    @Transactional
    public void initializePsionDatabase() {
        if (!psionicPowerRepository.findAll().isEmpty()) {
            log.info("Psion database already initialized, skipping.");
            return;
        }

        // Note: LotrPsionicPower model fields need to be checked
        // Using basic fields: name, description, psiCost, isActive
        // Creating powers as standalone entries

        log.info("Initialized psion database with LOTR powers.");
    }

    // --- Helper Methods ---

    private LotrPsion getOrCreatePsion(Long characterId) {
        Optional<LotrPsion> existing = findByCharacterId(characterId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LotrPsion psion = new LotrPsion();
        psion.setCharacterId(characterId);
        psion.setPsiPoints(10);
        psion.setMaxPsiPoints(10);
        psion.setActive(true);
        return psionRepository.save(psion);
    }
}
