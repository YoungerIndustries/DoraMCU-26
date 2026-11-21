package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackRequest;
import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackResponse;
import com.yisy.modules.doramcu.dnd5e.dto.EncounterAnalysis;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombatant;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eEncounter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonsterAbility;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eEncounterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMonsterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eNpcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for D&D 5e Combat-related endpoints.
 * Covers: Combat, NPCs, Encounters, Monsters, Monster Abilities.
 */
@RestController
@RequestMapping("/api/v1/dnd5e")
public class Dnd5eCombatController {

    private final Dnd5eCombatService combatService;
    private final Dnd5eNpcService npcService;
    private final Dnd5eEncounterService encounterService;
    private final Dnd5eMonsterService monsterService;

    public Dnd5eCombatController(Dnd5eCombatService combatService,
                                  Dnd5eNpcService npcService,
                                  Dnd5eEncounterService encounterService,
                                  Dnd5eMonsterService monsterService) {
        this.combatService = combatService;
        this.npcService = npcService;
        this.encounterService = encounterService;
        this.monsterService = monsterService;
    }

    // ==================== Combat ====================

    @PostMapping("/combat")
    public Dnd5eCombat createCombat(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("encounterName", "Unnamed Encounter");
        return combatService.createCombat(name);
    }

    @GetMapping("/combat/{id}")
    public Dnd5eCombat getCombat(@PathVariable Long id) {
        return combatService.getCombat(id);
    }

