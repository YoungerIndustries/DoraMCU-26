package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.CraftingCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.CreateCraftingRecipeRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.dto.DiceRollResponse;
import com.yisy.modules.doramcu.dnd5e.dto.HeroicInspirationRerollResponse;
import com.yisy.modules.doramcu.dnd5e.dto.InventorySummaryResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SkillCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.SpellCastResponse;
import com.yisy.modules.doramcu.dnd5e.dto.StartCraftingProjectRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eAdventureHook;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBastion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingProject;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingRecipe;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDarkGift;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDungeonRoom;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eExhaustion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFearDread;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eHeroicInspiration;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterGrowingItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eGrowingMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eModule;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eTensionPool;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillain;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillainPower;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCraftingService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eExhaustionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHeroicInspirationService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMagicItemService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHorrorService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eVillainService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eVillainService.VillainDamageResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eAdventureHookService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService.BastionFacilityResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService.BastionDamageResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGameplayService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eInventoryService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eModuleService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService.TensionConsequence;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService.WanderingEncounterResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService.ItemLevelUpResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService.ItemAbilities;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService.TrapCheckResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService.TreasureResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMagicItemService.UseItemResult;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for D&D 5e endpoints.
 */
@RestController
@RequestMapping("/api/v1/dnd5e")
public class Dnd5eController {

    private final Dnd5eCharacterService characterService;
    private final Dnd5eRulesService rulesService;
    private final Dnd5eDiceService diceService;
    private final Dnd5eGameplayService gameplayService;
    private final Dnd5eInventoryService inventoryService;
    private final Dnd5eCraftingService craftingService;
    private final Dnd5eHeroicInspirationService heroicInspirationService;
    private final Dnd5eExhaustionService exhaustionService;
    private final Dnd5eHorrorService horrorService;
    private final Dnd5eGrowingMagicItemService growingItemService;
    private final Dnd5eVillainService villainService;
    private final Dnd5eBastionService bastionService;
    private final Dnd5eAdventureHookService adventureHookService;
    private final Dnd5eDungeonRoomService dungeonRoomService;
    private final Dnd5eTensionPoolService tensionPoolService;
    private final Dnd5eModuleService moduleService;
    private final Dnd5eMagicItemService magicItemService;

    public Dnd5eController(Dnd5eCharacterService characterService,
                           Dnd5eRulesService rulesService,
                           Dnd5eDiceService diceService,
                           Dnd5eGameplayService gameplayService,
                           Dnd5eInventoryService inventoryService,
                           Dnd5eCraftingService craftingService,
                           Dnd5eHeroicInspirationService heroicInspirationService,
                           Dnd5eExhaustionService exhaustionService,
                           Dnd5eHorrorService horrorService,
                           Dnd5eGrowingMagicItemService growingItemService,
                           Dnd5eVillainService villainService,
                           Dnd5eBastionService bastionService,
                           Dnd5eAdventureHookService adventureHookService,
                           Dnd5eDungeonRoomService dungeonRoomService,
                           Dnd5eTensionPoolService tensionPoolService,
                           Dnd5eModuleService moduleService,
                           Dnd5eMagicItemService magicItemService) {
        this.characterService = characterService;
        this.rulesService = rulesService;
        this.diceService = diceService;
        this.gameplayService = gameplayService;
        this.inventoryService = inventoryService;
        this.craftingService = craftingService;
        this.heroicInspirationService = heroicInspirationService;
        this.exhaustionService = exhaustionService;
        this.horrorService = horrorService;
        this.growingItemService = growingItemService;
        this.villainService = villainService;
        this.bastionService = bastionService;
        this.adventureHookService = adventureHookService;
        this.dungeonRoomService = dungeonRoomService;
        this.tensionPoolService = tensionPoolService;
        this.moduleService = moduleService;
        this.magicItemService = magicItemService;
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

    // ==================== Horror (Ravenloft) ====================

    // --- Dark Gifts ---

    @GetMapping("/horror/dark-gifts")
    public List<Dnd5eDarkGift> listDarkGifts() {
        return horrorService.listDarkGifts();
    }

    @GetMapping("/horror/dark-gifts/{name}")
    public Dnd5eDarkGift getDarkGiftByName(@PathVariable String name) {
        return horrorService.getDarkGiftByName(name);
    }

    @GetMapping("/horror/dark-gifts/category/{category}")
    public List<Dnd5eDarkGift> listDarkGiftsByCategory(@PathVariable String category) {
        return horrorService.listDarkGiftsByCategory(category);
    }

    @GetMapping("/horror/dark-gifts/darklord/{darklord}")
    public List<Dnd5eDarkGift> listDarkGiftsByDarklord(@PathVariable String darklord) {
        return horrorService.listDarkGiftsByDarklord(darklord);
    }

    @GetMapping("/horror/dark-gifts/level/{level}")
    public List<Dnd5eDarkGift> listDarkGiftsByLevel(@PathVariable int level) {
        return horrorService.listDarkGiftsByLevel(level);
    }

    @GetMapping("/horror/dark-gifts/search")
    public List<Dnd5eDarkGift> searchDarkGifts(@RequestParam String keyword) {
        return horrorService.searchDarkGifts(keyword);
    }

    @GetMapping("/horror/dark-gifts/categories")
    public List<String> getDarkGiftCategories() {
        return horrorService.getDarkGiftCategories();
    }

    @PostMapping("/horror/dark-gifts")
    public Dnd5eDarkGift createDarkGift(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int minLevel = (int) body.getOrDefault("minLevel", 1);
        String category = (String) body.getOrDefault("category", "Undead");
        String benefit = (String) body.get("benefit");
        String curse = (String) body.get("curse");
        String darklord = (String) body.get("darklord");
        boolean repeatable = (boolean) body.getOrDefault("repeatable", false);
        String description = (String) body.getOrDefault("description", "");
        return horrorService.createDarkGift(name, minLevel, category, benefit, curse, darklord, repeatable, description);
    }

    @DeleteMapping("/horror/dark-gifts/{id}")
    public void deleteDarkGift(@PathVariable Long id) {
        horrorService.deleteDarkGift(id);
    }

    // --- Fear & Dread ---

    @GetMapping("/horror/fear-dread/{characterId}")
    public Dnd5eFearDread getFearDread(@PathVariable Long characterId) {
        return horrorService.getFearDread(characterId);
    }

    @GetMapping("/horror/fear-dread")
    public List<Dnd5eFearDread> listAllFearDread() {
        return horrorService.listAllFearDread();
    }

    @GetMapping("/horror/fear-dread/domain/{domain}")
    public List<Dnd5eFearDread> listByDomain(@PathVariable String domain) {
        return horrorService.listByDomain(domain);
    }

    @GetMapping("/horror/fear-dread/frightened")
    public List<Dnd5eFearDread> listFrightenedCharacters() {
        return horrorService.listFrightenedCharacters();
    }

    @PatchMapping("/horror/fear-dread/{characterId}/domain")
    public Dnd5eFearDread setDomain(@PathVariable Long characterId,
                                     @RequestBody Map<String, String> body) {
        String domain = body.getOrDefault("domain", "Barovia");
        String darklord = body.getOrDefault("darklord", "Strahd");
        return horrorService.setDomain(characterId, domain, darklord);
    }

    // --- Fear Checks ---

    @PostMapping("/horror/characters/{characterId}/fear-check")
    public Dnd5eHorrorService.FearCheckResult makeFearCheck(@PathVariable Long characterId,
                                                              @RequestBody Map<String, Object> body) {
        int wisModifier = (int) body.getOrDefault("wisModifier", 0);
        boolean proficient = (boolean) body.getOrDefault("proficient", false);
        int profBonus = (int) body.getOrDefault("profBonus", 2);
        return horrorService.makeFearCheck(characterId, wisModifier, proficient, profBonus);
    }

    @PatchMapping("/horror/characters/{characterId}/fear/increase")
    public Dnd5eFearDread increaseFear(@PathVariable Long characterId,
                                        @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        return horrorService.increaseFear(characterId, amount);
    }

    @PatchMapping("/horror/characters/{characterId}/fear/decrease")
    public Dnd5eFearDread decreaseFear(@PathVariable Long characterId,
                                        @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        return horrorService.decreaseFear(characterId, amount);
    }

    @PostMapping("/horror/characters/{characterId}/fear/clear")
    public Dnd5eFearDread clearFear(@PathVariable Long characterId) {
        return horrorService.clearFear(characterId);
    }

    @GetMapping("/horror/characters/{characterId}/fear/penalty")
    public int getFearPenalty(@PathVariable Long characterId) {
        return horrorService.getFearPenalty(characterId);
    }

    @GetMapping("/horror/characters/{characterId}/fear/effects")
    public String getFearEffects(@PathVariable Long characterId) {
        return horrorService.getFearEffects(characterId);
    }

    // --- Dread ---

    @PatchMapping("/horror/characters/{characterId}/dread/increase")
    public Dnd5eFearDread increaseDread(@PathVariable Long characterId,
                                         @RequestBody Map<String, Object> body) {
        int amount = (int) body.getOrDefault("amount", 5);
        String reason = (String) body.getOrDefault("reason", "Encountered horror");
        return horrorService.increaseDread(characterId, amount, reason);
    }

    @PatchMapping("/horror/characters/{characterId}/dread/decrease")
    public Dnd5eFearDread decreaseDread(@PathVariable Long characterId,
                                         @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 5);
        return horrorService.decreaseDread(characterId, amount);
    }

