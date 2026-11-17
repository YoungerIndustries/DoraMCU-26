package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaignTemplate;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCampaignTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Campaign Templates.
 * <p>
 * Campaign templates provide pre-built campaign frameworks with settings,
 * level ranges, and descriptions. They help GMs start new adventures quickly.
 * </p>
 */
@Service
public class LotrCampaignTemplateService {

    private static final Logger log = LoggerFactory.getLogger(LotrCampaignTemplateService.class);

    private final LotrCampaignTemplateRepository campaignTemplateRepository;

    public LotrCampaignTemplateService(LotrCampaignTemplateRepository campaignTemplateRepository) {
        this.campaignTemplateRepository = campaignTemplateRepository;
    }

    /**
     * Get all campaign templates.
     */
    @Transactional(readOnly = true)
    public List<LotrCampaignTemplate> findAll() {
        return campaignTemplateRepository.findAll();
    }

    /**
     * Get all active campaign templates.
     */
    @Transactional(readOnly = true)
    public List<LotrCampaignTemplate> findActiveTemplates() {
        return campaignTemplateRepository.findAll().stream()
                .filter(LotrCampaignTemplate::isActive)
                .toList();
    }

    /**
     * Get campaign template by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrCampaignTemplate> findById(Long id) {
        return campaignTemplateRepository.findById(id);
    }

    /**
     * Find campaign templates by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrCampaignTemplate> findByName(String name) {
        return campaignTemplateRepository.findAll().stream()
                .filter(t -> t.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find campaign templates by setting.
     */
    @Transactional(readOnly = true)
    public List<LotrCampaignTemplate> findBySetting(String setting) {
        return campaignTemplateRepository.findAll().stream()
                .filter(t -> setting.equals(t.getSetting()))
                .toList();
    }

    /**
     * Create a new campaign template.
     */
    @Transactional
    public LotrCampaignTemplate createCampaignTemplate(String name, String description,
                                                        int startingLevel, int maxLevel,
                                                        String setting) {
        LotrCampaignTemplate template = new LotrCampaignTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setStartingLevel(startingLevel);
        template.setMaxLevel(maxLevel);
        template.setSetting(setting);
        template.setActive(true);

        LotrCampaignTemplate saved = campaignTemplateRepository.save(template);
        log.info("Created campaign template: {} ({})", saved.getName(), saved.getSetting());
        return saved;
    }

    /**
     * Update an existing campaign template.
     */
    @Transactional
    public LotrCampaignTemplate updateCampaignTemplate(Long id, String name, String description,
                                                        int startingLevel, int maxLevel,
                                                        String setting) {
        LotrCampaignTemplate template = campaignTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign template not found: " + id));

        template.setName(name);
        template.setDescription(description);
        template.setStartingLevel(startingLevel);
        template.setMaxLevel(maxLevel);
        template.setSetting(setting);

        LotrCampaignTemplate saved = campaignTemplateRepository.save(template);
        log.info("Updated campaign template: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a campaign template.
     */
    @Transactional
    public void deleteCampaignTemplate(Long id) {
        if (!campaignTemplateRepository.existsById(id)) {
            throw new IllegalArgumentException("Campaign template not found: " + id);
        }
        campaignTemplateRepository.deleteById(id);
        log.info("Deleted campaign template: {}", id);
    }

    /**
     * Initialize the campaign template database with LOTR-themed templates.
     */
    @Transactional
    public void initializeCampaignTemplateDatabase() {
        if (!campaignTemplateRepository.findAll().isEmpty()) {
            log.info("Campaign template database already initialized, skipping.");
            return;
        }

        createCampaignTemplate("The Fellowship of the Ring",
                "Form the Fellowship and journey to destroy the One Ring.",
                1, 10, "Third Age");
        createCampaignTemplate("The Two Towers",
                "The Fellowship is broken. War ravages Rohan and Gondor.",
                5, 15, "Third Age");
        createCampaignTemplate("The Return of the King",
                "The final battle for Middle-earth. Aragorn claims his throne.",
                8, 20, "Third Age");
        createCampaignTemplate("The War of the Ring",
                "Command armies in the great war against Sauron.",
                10, 20, "Third Age");
        createCampaignTemplate("The Hobbit",
                "A small adventure in the Misty Mountains with dwarves.",
                1, 5, "Third Age");
        createCampaignTemplate("The Silmarillion",
                "Epic tales from the First Age of Middle-earth.",
                10, 20, "First Age");
        createCampaignTemplate("The Fall of Numenor",
                "The rise and fall of the greatest kingdom of Men.",
                5, 15, "Second Age");
        createCampaignTemplate("The Last Alliance",
                "Elves and Men unite against Sauron in the Second Age.",
                8, 15, "Second Age");

        log.info("Initialized campaign template database with {} LOTR templates.", campaignTemplateRepository.count());
    }
}
