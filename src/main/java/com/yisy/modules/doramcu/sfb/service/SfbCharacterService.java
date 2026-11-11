package com.yisy.modules.doramcu.sfb.service;

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
 * SFB implementation of the Character Creation 4X pillar.
 * <p>
 * Provides character creation with SFB races (Federation, Klingon, Romulan, etc.),
 * careers (Starfleet Academy, Klingon Defense Force, etc.), and SFB-themed skills.
 * Characters are stored in memory with race modifiers applied to base attributes.
 * </p>
 */
@Service
public class SfbCharacterService implements FourxCharacterService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(SfbCharacterService.class);

    private static final int BASE_ATTRIBUTE = 8;

    /** SFB races with their attribute modifiers. */
    private final Map<String, Map<String, Integer>> raceModifiers = new HashMap<>();

    /** SFB careers with skills and attribute bonuses. */
    private final List<Map<String, Object>> careers = new ArrayList<>();

    /** Created characters stored in memory. */
    private final Map<String, Map<String, Object>> characters = new ConcurrentHashMap<>();

    private final AtomicLong characterIdCounter = new AtomicLong(1);

    @PostConstruct
    public void initRacesAndCareers() {
        // --- SFB Races ---
        raceModifiers.put("FEDERATION", Map.of(
                "ST", 0, "DX", 0, "EN", 0, "IN", 1, "ED", 1, "SO", 1
        ));

        raceModifiers.put("KLINGON", Map.of(
                "ST", 2, "DX", 1, "EN", 1, "IN", -1, "ED", 0, "SO", -1
        ));

        raceModifiers.put("ROMULAN", Map.of(
                "ST", 0, "DX", 1, "EN", 0, "IN", 2, "ED", 0, "SO", 0
        ));

        raceModifiers.put("GORN", Map.of(
                "ST", 3, "DX", 0, "EN", 2, "IN", -2, "ED", -1, "SO", -1
        ));

        raceModifiers.put("THOLIAN", Map.of(
                "ST", 0, "DX", 1, "EN", 0, "IN", 2, "ED", 0, "SO", 0
        ));

        raceModifiers.put("M'HAARI", Map.of(
                "ST", 1, "DX", 1, "EN", 0, "IN", 0, "ED", 1, "SO", 1
        ));

        raceModifiers.put("SULIBAN", Map.of(
                "ST", 0, "DX", 2, "EN", 0, "IN", 1, "ED", 0, "SO", 0
        ));

        raceModifiers.put("TALOSIAN", Map.of(
                "ST", 1, "DX", 0, "EN", 1, "IN", 2, "ED", 0, "SO", 0
        ));

        raceModifiers.put("PEOPLIAN", Map.of(
                "ST", 0, "DX", 0, "EN", 0, "IN", 1, "ED", 1, "SO", 2
        ));

        raceModifiers.put("ANDROMEDAN", Map.of(
                "ST", 1, "DX", 1, "EN", 1, "IN", 0, "ED", 0, "SO", 0
        ));

        // --- SFB Careers ---
        careers.add(createCareer(
                "STARFLEET_ACADEMY", "Starfleet Academy", "Federation",
                List.of("IN+1", "ED+1"),
                List.of("Navigation", "Science", "Diplomacy", "Engineering", "Medicine")
        ));

        careers.add(createCareer(
                "STARFLEET_COMMAND", "Starfleet Command", "Federation",
                List.of("SO+1", "IN+1"),
                List.of("Leadership", "Tactics", "Diplomacy", "Administration", "Navigation")
        ));

        careers.add(createCareer(
                "STARFLEET_SCIENCE", "Starfleet Science", "Federation",
                List.of("IN+2"),
                List.of("Science", "Engineering", "Medicine", "Navigation", "Diplomacy")
        ));

        careers.add(createCareer(
                "KLINGON_DEFENSE_FORCE", "Klingon Defense Force", "Klingon",
                List.of("ST+1", "DX+1"),
                List.of("Combat", "Tactics", "Pilot", "Weapons", "Survival")
        ));

        careers.add(createCareer(
                "KLINGON_HONOR_GUARD", "Klingon Honor Guard", "Klingon",
                List.of("ST+2", "EN+1"),
                List.of("Combat", "Leadership", "Tactics", "Survival", "Pilot")
        ));

        careers.add(createCareer(
                "ROMULAN_SENTINEL", "Romulan Sentinel", "Romulan",
                List.of("DX+1", "IN+1"),
                List.of("Stealth", "Intelligence", "Pilot", "Weapons", "Navigation")
        ));

        careers.add(createCareer(
                "ROMULAN_SYNDICATE", "Romulan Syndicate", "Romulan",
                List.of("SO+1", "IN+1"),
                List.of("Diplomacy", "Intelligence", "Commerce", "Stealth", "Administration")
        ));

        careers.add(createCareer(
                "INDEPENDENT_MERCHANT", "Independent Merchant", "Independent",
                List.of("SO+1", "ED+1"),
                List.of("Commerce", "Navigation", "Pilot", "Diplomacy", "Mechanic")
        ));

        careers.add(createCareer(
                "PIRATE", "Pirate", "Independent",
                List.of("DX+1", "ST+1"),
                List.of("Combat", "Pilot", "Weapons", "Navigation", "Survival")
        ));

        careers.add(createCareer(
                "EXPLORER", "Explorer", "Independent",
                List.of("IN+1", "DX+1"),
                List.of("Science", "Navigation", "Survival", "Pilot", "Medicine")
        ));

        log.info("Initialized {} SFB races and {} SFB careers", raceModifiers.size(), careers.size());
    }

    private Map<String, Object> createCareer(String id, String name, String faction,
                                              List<String> attributeBonuses, List<String> skills) {
        Map<String, Object> career = new LinkedHashMap<>();
        career.put("id", id);
        career.put("name", name);
        career.put("faction", faction);
        career.put("attributeBonuses", attributeBonuses);
        career.put("skills", skills);
        return career;
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.CUSTOM;
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
        Map<String, Integer> modifiers = raceModifiers.getOrDefault(race.toUpperCase(), Map.of());
        attributes.put("ST", BASE_ATTRIBUTE + modifiers.getOrDefault("ST", 0));
        attributes.put("DX", BASE_ATTRIBUTE + modifiers.getOrDefault("DX", 0));
        attributes.put("EN", BASE_ATTRIBUTE + modifiers.getOrDefault("EN", 0));
        attributes.put("IN", BASE_ATTRIBUTE + modifiers.getOrDefault("IN", 0));
        attributes.put("ED", BASE_ATTRIBUTE + modifiers.getOrDefault("ED", 0));
        attributes.put("SO", BASE_ATTRIBUTE + modifiers.getOrDefault("SO", 0));
        character.put("attributes", attributes);
        character.put("skills", new ArrayList<String>());
        character.put("lifepath", new ArrayList<Map<String, Object>>());

        characters.put(String.valueOf(id), character);
        log.info("Created SFB character: {} ({})", name, race);
        return character;
    }

    @Override
    public Map<String, Object> createCharacterWithLifepath(String name, String race, String career) {
        Map<String, Object> character = createCharacter(name, race);

        // Apply career
        Map<String, Object> careerData = getCareerById(career);
        if (careerData != null) {
            character.put("career", career);
            applyCareerToCharacter(character, careerData);
        }

        return character;
    }

    @Override
    public List<String> getAvailableRaces() {
        return new ArrayList<>(raceModifiers.keySet());
    }

    @Override
    public List<String> getAvailableCareers() {
        return careers.stream()
                .map(c -> (String) c.get("id"))
                .toList();
    }

    @Override
    public List<String> getAvailableSkills(String career) {
        Map<String, Object> careerData = getCareerById(career);
        if (careerData != null) {
            @SuppressWarnings("unchecked")
            List<String> skills = (List<String>) careerData.get("skills");
            return skills;
        }
        return List.of();
    }

    @Override
    public Map<String, Integer> getRaceModifiers(String race) {
        return raceModifiers.getOrDefault(race.toUpperCase(), Map.of());
    }

    @Override
    public List<Map<String, Object>> getCareerSkills(String career) {
        Map<String, Object> careerData = getCareerById(career);
        if (careerData == null) return List.of();

        List<Map<String, Object>> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) careerData.get("skills");
        for (String skill : skills) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("skill", skill);
            entry.put("proficiency", 1);
            result.add(entry);
        }
        return result;
    }

    @Override
    public Map<String, Object> advanceCareer(Map<String, Object> character, String career) {
        Map<String, Object> careerData = getCareerById(career);
        if (careerData == null) return character;

        // Apply attribute bonuses
        @SuppressWarnings("unchecked")
        Map<String, Integer> attributes = (Map<String, Integer>) character.get("attributes");
        if (attributes != null) {
            @SuppressWarnings("unchecked")
            List<String> bonuses = (List<String>) careerData.get("attributeBonuses");
            if (bonuses != null) {
                for (String bonus : bonuses) {
                    String[] parts = bonus.split("\\+");
                    if (parts.length == 2) {
                        String attr = parts[0].trim();
                        int value = Integer.parseInt(parts[1].trim());
                        attributes.merge(attr, value, Integer::sum);
                    }
                }
            }
        }

        // Add random skill from career
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) character.get("skills");
        if (skills != null) {
            @SuppressWarnings("unchecked")
            List<String> careerSkills = (List<String>) careerData.get("skills");
            if (careerSkills != null && !careerSkills.isEmpty()) {
                Random rand = new Random();
                String newSkill = careerSkills.get(rand.nextInt(careerSkills.size()));
                if (!skills.contains(newSkill)) {
                    skills.add(newSkill);
                }
            }
        }

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

    private Map<String, Object> getCareerById(String careerId) {
        return careers.stream()
                .filter(c -> c.get("id").toString().equalsIgnoreCase(careerId))
                .findFirst()
                .orElse(null);
    }

    private void applyCareerToCharacter(Map<String, Object> character, Map<String, Object> career) {
        // Apply attribute bonuses
        @SuppressWarnings("unchecked")
        Map<String, Integer> attributes = (Map<String, Integer>) character.get("attributes");
        if (attributes != null) {
            @SuppressWarnings("unchecked")
            List<String> bonuses = (List<String>) career.get("attributeBonuses");
            if (bonuses != null) {
                for (String bonus : bonuses) {
                    String[] parts = bonus.split("\\+");
                    if (parts.length == 2) {
                        String attr = parts[0].trim();
                        int value = Integer.parseInt(parts[1].trim());
                        attributes.merge(attr, value, Integer::sum);
                    }
                }
            }
        }

        // Add career skills
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) character.get("skills");
        if (skills != null) {
            @SuppressWarnings("unchecked")
            List<String> careerSkills = (List<String>) career.get("skills");
            if (careerSkills != null) {
                skills.addAll(careerSkills);
            }
        }

        character.put("careerTerms", 1);
        character.put("experience", 100);
    }
}