    @PostMapping("/horror/characters/{characterId}/dread/clear")
    public Dnd5eFearDread clearDread(@PathVariable Long characterId) {
        return horrorService.clearDread(characterId);
    }

    @GetMapping("/horror/characters/{characterId}/dread/effects")
    public String getDreadEffects(@PathVariable Long characterId) {
        return horrorService.getDreadEffects(characterId);
    }

    @GetMapping("/horror/characters/{characterId}/dread/description")
    public String getDreadDescription(@PathVariable Long characterId) {
        return horrorService.getDreadDescription(characterId);
    }

    // --- Dark Gift Acquisition ---

    @PostMapping("/horror/characters/{characterId}/acquire-dark-gift")
    public Dnd5eFearDread acquireDarkGift(@PathVariable Long characterId,
                                           @RequestBody Map<String, String> body) {
        String giftName = body.get("giftName");
        return horrorService.acquireDarkGift(characterId, giftName);
    }

    // --- Reference Data ---

    @GetMapping("/horror/darklords")
    public List<String> getDarklords() {
        return horrorService.getDarklords();
    }

    @GetMapping("/horror/domains")
    public List<String> getDomains() {
        return horrorService.getDomains();
    }

    // ==================== Growing Magic Items ====================

    // --- Item Definitions ---

    @GetMapping("/growing-items")
    public List<Dnd5eGrowingMagicItem> listGrowingItems() {
        return growingItemService.listGrowingItems();
    }

    @GetMapping("/growing-items/{id}")
    public Dnd5eGrowingMagicItem getGrowingItem(@PathVariable Long id) {
        return growingItemService.getGrowingItem(id);
    }

    @GetMapping("/growing-items/by-name")
    public Dnd5eGrowingMagicItem getGrowingItemByName(@RequestParam String name) {
        return growingItemService.getGrowingItemByName(name);
    }

    @GetMapping("/growing-items/category")
    public List<Dnd5eGrowingMagicItem> listGrowingItemsByCategory(@RequestParam String category) {
        return growingItemService.listGrowingItemsByCategory(category);
    }

    @GetMapping("/growing-items/rarity")
    public List<Dnd5eGrowingMagicItem> listGrowingItemsByRarity(@RequestParam String rarity) {
        return growingItemService.listGrowingItemsByRarity(rarity);
    }

    @GetMapping("/growing-items/search")
    public List<Dnd5eGrowingMagicItem> searchGrowingItems(@RequestParam String keyword) {
        return growingItemService.searchGrowingItems(keyword);
    }

