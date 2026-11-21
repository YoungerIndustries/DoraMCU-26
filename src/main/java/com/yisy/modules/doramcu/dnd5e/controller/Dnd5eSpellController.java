package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsionicPower;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpellService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for D&D 5e Spell-related endpoints.
 * Covers: Spells, Psion, Power Points, Psionic Surge, Psionic Powers.
 */
@RestController
@RequestMapping("/api/v1/dnd5e")
public class Dnd5eSpellController {

    private final Dnd5eSpellService spellService;
    private final Dnd5ePsionService psionService;

    public Dnd5eSpellController(Dnd5eSpellService spellService,
                                 Dnd5ePsionService psionService) {
        this.spellService = spellService;
        this.psionService = psionService;
    }

    // ==================== Spells ====================

    @GetMapping("/spells")
    public List<Dnd5eSpell> getAllSpells() {
        return spellService.getAllSpells();
    }

    @GetMapping("/spells/level/{level}")
    public List<Dnd5eSpell> getSpellsByLevel(@PathVariable int level) {
        return spellService.getSpellsByLevel(level);
    }

    @GetMapping("/spells/school/{school}")
    public List<Dnd5eSpell> getSpellsBySchool(@PathVariable String school) {
        return spellService.getSpellsBySchool(school);
    }

    @GetMapping("/spells/search")
    public List<Dnd5eSpell> searchSpells(@RequestParam String keyword) {
        return spellService.searchSpells(keyword);
    }

    // ==================== Psion ====================

    @PostMapping("/characters/{characterId}/psion")
    public Dnd5ePsion createPsion(@PathVariable Long characterId,
                                   @RequestBody Map<String, String> body) {
        String subclass = body.getOrDefault("subclass", "Psi Warper");
        return psionService.getOrCreatePsion(characterId, subclass);
    }

    @GetMapping("/characters/{characterId}/psion")
    public Dnd5ePsion getPsion(@PathVariable Long characterId) {
        return psionService.getPsion(characterId);
    }

    @GetMapping("/psions")
    public List<Dnd5ePsion> listAllPsions() {
        return psionService.listAllPsions();
    }

    @GetMapping("/psions/subclass/{subclass}")
    public List<Dnd5ePsion> listPsionsBySubclass(@PathVariable String subclass) {
        return psionService.listPsionsBySubclass(subclass);
    }

    @PatchMapping("/characters/{characterId}/psion/level")
    public Dnd5ePsion updatePsionLevel(@PathVariable Long characterId,
                                        @RequestBody Map<String, Integer> body) {
        int level = body.getOrDefault("level", 1);
        return psionService.updatePsionLevel(characterId, level);
    }

    @PatchMapping("/characters/{characterId}/psion/ability")
    public Dnd5ePsion setPrimaryAbility(@PathVariable Long characterId,
                                         @RequestBody Map<String, String> body) {
        String ability = body.getOrDefault("ability", "INT");
        return psionService.setPrimaryAbility(characterId, ability);
    }

    // ==================== Power Points ====================

    @PostMapping("/characters/{characterId}/psion/spend")
    public Dnd5ePsionService.PsionicPowerResult spendPowerPoints(@PathVariable Long characterId,
                                                @RequestBody Map<String, Object> body) {
        String powerName = (String) body.get("powerName");
        int cost = (int) body.get("cost");
        return psionService.spendPowerPoints(characterId, powerName, cost);
    }

    @PostMapping("/characters/{characterId}/psion/restore-power-points")
    public Dnd5ePsion restorePowerPoints(@PathVariable Long characterId) {
        return psionService.restorePowerPoints(characterId);
    }

    @PatchMapping("/characters/{characterId}/psion/power-points")
    public Dnd5ePsion addPowerPoints(@PathVariable Long characterId,
                                      @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 0);
        return psionService.addPowerPoints(characterId, amount);
    }

    // ==================== Psionic Surge ====================

    @PostMapping("/characters/{characterId}/psion/surge/activate")
    public Dnd5ePsion activateSurge(@PathVariable Long characterId) {
        return psionService.activateSurge(characterId);
    }

    @PostMapping("/characters/{characterId}/psion/surge/deactivate")
    public Dnd5ePsion deactivateSurge(@PathVariable Long characterId) {
        return psionService.deactivateSurge(characterId);
    }

    @PostMapping("/characters/{characterId}/psion/restore-surge")
    public Dnd5ePsion restoreSurgePoints(@PathVariable Long characterId) {
        return psionService.restoreSurgePoints(characterId);
    }

    // ==================== Psionic Powers ====================

    @GetMapping("/psionic-powers")
    public List<Dnd5ePsionicPower> listAllPowers() {
        return psionService.listAllPowers();
    }

    @GetMapping("/psionic-powers/{name}")
    public Dnd5ePsionicPower getPowerByName(@PathVariable String name) {
        return psionService.getPowerByName(name);
    }

    @GetMapping("/psionic-powers/level/{level}")
    public List<Dnd5ePsionicPower> listPowersByLevel(@PathVariable int level) {
        return psionService.listPowersByLevel(level);
    }

    @GetMapping("/psionic-powers/subclass/{subclass}")
    public List<Dnd5ePsionicPower> listPowersBySubclass(@PathVariable String subclass) {
        return psionService.listPowersBySubclass(subclass);
    }

    @GetMapping("/psionic-powers/cantrips")
    public List<Dnd5ePsionicPower> listCantrips() {
        return psionService.listCantrips();
    }

    @GetMapping("/psionic-powers/school/{school}")
    public List<Dnd5ePsionicPower> listPowersBySchool(@PathVariable String school) {
        return psionService.listPowersBySchool(school);
    }

    @GetMapping("/psionic-powers/search")
    public List<Dnd5ePsionicPower> searchPowers(@RequestParam String keyword) {
        return psionService.searchPowers(keyword);
    }

    @PostMapping("/psionic-powers")
    public Dnd5ePsionicPower createPower(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int level = (int) body.getOrDefault("level", 0);
        int cost = (int) body.getOrDefault("cost", 0);
        String castingTime = (String) body.getOrDefault("castingTime", "1 action");
        String duration = (String) body.getOrDefault("duration", "Instantaneous");
        String range = (String) body.getOrDefault("range", "Self");
        boolean concentration = (boolean) body.getOrDefault("concentration", false);
        boolean ritual = (boolean) body.getOrDefault("ritual", false);
        String subclass = (String) body.getOrDefault("subclass", "All");
        String school = (String) body.getOrDefault("school", "Evocation");
        String description = (String) body.getOrDefault("description", "");
        return psionService.createPower(name, level, cost, castingTime, duration, range,
                concentration, ritual, subclass, school, description);
    }

    @DeleteMapping("/psionic-powers/{name}")
    public void deletePower(@PathVariable String name) {
        psionService.deletePower(name);
    }

    @PostMapping("/characters/{characterId}/psion/use-power")
    public Dnd5ePsionService.PsionicPowerResult usePower(@PathVariable Long characterId,
                                        @RequestBody Map<String, Object> body) {
        String powerName = (String) body.get("powerName");
        boolean useSurge = (boolean) body.getOrDefault("useSurge", false);
        return psionService.usePower(characterId, powerName, useSurge);
    }

    @PostMapping("/characters/{characterId}/psion/long-rest")
    public Dnd5ePsion psionLongRest(@PathVariable Long characterId) {
        return psionService.longRest(characterId);
    }
}
