package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpecies;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrSpeciesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Species (races).
 * <p>
 * Species determine ability score bonuses, size, speed, darkvision,
 * and other racial traits. LOTR species include Elves, Men, Dwarves,
 * Hobbits, Ents, and Maiar.
 * </p>
 */
@Service
public class LotrSpeciesService {

    private static final Logger log = LoggerFactory.getLogger(LotrSpeciesService.class);

    private final LotrSpeciesRepository speciesRepository;

    public LotrSpeciesService(LotrSpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    /**
     * Get all species.
     */
    @Transactional(readOnly = true)
    public List<LotrSpecies> findAll() {
        return speciesRepository.findAll();
    }

    /**
     * Get all active species.
     */
    @Transactional(readOnly = true)
    public List<LotrSpecies> findActiveSpecies() {
        return speciesRepository.findAll().stream()
                .filter(LotrSpecies::isActive)
                .toList();
    }

    /**
     * Get species by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrSpecies> findById(Long id) {
        return speciesRepository.findById(id);
    }

    /**
     * Find species by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrSpecies> findByName(String name) {
        return speciesRepository.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Create a new species.
     */
    @Transactional
    public LotrSpecies createSpecies(String name, String description, String size, int speed,
                                      boolean darkvision, String ageModifier, String heightModifier,
                                      String weightModifier, String abilityIncrease) {
        LotrSpecies species = new LotrSpecies();
        species.setName(name);
        species.setDescription(description);
        species.setSize(size);
        species.setSpeed(speed);
        species.setDarkvision(darkvision);
        species.setAgeModifier(ageModifier);
        species.setHeightModifier(heightModifier);
        species.setWeightModifier(weightModifier);
        species.setAbilityIncrease(abilityIncrease);
        species.setActive(true);

        LotrSpecies saved = speciesRepository.save(species);
        log.info("Created species: {} ({})", saved.getName(), saved.getSize());
        return saved;
    }

    /**
     * Update an existing species.
     */
    @Transactional
    public LotrSpecies updateSpecies(Long id, String name, String description, String size, int speed,
                                      boolean darkvision, String ageModifier, String heightModifier,
                                      String weightModifier, String abilityIncrease) {
        LotrSpecies species = speciesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Species not found: " + id));

        species.setName(name);
        species.setDescription(description);
        species.setSize(size);
        species.setSpeed(speed);
        species.setDarkvision(darkvision);
        species.setAgeModifier(ageModifier);
        species.setHeightModifier(heightModifier);
        species.setWeightModifier(weightModifier);
        species.setAbilityIncrease(abilityIncrease);

        LotrSpecies saved = speciesRepository.save(species);
        log.info("Updated species: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a species.
     */
    @Transactional
    public void deleteSpecies(Long id) {
        if (!speciesRepository.existsById(id)) {
            throw new IllegalArgumentException("Species not found: " + id);
        }
        speciesRepository.deleteById(id);
        log.info("Deleted species: {}", id);
    }

    /**
     * Initialize the species database with LOTR-themed species.
     */
    @Transactional
    public void initializeSpeciesDatabase() {
        if (!speciesRepository.findAll().isEmpty()) {
            log.info("Species database already initialized, skipping.");
            return;
        }

        createSpecies("Elf",
                "Graceful and long-lived, elves are masters of craft and magic.",
                "Medium", 30, true,
                "Adolescence at 100, considered young until 200",
                "5-6 feet tall",
                "100-160 pounds",
                "+2 Dex, +1 Cha, +1 Int");

        createSpecies("Man",
                "Mortal and diverse, men are the dominant race of Middle-earth.",
                "Medium", 30, false,
                "Adolescence at 15, considered young until 30",
                "5-6 feet tall",
                "120-200 pounds",
                "+2 Str or +2 Wis");

        createSpecies("Dwarf",
                "Stout and resilient, dwarves are master craftsmen and miners.",
                "Medium", 25, true,
                "Adolescence at 40, considered young until 50",
                "4-5 feet tall",
                "150-250 pounds",
                "+2 Con, +1 Str");

        createSpecies("Hobbit",
                "Small and unassuming, hobbits value peace, comfort, and good food.",
                "Small", 25, false,
                "Adolescence at 7, considered young until 33",
                "2.5-3.5 feet tall",
                "30-100 pounds",
                "+2 Dex, +1 Wis, +1 Cha");

        createSpecies("Ent",
                "Ancient tree-herders, ents are slow but incredibly powerful.",
                "Huge", 15, true,
                "Age measured in millennia",
                "20-30 feet tall",
                "Several tons",
                "+4 Str, +4 Con");

        createSpecies("Maia",
                "Spiritual beings of great power, including the wizards and Sauron.",
                "Medium", 30, true,
                "Ageless spirits",
                "Appears human",
                "Appears human",
                "+4 Int, +2 Wis");

        createSpecies("Orc",
                "Ugly and cruel, orcs are the foot soldiers of darkness.",
                "Medium", 30, true,
                "Adolescence at 10, considered young until 20",
                "5-7 feet tall",
                "150-300 pounds",
                "+2 Str, +1 Con");

        log.info("Initialized species database with {} LOTR species.", speciesRepository.count());
    }
}
