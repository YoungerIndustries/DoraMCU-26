package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eSessionRequest;
import com.yisy.modules.doramcu.dnd5e.dto.DiceRollResponse;
import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackRequest;
import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackResponse;
import com.yisy.modules.doramcu.dnd5e.dto.InventorySummaryResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SkillCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SpellCastResponse;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombatant;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGameplayService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eInventoryService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpellService;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for D&D 5e endpoints.
 */
@RestController
@RequestMapping("/api/v1/dnd5e")
public class Dnd5eController {

    private final Dnd5eCharacterService characterService;
    private final Dnd5eCombatService combatService;
    private final Dnd5eSpellService spellService;
    private final Dnd5eRulesService rulesService;
    private final Dnd5eDiceService diceService;
    private final Dnd5eGameplayService gameplayService;
    private final Dnd5eCampaignService campaignService;
    private final Dnd5eInventoryService inventoryService;

    public Dnd5eController(Dnd5eCharacterService characterService,
                           Dnd5eCombatService combatService,
                           Dnd5eSpellService spellService,
                           Dnd5eRulesService rulesService,
                           Dnd5eDiceService diceService,
                           Dnd5eGameplayService gameplayService,
                           Dnd5eCampaignService campaignService,
                           Dnd5eInventoryService inventoryService) {
        this.characterService = characterService;
        this.combatService = combatService;
        this.spellService = spellService;
        this.rulesService = rulesService;
        this.diceService = diceService;
        this.gameplayService = gameplayService;
        this.campaignService = campaignService;
        this.inventoryService = inventoryService;
    }

    // ==================== Characters ====================

    @PostMapping("/characters")
    public Dnd5eCharacter createCharacter(@RequestBody CreateDnd5eCharacterRequest request) {
        return characterService.createCharacter(request);
    }

    @GetMapping("/characters")
    public List<Dnd5eCharacter> listCharacters() {
        return characterService.listCharacters();
    }

    @GetMapping("/characters/{id}")
    public Dnd5eCharacter getCharacter(@PathVariable Long id) {
        return characterService.getCharacter(id);
    }

    @PatchMapping("/characters/{id}")
    public Dnd5eCharacter updateCharacter(@PathVariable Long id, @RequestBody UpdateDnd5eCharacterRequest request) {
        return characterService.updateCharacter(id, request);
    }

    @DeleteMapping("/characters/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.ok().build();
    }

    // ==================== Rules ====================

    @GetMapping("/rules/summary")
    public Map<String, Object> getRulesSummary() {
        List<RulesEntry> rules = rulesService.getAllRules();
        List<String> categories = rulesService.getCategories();
        List<String> sources = rulesService.getSources();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRules", rules.size());
        summary.put("categories", categories);
        summary.put("sources", sources);
        return summary;
    }

    @GetMapping("/rules/category")
    public List<RulesEntry> getRulesByCategory(@RequestParam String category) {
        return rulesService.getRulesByCategory(category);
    }

    @GetMapping("/rules/source")
    public List<RulesEntry> getRulesBySource(@RequestParam String source) {
        return rulesService.getRulesBySource(source);
    }

    @GetMapping("/rules/search")
    public List<RulesEntry> searchRules(@RequestParam String keyword) {
        return rulesService.searchRules(keyword);
    }

    @GetMapping("/rules/categories")
    public List<String> getCategories() {
        return rulesService.getCategories();
    }

    @GetMapping("/rules/tables")
    public List<RulesTable> getAllTables() {
        return rulesService.getAllTableEntries();
    }

