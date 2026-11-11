package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5Career;
import com.yisy.modules.doramcu.babylon5.model.B5Characteristics;
import com.yisy.modules.doramcu.babylon5.model.B5Race;
import com.yisy.modules.doramcu.babylon5.model.B5Skill;
import com.yisy.modules.doramcu.babylon5.model.B5WealthSystem;
import com.yisy.modules.doramcu.fourx.FourxCharacterService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * B5 implementation of the Character Creation 4X pillar.
 * <p>
 * Provides character creation with B5 races, careers (Earth Force, Narn military,
 * Centauri nobility, etc.), and B5-themed skills. Characters are stored in memory
 * with race modifiers applied to base attributes.
 * </p>
 */
@Service
public class B5CharacterService implements FourxCharacterService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(B5CharacterService.class);

    private static final int BASE_ATTRIBUTE = 8;

    private final Babylon5DatabaseService databaseService;
    private final Map<String, Map<String, Object>> characters = new ConcurrentHashMap<>();
    private final AtomicLong characterIdCounter = new AtomicLong(1);

    public B5CharacterService(Babylon5DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostConstruct
    public void init() {
        log.info("Initialized {} B5 careers from enum", B5Career.values().length);
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.BABYLON5;
    }

    @Override
    public Map<String, Object> createCharacter(String name, String race) {
        Map<String, Object> character = new LinkedHashMap<>();
        long id = characterIdCounter.getAndIncrement();
        character.put("id", String.valueOf(id));
        character.put("name", name);
        character.put("race", race);
        character.put("career", null);
        character.put("careerTerms", 0);
        character.put("experience", 0);

        // Apply race modifiers
        Map<String, Integer> attributes = new LinkedHashMap<>();
        B5Race b5Race = databaseService.getRace(race).orElse(null);
        if (b5Race != null && b5Race.getCharacteristics() != null) {
            B5Characteristics chars = b5Race.getCharacteristics();
            attributes.put("ST", BASE_ATTRIBUTE + chars.getST());
            attributes.put("DX", BASE_ATTRIBUTE + chars.getDX());
            attributes.put("EN", BASE_ATTRIBUTE + chars.getEN());
            attributes.put("IN", BASE_ATTRIBUTE + chars.getIN());
            attributes.put("ED", BASE_ATTRIBUTE + chars.getED());
            attributes.put("SO", BASE_ATTRIBUTE + chars.getSO());
        } else {
            // Default human attributes
            attributes.put("ST", BASE_ATTRIBUTE);
            attributes.put("DX", BASE_ATTRIBUTE);
            attributes.put("EN", BASE_ATTRIBUTE);
            attributes.put("IN", BASE_ATTRIBUTE);
            attributes.put("ED", BASE_ATTRIBUTE);
            attributes.put("SO", BASE_ATTRIBUTE);
        }
        character.put("attributes", attributes);
        character.put("skills", new ArrayList<String>());
        character.put("lifepath", new ArrayList<Map<String, Object>>());

        characters.put(String.valueOf(id), character);
        log.info("Created B5 character: {} ({})", name, race);
        return character;
    }

    @Override
    public Map<String, Object> createCharacterWithLifepath(String name, String race, String career) {
        Map<String, Object> character = createCharacter(name, race);

        // Apply career
        B5Career careerData = getCareerByName(career);
        if (careerData != null) {
            character.put("career", career);
            applyCareerToCharacter(character, careerData);
        }

        return character;
    }

    @Override
    public List<String> getAvailableRaces() {
        return databaseService.getAllRaces().stream()
                .map(B5Race::getId)
                .toList();
    }

    @Override
    public List<String> getAvailableCareers() {
        return Arrays.stream(B5Career.values())
                .map(Enum::name)
                .toList();
    }

    @Override
    public List<String> getAvailableSkills(String career) {
        B5Career careerData = getCareerByName(career);
        if (careerData != null) {
            return careerData.getSkillTable().stream()
                    .map(B5Skill::getName)
                    .toList();
        }
        return List.of();
    }

    @Override
    public Map<String, Integer> getRaceModifiers(String race) {
        Map<String, Integer> modifiers = new HashMap<>();
        B5Race b5Race = databaseService.getRace(race).orElse(null);
        if (b5Race != null && b5Race.getCharacteristics() != null) {
            B5Characteristics chars = b5Race.getCharacteristics();
            modifiers.put("ST", chars.getST());
            modifiers.put("DX", chars.getDX());
            modifiers.put("EN", chars.getEN());
            modifiers.put("IN", chars.getIN());
            modifiers.put("ED", chars.getED());
            modifiers.put("SO", chars.getSO());
        }
        return modifiers;
    }

    @Override
    public List<Map<String, Object>> getCareerSkills(String career) {
        B5Career careerData = getCareerByName(career);
        if (careerData == null) return List.of();

        List<Map<String, Object>> result = new ArrayList<>();
        for (B5Skill skill : careerData.getSkillTable()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("skill", skill.getName());
            entry.put("category", skill.getCategory().getName());
            entry.put("proficiency", 1);
            result.add(entry);
        }
        return result;
    }

    @Override
    public Map<String, Object> advanceCareer(Map<String, Object> character, String career) {
        B5Career careerData = getCareerByName(career);
        if (careerData == null) return character;

        // Apply attribute bonuses
        @SuppressWarnings("unchecked")
        Map<String, Integer> attributes = (Map<String, Integer>) character.get("attributes");
        if (attributes != null && careerData.getAttributeBonuses() != null) {
            for (String bonus : careerData.getAttributeBonuses()) {
                String[] parts = bonus.split("\\+");
                if (parts.length == 2) {
                    String attr = parts[0].trim();
                    int value = Integer.parseInt(parts[1].trim());
                    attributes.merge(attr, value, Integer::sum);
                }
            }
        }

        // Add random skill from career
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) character.get("skills");
        if (skills != null && !careerData.getSkillTable().isEmpty()) {
            Random rand = new Random();
            B5Skill newSkill = careerData.getSkillTable().get(rand.nextInt(careerData.getSkillTable().size()));
            if (!skills.contains(newSkill.getName())) {
                skills.add(newSkill.getName());
            }
        }

        // Roll wealth for this term
        B5WealthSystem.WealthResult wealth = B5WealthSystem.rollWealth(careerData);
        int currentCredits = (int) character.getOrDefault("credits", 0);
        character.put("credits", currentCredits + wealth.getCredits());
        character.put("retirementBenefit", wealth.getRetirementBenefit().getName());

        // Update career terms and experience
        int terms = (int) character.getOrDefault("careerTerms", 0) + 1;
        character.put("careerTerms", terms);
        character.put("experience", (int) character.getOrDefault("experience", 0) + 100);

        // Add lifepath entry
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lifepath = (List<Map<String, Object>>) character.get("lifepath");
        if (lifepath != null) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("term", terms);
            entry.put("career", career);
            entry.put("wealth", wealth.getCredits());
            entry.put("skillsGained", List.of());
            lifepath.add(entry);
        }

        characters.put((String) character.get("id"), character);
        return character;
    }

    @Override
    public List<Map<String, Object>> getAllCharacters() {
        return new ArrayList<>(characters.values());
    }

    private B5Career getCareerByName(String careerName) {
        try {
            return B5Career.valueOf(careerName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void applyCareerToCharacter(Map<String, Object> character, B5Career career) {
        // Apply attribute bonuses
        @SuppressWarnings("unchecked")
        Map<String, Integer> attributes = (Map<String, Integer>) character.get("attributes");
        if (attributes != null && career.getAttributeBonuses() != null) {
            for (String bonus : career.getAttributeBonuses()) {
                String[] parts = bonus.split("\\+");
                if (parts.length == 2) {
                    String attr = parts[0].trim();
                    int value = Integer.parseInt(parts[1].trim());
                    attributes.merge(attr, value, Integer::sum);
                }
            }
        }

        // Add career skills
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) character.get("skills");
        if (skills != null) {
            for (B5Skill skill : career.getSkillTable()) {
                skills.add(skill.getName());
            }
        }

        character.put("careerTerms", 1);
        character.put("experience", 100);
    }
}