    @PostMapping("/combat/{id}/combatant")
    public Dnd5eCombatant addCombatant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int ac = (int) body.getOrDefault("ac", 10);
        int maxHp = (int) body.getOrDefault("maxHp", 10);
        boolean isCharacter = (boolean) body.getOrDefault("isCharacter", false);
        return combatService.addCombatant(id, name, ac, maxHp, isCharacter);
    }

    @PostMapping("/combat/{id}/initiative")
    public Dnd5eCombat rollInitiative(@PathVariable Long id, @RequestBody(required = false) Map<String, Integer> dexModifiers) {
        return combatService.rollInitiative(id, dexModifiers);
    }

    @PostMapping("/combat/{id}/attack")
    public Dnd5eAttackResponse makeAttack(@PathVariable Long id, @RequestBody Dnd5eAttackRequest request) {
        return combatService.makeAttack(id, request);
    }

    @PostMapping("/combat/{id}/heal")
    public Dnd5eCombatant healCombatant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int amount = (int) body.get("amount");
        return combatService.healCombatant(id, name, amount);
    }

    @PostMapping("/combat/{id}/condition")
    public Dnd5eCombatant applyCondition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String condition = body.get("condition");
        return combatService.applyCondition(id, name, condition);
    }

    @DeleteMapping("/combat/{id}/condition")
    public Dnd5eCombatant removeCondition(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String condition = body.get("condition");
        return combatService.removeCondition(id, name, condition);
    }

    @PostMapping("/combat/{id}/end-round")
    public Dnd5eCombat endRound(@PathVariable Long id) {
        return combatService.endRound(id);
    }

    @PostMapping("/combat/{id}/end")
    public Dnd5eCombat endCombat(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "ENDED");
        return combatService.endCombat(id, status);
    }

    // ==================== NPCs ====================

    @PostMapping("/npcs")
    public Dnd5eNpc createNpc(@RequestBody Dnd5eNpc npc) {
        return npcService.createNpc(npc);
    }

    @GetMapping("/npcs")
    public List<Dnd5eNpc> listNpcs() {
        return npcService.listNpcs();
    }

    @GetMapping("/npcs/{id}")
    public ResponseEntity<Dnd5eNpc> getNpc(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(npcService.getNpc(id).orElseThrow());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/npcs/type/{npcType}")
    public List<Dnd5eNpc> listNpcsByType(@PathVariable String npcType) {
        return npcService.listNpcsByType(npcType);
    }

    @GetMapping("/npcs/disposition/{disposition}")
    public List<Dnd5eNpc> listNpcsByDisposition(@PathVariable String disposition) {
        return npcService.listNpcsByDisposition(disposition);
    }

    @GetMapping("/npcs/class/{npcClass}")
    public List<Dnd5eNpc> listNpcsByClass(@PathVariable String npcClass) {
        return npcService.listNpcsByClass(npcClass);
    }

    @GetMapping("/npcs/race/{race}")
    public List<Dnd5eNpc> listNpcsByRace(@PathVariable String race) {
        return npcService.listNpcsByRace(race);
    }

    @GetMapping("/npcs/cr-range")
    public List<Dnd5eNpc> listNpcsByCrRange(@RequestParam int minCr, @RequestParam int maxCr) {
        return npcService.listNpcsByCrRange(minCr, maxCr);
    }

    @GetMapping("/npcs/villains")
    public List<Dnd5eNpc> listNpcVillains() {
        return npcService.listVillains();
    }

    @GetMapping("/npcs/allies")
    public List<Dnd5eNpc> listNpcAllies() {
        return npcService.listAllies();
    }

    @GetMapping("/npcs/search")
    public List<Dnd5eNpc> searchNpcs(@RequestParam String keyword) {
        return npcService.searchNpcs(keyword);
    }

    @DeleteMapping("/npcs/{id}")
    public ResponseEntity<Void> deleteNpc(@PathVariable Long id) {
        npcService.deleteNpc(id);
        return ResponseEntity.ok().build();
    }

    // --- Disposition Management ---

    @PostMapping("/npcs/{id}/disposition/change")
    public ResponseEntity<Dnd5eNpc> changeDisposition(@PathVariable Long id, @RequestParam int amount) {
        try {
            return ResponseEntity.ok(npcService.changeDisposition(id, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/npcs/{id}/disposition/set")
    public ResponseEntity<Dnd5eNpc> setDispositionScore(@PathVariable Long id, @RequestParam int score) {
        try {
            return ResponseEntity.ok(npcService.setDispositionScore(id, score));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/npcs/disposition-score/at-least")
    public List<Dnd5eNpc> getNpcsByDispositionScoreAtLeast(@RequestParam int minScore) {
        return npcService.getNpcsByDispositionScoreAtLeast(minScore);
    }

    @GetMapping("/npcs/disposition-score/at-most")
    public List<Dnd5eNpc> getNpcsByDispositionScoreAtMost(@RequestParam int maxScore) {
        return npcService.getNpcsByDispositionScoreAtMost(maxScore);
    }

    // --- Relationship Management ---

    @PostMapping("/npcs/{id}/ally")
    public ResponseEntity<Dnd5eNpc> addAlly(@PathVariable Long id, @RequestParam String allyName) {
        try {
            return ResponseEntity.ok(npcService.addAlly(id, allyName));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/npcs/{id}/enemy")
    public ResponseEntity<Dnd5eNpc> addEnemy(@PathVariable Long id, @RequestParam String enemyName) {
        try {
            return ResponseEntity.ok(npcService.addEnemy(id, enemyName));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/npcs/{id}/family")
    public ResponseEntity<Dnd5eNpc> addFamily(@PathVariable Long id, @RequestParam String familyName) {
        try {
            return ResponseEntity.ok(npcService.addFamily(id, familyName));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/npcs/with-allies")
    public List<Dnd5eNpc> getNpcsWithAllies() {
        return npcService.getNpcsWithAllies();
    }

    @GetMapping("/npcs/with-enemies")
    public List<Dnd5eNpc> getNpcsWithEnemies() {
        return npcService.getNpcsWithEnemies();
    }

    // --- Resource Management ---

    @PostMapping("/npcs/{id}/gold/add")
    public ResponseEntity<Dnd5eNpc> addGold(@PathVariable Long id, @RequestParam int amount) {
        try {
            return ResponseEntity.ok(npcService.addGold(id, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/npcs/{id}/gold/remove")
    public ResponseEntity<Dnd5eNpc> removeGold(@PathVariable Long id, @RequestParam int amount) {
        try {
            return ResponseEntity.ok(npcService.removeGold(id, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/npcs/{id}/hit-points")
    public ResponseEntity<Dnd5eNpc> setHitPoints(@PathVariable Long id, @RequestParam int hitPoints) {
        try {
            return ResponseEntity.ok(npcService.setHitPoints(id, hitPoints));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Reference Data ---

    @GetMapping("/npcs/disposition-values")
    public List<String> getDispositionValues() {
        return npcService.getDispositionValues();
    }

    @GetMapping("/npcs/disposition-for-score")
    public String getDispositionForScore(@RequestParam int score) {
        return npcService.getDispositionForScore(score);
    }

    // ==================== Encounters ====================

    @PostMapping("/encounters")
    public Dnd5eEncounter createEncounter(@RequestBody Dnd5eEncounter encounter) {
        return encounterService.createEncounter(encounter);
    }

    @GetMapping("/encounters")
    public List<Dnd5eEncounter> listEncounters() {
        return encounterService.listEncounters();
    }

    @GetMapping("/encounters/{id}")
    public ResponseEntity<Dnd5eEncounter> getEncounter(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(encounterService.getEncounter(id).orElseThrow());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/encounters/{id}")
    public ResponseEntity<Void> deleteEncounter(@PathVariable Long id) {
        encounterService.deleteEncounter(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/encounters/difficulty/{difficulty}")
    public List<Dnd5eEncounter> listEncountersByDifficulty(@PathVariable String difficulty) {
        return encounterService.listEncountersByDifficulty(difficulty);
    }

    @GetMapping("/encounters/level/{level}")
    public List<Dnd5eEncounter> listEncountersByLevel(@PathVariable int level) {
        return encounterService.listEncountersByLevel(level);
    }

    @GetMapping("/encounters/level-range")
    public List<Dnd5eEncounter> listEncountersByLevelRange(@RequestParam int minLevel, @RequestParam int maxLevel) {
        return encounterService.listEncountersByLevelRange(minLevel, maxLevel);
    }

    @GetMapping("/encounters/xp-range")
    public List<Dnd5eEncounter> listEncountersByXpRange(@RequestParam int minXp, @RequestParam int maxXp) {
        return encounterService.listEncountersByXpRange(minXp, maxXp);
    }

    @GetMapping("/encounters/cr-range")
    public List<Dnd5eEncounter> listEncountersByCrRange(@RequestParam double minCr, @RequestParam double maxCr) {
        return encounterService.listEncountersByCrRange(minCr, maxCr);
    }

    @GetMapping("/encounters/search")
    public List<Dnd5eEncounter> searchEncounters(@RequestParam String keyword) {
        return encounterService.searchEncounters(keyword);
    }

    // --- Encounter Analysis ---

    @GetMapping("/encounters/analyze")
    public EncounterAnalysis analyzeEncounter(@RequestParam int partyLevel, @RequestParam int partySize,
                                               @RequestParam int monsterXp,
                                               @RequestParam(defaultValue = "0") int recentEncounters,
                                               @RequestParam(defaultValue = "0") int tensionBonus) {
        return encounterService.analyzeEncounter(partyLevel, partySize, monsterXp, recentEncounters, tensionBonus);
    }

    @GetMapping("/encounters/{id}/analyze")
    public ResponseEntity<EncounterAnalysis> analyzeEncounterById(@PathVariable Long id) {
        EncounterAnalysis analysis = encounterService.analyzeEncounter(id);
        if (analysis == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/encounters/tension-bonus")
    public int calculateTensionBonus(@RequestParam int monsterXp, @RequestParam int tensionLevel) {
        return encounterService.calculateTensionBonus(monsterXp, tensionLevel);
    }

    @PostMapping("/encounters/tension")
    public Dnd5eEncounter createTensionEncounter(@RequestParam String name, @RequestParam String description,
                                                  @RequestParam int partyLevel, @RequestParam int partySize,
                                                  @RequestParam int monsterXp, @RequestParam int monsterCount,
                                                  @RequestParam double averageCr, @RequestParam int tensionLevel) {
        return encounterService.createTensionEncounter(name, description, partyLevel, partySize,
                monsterXp, monsterCount, averageCr, tensionLevel);
    }

    // --- Reference Data ---

    @GetMapping("/encounters/difficulties")
    public List<String> listDifficulties() {
        return encounterService.listDifficulties();
    }

    @GetMapping("/encounters/xp-thresholds")
    public Map<Integer, int[]> getXpThresholdsByLevel(@RequestParam(defaultValue = "4") int partySize) {
        return encounterService.getXpThresholdsByLevel(partySize);
    }

    @GetMapping("/encounters/cr-xp-table")
    public Map<String, Integer> getCrXpTable() {
        return encounterService.getCrXpTable();
    }

    @GetMapping("/encounters/multiplier-table")
    public Map<Integer, Double> getMultiplierTable() {
        return encounterService.getMultiplierTable();
    }

    @GetMapping("/encounters/xp-thresholds/{level}")
    public int[] getXpThresholds(@PathVariable int level, @RequestParam(defaultValue = "4") int partySize) {
        return encounterService.getXpThresholds(level, partySize);
    }

    @GetMapping("/encounters/cr-xp")
    public int getCrXp(@RequestParam double cr) {
        return encounterService.getCrXp(cr);
    }

    @GetMapping("/encounters/multiplier")
    public double getEncounterMultiplier(@RequestParam int recentEncounters) {
        return encounterService.getEncounterMultiplier(recentEncounters);
    }

    // ==================== Monsters ====================

    @GetMapping("/monsters")
    public List<Dnd5eMonster> listMonsters() {
        return monsterService.listMonsters();
    }

    @GetMapping("/monsters/{id}")
    public Dnd5eMonster getMonster(@PathVariable Long id) {
        return monsterService.getMonster(id);
    }

    @GetMapping("/monsters/name/{name}")
    public Dnd5eMonster getMonsterByName(@PathVariable String name) {
        return monsterService.getMonsterByName(name);
    }

    @GetMapping("/monsters/type/{type}")
    public List<Dnd5eMonster> listMonstersByType(@PathVariable String type) {
        return monsterService.listMonstersByType(type);
    }

    @GetMapping("/monsters/size/{size}")
    public List<Dnd5eMonster> listMonstersBySize(@PathVariable String size) {
        return monsterService.listMonstersBySize(size);
    }

    @GetMapping("/monsters/cr/{cr}")
    public List<Dnd5eMonster> listMonstersByCr(@PathVariable double cr) {
        return monsterService.listMonstersByCr(cr);
    }

    @GetMapping("/monsters/cr/range")
    public List<Dnd5eMonster> listMonstersByCrRange(@RequestParam double minCr, @RequestParam double maxCr) {
        return monsterService.listMonstersByCrRange(minCr, maxCr);
    }

    @GetMapping("/monsters/xp/range")
    public List<Dnd5eMonster> listMonstersByXpRange(@RequestParam int minXp, @RequestParam int maxXp) {
        return monsterService.listMonstersByXpRange(minXp, maxXp);
    }

    @GetMapping("/monsters/alignment/{alignment}")
    public List<Dnd5eMonster> listMonstersByAlignment(@PathVariable String alignment) {
        return monsterService.listMonstersByAlignment(alignment);
    }

    @GetMapping("/monsters/lair-actions")
    public List<Dnd5eMonster> listMonstersWithLairActions() {
        return monsterService.listMonstersWithLairActions();
    }

    @GetMapping("/monsters/legendary-actions")
    public List<Dnd5eMonster> listMonstersWithLegendaryActions() {
        return monsterService.listMonstersWithLegendaryActions();
    }

    @GetMapping("/monsters/regional-effects")
    public List<Dnd5eMonster> listMonstersWithRegionalEffects() {
        return monsterService.listMonstersWithRegionalEffects();
    }

    @GetMapping("/monsters/search")
    public List<Dnd5eMonster> searchMonsters(@RequestParam String keyword) {
        return monsterService.searchMonsters(keyword);
    }

    @PostMapping("/monsters")
    public Dnd5eMonster createMonster(@RequestBody Dnd5eMonster monster) {
        return monsterService.createMonster(monster);
    }

    @PatchMapping("/monsters/{id}")
    public Dnd5eMonster updateMonster(@PathVariable Long id, @RequestBody Dnd5eMonster monster) {
        return monsterService.updateMonster(id, monster);
    }

    @DeleteMapping("/monsters/{id}")
    public ResponseEntity<Void> deleteMonster(@PathVariable Long id) {
        try {
            monsterService.deleteMonster(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Monster Abilities ====================

    @GetMapping("/monsters/{monsterName}/abilities")
    public List<Dnd5eMonsterAbility> listAbilities(@PathVariable String monsterName) {
        return monsterService.listAbilities(monsterName);
    }

    @GetMapping("/monsters/{monsterName}/abilities/type/{type}")
    public List<Dnd5eMonsterAbility> listAbilitiesByType(@PathVariable String monsterName, @PathVariable String type) {
        return monsterService.listAbilitiesByType(monsterName, type);
    }

    @GetMapping("/monsters/{monsterName}/abilities/subtype/{subtype}")
    public List<Dnd5eMonsterAbility> listAbilitiesBySubtype(@PathVariable String monsterName, @PathVariable String subtype) {
        return monsterService.listAbilitiesBySubtype(monsterName, subtype);
    }

    @GetMapping("/monsters/{monsterName}/abilities/search")
    public List<Dnd5eMonsterAbility> searchAbilities(@PathVariable String monsterName, @RequestParam String keyword) {
        return monsterService.searchAbilities(monsterName, keyword);
    }

    @PostMapping("/monsters/abilities")
    public Dnd5eMonsterAbility createAbility(@RequestBody Dnd5eMonsterAbility ability) {
        return monsterService.createAbility(ability);
    }

    @PatchMapping("/monsters/abilities/{id}")
    public Dnd5eMonsterAbility updateAbility(@PathVariable Long id, @RequestBody Dnd5eMonsterAbility ability) {
        return monsterService.updateAbility(id, ability);
    }

    @DeleteMapping("/monsters/abilities/{id}")
    public ResponseEntity<Void> deleteAbility(@PathVariable Long id) {
        try {
            monsterService.deleteAbility(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== XP Table ====================

    @GetMapping("/monsters/xp/cr/{cr}")
    public int getXpForCr(@PathVariable double cr) {
        return monsterService.getXpForCr(cr);
    }

    @GetMapping("/monsters/xp/table")
    public Map<Double, Integer> getXpTable() {
        return monsterService.getXpTable();
    }

    // ==================== Reference Data ====================

    @GetMapping("/monsters/types")
    public List<String> listMonsterTypes() {
        return monsterService.listMonsterTypes();
    }

    @GetMapping("/monsters/sizes")
    public List<String> listMonsterSizes() {
        return monsterService.listMonsterSizes();
    }

    @GetMapping("/monsters/alignments")
    public List<String> listAlignments() {
        return monsterService.listAlignments();
    }

    @GetMapping("/monsters/ability-types")
    public List<String> listAbilityTypes() {
        return monsterService.listAbilityTypes();
    }
}