    @GetMapping("/rules/table")
    public List<RulesTable> getTableEntries(@RequestParam String tableName) {
        return rulesService.getTableEntries(tableName);
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

    // ==================== Dice Roller ====================

    @PostMapping("/dice/roll")
    public DiceRollResponse rollDice(@RequestBody Map<String, String> body) {
        String formula = body.getOrDefault("formula", "d20");
        return diceService.rollDice(formula);
    }

    @PostMapping("/dice/roll/advantage")
    public DiceRollResponse rollWithAdvantage(@RequestBody Map<String, String> body) {
        String formula = body.getOrDefault("formula", "d20");
        return diceService.rollWithAdvantage(formula);
    }

    @PostMapping("/dice/roll/disadvantage")
    public DiceRollResponse rollWithDisadvantage(@RequestBody Map<String, String> body) {
        String formula = body.getOrDefault("formula", "d20");
        return diceService.rollWithDisadvantage(formula);
    }

    @PostMapping("/dice/roll/multiple")
    public DiceRollResponse rollMultiple(@RequestBody List<String> formulas) {
        return diceService.rollMultiple(formulas);
    }

    // ==================== Gameplay ====================

    @PostMapping("/gameplay/skill-check")
    public SkillCheckResponse skillCheck(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        String skill = (String) body.get("skill");
        int dc = (int) body.getOrDefault("dc", 10);
        boolean advantage = (boolean) body.getOrDefault("advantage", false);
        boolean disadvantage = (boolean) body.getOrDefault("disadvantage", false);
        return gameplayService.skillCheck(characterId, skill, dc, advantage, disadvantage);
    }

    @PostMapping("/gameplay/saving-throw")
    public SkillCheckResponse savingThrow(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        String ability = (String) body.get("ability");
        int dc = (int) body.getOrDefault("dc", 10);
        boolean advantage = (boolean) body.getOrDefault("advantage", false);
        boolean disadvantage = (boolean) body.getOrDefault("disadvantage", false);
        return gameplayService.savingThrow(characterId, ability, dc, advantage, disadvantage);
    }

    @PostMapping("/gameplay/cast-spell")
    public SpellCastResponse castSpell(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        Long spellId = Long.valueOf((int) body.get("spellId"));
        int slotLevel = (int) body.getOrDefault("slotLevel", 0);
        return gameplayService.castSpell(characterId, spellId, slotLevel);
    }

    @PostMapping("/gameplay/short-rest")
    public Dnd5eCharacter shortRest(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        int hitDice = (int) body.getOrDefault("hitDice", 1);
        return gameplayService.shortRest(characterId, hitDice);
    }

    @PostMapping("/gameplay/long-rest")
    public Dnd5eCharacter longRest(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        return gameplayService.longRest(characterId);
    }

    @PostMapping("/gameplay/death-save")
    public SkillCheckResponse deathSavingThrow(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        boolean advantage = (boolean) body.getOrDefault("advantage", false);
        boolean disadvantage = (boolean) body.getOrDefault("disadvantage", false);
        return gameplayService.deathSavingThrow(characterId, advantage, disadvantage);
    }

    @PostMapping("/gameplay/award-xp")
    public Dnd5eCharacter awardXp(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf((int) body.get("characterId"));
        int xp = (int) body.get("xp");
        return gameplayService.awardXp(characterId, xp);
    }

    // ==================== Campaigns ====================

    @PostMapping("/campaigns")
    public Dnd5eCampaign createCampaign(@RequestBody CreateDnd5eCampaignRequest request) {
        return campaignService.createCampaign(request);
    }

    @GetMapping("/campaigns")
    public List<Dnd5eCampaign> listCampaigns() {
        return campaignService.listCampaigns();
    }

    @GetMapping("/campaigns/active")
    public List<Dnd5eCampaign> listActiveCampaigns() {
        return campaignService.listActiveCampaigns();
    }

    @GetMapping("/campaigns/{id}")
    public Dnd5eCampaign getCampaign(@PathVariable Long id) {
        return campaignService.getCampaign(id);
    }

    @PatchMapping("/campaigns/{id}")
    public Dnd5eCampaign updateCampaign(@PathVariable Long id, @RequestBody CreateDnd5eCampaignRequest request) {
        return campaignService.updateCampaign(id, request);
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/campaigns/{id}/active")
    public Dnd5eCampaign setCampaignActive(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        campaignService.setActive(id, active);
        return campaignService.getCampaign(id);
    }

    // ==================== Campaign Characters ====================

    @PostMapping("/campaigns/{id}/characters")
    public Dnd5eCampaignCharacter addCharacterToCampaign(@PathVariable Long id, @RequestBody AddCampaignCharacterRequest request) {
        return campaignService.addCharacterToCampaign(id, request);
    }

    @DeleteMapping("/campaigns/{campaignId}/characters/{characterId}")
    public ResponseEntity<Void> removeCharacterFromCampaign(@PathVariable Long campaignId, @PathVariable Long characterId) {
        campaignService.removeCharacterFromCampaign(campaignId, characterId);
        return ResponseEntity.ok().build();
    }

    // ==================== Sessions ====================

    @PostMapping("/campaigns/{id}/sessions")
    public Dnd5eSession createSession(@PathVariable Long id, @RequestBody CreateDnd5eSessionRequest request) {
        return campaignService.createSession(id, request);
    }

    @GetMapping("/campaigns/{id}/sessions")
    public List<Dnd5eSession> listSessions(@PathVariable Long id) {
        return campaignService.listSessions(id);
    }

    @GetMapping("/campaigns/{campaignId}/sessions/{sessionId}")
    public Dnd5eSession getSession(@PathVariable Long campaignId, @PathVariable Long sessionId) {
        return campaignService.getSession(campaignId, sessionId);
    }

    @PatchMapping("/campaigns/{campaignId}/sessions/{sessionId}")
    public Dnd5eSession updateSession(@PathVariable Long campaignId, @PathVariable Long sessionId, @RequestBody CreateDnd5eSessionRequest request) {
        return campaignService.updateSession(campaignId, sessionId, request);
    }

    @DeleteMapping("/campaigns/{campaignId}/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long campaignId, @PathVariable Long sessionId) {
        campaignService.deleteSession(campaignId, sessionId);
        return ResponseEntity.ok().build();
    }

    // ==================== Inventory ====================

    @PostMapping("/inventory/items")
    public Dnd5eInventoryItem createItem(@RequestBody CreateDnd5eInventoryItemRequest request) {
        return inventoryService.createItem(request.getCharacterId(), request);
    }

    @GetMapping("/inventory/items/{id}")
    public Dnd5eInventoryItem getItem(@PathVariable Long id) {
        return inventoryService.getItem(id);
    }

    @GetMapping("/inventory/character/{characterId}")
    public List<Dnd5eInventoryItem> listItems(@PathVariable Long characterId) {
        return inventoryService.listItems(characterId);
    }

    @PatchMapping("/inventory/items/{id}")
    public Dnd5eInventoryItem updateItem(@PathVariable Long id, @RequestBody UpdateDnd5eInventoryItemRequest request) {
        return inventoryService.updateItem(id, request);
    }

    @DeleteMapping("/inventory/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/inventory/items/{id}/equip")
    public Dnd5eInventoryItem equipItem(@PathVariable Long id) {
        return inventoryService.equipItem(id);
    }

    @PatchMapping("/inventory/items/{id}/unequip")
    public Dnd5eInventoryItem unequipItem(@PathVariable Long id) {
        return inventoryService.unequipItem(id);
    }

    @GetMapping("/inventory/character/{characterId}/equipped")
    public List<Dnd5eInventoryItem> getEquippedItems(@PathVariable Long characterId) {
        return inventoryService.getEquippedItems(characterId);
    }

    @GetMapping("/inventory/character/{characterId}/type/{itemType}")
    public List<Dnd5eInventoryItem> getItemsByType(@PathVariable Long characterId, @PathVariable String itemType) {
        return inventoryService.getItemsByType(characterId, itemType);
    }

    @PatchMapping("/inventory/items/{id}/attune")
    public Dnd5eInventoryItem attuneItem(@PathVariable Long id) {
        return inventoryService.attuneItem(id);
    }

    @PatchMapping("/inventory/items/{id}/unattune")
    public Dnd5eInventoryItem unattuneItem(@PathVariable Long id) {
        return inventoryService.unattuneItem(id);
    }

    @GetMapping("/inventory/character/{characterId}/attuned")
    public List<Dnd5eInventoryItem> getAttunedItems(@PathVariable Long characterId) {
        return inventoryService.getAttunedItems(characterId);
    }

    @PatchMapping("/inventory/items/{id}/add-quantity")
    public Dnd5eInventoryItem addItemQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        return inventoryService.addItemQuantity(id, amount);
    }

    @PatchMapping("/inventory/items/{id}/consume")
    public Dnd5eInventoryItem consumeItem(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        return inventoryService.consumeItem(id, amount);
    }

    @PatchMapping("/inventory/items/{id}/use-charge")
    public Dnd5eInventoryItem useCharge(@PathVariable Long id) {
        return inventoryService.useCharge(id);
    }

    @PatchMapping("/inventory/items/{id}/recharge")
    public Dnd5eInventoryItem rechargeItem(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int charges = body.getOrDefault("charges", 1);
        return inventoryService.rechargeItem(id, charges);
    }

    @GetMapping("/inventory/character/{characterId}/search")
    public List<Dnd5eInventoryItem> searchItems(@PathVariable Long characterId, @RequestParam String keyword) {
        return inventoryService.searchItems(characterId, keyword);
    }

    @GetMapping("/inventory/character/{characterId}/rarity/{rarity}")
    public List<Dnd5eInventoryItem> getItemsByRarity(@PathVariable Long characterId, @PathVariable String rarity) {
        return inventoryService.getItemsByRarity(characterId, rarity);
    }

    @GetMapping("/inventory/character/{characterId}/summary")
    public InventorySummaryResponse getInventorySummary(@PathVariable Long characterId) {
        return inventoryService.getInventorySummary(characterId);
    }
}
