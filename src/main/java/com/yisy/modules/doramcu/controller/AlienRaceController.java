package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.traveller.AlienRace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for alien races from Classic Traveller S03: Spinward Marches.
 * <p>
 * Provides endpoints for alien race information, modifiers, and character race selection.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/alien-races}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/alien-races")
public class AlienRaceController {

    private static final Logger log = LoggerFactory.getLogger(AlienRaceController.class);

    /**
     * Returns all alien races with their traits and modifiers.
     *
     * @return list of all alien races
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAlienRaces() {
        log.debug("GET /api/v1/traveller/alien-races");
        List<Map<String, Object>> races = new ArrayList<>();
        for (AlienRace race : AlienRace.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", race.name());
            map.put("displayName", race.getDisplayName());
            map.put("description", race.getDescription());
            map.put("combatDM", race.getCombatDM());
            map.put("socialDM", race.getSocialDM());
            map.put("navigationDM", race.getNavigationDM());
            map.put("armorBonus", race.getArmorBonus());
            map.put("tradeBonusPercent", race.getTradeBonusPercent());
            races.add(map);
        }
        return ResponseEntity.ok(races);
    }

    /**
     * Returns a specific alien race by name.
     *
     * @param name the race name (case-insensitive)
     * @return the alien race details or 404
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getAlienRace(@PathVariable String name) {
        log.debug("GET /api/v1/traveller/alien-races/{}", name);
        AlienRace race;
        try {
            race = AlienRace.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", race.name());
        map.put("displayName", race.getDisplayName());
        map.put("description", race.getDescription());
        map.put("combatDM", race.getCombatDM());
        map.put("socialDM", race.getSocialDM());
        map.put("navigationDM", race.getNavigationDM());
        map.put("armorBonus", race.getArmorBonus());
        map.put("tradeBonusPercent", race.getTradeBonusPercent());

        // Add trait descriptions
        List<String> traits = new ArrayList<>();
        if (race.getCombatDM() > 0) {
            traits.add("+" + race.getCombatDM() + " combat die modifier");
        } else if (race.getCombatDM() < 0) {
            traits.add(race.getCombatDM() + " combat die modifier");
        }
        if (race.getSocialDM() > 0) {
            traits.add("+" + race.getSocialDM() + " social die modifier");
        } else if (race.getSocialDM() < 0) {
            traits.add(race.getSocialDM() + " social die modifier");
        }
        if (race.getNavigationDM() > 0) {
            traits.add("+" + race.getNavigationDM() + " navigation die modifier");
        }
        if (race.getArmorBonus() > 0) {
            traits.add("+" + race.getArmorBonus() + " armor class bonus");
        }
        if (race.getTradeBonusPercent() > 0) {
            traits.add("+" + race.getTradeBonusPercent() + "% trade profit bonus");
        }
        map.put("traits", traits);
        return ResponseEntity.ok(map);
    }

    /**
     * Returns the combat modifier for a specific race.
     *
     * @param name the race name (case-insensitive)
     * @return the combat die modifier
     */
    @GetMapping("/{name}/combat-dm")
    public ResponseEntity<Map<String, Object>> getCombatDM(@PathVariable String name) {
        log.debug("GET /api/v1/traveller/alien-races/{}/combat-dm", name);
        AlienRace race;
        try {
            race = AlienRace.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("race", race.getDisplayName());
        result.put("combatDM", race.getCombatDM());
        return ResponseEntity.ok(result);
    }

    /**
     * Returns the trade modifier for a specific race.
     *
     * @param name the race name (case-insensitive)
     * @return the trade bonus percentage
     */
    @GetMapping("/{name}/trade-dm")
    public ResponseEntity<Map<String, Object>> getTradeDM(@PathVariable String name) {
        log.debug("GET /api/v1/traveller/alien-races/{}/trade-dm", name);
        AlienRace race;
        try {
            race = AlienRace.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("race", race.getDisplayName());
        result.put("tradeBonusPercent", race.getTradeBonusPercent());
        return ResponseEntity.ok(result);
    }

    /**
     * Compares two alien races side by side.
     *
     * @param name1 the first race name
     * @param name2 the second race name
     * @return comparison of the two races
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareRaces(
            @RequestParam String name1, @RequestParam String name2) {
        log.debug("GET /api/v1/traveller/alien-races/compare?name1={}&name2={}", name1, name2);

        AlienRace race1, race2;
        try {
            race1 = AlienRace.valueOf(name1.toUpperCase());
            race2 = AlienRace.valueOf(name2.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("race1", raceToSummary(race1));
        comparison.put("race2", raceToSummary(race2));

        // Determine advantages
        List<String> race1Advantages = new ArrayList<>();
        List<String> race2Advantages = new ArrayList<>();

        if (race1.getCombatDM() > race2.getCombatDM()) {
            race1Advantages.add("Better combat DM");
        } else if (race2.getCombatDM() > race1.getCombatDM()) {
            race2Advantages.add("Better combat DM");
        }
        if (race1.getSocialDM() > race2.getSocialDM()) {
            race1Advantages.add("Better social DM");
        } else if (race2.getSocialDM() > race1.getSocialDM()) {
            race2Advantages.add("Better social DM");
        }
        if (race1.getNavigationDM() > race2.getNavigationDM()) {
            race1Advantages.add("Better navigation DM");
        } else if (race2.getNavigationDM() > race1.getNavigationDM()) {
            race2Advantages.add("Better navigation DM");
        }
        if (race1.getArmorBonus() > race2.getArmorBonus()) {
            race1Advantages.add("Better armor bonus");
        } else if (race2.getArmorBonus() > race1.getArmorBonus()) {
            race2Advantages.add("Better armor bonus");
        }
        if (race1.getTradeBonusPercent() > race2.getTradeBonusPercent()) {
            race1Advantages.add("Better trade bonus");
        } else if (race2.getTradeBonusPercent() > race1.getTradeBonusPercent()) {
            race2Advantages.add("Better trade bonus");
        }

        comparison.put("race1Advantages", race1Advantages);
        comparison.put("race2Advantages", race2Advantages);
        return ResponseEntity.ok(comparison);
    }

    private Map<String, Object> raceToSummary(AlienRace race) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", race.name());
        map.put("displayName", race.getDisplayName());
        map.put("combatDM", race.getCombatDM());
        map.put("socialDM", race.getSocialDM());
        map.put("navigationDM", race.getNavigationDM());
        map.put("armorBonus", race.getArmorBonus());
        map.put("tradeBonusPercent", race.getTradeBonusPercent());
        return map;
    }
}
