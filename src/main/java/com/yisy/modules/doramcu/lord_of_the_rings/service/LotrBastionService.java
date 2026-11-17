package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBastion;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrBastionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Bastions (fortresses).
 * <p>
 * Bastions represent strongholds like Minas Tirith, Helm's Deep, and Rivendell.
 * They have defense ratings, capacity, and can be damaged or improved.
 * </p>
 */
@Service
public class LotrBastionService {

    private static final Logger log = LoggerFactory.getLogger(LotrBastionService.class);

    private final LotrBastionRepository bastionRepository;

    public LotrBastionService(LotrBastionRepository bastionRepository) {
        this.bastionRepository = bastionRepository;
    }

    /**
     * Get all bastions.
     */
    @Transactional(readOnly = true)
    public List<LotrBastion> findAll() {
        return bastionRepository.findAll();
    }

    /**
     * Get all active bastions.
     */
    @Transactional(readOnly = true)
    public List<LotrBastion> findActiveBastions() {
        return bastionRepository.findAll().stream()
                .filter(LotrBastion::isActive)
                .toList();
    }

    /**
     * Get bastion by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrBastion> findById(Long id) {
        return bastionRepository.findById(id);
    }

    /**
     * Find bastions by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrBastion> findByName(String name) {
        return bastionRepository.findAll().stream()
                .filter(b -> b.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Create a new bastion.
     */
    @Transactional
    public LotrBastion createBastion(String name, String description, String location,
                                      int defenseRating, int capacity) {
        LotrBastion bastion = new LotrBastion();
        bastion.setName(name);
        bastion.setDescription(description);
        bastion.setLocation(location);
        bastion.setDefenseRating(defenseRating);
        bastion.setCapacity(capacity);
        bastion.setActive(true);

        LotrBastion saved = bastionRepository.save(bastion);
        log.info("Created bastion: {} ({})", saved.getName(), saved.getLocation());
        return saved;
    }

    /**
     * Update an existing bastion.
     */
    @Transactional
    public LotrBastion updateBastion(Long id, String name, String description, String location,
                                      int defenseRating, int capacity) {
        LotrBastion bastion = bastionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bastion not found: " + id));

        bastion.setName(name);
        bastion.setDescription(description);
        bastion.setLocation(location);
        bastion.setDefenseRating(defenseRating);
        bastion.setCapacity(capacity);

        LotrBastion saved = bastionRepository.save(bastion);
        log.info("Updated bastion: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a bastion.
     */
    @Transactional
    public void deleteBastion(Long id) {
        if (!bastionRepository.existsById(id)) {
            throw new IllegalArgumentException("Bastion not found: " + id);
        }
        bastionRepository.deleteById(id);
        log.info("Deleted bastion: {}", id);
    }

    /**
     * Damage a bastion during a siege.
     */
    @Transactional
    public String damageBastion(Long id, int damage) {
        LotrBastion bastion = bastionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bastion not found: " + id));

        int newDefense = Math.max(0, bastion.getDefenseRating() - damage);
        bastion.setDefenseRating(newDefense);

        if (newDefense <= 0) {
            bastion.setActive(false);
        }

        bastionRepository.save(bastion);

        if (newDefense <= 0) {
            return String.format("%s has fallen! The fortress is no more.", bastion.getName());
        }
        return String.format("%s takes damage. Defense rating: %d", bastion.getName(), newDefense);
    }

    /**
     * Repair a bastion.
     */
    @Transactional
    public String repairBastion(Long id, int repair) {
        LotrBastion bastion = bastionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bastion not found: " + id));

        int newDefense = Math.min(100, bastion.getDefenseRating() + repair);
        bastion.setDefenseRating(newDefense);
        bastion.setActive(true);
        bastionRepository.save(bastion);

        return String.format("%s is repaired. Defense rating: %d", bastion.getName(), newDefense);
    }

    /**
     * Build a facility at a bastion (increase capacity).
     */
    @Transactional
    public String buildFacility(Long id, int capacityIncrease) {
        LotrBastion bastion = bastionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bastion not found: " + id));

        int newCapacity = bastion.getCapacity() + capacityIncrease;
        bastion.setCapacity(newCapacity);
        bastionRepository.save(bastion);

        return String.format("New facility built at %s. Capacity: %d", bastion.getName(), newCapacity);
    }

    /**
     * Initialize the bastion database with LOTR-themed strongholds.
     */
    @Transactional
    public void initializeBastionDatabase() {
        if (!bastionRepository.findAll().isEmpty()) {
            log.info("Bastion database already initialized, skipping.");
            return;
        }

        createBastion("Minas Tirith", "The White City, capital of Gondor.", "Gondor", 90, 10000);
        createBastion("Helm's Deep", "The Hornburg, fortress of Rohan.", "Rohan", 80, 5000);
        createBastion("Rivendell", "Imladris, the Last Homely House.", "Eriador", 70, 1000);
        createBastion("Lothlórien", "The Golden Wood, realm of Galadriel.", "Lothlórien", 75, 2000);
        createBastion("Edoras", "Capital of Rohan, seat of the King.", "Rohan", 50, 3000);
        createBastion("Osgiliath", "The ancient capital of Gondor, now ruined.", "Gondor", 30, 2000);
        createBastion("Dol Guldur", "The Tower of Dread, stronghold of the Necromancer.", "Mirkwood", 60, 500);
        createBastion("Barad-dûr", "The Dark Tower, seat of Sauron.", "Mordor", 100, 50000);
        createBastion("Isengard", "The fortress of Saruman.", "Isengard", 70, 10000);
        createBastion("Dunharrow", "The hidden refuge of Rohan.", "Rohan", 40, 1000);

        log.info("Initialized bastion database with {} LOTR strongholds.", bastionRepository.count());
    }
}
