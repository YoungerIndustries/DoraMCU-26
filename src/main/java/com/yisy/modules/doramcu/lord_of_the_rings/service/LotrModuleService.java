package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrModule;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Modules.
 * <p>
 * Modules are self-contained adventures or campaign arcs. They have types
 * (adventure, campaign, scenario) and descriptions.
 * </p>
 */
@Service
public class LotrModuleService {

    private static final Logger log = LoggerFactory.getLogger(LotrModuleService.class);

    private final LotrModuleRepository moduleRepository;

    public LotrModuleService(LotrModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    /**
     * Get all modules.
     */
    @Transactional(readOnly = true)
    public List<LotrModule> findAll() {
        return moduleRepository.findAll();
    }

    /**
     * Get all active modules.
     */
    @Transactional(readOnly = true)
    public List<LotrModule> findActiveModules() {
        return moduleRepository.findAll().stream()
                .filter(LotrModule::isActive)
                .toList();
    }

    /**
     * Get module by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrModule> findById(Long id) {
        return moduleRepository.findById(id);
    }

    /**
     * Find modules by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrModule> findByName(String name) {
        return moduleRepository.findAll().stream()
                .filter(m -> m.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find modules by type.
     */
    @Transactional(readOnly = true)
    public List<LotrModule> findByType(String type) {
        return moduleRepository.findAll().stream()
                .filter(m -> type.equals(m.getType()))
                .toList();
    }

    /**
     * Create a new module.
     */
    @Transactional
    public LotrModule createModule(String name, String description, String type) {
        LotrModule module = new LotrModule();
        module.setName(name);
        module.setDescription(description);
        module.setType(type);
        module.setActive(true);

        LotrModule saved = moduleRepository.save(module);
        log.info("Created module: {} ({})", saved.getName(), saved.getType());
        return saved;
    }

    /**
     * Update an existing module.
     */
    @Transactional
    public LotrModule updateModule(Long id, String name, String description, String type) {
        LotrModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id));

        module.setName(name);
        module.setDescription(description);
        module.setType(type);

        LotrModule saved = moduleRepository.save(module);
        log.info("Updated module: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a module.
     */
    @Transactional
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Module not found: " + id);
        }
        moduleRepository.deleteById(id);
        log.info("Deleted module: {}", id);
    }

    /**
     * Initialize the module database with LOTR-themed modules.
     */
    @Transactional
    public void initializeModuleDatabase() {
        if (!moduleRepository.findAll().isEmpty()) {
            log.info("Module database already initialized, skipping.");
            return;
        }

        createModule("The Fellowship of the Ring",
                "Form the Fellowship and journey from the Shire to Moria.",
                "Campaign");
        createModule("The Two Towers",
                "The broken Fellowship fights on multiple fronts.",
                "Campaign");
        createModule("The Return of the King",
                "The final battle for Middle-earth.",
                "Campaign");
        createModule("The Hobbit",
                "Bilbo's adventure with Thorin and Company.",
                "Adventure");
        createModule("Riddles in the Dark",
                "Escape the Mines of Moria and the Balrog.",
                "Adventure");
        createModule("The Black Riders",
                "Flee the Nazgûl and reach Rivendell.",
                "Adventure");
        createModule("The Scouring of the Shire",
                "Free the Shire from Saruman's occupation.",
                "Adventure");
        createModule("The Battle of Helm's Deep",
                "Defend the Hornburg against Saruman's army.",
                "Scenario");
        createModule("The Siege of Minas Tirith",
                "Defend the White City against Mordor's forces.",
                "Scenario");
        createModule("Journey to Mount Doom",
                "Reach the Cracks of Doom and destroy the Ring.",
                "Scenario");

        log.info("Initialized module database with {} LOTR modules.", moduleRepository.count());
    }
}