    @PostMapping("/growing-items")
    public Dnd5eGrowingMagicItem createGrowingItem(@RequestBody Map<String, String> body) {
        return growingItemService.createGrowingItem(
                body.get("name"), body.get("category"), body.get("rarity"),
                Integer.parseInt(body.getOrDefault("maxLevel", "20")),
                body.get("description"),
                body.getOrDefault("ability1", null), body.getOrDefault("ability5", null),
                body.getOrDefault("ability10", null), body.getOrDefault("ability15", null),
                body.getOrDefault("ability20", null),
                body.getOrDefault("statBonus1", null), body.getOrDefault("statBonus5", null),
                body.getOrDefault("statBonus10", null), body.getOrDefault("statBonus15", null),
                body.getOrDefault("statBonus20", null)
        );
    }

    @DeleteMapping("/growing-items/{id}")
    public ResponseEntity<Void> deleteGrowingItem(@PathVariable Long id) {
        growingItemService.deleteGrowingItem(id);
        return ResponseEntity.ok().build();
    }

    // --- Character Item Bindings ---

    @PostMapping("/characters/{characterId}/growing-items/{itemId}")
    public Dnd5eCharacterGrowingItem bindItemToCharacter(@PathVariable Long characterId,
                                                          @PathVariable Long itemId) {
        return growingItemService.bindItemToCharacter(characterId, itemId);
    }

    @GetMapping("/characters/{characterId}/growing-items")
    public List<Dnd5eCharacterGrowingItem> listCharacterGrowingItems(@PathVariable Long characterId) {
        return growingItemService.listCharacterGrowingItems(characterId);
    }

    @GetMapping("/characters/{characterId}/growing-items/{itemId}")
    public Dnd5eCharacterGrowingItem getCharacterGrowingItem(@PathVariable Long characterId,
                                                              @PathVariable Long itemId) {
        return growingItemService.getCharacterGrowingItem(characterId, itemId);
    }

