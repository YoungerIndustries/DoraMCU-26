package com.yisy.modules.doramcu.controller.players;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import com.yisy.modules.doramcu.shared.model.SharedCharacter;
import com.yisy.modules.doramcu.shared.service.SharedCharacterService;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.service.character.CharacterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified characters controller that aggregates characters from all available
 * game systems and exposes them through a single endpoint for the player
 * dashboard.
 *
 * Directly injects game service beans instead of making HTTP calls, since
 * all code runs in the same JVM when consolidated into DoraMCU.
 */
@RestController
@RequestMapping("/api/v1/characters")
public class UnifiedCharactersController {

    private static final Logger log = LoggerFactory.getLogger(UnifiedCharactersController.class);

    private final CharacterService travellerCharacterService;
    private final SharedCharacterService sharedCharacterService;
    private final Earth2026Service earth2026Service;

    public UnifiedCharactersController(CharacterService travellerCharacterService, SharedCharacterService sharedCharacterService, Earth2026Service earth2026Service) {
        this.travellerCharacterService = travellerCharacterService;
        this.sharedCharacterService = sharedCharacterService;
        this.earth2026Service = earth2026Service;
    }

    /**
     * Get all characters from all game systems.
     *
     * @param source optional filter by game system (e.g., traveller, shared)
     * @return list of unified character objects
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCharacters(
            @RequestParam(required = false) String source) {

        List<Map<String, Object>> allCharacters = new ArrayList<>();

        // Fetch Traveller characters
        if (source == null || "traveller".equals(source)) {
            try {
                List<TravellerCharacter> travellerChars = travellerCharacterService.listCharacters();
                for (TravellerCharacter tc : travellerChars) {
                    allCharacters.add(unifyTravellerCharacter(tc));
                }
            } catch (Exception e) {
                log.debug("No Traveller characters available: {}", e.getMessage());
            }
        }

        // Fetch shared characters
        if (source == null || "shared".equals(source)) {
            try {
                List<SharedCharacter> sharedChars = sharedCharacterService.listSharedCharacters();
                for (SharedCharacter sc : sharedChars) {
                    allCharacters.add(unifySharedCharacter(sc));
                }
            } catch (Exception e) {
                log.debug("No shared characters available: {}", e.getMessage());
            }
        }

        // Fetch Earth 2026 characters
        if (source == null || "earth2026".equals(source) || "earth".equals(source)) {
            try {
                List<Earth2026Character> earthChars = earth2026Service.getCharactersOnEarth();
                for (Earth2026Character ec : earthChars) {
                    allCharacters.add(unifyEarth2026Character(ec));
                }
            } catch (Exception e) {
                log.debug("No Earth 2026 characters available: {}", e.getMessage());
            }
        }

        log.debug("Aggregated {} characters from all game systems", allCharacters.size());
        return ResponseEntity.ok(allCharacters);
    }

    /**
     * Get a specific character by ID.
     */
    @GetMapping("/{characterId}")
    public ResponseEntity<Map<String, Object>> getCharacter(@PathVariable String characterId) {
        // Try Traveller first (integer ID)
        try {
            int id = Integer.parseInt(characterId);
            TravellerCharacter tc = travellerCharacterService.getCharacter(id);
            if (tc != null) {
                return ResponseEntity.ok(unifyTravellerCharacter(tc));
            }
        } catch (NumberFormatException e) {
            // Not a Traveller ID format
        }

        // Try shared characters (Long ID)
        try {
            long id = Long.parseLong(characterId);
            SharedCharacter sc = sharedCharacterService.getSharedCharacter(id).orElse(null);
            if (sc != null) {
                return ResponseEntity.ok(unifySharedCharacter(sc));
            }
        } catch (NumberFormatException e) {
            // Not a shared character ID format
        }

        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> unifyTravellerCharacter(TravellerCharacter tc) {
        Map<String, Object> unified = new HashMap<>();
        unified.put("source", "traveller");
        unified.put("id", tc.getId());
        unified.put("name", tc.getName());
        unified.put("description", "Traveller character");
        unified.put("class", tc.getCareerTours().isEmpty() ? null : tc.getCareerTours().get(tc.getCareerTours().size() - 1).getCareer().getName());
        unified.put("level", tc.getAge());
        unified.put("experience", 0);
        unified.put("hitPoints", 0);
        unified.put("armorClass", 0);
        unified.put("strength", tc.getAbility(AbilityScore.END));
        unified.put("dexterity", tc.getAbility(AbilityScore.DEX));
        unified.put("constitution", tc.getAbility(AbilityScore.ST));
        unified.put("intelligence", tc.getAbility(AbilityScore.INT));
        unified.put("wisdom", tc.getAbility(AbilityScore.EDU));
        unified.put("charisma", tc.getAbility(AbilityScore.SOC));
        unified.put("skills", tc.getSkills());
        unified.put("equipment", null);
        unified.put("credits", tc.getWealth());
        unified.put("reputation", 0);
        unified.put("homeWorld", null);
        unified.put("faction", null);
        return unified;
    }

    private Map<String, Object> unifySharedCharacter(SharedCharacter sc) {
        Map<String, Object> unified = new HashMap<>();
        unified.put("source", "shared");
        unified.put("id", sc.getId());
        unified.put("name", sc.getName());
        unified.put("description", sc.getDescription());
        unified.put("class", null);
        unified.put("level", sc.getAge());
        unified.put("experience", 0);
        unified.put("hitPoints", 0);
        unified.put("armorClass", 0);
        unified.put("strength", sc.getStrength());
        unified.put("dexterity", sc.getDexterity());
        unified.put("constitution", sc.getConstitution());
        unified.put("intelligence", sc.getIntelligence());
        unified.put("wisdom", sc.getWisdom());
        unified.put("charisma", sc.getCharisma());
        unified.put("skills", null);
        unified.put("equipment", null);
        unified.put("credits", 0);
        unified.put("reputation", 0);
        unified.put("homeWorld", null);
        unified.put("faction", null);
        return unified;
    }

    private Map<String, Object> unifyEarth2026Character(Earth2026Character ec) {
        Map<String, Object> unified = new HashMap<>();
        unified.put("source", "earth2026");
        unified.put("id", ec.getId());
        unified.put("name", ec.getName());
        unified.put("description", "Earth 2026 character - " + (ec.getEarthLocation() != null ? ec.getEarthLocation() : "San Diego, CA"));
        unified.put("class", ec.getOccupation());
        unified.put("level", ec.getLevel());
        unified.put("experience", ec.getExperiencePoints());
        unified.put("hitPoints", ec.getHitPoints());
        unified.put("armorClass", 0);
        unified.put("strength", ec.getStrScore());
        unified.put("dexterity", ec.getDexScore());
        unified.put("constitution", ec.getConScore());
        unified.put("intelligence", ec.getIntScore());
        unified.put("wisdom", ec.getWisScore());
        unified.put("charisma", ec.getChaScore());
        unified.put("skills", null);
        unified.put("equipment", null);
        unified.put("credits", 0);
        unified.put("reputation", 0);
        unified.put("homeWorld", "Earth");
        unified.put("faction", null);
        return unified;
    }
}
