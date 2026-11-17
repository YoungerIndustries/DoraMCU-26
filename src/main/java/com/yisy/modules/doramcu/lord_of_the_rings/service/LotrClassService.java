package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrClass;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Classes (Callings).
 * <p>
 * Classes define character progression paths in the Lord of the Rings RPG.
 * Each class has hit dice, primary abilities, proficiencies, and caster status.
 * </p>
 */
@Service
public class LotrClassService {

    private static final Logger log = LoggerFactory.getLogger(LotrClassService.class);

    private final LotrClassRepository classRepository;

    public LotrClassService(LotrClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    /**
     * Get all classes.
     */
    @Transactional(readOnly = true)
    public List<LotrClass> findAll() {
        return classRepository.findAll();
    }

    /**
     * Get all active classes.
     */
    @Transactional(readOnly = true)
    public List<LotrClass> findActiveClasses() {
        return classRepository.findAll().stream()
                .filter(LotrClass::isActive)
                .toList();
    }

    /**
     * Get class by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrClass> findById(Long id) {
        return classRepository.findById(id);
    }

    /**
     * Find classes by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrClass> findByName(String name) {
        return classRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find caster classes.
     */
    @Transactional(readOnly = true)
    public List<LotrClass> findCasterClasses() {
        return classRepository.findAll().stream()
                .filter(LotrClass::isCaster)
                .toList();
    }

    /**
     * Create a new class.
     */
    @Transactional
    public LotrClass createClass(String name, String description, String hitDice,
                                  String primaryAbility, String proficiency1, String proficiency2,
                                  boolean isCaster, String casterAbility) {
        LotrClass cls = new LotrClass();
        cls.setName(name);
        cls.setDescription(description);
        cls.setHitDice(hitDice);
        cls.setPrimaryAbility(primaryAbility);
        cls.setProficiency1(proficiency1);
        cls.setProficiency2(proficiency2);
        cls.setCaster(isCaster);
        cls.setCasterAbility(casterAbility);
        cls.setActive(true);

        LotrClass saved = classRepository.save(cls);
        log.info("Created class: {} ({})", saved.getName(), saved.getHitDice());
        return saved;
    }

    /**
     * Update an existing class.
     */
    @Transactional
    public LotrClass updateClass(Long id, String name, String description, String hitDice,
                                  String primaryAbility, String proficiency1, String proficiency2,
                                  boolean isCaster, String casterAbility) {
        LotrClass cls = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + id));

        cls.setName(name);
        cls.setDescription(description);
        cls.setHitDice(hitDice);
        cls.setPrimaryAbility(primaryAbility);
        cls.setProficiency1(proficiency1);
        cls.setProficiency2(proficiency2);
        cls.setCaster(isCaster);
        cls.setCasterAbility(casterAbility);

        LotrClass saved = classRepository.save(cls);
        log.info("Updated class: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a class.
     */
    @Transactional
    public void deleteClass(Long id) {
        if (!classRepository.existsById(id)) {
            throw new IllegalArgumentException("Class not found: " + id);
        }
        classRepository.deleteById(id);
        log.info("Deleted class: {}", id);
    }

    /**
     * Initialize the class database with LOTR-themed classes.
     */
    @Transactional
    public void initializeClassDatabase() {
        if (!classRepository.findAll().isEmpty()) {
            log.info("Class database already initialized, skipping.");
            return;
        }

        createClass("Scout", "Swift and stealthy explorer", "d6", "Dex",
                "Light armor", "Simple weapons", false, null);
        createClass("Warrior", "Trained combatant", "d10", "Str",
                "All armor", "All weapons", false, null);
        createClass("Ranger", "Tracker and hunter", "d8", "Dex",
                "Light and medium armor", "All weapons", false, null);
        createClass("Bard", "Inspiring performer", "d8", "Cha",
                "Light armor", "Simple weapons", true, "Cha");
        createClass("Mage", "Arcane spellcaster", "d6", "Int",
                "None", "Wands and staves", true, "Int");
        createClass("Healer", "Divine healer", "d8", "Wis",
                "Light armor", "Maces and staffs", true, "Wis");

        log.info("Initialized class database with {} LOTR classes.", classRepository.count());
    }
}