    @DeleteMapping("/characters/{characterId}/growing-items/{itemId}")
    public ResponseEntity<Void> unbindItemFromCharacter(@PathVariable Long characterId,
                                                         @PathVariable Long itemId) {
        growingItemService.unbindItemFromCharacter(characterId, itemId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/characters/{characterId}/growing-items/{itemId}/attunement")
    public Dnd5eCharacterGrowingItem toggleAttunement(@PathVariable Long characterId,
                                                       @PathVariable Long itemId) {
        return growingItemService.toggleAttunement(characterId, itemId);
    }

    // --- Level Up ---

    @PatchMapping("/characters/{characterId}/growing-items/level-up")
    public List<ItemLevelUpResult> levelUpItems(@PathVariable Long characterId,
                                                 @RequestBody Map<String, Integer> body) {
        int newLevel = body.get("newLevel");
        return growingItemService.levelUpItems(characterId, newLevel);
    }

    @GetMapping("/characters/{characterId}/growing-items/{itemId}/abilities")
    public ItemAbilities getAbilities(@PathVariable Long characterId,
                                       @PathVariable Long itemId) {
        return growingItemService.getAbilities(characterId, itemId);
    }

    // --- Reference Data ---

    @GetMapping("/growing-items/categories")
    public List<String> getGrowingItemCategories() {
        return growingItemService.getCategories();
    }

    @GetMapping("/growing-items/rarities")
    public List<String> getGrowingItemRarities() {
        return growingItemService.getRarities();
    }

    // ==================== Villains ====================

    // --- Villain CRUD ---

    @GetMapping("/villains")
    public List<Dnd5eVillain> listVillains() {
        return villainService.listVillains();
    }

    @GetMapping("/villains/{id}")
    public ResponseEntity<Dnd5eVillain> getVillain(@PathVariable Long id) {
        return villainService.getVillain(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/villains/by-name")
    public ResponseEntity<Dnd5eVillain> getVillainByName(@RequestParam String name) {
        return villainService.getVillainByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/villains/type")
    public List<Dnd5eVillain> listVillainsByType(@RequestParam String villainType) {
        return villainService.listVillainsByType(villainType);
    }

    @GetMapping("/villains/cr-range")
    public List<Dnd5eVillain> listVillainsByCrRange(@RequestParam int minCr,
                                                     @RequestParam int maxCr) {
        return villainService.listVillainsByCrRange(minCr, maxCr);
    }

    @GetMapping("/villains/liches")
    public List<Dnd5eVillain> listLiches() {
        return villainService.listLiches();
    }

    @GetMapping("/villains/death-knights")
    public List<Dnd5eVillain> listDeathKnights() {
        return villainService.listDeathKnights();
    }

    @GetMapping("/villains/search")
    public List<Dnd5eVillain> searchVillains(@RequestParam String keyword) {
        return villainService.searchVillains(keyword);
    }

    @PostMapping("/villains")
    public Dnd5eVillain createVillain(@RequestBody Dnd5eVillain villain) {
        return villainService.createVillain(villain);
    }

    @PutMapping("/villains/{id}")
    public ResponseEntity<Dnd5eVillain> updateVillain(@PathVariable Long id,
                                                       @RequestBody Dnd5eVillain updates) {
        try {
            Dnd5eVillain updated = villainService.updateVillain(id, updates);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/villains/{id}")
    public ResponseEntity<Void> deleteVillain(@PathVariable Long id) {
        if (villainService.deleteVillain(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Villain Powers ---

    @GetMapping("/villains/{villainId}/powers")
    public List<Dnd5eVillainPower> listPowers(@PathVariable Long villainId) {
        return villainService.listPowers(villainId);
    }

    @GetMapping("/villains/{villainId}/legendary-actions")
    public List<Dnd5eVillainPower> listLegendaryActions(@PathVariable Long villainId) {
        return villainService.listLegendaryActions(villainId);
    }

    @GetMapping("/villains/{villainId}/lair-actions")
    public List<Dnd5eVillainPower> listLairActions(@PathVariable Long villainId) {
        return villainService.listLairActions(villainId);
    }

    @GetMapping("/villains/{villainId}/regional-effects")
    public List<Dnd5eVillainPower> listRegionalEffects(@PathVariable Long villainId) {
        return villainService.listRegionalEffects(villainId);
    }

    @PostMapping("/villains/{villainId}/powers")
    public Dnd5eVillainPower createPower(@PathVariable Long villainId,
                                          @RequestBody Dnd5eVillainPower power) {
        return villainService.createPower(villainId, power);
    }

    @PutMapping("/villain-powers/{id}")
    public ResponseEntity<Dnd5eVillainPower> updatePower(@PathVariable Long id,
                                                           @RequestBody Dnd5eVillainPower updates) {
        try {
            Dnd5eVillainPower updated = villainService.updatePower(id, updates);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/villain-powers/{id}")
    public ResponseEntity<Void> deletePower(@PathVariable Long id) {
        if (villainService.deletePower(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Combat Mechanics ---

    @PostMapping("/villains/{villainId}/damage")
    public VillainDamageResult dealDamage(@PathVariable Long villainId,
                                           @RequestBody Map<String, Integer> body) {
        int damage = body.get("damage");
        return villainService.dealDamage(villainId, damage);
    }

    @PostMapping("/villains/{villainId}/legendary-resistance")
    public Map<String, Boolean> useLegendaryResistance(@PathVariable Long villainId) {
        boolean used = villainService.useLegendaryResistance(villainId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("used", used);
        return result;
    }

    @PostMapping("/villains/{villainId}/restore-resistances")
    public ResponseEntity<Void> restoreLegendaryResistances(@PathVariable Long villainId) {
        villainService.restoreLegendaryResistances(villainId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/villains/{villainId}/destroy-phylactery")
    public Map<String, Boolean> destroyPhylactery(@PathVariable Long villainId) {
        boolean destroyed = villainService.destroyPhylactery(villainId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("destroyed", destroyed);
        return result;
    }

    @PostMapping("/villains/{villainId}/spawn-minions")
    public List<String> spawnMinions(@PathVariable Long villainId) {
        return villainService.spawnMinions(villainId);
    }

    @PostMapping("/villains/{villainId}/reset")
    public ResponseEntity<Void> resetVillain(@PathVariable Long villainId) {
        villainService.resetVillain(villainId);
        return ResponseEntity.ok().build();
    }

    // --- Reference Data ---

    @GetMapping("/villains/types")
    public List<String> getVillainTypes() {
        return villainService.getVillainTypes();
    }

    // ==================== Bastions ====================

    // --- Bastion CRUD ---

    @GetMapping("/bastions")
    public List<Dnd5eBastion> listBastions() {
        return bastionService.listBastions();
    }

    @GetMapping("/bastions/active")
    public List<Dnd5eBastion> listActiveBastions() {
        return bastionService.listActiveBastions();
    }

    @GetMapping("/bastions/{id}")
    public ResponseEntity<Dnd5eBastion> getBastion(@PathVariable Long id) {
        return bastionService.getBastion(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bastions/by-name")
    public ResponseEntity<Dnd5eBastion> getBastionByName(@RequestParam String name) {
        return bastionService.getBastionByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bastions/theme")
    public List<Dnd5eBastion> listBastionsByTheme(@RequestParam String theme) {
        return bastionService.listBastionsByTheme(theme);
    }

    @GetMapping("/bastions/level-range")
    public List<Dnd5eBastion> listBastionsByLevelRange(@RequestParam int minLevel,
                                                        @RequestParam int maxLevel) {
        return bastionService.listBastionsByLevelRange(minLevel, maxLevel);
    }

    @GetMapping("/bastions/crafting")
    public List<Dnd5eBastion> listBastionsWithCrafting() {
        return bastionService.listBastionsWithCrafting();
    }

    @GetMapping("/bastions/training")
    public List<Dnd5eBastion> listBastionsWithTraining() {
        return bastionService.listBastionsWithTraining();
    }

    @GetMapping("/bastions/library")
    public List<Dnd5eBastion> listBastionsWithLibrary() {
        return bastionService.listBastionsWithLibrary();
    }

    @GetMapping("/bastions/healing")
    public List<Dnd5eBastion> listBastionsWithHealing() {
        return bastionService.listBastionsWithHealing();
    }

    @GetMapping("/bastions/search")
    public List<Dnd5eBastion> searchBastions(@RequestParam String keyword) {
        return bastionService.searchBastions(keyword);
    }

    @PostMapping("/bastions")
    public Dnd5eBastion createBastion(@RequestBody Dnd5eBastion bastion) {
        return bastionService.createBastion(bastion);
    }

    @PutMapping("/bastions/{id}")
    public ResponseEntity<Dnd5eBastion> updateBastion(@PathVariable Long id,
                                                       @RequestBody Dnd5eBastion updates) {
        try {
            Dnd5eBastion updated = bastionService.updateBastion(id, updates);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/bastions/{id}")
    public ResponseEntity<Void> deleteBastion(@PathVariable Long id) {
        if (bastionService.deleteBastion(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Level-Up Mechanics ---

    @PostMapping("/bastions/{bastionId}/level-up")
    public List<String> levelUpBastion(@PathVariable Long bastionId) {
        return bastionService.levelUpBastion(bastionId);
    }

    @GetMapping("/bastions/{bastionId}/perks")
    public List<String> getAvailablePerks(@PathVariable Long bastionId) {
        return bastionService.getAvailablePerks(bastionId);
    }

    // --- Facility Management ---

    @PostMapping("/bastions/{bastionId}/facility")
    public BastionFacilityResult addFacility(@PathVariable Long bastionId,
                                              @RequestBody Map<String, String> body) {
        String facilityType = body.get("facilityType");
        return bastionService.addFacility(bastionId, facilityType);
    }

    // --- Resource Management ---

    @PostMapping("/bastions/{bastionId}/invest")
    public ResponseEntity<Void> investGold(@PathVariable Long bastionId,
                                            @RequestBody Map<String, Integer> body) {
        int amount = body.get("amount");
        bastionService.investGold(bastionId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bastions/{bastionId}/withdraw")
    public Map<String, Boolean> withdrawGold(@PathVariable Long bastionId,
                                              @RequestBody Map<String, Integer> body) {
        int amount = body.get("amount");
        boolean success = bastionService.withdrawGold(bastionId, amount);
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    // --- Bastion Defense ---

    @PostMapping("/bastions/{bastionId}/siege-damage")
    public BastionDamageResult dealSiegeDamage(@PathVariable Long bastionId,
                                                @RequestBody Map<String, Integer> body) {
        int damage = body.get("damage");
        return bastionService.dealSiegeDamage(bastionId, damage);
    }

    @PostMapping("/bastions/{bastionId}/repair")
    public ResponseEntity<Void> repairBastion(@PathVariable Long bastionId,
                                               @RequestBody Map<String, Integer> body) {
        int amount = body.get("amount");
        bastionService.repairBastion(bastionId, amount);
        return ResponseEntity.ok().build();
    }

    // --- Reference Data ---

    @GetMapping("/bastions/themes")
    public List<String> getBastionThemes() {
        return bastionService.getBastionThemes();
    }

    // ==================== Adventure Hooks ====================

    // --- Hook CRUD ---

    @GetMapping("/adventure-hooks")
    public List<Dnd5eAdventureHook> listAdventureHooks() {
        return adventureHookService.listHooks();
    }

    @GetMapping("/adventure-hooks/active")
    public List<Dnd5eAdventureHook> listActiveAdventureHooks() {
        return adventureHookService.listActiveHooks();
    }

    @GetMapping("/adventure-hooks/{id}")
    public ResponseEntity<Dnd5eAdventureHook> getAdventureHook(@PathVariable Long id) {
        return adventureHookService.getHook(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/adventure-hooks/by-name")
    public ResponseEntity<Dnd5eAdventureHook> getAdventureHookByName(@RequestParam String name) {
        return adventureHookService.getHookByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/adventure-hooks/type")
    public List<Dnd5eAdventureHook> listAdventureHooksByType(@RequestParam String type) {
        return adventureHookService.listHooksByType(type);
    }

    @GetMapping("/adventure-hooks/theme")
    public List<Dnd5eAdventureHook> listAdventureHooksByTheme(@RequestParam String theme) {
        return adventureHookService.listHooksByTheme(theme);
    }

    @GetMapping("/adventure-hooks/urgency")
    public List<Dnd5eAdventureHook> listAdventureHooksByUrgency(@RequestParam String urgency) {
        return adventureHookService.listHooksByUrgency(urgency);
    }

    @GetMapping("/adventure-hooks/target-background")
    public List<Dnd5eAdventureHook> listAdventureHooksByTargetBackground(@RequestParam String background) {
        return adventureHookService.listHooksByTargetBackground(background);
    }

    @GetMapping("/adventure-hooks/target-race")
    public List<Dnd5eAdventureHook> listAdventureHooksByTargetRace(@RequestParam String race) {
        return adventureHookService.listHooksByTargetRace(race);
    }

    @GetMapping("/adventure-hooks/target-class")
    public List<Dnd5eAdventureHook> listAdventureHooksByTargetClass(@RequestParam String characterClass) {
        return adventureHookService.listHooksByTargetClass(characterClass);
    }

    @GetMapping("/adventure-hooks/search")
    public List<Dnd5eAdventureHook> searchAdventureHooks(@RequestParam String keyword) {
        return adventureHookService.searchHooks(keyword);
    }

    @PostMapping("/adventure-hooks")
    public Dnd5eAdventureHook createAdventureHook(@RequestBody Dnd5eAdventureHook hook) {
        return adventureHookService.createHook(hook);
    }

    @PutMapping("/adventure-hooks/{id}")
    public ResponseEntity<Dnd5eAdventureHook> updateAdventureHook(@PathVariable Long id,
                                                                   @RequestBody Dnd5eAdventureHook updates) {
        try {
            Dnd5eAdventureHook updated = adventureHookService.updateHook(id, updates);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/adventure-hooks/{id}")
    public ResponseEntity<Void> deleteAdventureHook(@PathVariable Long id) {
        if (adventureHookService.deleteHook(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/adventure-hooks/{id}/toggle-active")
    public ResponseEntity<Void> toggleAdventureHookActive(@PathVariable Long id) {
        adventureHookService.toggleHookActive(id);
        return ResponseEntity.ok().build();
    }

    // --- Hook Generation ---

    @PostMapping("/characters/{characterId}/generate-hooks")
    public List<Dnd5eAdventureHook> generateHooksForCharacter(@PathVariable Long characterId) {
        Dnd5eCharacter character = characterService.getCharacter(characterId);
        return adventureHookService.generateHooksForCharacter(character);
    }

    @PostMapping("/characters/{characterId}/generate-hooks/theme")
    public List<Dnd5eAdventureHook> generateHooksForCharacterWithTheme(
            @PathVariable Long characterId, @RequestParam String theme) {
        Dnd5eCharacter character = characterService.getCharacter(characterId);
        return adventureHookService.generateHooksForCharacterWithTheme(character, theme);
    }

    @GetMapping("/adventure-hooks/generate/level")
    public List<Dnd5eAdventureHook> generateHooksByLevel(@RequestParam int level) {
        return adventureHookService.generateHooksByLevel(level);
    }

    @GetMapping("/adventure-hooks/generate/background")
    public List<Dnd5eAdventureHook> generateHooksByBackground(@RequestParam String background) {
        return adventureHookService.generateHooksByBackground(background);
    }

    @GetMapping("/adventure-hooks/generate/race")
    public List<Dnd5eAdventureHook> generateHooksByRace(@RequestParam String race) {
        return adventureHookService.generateHooksByRace(race);
    }

    @GetMapping("/adventure-hooks/generate/class")
    public List<Dnd5eAdventureHook> generateHooksByClass(@RequestParam String characterClass) {
        return adventureHookService.generateHooksByClass(characterClass);
    }

    @GetMapping("/adventure-hooks/generate/random")
    public ResponseEntity<Dnd5eAdventureHook> generateRandomHook() {
        return adventureHookService.generateRandomHook()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/adventure-hooks/generate/random/type")
    public ResponseEntity<Dnd5eAdventureHook> generateRandomHookByType(@RequestParam String type) {
        return adventureHookService.generateRandomHookByType(type)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/adventure-hooks/generate/random/theme")
    public ResponseEntity<Dnd5eAdventureHook> generateRandomHookByTheme(@RequestParam String theme) {
        return adventureHookService.generateRandomHookByTheme(theme)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // --- Reference Data ---

    @GetMapping("/adventure-hooks/types")
    public List<String> getAdventureHookTypes() {
        return adventureHookService.getHookTypes();
    }

    @GetMapping("/adventure-hooks/themes")
    public List<String> getAdventureHookThemes() {
        return adventureHookService.getHookThemes();
    }

    @GetMapping("/adventure-hooks/urgencies")
    public List<String> getAdventureHookUrgencies() {
        return adventureHookService.getHookUrgencies();
    }

    // ==================== Dungeon Rooms ====================

    @GetMapping("/dungeon-rooms")
    public List<Dnd5eDungeonRoom> listDungeonRooms() {
        return dungeonRoomService.listActiveRooms();
    }

    @GetMapping("/dungeon-rooms/{id}")
    public ResponseEntity<Dnd5eDungeonRoom> getDungeonRoom(@PathVariable Long id) {
        return dungeonRoomService.getRoom(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/dungeon-rooms/dungeon/{dungeonId}")
    public List<Dnd5eDungeonRoom> listRoomsByDungeon(@PathVariable String dungeonId) {
        return dungeonRoomService.listRoomsByDungeon(dungeonId);
    }

    @GetMapping("/dungeon-rooms/theme")
    public List<Dnd5eDungeonRoom> listRoomsByTheme(@RequestParam String theme) {
        return dungeonRoomService.listRoomsByTheme(theme);
    }

    @GetMapping("/dungeon-rooms/level")
    public List<Dnd5eDungeonRoom> listRoomsByLevel(@RequestParam int level) {
        return dungeonRoomService.listRoomsByLevel(level);
    }

    @GetMapping("/dungeon-rooms/dungeon/{dungeonId}/level")
    public List<Dnd5eDungeonRoom> listRoomsByDungeonAndLevel(@PathVariable String dungeonId, @RequestParam int level) {
        return dungeonRoomService.listRoomsByDungeonAndLevel(dungeonId, level);
    }

    @GetMapping("/dungeon-rooms/discovered")
    public List<Dnd5eDungeonRoom> listDiscoveredRooms() {
        return dungeonRoomService.listDiscoveredRooms();
    }

    @GetMapping("/dungeon-rooms/undiscovered")
    public List<Dnd5eDungeonRoom> listUndiscoveredRooms() {
        return dungeonRoomService.listUndiscoveredRooms();
    }

    @GetMapping("/dungeon-rooms/dungeon/{dungeonId}/discovered")
    public List<Dnd5eDungeonRoom> listDiscoveredRoomsByDungeon(@PathVariable String dungeonId) {
        return dungeonRoomService.listDiscoveredRoomsByDungeon(dungeonId);
    }

    @GetMapping("/dungeon-rooms/dungeon/{dungeonId}/undiscovered")
    public List<Dnd5eDungeonRoom> listUndiscoveredRoomsByDungeon(@PathVariable String dungeonId) {
        return dungeonRoomService.listUndiscoveredRoomsByDungeon(dungeonId);
    }

    @GetMapping("/dungeon-rooms/traps")
    public List<Dnd5eDungeonRoom> listRoomsTraps() {
        return dungeonRoomService.listRoomsTraps();
    }

    @GetMapping("/dungeon-rooms/treasure")
    public List<Dnd5eDungeonRoom> listRoomsTreasure() {
        return dungeonRoomService.listRoomsTreasure();
    }

    @GetMapping("/dungeon-rooms/encounters")
    public List<Dnd5eDungeonRoom> listRoomsEncounters() {
        return dungeonRoomService.listRoomsEncounters();
    }

    @GetMapping("/dungeon-rooms/search")
    public List<Dnd5eDungeonRoom> searchDungeonRooms(@RequestParam String keyword) {
        return dungeonRoomService.searchRooms(keyword);
    }

    @PostMapping("/dungeon-rooms")
    public Dnd5eDungeonRoom createDungeonRoom(@RequestBody Dnd5eDungeonRoom room) {
        return dungeonRoomService.createRoom(room);
    }

    @PutMapping("/dungeon-rooms/{id}")
    public ResponseEntity<Dnd5eDungeonRoom> updateDungeonRoom(@PathVariable Long id, @RequestBody Dnd5eDungeonRoom updates) {
        try {
            return ResponseEntity.ok(dungeonRoomService.updateRoom(id, updates));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/dungeon-rooms/{id}")
    public ResponseEntity<Void> deleteDungeonRoom(@PathVariable Long id) {
        if (dungeonRoomService.deleteRoom(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/dungeon-rooms/{id}/toggle-active")
    public ResponseEntity<Void> toggleDungeonRoomActive(@PathVariable Long id) {
        dungeonRoomService.toggleRoomActive(id);
        return ResponseEntity.ok().build();
    }

    // --- Discovery ---

    @PostMapping("/dungeon-rooms/{id}/discover")
    public ResponseEntity<Dnd5eDungeonRoom> discoverRoom(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(dungeonRoomService.discoverRoom(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/dungeon-rooms/{id}/undiscover")
    public ResponseEntity<Void> undiscoverRoom(@PathVariable Long id) {
        dungeonRoomService.undiscoverRoom(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dungeon-rooms/dungeon/{dungeonId}/discover-all")
    public ResponseEntity<Void> discoverAllInDungeon(@PathVariable String dungeonId) {
        dungeonRoomService.discoverAllInDungeon(dungeonId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dungeon-rooms/dungeon/{dungeonId}/undiscover-all")
    public ResponseEntity<Void> undiscoverAllInDungeon(@PathVariable String dungeonId) {
        dungeonRoomService.undiscoverAllInDungeon(dungeonId);
        return ResponseEntity.ok().build();
    }

    // --- Connections ---

    @PostMapping("/dungeon-rooms/{fromId}/connect/{toId}")
    public ResponseEntity<Void> addConnection(@PathVariable Long fromId, @PathVariable Long toId) {
        try {
            dungeonRoomService.addConnection(fromId, toId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/dungeon-rooms/{fromId}/connect/{toId}")
    public ResponseEntity<Void> removeConnection(@PathVariable Long fromId, @PathVariable Long toId) {
        try {
            dungeonRoomService.removeConnection(fromId, toId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/dungeon-rooms/{id}/connections")
    public List<Dnd5eDungeonRoom> getConnectedRooms(@PathVariable Long id) {
        return dungeonRoomService.getConnectedRooms(id);
    }

    @PostMapping("/dungeon-rooms/dungeon/{dungeonId}/circular-connections")
    public ResponseEntity<Void> createCircularConnections(@PathVariable String dungeonId) {
        dungeonRoomService.createCircularConnections(dungeonId);
        return ResponseEntity.ok().build();
    }

    // --- Trap Checks ---

    @PostMapping("/dungeon-rooms/{id}/trap-check")
    public TrapCheckResult makeTrapCheck(@PathVariable Long id,
                                          @RequestParam int dexModifier,
                                          @RequestParam int proficiencyBonus,
                                          @RequestParam int roll) {
        return dungeonRoomService.makeTrapCheck(id, dexModifier, proficiencyBonus, roll);
    }

    // --- Treasure ---

    @GetMapping("/dungeon-rooms/{id}/treasure")
    public ResponseEntity<TreasureResult> discoverTreasure(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(dungeonRoomService.discoverTreasure(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Reference Data ---

    @GetMapping("/dungeon-rooms/dungeons")
    public List<String> listDungeonIds() {
        return dungeonRoomService.listDungeonIds();
    }

    @GetMapping("/dungeon-rooms/themes")
    public List<String> listDungeonThemes() {
        return dungeonRoomService.listThemes();
    }

    // ==================== Tension Pools ====================

    @PostMapping("/tension-pools")
    public Dnd5eTensionPool createTensionPool(@RequestBody Dnd5eTensionPool pool) {
        return tensionPoolService.createPool(pool);
    }

    @GetMapping("/tension-pools")
    public List<Dnd5eTensionPool> listTensionPools() {
        return tensionPoolService.listPools();
    }

    @GetMapping("/tension-pools/{id}")
    public ResponseEntity<Dnd5eTensionPool> getTensionPool(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tensionPoolService.getPool(id).orElseThrow());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tension-pools/area/{areaId}")
    public List<Dnd5eTensionPool> listTensionPoolsByArea(@PathVariable String areaId) {
        return tensionPoolService.listPoolsByArea(areaId);
    }

    @GetMapping("/tension-pools/search")
    public List<Dnd5eTensionPool> searchTensionPools(@RequestParam String keyword) {
        return tensionPoolService.searchPools(keyword);
    }

    @DeleteMapping("/tension-pools/{id}")
    public ResponseEntity<Void> deleteTensionPool(@PathVariable Long id) {
        tensionPoolService.deletePool(id);
        return ResponseEntity.ok().build();
    }

    // --- Tension Management ---

    @PostMapping("/tension-pools/{id}/increase")
    public List<TensionConsequence> increaseTension(@PathVariable Long id, @RequestParam int amount) {
        return tensionPoolService.increaseTension(id, amount);
    }

    @PostMapping("/tension-pools/{id}/decrease")
    public ResponseEntity<Dnd5eTensionPool> decreaseTension(@PathVariable Long id, @RequestParam int amount) {
        try {
            return ResponseEntity.ok(tensionPoolService.decreaseTension(id, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tension-pools/{id}/set")
    public ResponseEntity<Dnd5eTensionPool> setTension(@PathVariable Long id, @RequestParam int tension) {
        try {
            return ResponseEntity.ok(tensionPoolService.setTension(id, tension));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tension-pools/{id}/reset")
    public ResponseEntity<Dnd5eTensionPool> resetTension(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tensionPoolService.resetTension(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tension-pools/reset-all")
    public Map<String, Integer> resetAllTensionPools() {
        int count = tensionPoolService.resetAllPools();
        Map<String, Integer> result = new HashMap<>();
        result.put("resetCount", count);
        return result;
    }

    // --- Wandering Encounters ---

    @PostMapping("/tension-pools/{id}/wandering-encounter")
    public WanderingEncounterResult rollWanderingEncounter(@PathVariable Long id, @RequestParam int roll) {
        return tensionPoolService.rollWanderingEncounter(id, roll);
    }

    @GetMapping("/tension-pools/{id}/encounter-dc")
    public int getEncounterDc(@PathVariable Long id) {
        return tensionPoolService.getEncounterDc(id);
    }

    // --- Reference Data ---

    @GetMapping("/tension-pools/areas")
    public List<String> listTensionPoolAreas() {
        return tensionPoolService.listAreas();
    }

    @GetMapping("/tension-pools/maxed")
    public List<Dnd5eTensionPool> listMaxedTensionPools() {
        return tensionPoolService.listMaxedPools();
    }

    @GetMapping("/tension-pools/empty")
    public List<Dnd5eTensionPool> listEmptyTensionPools() {
        return tensionPoolService.listEmptyPools();
    }

    // ==================== Magic Items ====================

    @PostMapping("/magic-items")
    public Dnd5eMagicItem createMagicItem(@RequestBody Dnd5eMagicItem item) {
        return magicItemService.createMagicItem(item);
    }

    @GetMapping("/magic-items")
    public List<Dnd5eMagicItem> listMagicItems() {
        return magicItemService.listMagicItems();
    }

    @GetMapping("/magic-items/active")
    public List<Dnd5eMagicItem> listActiveMagicItems() {
        return magicItemService.listActiveMagicItems();
    }

    @GetMapping("/magic-items/{id}")
    public ResponseEntity<Dnd5eMagicItem> getMagicItem(@PathVariable Long id) {
        return magicItemService.getMagicItem(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/magic-items/name/{name}")
    public ResponseEntity<Dnd5eMagicItem> getMagicItemByName(@PathVariable String name) {
        return magicItemService.getMagicItemByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/magic-items/{id}")
    public ResponseEntity<Dnd5eMagicItem> updateMagicItem(@PathVariable Long id, @RequestBody Dnd5eMagicItem updated) {
        try {
            return ResponseEntity.ok(magicItemService.updateMagicItem(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/magic-items/{id}")
    public ResponseEntity<Void> deleteMagicItem(@PathVariable Long id) {
        magicItemService.deleteMagicItem(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/magic-items/rarity/{rarity}")
    public List<Dnd5eMagicItem> listMagicItemsByRarity(@PathVariable String rarity) {
        return magicItemService.listMagicItemsByRarity(rarity);
    }

    @GetMapping("/magic-items/category/{category}")
    public List<Dnd5eMagicItem> listMagicItemsByCategory(@PathVariable String category) {
        return magicItemService.listMagicItemsByCategory(category);
    }

    @GetMapping("/magic-items/attunement")
    public List<Dnd5eMagicItem> listAttunementItems() {
        return magicItemService.listAttunementItems();
    }

    @GetMapping("/magic-items/charged")
    public List<Dnd5eMagicItem> listChargedItems() {
        return magicItemService.listChargedItems();
    }

    @GetMapping("/magic-items/cursed")
    public List<Dnd5eMagicItem> listCursedItems() {
        return magicItemService.listCursedItems();
    }

    @GetMapping("/magic-items/search")
    public List<Dnd5eMagicItem> searchMagicItems(@RequestParam String keyword) {
        return magicItemService.searchMagicItems(keyword);
    }

    @GetMapping("/magic-items/rarities")
    public List<String> listAllMagicItemRarities() {
        return magicItemService.listAllRarities();
    }

    @GetMapping("/magic-items/categories")
    public List<String> listAllMagicItemCategories() {
        return magicItemService.listAllCategories();
    }

    // --- Character-Item Binding ---

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}")
    public ResponseEntity<Dnd5eCharacterMagicItem> giveItemToCharacter(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            return ResponseEntity.ok(magicItemService.giveItemToCharacter(characterId, magicItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/characters/{characterId}/magic-items")
    public List<Dnd5eCharacterMagicItem> listCharacterMagicItems(@PathVariable Long characterId) {
        return magicItemService.listCharacterItems(characterId);
    }

    @GetMapping("/characters/{characterId}/magic-items/attuned")
    public List<Dnd5eCharacterMagicItem> listAttunedMagicItems(@PathVariable Long characterId) {
        return magicItemService.listAttunedItems(characterId);
    }

    @GetMapping("/characters/{characterId}/magic-items/count-attuned")
    public int countAttunedMagicItems(@PathVariable Long characterId) {
        return magicItemService.countAttunedItems(characterId);
    }

    @GetMapping("/characters/{characterId}/magic-items/{magicItemId}")
    public ResponseEntity<Dnd5eCharacterMagicItem> getCharacterMagicItem(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        return magicItemService.getCharacterItem(characterId, magicItemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/characters/{characterId}/magic-items/{magicItemId}")
    public ResponseEntity<Void> removeItemFromCharacter(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            magicItemService.removeItemFromCharacter(characterId, magicItemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Attunement ---

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}/attune")
    public ResponseEntity<Dnd5eCharacterMagicItem> attuneToItem(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            return ResponseEntity.ok(magicItemService.attuneToItem(characterId, magicItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}/deattune")
    public ResponseEntity<Dnd5eCharacterMagicItem> deattuneFromItem(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            return ResponseEntity.ok(magicItemService.deattuneFromItem(characterId, magicItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/characters/{characterId}/magic-items/can-attune-more")
    public boolean canAttuneMore(@PathVariable Long characterId) {
        return magicItemService.canAttuneMore(characterId);
    }

    // --- Charges ---

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}/use")
    public ResponseEntity<UseItemResult> useMagicItem(@PathVariable Long characterId, @PathVariable Long magicItemId,
                                                      @RequestParam(defaultValue = "1") int charges) {
        try {
            return ResponseEntity.ok(magicItemService.useItem(characterId, magicItemId, charges));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}/recharge")
    public ResponseEntity<Dnd5eCharacterMagicItem> rechargeMagicItem(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            return ResponseEntity.ok(magicItemService.rechargeItem(characterId, magicItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/characters/{characterId}/magic-items/recharge-short-rest")
    public int rechargeAfterShortRest(@PathVariable Long characterId) {
        return magicItemService.rechargeAfterShortRest(characterId);
    }

    @PostMapping("/characters/{characterId}/magic-items/recharge-long-rest")
    public int rechargeAfterLongRest(@PathVariable Long characterId) {
        return magicItemService.rechargeAfterLongRest(characterId);
    }

    @PostMapping("/characters/{characterId}/magic-items/recharge-dawn")
    public int rechargeAtDawn(@PathVariable Long characterId) {
        return magicItemService.rechargeAtDawn(characterId);
    }

    // --- Curses ---

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}/reveal-curse")
    public ResponseEntity<Dnd5eCharacterMagicItem> revealCurse(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            return ResponseEntity.ok(magicItemService.revealCurse(characterId, magicItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/characters/{characterId}/magic-items/{magicItemId}/remove-curse")
    public ResponseEntity<Boolean> removeCurse(@PathVariable Long characterId, @PathVariable Long magicItemId) {
        try {
            return ResponseEntity.ok(magicItemService.removeCurse(characterId, magicItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
