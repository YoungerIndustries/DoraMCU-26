package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CraftingCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.CreateCraftingRecipeRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eSessionRequest;
import com.yisy.modules.doramcu.dnd5e.dto.DiceRollResponse;
import com.yisy.modules.doramcu.dnd5e.dto.HeroicInspirationRerollResponse;
import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackRequest;
import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackResponse;
import com.yisy.modules.doramcu.dnd5e.dto.InventorySummaryResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SkillCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SpellCastResponse;
import com.yisy.modules.doramcu.dnd5e.dto.StartCraftingProjectRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterToolProficiency;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombatant;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingProject;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingRecipe;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eExhaustion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eHeroicInspiration;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsionicPower;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCraftingService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eExhaustionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eFeatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHeroicInspirationService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService.PsionicPowerResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService.ToolCheckResult;
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
    private final Dnd5eCraftingService craftingService;
    private final Dnd5eHeroicInspirationService heroicInspirationService;
    private final Dnd5eExhaustionService exhaustionService;
    private final Dnd5eFeatService featService;
    private final Dnd5eToolProficiencyService toolProficiencyService;
    private final Dnd5ePsionService psionService;

    public Dnd5eController(Dnd5eCharacterService characterService,
                           Dnd5eCombatService combatService,
                           Dnd5eSpellService spellService,
                           Dnd5eRulesService rulesService,
                           Dnd5eDiceService diceService,
                           Dnd5eGameplayService gameplayService,
                           Dnd5eCampaignService campaignService,
                           Dnd5eInventoryService inventoryService,
                           Dnd5eCraftingService craftingService,
                           Dnd5eHeroicInspirationService heroicInspirationService,
                           Dnd5eExhaustionService exhaustionService,
                           Dnd5eFeatService featService,
                           Dnd5eToolProficiencyService toolProficiencyService,
                           Dnd5ePsionService psionService) {
        this.characterService = characterService;
        this.combatService = combatService;
        this.spellService = spellService;
        this.rulesService = rulesService;
        this.diceService = diceService;
        this.gameplayService = gameplayService;
        this.campaignService = campaignService;
        this.inventoryService = inventoryService;
        this.craftingService = craftingService;
        this.heroicInspirationService = heroicInspirationService;
        this.exhaustionService = exhaustionService;
        this.featService = featService;
        this.toolProficiencyService = toolProficiencyService;
        this.psionService = psionService;
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

    // ==================== Crafting ====================

    @PostMapping("/crafting/recipes")
    public Dnd5eCraftingRecipe createRecipe(@RequestBody CreateCraftingRecipeRequest request) {
        return craftingService.createRecipe(request);
    }

    @GetMapping("/crafting/recipes")
    public List<Dnd5eCraftingRecipe> listRecipes() {
        return craftingService.listRecipes();
    }

    @GetMapping("/crafting/recipes/{id}")
    public Dnd5eCraftingRecipe getRecipe(@PathVariable Long id) {
        return craftingService.getRecipe(id);
    }

    @GetMapping("/crafting/recipes/category/{category}")
    public List<Dnd5eCraftingRecipe> listRecipesByCategory(@PathVariable String category) {
        return craftingService.listRecipesByCategory(category);
    }

    @GetMapping("/crafting/recipes/tool/{tool}")
    public List<Dnd5eCraftingRecipe> listRecipesByTool(@PathVariable String tool) {
        return craftingService.listRecipesByTool(tool);
    }

    @GetMapping("/crafting/recipes/search")
    public List<Dnd5eCraftingRecipe> searchRecipes(@RequestParam String keyword) {
        return craftingService.searchRecipes(keyword);
    }

    @DeleteMapping("/crafting/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        craftingService.deleteRecipe(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/crafting/tools")
    public List<Dnd5eCraftingTool> listTools() {
        return craftingService.listTools();
    }

    @GetMapping("/crafting/tools/{name}")
    public Dnd5eCraftingTool getTool(@PathVariable String name) {
        return craftingService.getToolByName(name);
    }

    @GetMapping("/crafting/tools/ability/{ability}")
    public List<Dnd5eCraftingTool> listToolsByAbility(@PathVariable String ability) {
        return craftingService.listToolsByAbility(ability);
    }

    @PostMapping("/crafting/projects")
    public Dnd5eCraftingProject startProject(@RequestBody StartCraftingProjectRequest request) {
        return craftingService.startProject(request);
    }

    @GetMapping("/crafting/projects/{id}")
    public Dnd5eCraftingProject getProject(@PathVariable Long id) {
        return craftingService.getProject(id);
    }

    @GetMapping("/crafting/character/{characterId}/projects")
    public List<Dnd5eCraftingProject> listProjects(@PathVariable Long characterId) {
        return craftingService.listProjects(characterId);
    }

    @GetMapping("/crafting/character/{characterId}/active")
    public List<Dnd5eCraftingProject> listActiveProjects(@PathVariable Long characterId) {
        return craftingService.listActiveProjects(characterId);
    }

    @PostMapping("/crafting/projects/{id}/check")
    public CraftingCheckResponse makeCraftingCheck(@PathVariable Long id) {
        return craftingService.makeCraftingCheck(id);
    }

    @PatchMapping("/crafting/projects/{id}/cancel")
    public ResponseEntity<Void> cancelProject(@PathVariable Long id) {
        craftingService.cancelProject(id);
        return ResponseEntity.ok().build();
    }

    // ==================== Heroic Inspiration ====================

    @GetMapping("/inspiration/{characterId}")
    public Dnd5eHeroicInspiration getInspiration(@PathVariable Long characterId) {
        return heroicInspirationService.getOrCreateInspiration(characterId);
    }

    @PostMapping("/inspiration/{characterId}/award")
    public Dnd5eHeroicInspiration awardInspiration(@PathVariable Long characterId,
                                                    @RequestBody Map<String, Object> body) {
        String source = (String) body.getOrDefault("source", "DM Award");
        int points = (int) body.getOrDefault("points", 1);
        return heroicInspirationService.awardInspiration(characterId, source, points);
    }

    @PostMapping("/inspiration/{characterId}/reroll")
    public HeroicInspirationRerollResponse rerollWithInspiration(@PathVariable Long characterId,
                                                                  @RequestBody Map<String, Object> body) {
        String description = (String) body.getOrDefault("description", "Reroll");
        int originalRoll = (int) body.get("originalRoll");
        int diceSides = (int) body.getOrDefault("diceSides", 20);
        return heroicInspirationService.rerollWithInspiration(characterId, description, originalRoll, diceSides);
    }

    @GetMapping("/inspiration/{characterId}/available")
    public boolean hasAvailableInspiration(@PathVariable Long characterId) {
        return heroicInspirationService.hasAvailableInspiration(characterId);
    }

    @GetMapping("/inspiration")
    public List<Dnd5eHeroicInspiration> listAllInspiration() {
        return heroicInspirationService.listAllInspiration();
    }

    @GetMapping("/inspiration/available")
    public List<Dnd5eHeroicInspiration> listAvailableInspiration() {
        return heroicInspirationService.listAvailableInspiration();
    }

    @GetMapping("/inspiration/source/{source}")
    public List<Dnd5eHeroicInspiration> listInspirationBySource(@PathVariable String source) {
        return heroicInspirationService.listInspirationBySource(source);
    }

    @PostMapping("/inspiration/{characterId}/reset")
    public Dnd5eHeroicInspiration resetInspiration(@PathVariable Long characterId) {
        return heroicInspirationService.resetInspiration(characterId);
    }

    @PatchMapping("/inspiration/{characterId}/points")
    public Dnd5eHeroicInspiration setInspirationPoints(@PathVariable Long characterId,
                                                        @RequestBody Map<String, Object> body) {
        int points = (int) body.getOrDefault("points", 0);
        String source = (String) body.getOrDefault("source", "DM Adjustment");
        return heroicInspirationService.setInspirationPoints(characterId, points, source);
    }

    @PatchMapping("/inspiration/{characterId}/max")
    public Dnd5eHeroicInspiration setMaxPoints(@PathVariable Long characterId,
                                                @RequestBody Map<String, Integer> body) {
        int maxPoints = body.getOrDefault("maxPoints", 1);
        return heroicInspirationService.setMaxPoints(characterId, maxPoints);
    }

    // ==================== Exhaustion ====================

    @GetMapping("/exhaustion/{characterId}")
    public int getExhaustionLevel(@PathVariable Long characterId) {
        return exhaustionService.getExhaustionLevel(characterId);
    }

    @PostMapping("/exhaustion/{characterId}/gain")
    public Dnd5eCharacter gainExhaustion(@PathVariable Long characterId,
                                          @RequestBody Map<String, Object> body) {
        int levels = (int) body.getOrDefault("levels", 1);
        String source = (String) body.getOrDefault("source", "DM Award");
        String description = (String) body.getOrDefault("description", null);
        return exhaustionService.gainExhaustion(characterId, levels, source, description);
    }

    @PostMapping("/exhaustion/{characterId}/remove")
    public Dnd5eCharacter removeExhaustion(@PathVariable Long characterId,
                                            @RequestBody Map<String, Object> body) {
        int levels = (int) body.getOrDefault("levels", 1);
        String source = (String) body.getOrDefault("source", "DM Award");
        String description = (String) body.getOrDefault("description", null);
        return exhaustionService.removeExhaustion(characterId, levels, source, description);
    }

    @PatchMapping("/exhaustion/{characterId}/level")
    public Dnd5eCharacter setExhaustionLevel(@PathVariable Long characterId,
                                              @RequestBody Map<String, Object> body) {
        int level = (int) body.getOrDefault("level", 0);
        String source = (String) body.getOrDefault("source", "DM Adjustment");
        return exhaustionService.setExhaustionLevel(characterId, level, source);
    }

    @PostMapping("/exhaustion/{characterId}/clear")
    public Dnd5eCharacter clearExhaustion(@PathVariable Long characterId) {
        return exhaustionService.clearExhaustion(characterId);
    }

    @GetMapping("/exhaustion/{characterId}/penalty")
    public int getExhaustionPenalty(@PathVariable Long characterId) {
        return exhaustionService.getCharacterD20Penalty(characterId);
    }

    @GetMapping("/exhaustion/{characterId}/speed")
    public int getExhaustionEffectiveSpeed(@PathVariable Long characterId) {
        return exhaustionService.getCharacterEffectiveSpeed(characterId);
    }

    @GetMapping("/exhaustion/{characterId}/history")
    public List<Dnd5eExhaustion> getExhaustionHistory(@PathVariable Long characterId) {
        return exhaustionService.getExhaustionHistory(characterId);
    }

    @GetMapping("/exhaustion/events")
    public List<Dnd5eExhaustion> listAllExhaustionEvents() {
        return exhaustionService.listAllExhaustionEvents();
    }

    @GetMapping("/exhaustion/source/{source}")
    public List<Dnd5eExhaustion> listExhaustionBySource(@PathVariable String source) {
        return exhaustionService.listExhaustionBySource(source);
    }

    @PostMapping("/exhaustion/{characterId}/starvation")
    public Dnd5eCharacter applyStarvation(@PathVariable Long characterId) {
        return exhaustionService.applyStarvation(characterId);
    }

    @PostMapping("/exhaustion/{characterId}/dehydration")
    public Dnd5eCharacter applyDehydration(@PathVariable Long characterId) {
        return exhaustionService.applyDehydration(characterId);
    }

    @PostMapping("/exhaustion/{characterId}/temperature")
    public Dnd5eCharacter applyExtremeTemperature(@PathVariable Long characterId,
                                                    @RequestBody Map<String, Boolean> body) {
        boolean isHot = body.getOrDefault("isHot", false);
        return exhaustionService.applyExtremeTemperature(characterId, isHot);
    }

    @PostMapping("/exhaustion/{characterId}/sleep-deprivation")
    public Dnd5eCharacter applySleepDeprivation(@PathVariable Long characterId) {
        return exhaustionService.applySleepDeprivation(characterId);
    }

    @PostMapping("/exhaustion/{characterId}/long-rest")
    public Dnd5eCharacter removeExhaustionAfterLongRest(@PathVariable Long characterId) {
        return exhaustionService.removeExhaustionAfterLongRest(characterId);
    }

    // ==================== Feats ====================

    @GetMapping("/feats")
    public List<Dnd5eFeat> listFeats() {
        return featService.listFeats();
    }

    @GetMapping("/feats/{id}")
    public Dnd5eFeat getFeat(@PathVariable Long id) {
        return featService.getFeat(id);
    }

    @GetMapping("/feats/name/{name}")
    public Dnd5eFeat getFeatByName(@PathVariable String name) {
        return featService.getFeatByName(name);
    }

    @GetMapping("/feats/category/{category}")
    public List<Dnd5eFeat> listFeatsByCategory(@PathVariable String category) {
        return featService.listFeatsByCategory(category);
    }

    @GetMapping("/feats/ability/{ability}")
    public List<Dnd5eFeat> listFeatsByAbility(@PathVariable String ability) {
        return featService.listFeatsByAbility(ability);
    }

    @GetMapping("/feats/search/{keyword}")
    public List<Dnd5eFeat> searchFeats(@PathVariable String keyword) {
        return featService.searchFeats(keyword);
    }

    @GetMapping("/feats/available/{characterId}")
    public List<Dnd5eFeat> listAvailableFeats(@PathVariable Long characterId) {
        return featService.listAvailableFeats(characterId);
    }

    @PostMapping("/feats")
    public Dnd5eFeat createFeat(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String category = (String) body.get("category");
        String abilityRequirement = (String) body.get("abilityRequirement");
        int abilityScoreRequired = (int) body.getOrDefault("abilityScoreRequired", 0);
        String prerequisite = (String) body.get("prerequisite");
        boolean repeatable = (boolean) body.getOrDefault("repeatable", false);
        int abilityIncrease = (int) body.getOrDefault("abilityIncrease", 0);
        String abilityIncreased = (String) body.get("abilityIncreased");
        return featService.createFeat(name, description, category, abilityRequirement,
                abilityScoreRequired, prerequisite, repeatable, abilityIncrease, abilityIncreased);
    }

    @DeleteMapping("/feats/{name}")
    public void deleteFeat(@PathVariable String name) {
        featService.deleteFeat(name);
    }

    @PostMapping("/characters/{characterId}/feats/{featName}")
    public Dnd5eCharacterFeat assignFeatToCharacter(@PathVariable Long characterId,
                                                     @PathVariable String featName) {
        return featService.assignFeatToCharacter(characterId, featName);
    }

    @DeleteMapping("/characters/{characterId}/feats/{featName}")
    public void removeFeatFromCharacter(@PathVariable Long characterId,
                                         @PathVariable String featName) {
        featService.removeFeatFromCharacter(characterId, featName);
    }

    @PatchMapping("/characters/{characterId}/feats/{featName}/toggle")
    public Dnd5eCharacterFeat toggleFeatActive(@PathVariable Long characterId,
                                                @PathVariable String featName,
                                                @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        return featService.toggleFeatActive(characterId, featName, active);
    }

    @GetMapping("/characters/{characterId}/feats")
    public List<Dnd5eCharacterFeat> listCharacterFeats(@PathVariable Long characterId) {
        return featService.listCharacterFeats(characterId);
    }

    @GetMapping("/characters/{characterId}/feats/active")
    public List<Dnd5eCharacterFeat> listActiveCharacterFeats(@PathVariable Long characterId) {
        return featService.listActiveCharacterFeats(characterId);
    }

    @GetMapping("/characters/{characterId}/feats/{featName}/has")
    public boolean hasFeat(@PathVariable Long characterId,
                            @PathVariable String featName) {
        return featService.hasFeat(characterId, featName);
    }

    // ==================== Tool Proficiencies ====================

    @GetMapping("/tools")
    public List<Dnd5eCraftingTool> listAllTools() {
        return toolProficiencyService.listAllTools();
    }

    @GetMapping("/tools/{name}")
    public Dnd5eCraftingTool getToolByName(@PathVariable String name) {
        return toolProficiencyService.getToolByName(name);
    }

    @GetMapping("/tools/type/{toolType}")
    public List<Dnd5eCraftingTool> listToolsByType(@PathVariable String toolType) {
        return toolProficiencyService.listToolsByType(toolType);
    }

    @PostMapping("/characters/{characterId}/tools/{toolName}")
    public Dnd5eCharacterToolProficiency addToolProficiency(@PathVariable Long characterId,
                                                             @PathVariable String toolName,
                                                             @RequestBody Map<String, Integer> body) {
        int level = body.getOrDefault("level", 1);
        return toolProficiencyService.addProficiency(characterId, toolName, level);
    }

    @DeleteMapping("/characters/{characterId}/tools/{toolName}")
    public ResponseEntity<Void> removeToolProficiency(@PathVariable Long characterId,
                                                       @PathVariable String toolName) {
        toolProficiencyService.removeProficiency(characterId, toolName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/characters/{characterId}/tools")
    public List<Dnd5eCharacterToolProficiency> listToolProficiencies(@PathVariable Long characterId) {
        return toolProficiencyService.listProficiencies(characterId);
    }

    @GetMapping("/characters/{characterId}/tools/active")
    public List<Dnd5eCharacterToolProficiency> listActiveToolProficiencies(@PathVariable Long characterId) {
        return toolProficiencyService.listActiveProficiencies(characterId);
    }

    @GetMapping("/characters/{characterId}/tools/{toolName}/proficient")
    public boolean isToolProficient(@PathVariable Long characterId,
                                     @PathVariable String toolName) {
        return toolProficiencyService.isProficient(characterId, toolName);
    }

    @GetMapping("/characters/{characterId}/tools/{toolName}/expertise")
    public boolean hasToolExpertise(@PathVariable Long characterId,
                                     @PathVariable String toolName) {
        return toolProficiencyService.hasExpertise(characterId, toolName);
    }

    @PatchMapping("/characters/{characterId}/tools/{toolName}/expertise")
    public Dnd5eCharacterToolProficiency setToolExpertise(@PathVariable Long characterId,
                                                           @PathVariable String toolName,
                                                           @RequestBody Map<String, Boolean> body) {
        boolean expertise = body.getOrDefault("expertise", true);
        return toolProficiencyService.setExpertise(characterId, toolName, expertise);
    }

    @PatchMapping("/characters/{characterId}/tools/{toolName}/toggle")
    public Dnd5eCharacterToolProficiency toggleToolActive(@PathVariable Long characterId,
                                                           @PathVariable String toolName) {
        return toolProficiencyService.toggleActive(characterId, toolName);
    }

    @PostMapping("/characters/{characterId}/tools/{toolName}/check")
    public ToolCheckResult makeToolCheck(@PathVariable Long characterId,
                                          @PathVariable String toolName,
                                          @RequestBody Map<String, Integer> body) {
        int dc = body.getOrDefault("dc", 10);
        return toolProficiencyService.makeToolCheck(characterId, toolName, dc);
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
    public PsionicPowerResult spendPowerPoints(@PathVariable Long characterId,
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
    public PsionicPowerResult usePower(@PathVariable Long characterId,
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
