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
import com.yisy.modules.doramcu.dnd5e.dto.EncounterAnalysis;
import com.yisy.modules.doramcu.dnd5e.dto.StartCraftingProjectRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eAdventureHook;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBastion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBackground;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eClass;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eClassFeature;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterClass;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSubclass;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignTemplate;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterBackground;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterToolProficiency;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombatant;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingProject;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingRecipe;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDarkGift;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDungeonRoom;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eExhaustion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFearDread;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eHeroicInspiration;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterGrowingItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eGrowingMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsionicPower;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eEncounter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eModule;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonsterAbility;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpecies;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eTensionPool;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillain;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillainPower;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBackgroundService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eClassService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignTemplateService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCraftingService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService.TrapCheckResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService.TreasureResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eExhaustionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eFeatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHeroicInspirationService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService.ItemAbilities;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService.ItemLevelUpResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMagicItemService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMagicItemService.UseItemResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService.PsionicPowerResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHorrorService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eVillainService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eVillainService.VillainDamageResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eAdventureHookService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService.BastionFacilityResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService.BastionDamageResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService.ToolCheckResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGameplayService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eInventoryService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpellService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eEncounterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eModuleService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMonsterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eNpcService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpeciesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService.TensionConsequence;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService.WanderingEncounterResult;
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
    private final Dnd5eHorrorService horrorService;
    private final Dnd5eGrowingMagicItemService growingItemService;
    private final Dnd5eVillainService villainService;
    private final Dnd5eBastionService bastionService;
    private final Dnd5eAdventureHookService adventureHookService;
    private final Dnd5eDungeonRoomService dungeonRoomService;
    private final Dnd5eTensionPoolService tensionPoolService;
    private final Dnd5eNpcService npcService;
    private final Dnd5eEncounterService encounterService;
    private final Dnd5eModuleService moduleService;
    private final Dnd5eSpeciesService speciesService;
    private final Dnd5eCampaignTemplateService campaignTemplateService;
    private final Dnd5eMagicItemService magicItemService;
    private final Dnd5eBackgroundService backgroundService;
    private final Dnd5eClassService classService;
    private final Dnd5eMonsterService monsterService;

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
                           Dnd5ePsionService psionService,
                           Dnd5eHorrorService horrorService,
                           Dnd5eGrowingMagicItemService growingItemService,
                           Dnd5eVillainService villainService,
                           Dnd5eBastionService bastionService,
                           Dnd5eAdventureHookService adventureHookService,
                           Dnd5eDungeonRoomService dungeonRoomService,
                           Dnd5eTensionPoolService tensionPoolService,
                           Dnd5eNpcService npcService,
                           Dnd5eEncounterService encounterService,
                           Dnd5eModuleService moduleService,
                           Dnd5eSpeciesService speciesService,
                           Dnd5eCampaignTemplateService campaignTemplateService,
                           Dnd5eMagicItemService magicItemService,
                           Dnd5eBackgroundService backgroundService,
                           Dnd5eClassService classService,
                           Dnd5eMonsterService monsterService) {
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
        this.horrorService = horrorService;
        this.growingItemService = growingItemService;
        this.villainService = villainService;
        this.bastionService = bastionService;
        this.adventureHookService = adventureHookService;
        this.dungeonRoomService = dungeonRoomService;
        this.tensionPoolService = tensionPoolService;
        this.npcService = npcService;
        this.encounterService = encounterService;
        this.moduleService = moduleService;
        this.speciesService = speciesService;
        this.campaignTemplateService = campaignTemplateService;
        this.magicItemService = magicItemService;
        this.backgroundService = backgroundService;
        this.classService = classService;
        this.monsterService = monsterService;
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

    // ==================== Modules ====================

    @PostMapping("/modules")
    public Dnd5eModule createModule(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String tagline = (String) request.get("tagline");
        String description = (String) request.get("description");
        String theme = (String) request.get("theme");
        int minLevel = request.get("minLevel") != null ? (Integer) request.get("minLevel") : 1;
        int maxLevel = request.get("maxLevel") != null ? (Integer) request.get("maxLevel") : 5;
        int estimatedSessions = request.get("estimatedSessions") != null ? (Integer) request.get("estimatedSessions") : 4;
        return moduleService.createModule(name, tagline, description, theme, minLevel, maxLevel, estimatedSessions);
    }

    @GetMapping("/modules")
    public List<Dnd5eModule> listModules() {
        return moduleService.listModules();
    }

    @GetMapping("/modules/{id}")
    public ResponseEntity<Dnd5eModule> getModule(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.getModule(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/modules/{id}")
    public ResponseEntity<Dnd5eModule> updateModule(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String tagline = (String) request.get("tagline");
            String description = (String) request.get("description");
            String theme = (String) request.get("theme");
            int minLevel = request.get("minLevel") != null ? (Integer) request.get("minLevel") : 1;
            int maxLevel = request.get("maxLevel") != null ? (Integer) request.get("maxLevel") : 5;
            int estimatedSessions = request.get("estimatedSessions") != null ? (Integer) request.get("estimatedSessions") : 4;
            String status = (String) request.get("status");
            String summary = (String) request.get("summary");
            String dmNotes = (String) request.get("dmNotes");
            return ResponseEntity.ok(moduleService.updateModule(id, name, tagline, description, theme,
                    minLevel, maxLevel, estimatedSessions, status, summary, dmNotes));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        try {
            moduleService.deleteModule(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/theme/{theme}")
    public List<Dnd5eModule> listModulesByTheme(@PathVariable String theme) {
        return moduleService.listModulesByTheme(theme);
    }

    @GetMapping("/modules/status/{status}")
    public List<Dnd5eModule> listModulesByStatus(@PathVariable String status) {
        return moduleService.listModulesByStatus(status);
    }

    @GetMapping("/modules/party-level/{partyLevel}")
    public List<Dnd5eModule> listModulesByPartyLevel(@PathVariable int partyLevel) {
        return moduleService.listModulesByPartyLevel(partyLevel);
    }

    @GetMapping("/modules/search")
    public List<Dnd5eModule> searchModules(@RequestParam String keyword) {
        return moduleService.searchModules(keyword);
    }

    // --- Component Linking ---

    @PostMapping("/modules/{id}/hooks/{hookId}")
    public ResponseEntity<Dnd5eModule> addHook(@PathVariable Long id, @PathVariable String hookId) {
        try {
            return ResponseEntity.ok(moduleService.addHook(id, hookId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/hooks/{hookId}")
    public ResponseEntity<Dnd5eModule> removeHook(@PathVariable Long id, @PathVariable String hookId) {
        try {
            return ResponseEntity.ok(moduleService.removeHook(id, hookId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/hooks")
    public List<String> getHookIds(@PathVariable Long id) {
        return moduleService.getHookIds(id);
    }

    @PostMapping("/modules/{id}/rooms/{roomId}")
    public ResponseEntity<Dnd5eModule> addRoom(@PathVariable Long id, @PathVariable String roomId) {
        try {
            return ResponseEntity.ok(moduleService.addRoom(id, roomId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/rooms/{roomId}")
    public ResponseEntity<Dnd5eModule> removeRoom(@PathVariable Long id, @PathVariable String roomId) {
        try {
            return ResponseEntity.ok(moduleService.removeRoom(id, roomId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/rooms")
    public List<String> getRoomIds(@PathVariable Long id) {
        return moduleService.getRoomIds(id);
    }

    @PostMapping("/modules/{id}/npcs/{npcId}")
    public ResponseEntity<Dnd5eModule> addNpc(@PathVariable Long id, @PathVariable String npcId) {
        try {
            return ResponseEntity.ok(moduleService.addNpc(id, npcId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/npcs/{npcId}")
    public ResponseEntity<Dnd5eModule> removeNpc(@PathVariable Long id, @PathVariable String npcId) {
        try {
            return ResponseEntity.ok(moduleService.removeNpc(id, npcId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/npcs")
    public List<String> getNpcIds(@PathVariable Long id) {
        return moduleService.getNpcIds(id);
    }

    @PostMapping("/modules/{id}/encounters/{encounterId}")
    public ResponseEntity<Dnd5eModule> addEncounter(@PathVariable Long id, @PathVariable String encounterId) {
        try {
            return ResponseEntity.ok(moduleService.addEncounter(id, encounterId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/encounters/{encounterId}")
    public ResponseEntity<Dnd5eModule> removeEncounter(@PathVariable Long id, @PathVariable String encounterId) {
        try {
            return ResponseEntity.ok(moduleService.removeEncounter(id, encounterId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/encounters")
    public List<String> getEncounterIds(@PathVariable Long id) {
        return moduleService.getEncounterIds(id);
    }

    @PostMapping("/modules/{id}/villains/{villainId}")
    public ResponseEntity<Dnd5eModule> addVillain(@PathVariable Long id, @PathVariable String villainId) {
        try {
            return ResponseEntity.ok(moduleService.addVillain(id, villainId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/villains/{villainId}")
    public ResponseEntity<Dnd5eModule> removeVillain(@PathVariable Long id, @PathVariable String villainId) {
        try {
            return ResponseEntity.ok(moduleService.removeVillain(id, villainId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/villains")
    public List<String> getVillainIds(@PathVariable Long id) {
        return moduleService.getVillainIds(id);
    }

    @PostMapping("/modules/{id}/bastions/{bastionId}")
    public ResponseEntity<Dnd5eModule> addBastion(@PathVariable Long id, @PathVariable String bastionId) {
        try {
            return ResponseEntity.ok(moduleService.addBastion(id, bastionId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/bastions/{bastionId}")
    public ResponseEntity<Dnd5eModule> removeBastion(@PathVariable Long id, @PathVariable String bastionId) {
        try {
            return ResponseEntity.ok(moduleService.removeBastion(id, bastionId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/bastions")
    public List<String> getBastionIds(@PathVariable Long id) {
        return moduleService.getBastionIds(id);
    }

    @PostMapping("/modules/{id}/tension-pools/{tensionPoolId}")
    public ResponseEntity<Dnd5eModule> addTensionPool(@PathVariable Long id, @PathVariable String tensionPoolId) {
        try {
            return ResponseEntity.ok(moduleService.addTensionPool(id, tensionPoolId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/tension-pools/{tensionPoolId}")
    public ResponseEntity<Dnd5eModule> removeTensionPool(@PathVariable Long id, @PathVariable String tensionPoolId) {
        try {
            return ResponseEntity.ok(moduleService.removeTensionPool(id, tensionPoolId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/tension-pools")
    public List<String> getTensionPoolIds(@PathVariable Long id) {
        return moduleService.getTensionPoolIds(id);
    }

    @PostMapping("/modules/{id}/growing-items/{itemId}")
    public ResponseEntity<Dnd5eModule> addGrowingItem(@PathVariable Long id, @PathVariable String itemId) {
        try {
            return ResponseEntity.ok(moduleService.addGrowingItem(id, itemId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/growing-items/{itemId}")
    public ResponseEntity<Dnd5eModule> removeGrowingItem(@PathVariable Long id, @PathVariable String itemId) {
        try {
            return ResponseEntity.ok(moduleService.removeGrowingItem(id, itemId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/growing-items")
    public List<String> getGrowingItemIds(@PathVariable Long id) {
        return moduleService.getGrowingItemIds(id);
    }

    @PostMapping("/modules/{id}/dark-gifts/{darkGiftId}")
    public ResponseEntity<Dnd5eModule> addDarkGift(@PathVariable Long id, @PathVariable String darkGiftId) {
        try {
            return ResponseEntity.ok(moduleService.addDarkGift(id, darkGiftId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/dark-gifts/{darkGiftId}")
    public ResponseEntity<Dnd5eModule> removeDarkGift(@PathVariable Long id, @PathVariable String darkGiftId) {
        try {
            return ResponseEntity.ok(moduleService.removeDarkGift(id, darkGiftId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/dark-gifts")
    public List<String> getDarkGiftIds(@PathVariable Long id) {
        return moduleService.getDarkGiftIds(id);
    }

    @PostMapping("/modules/{id}/feats/{featId}")
    public ResponseEntity<Dnd5eModule> addFeat(@PathVariable Long id, @PathVariable String featId) {
        try {
            return ResponseEntity.ok(moduleService.addFeat(id, featId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/feats/{featId}")
    public ResponseEntity<Dnd5eModule> removeFeat(@PathVariable Long id, @PathVariable String featId) {
        try {
            return ResponseEntity.ok(moduleService.removeFeat(id, featId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/feats")
    public List<String> getFeatIds(@PathVariable Long id) {
        return moduleService.getFeatIds(id);
    }

    @PostMapping("/modules/{id}/crafting-recipes/{recipeId}")
    public ResponseEntity<Dnd5eModule> addCraftingRecipe(@PathVariable Long id, @PathVariable String recipeId) {
        try {
            return ResponseEntity.ok(moduleService.addCraftingRecipe(id, recipeId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/crafting-recipes/{recipeId}")
    public ResponseEntity<Dnd5eModule> removeCraftingRecipe(@PathVariable Long id, @PathVariable String recipeId) {
        try {
            return ResponseEntity.ok(moduleService.removeCraftingRecipe(id, recipeId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/crafting-recipes")
    public List<String> getCraftingRecipeIds(@PathVariable Long id) {
        return moduleService.getCraftingRecipeIds(id);
    }

    @PostMapping("/modules/{id}/psionic-powers/{powerId}")
    public ResponseEntity<Dnd5eModule> addPsionicPower(@PathVariable Long id, @PathVariable String powerId) {
        try {
            return ResponseEntity.ok(moduleService.addPsionicPower(id, powerId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}/psionic-powers/{powerId}")
    public ResponseEntity<Dnd5eModule> removePsionicPower(@PathVariable Long id, @PathVariable String powerId) {
        try {
            return ResponseEntity.ok(moduleService.removePsionicPower(id, powerId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/psionic-powers")
    public List<String> getPsionicPowerIds(@PathVariable Long id) {
        return moduleService.getPsionicPowerIds(id);
    }

    // --- Summary & Statistics ---

    @PostMapping("/modules/{id}/generate-summary")
    public ResponseEntity<String> generateSummary(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.generateSummary(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getModuleStatistics(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.getModuleStatistics(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/modules/{id}/increment-play-count")
    public ResponseEntity<Dnd5eModule> incrementPlayCount(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.incrementPlayCount(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/modules/{id}/rating")
    public ResponseEntity<Dnd5eModule> setRating(@PathVariable Long id, @RequestParam double rating) {
        try {
            return ResponseEntity.ok(moduleService.setRating(id, rating));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/modules/{id}/validate")
    public ResponseEntity<List<String>> validateModule(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.validateModule(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Reference Data ---

    @GetMapping("/modules/themes")
    public List<String> listModuleThemes() {
        return moduleService.listThemes();
    }

    @GetMapping("/modules/statuses")
    public List<String> listModuleStatuses() {
        return moduleService.listStatuses();
    }

    @GetMapping("/modules/most-played")
    public List<Dnd5eModule> findMostPlayedModules() {
        return moduleService.findMostPlayed();
    }

    @GetMapping("/modules/top-rated")
    public List<Dnd5eModule> findTopRatedModules(@RequestParam(defaultValue = "4.0") double minRating) {
        return moduleService.findTopRated(minRating);
    }

    @GetMapping("/modules/theme-suggestions")
    public Map<String, Object> getThemeSuggestions(@RequestParam String theme) {
        return moduleService.getThemeSuggestions(theme);
    }

    // ==================== Species ====================

    @PostMapping("/species")
    public Dnd5eSpecies createSpecies(@RequestBody Dnd5eSpecies species) {
        return speciesService.createSpecies(species);
    }

    @GetMapping("/species")
    public List<Dnd5eSpecies> listSpecies() {
        return speciesService.listSpecies();
    }

    @GetMapping("/species/{id}")
    public ResponseEntity<Dnd5eSpecies> getSpecies(@PathVariable Long id) {
        return speciesService.getSpecies(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/species/name/{name}")
    public ResponseEntity<Dnd5eSpecies> getSpeciesByName(@PathVariable String name) {
        return speciesService.getSpeciesByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/species/{id}")
    public ResponseEntity<Dnd5eSpecies> updateSpecies(@PathVariable Long id, @RequestBody Dnd5eSpecies updated) {
        try {
            return ResponseEntity.ok(speciesService.updateSpecies(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/species/{id}")
    public ResponseEntity<Void> deleteSpecies(@PathVariable Long id) {
        speciesService.deleteSpecies(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/species/category/{category}")
    public List<Dnd5eSpecies> listSpeciesByCategory(@PathVariable String category) {
        return speciesService.listSpeciesByCategory(category);
    }

    @GetMapping("/species/size/{size}")
    public List<Dnd5eSpecies> listSpeciesBySize(@PathVariable String size) {
        return speciesService.listSpeciesBySize(size);
    }

    @GetMapping("/species/subspecies/{subspecies}")
    public List<Dnd5eSpecies> listSpeciesBySubspecies(@PathVariable String subspecies) {
        return speciesService.listSpeciesBySubspecies(subspecies);
    }

    @GetMapping("/species/darkvision")
    public List<Dnd5eSpecies> listSpeciesWithDarkvision() {
        return speciesService.listSpeciesWithDarkvision();
    }

    @GetMapping("/species/lifespan")
    public List<Dnd5eSpecies> listSpeciesByLifespan(@RequestParam int minLifespan) {
        return speciesService.listSpeciesByLifespan(minLifespan);
    }

    @GetMapping("/species/search")
    public List<Dnd5eSpecies> searchSpecies(@RequestParam String keyword) {
        return speciesService.searchSpecies(keyword);
    }

    @GetMapping("/species/trait/{trait}")
    public List<Dnd5eSpecies> listSpeciesByRacialTrait(@PathVariable String trait) {
        return speciesService.listSpeciesByRacialTrait(trait);
    }

    // --- Name Generation ---

    @GetMapping("/species/{speciesName}/generate-name")
    public Map<String, String> generateName(@PathVariable String speciesName,
                                             @RequestParam(defaultValue = "true") boolean male) {
        String first = speciesService.generateFirstName(speciesName, male);
        String last = speciesService.generateLastName(speciesName);
        String full = speciesService.generateFullName(speciesName, male);
        Map<String, String> result = new HashMap<>();
        result.put("firstName", first);
        result.put("lastName", last);
        result.put("fullName", full);
        return result;
    }

    @GetMapping("/species/{speciesName}/generate-title")
    public ResponseEntity<String> generateTitle(@PathVariable String speciesName,
                                                 @RequestParam(defaultValue = "leader") String role) {
        String title = speciesService.generateTitle(speciesName, role);
        if (title.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(title);
    }

    // --- Ability Score Increases ---

    @GetMapping("/species/{speciesName}/ability-increases")
    public ResponseEntity<Map<String, Integer>> getAbilityIncreases(@PathVariable String speciesName) {
        Map<String, Integer> increases = speciesService.getAbilityIncreases(speciesName);
        if (increases.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(increases);
    }

    @GetMapping("/species/{speciesName}/racial-traits")
    public ResponseEntity<List<String>> getRacialTraits(@PathVariable String speciesName) {
        List<String> traits = speciesService.getRacialTraits(speciesName);
        if (traits.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(traits);
    }

    // --- Reference Data ---

    @GetMapping("/species/categories")
    public List<String> listSpeciesCategories() {
        return speciesService.getCategories();
    }

    @GetMapping("/species/sizes")
    public List<String> listSpeciesSizes() {
        return speciesService.getSizes();
    }

    // ==================== Campaign Templates ====================

    @PostMapping("/campaign-templates")
    public Dnd5eCampaignTemplate createCampaignTemplate(@RequestBody Dnd5eCampaignTemplate template) {
        return campaignTemplateService.createTemplate(template);
    }

    @GetMapping("/campaign-templates")
    public List<Dnd5eCampaignTemplate> listCampaignTemplates() {
        return campaignTemplateService.listTemplates();
    }

    @GetMapping("/campaign-templates/active")
    public List<Dnd5eCampaignTemplate> listActiveCampaignTemplates() {
        return campaignTemplateService.listActiveTemplates();
    }

    @GetMapping("/campaign-templates/{id}")
    public ResponseEntity<Dnd5eCampaignTemplate> getCampaignTemplate(@PathVariable Long id) {
        return campaignTemplateService.getTemplate(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/campaign-templates/name/{name}")
    public ResponseEntity<Dnd5eCampaignTemplate> getCampaignTemplateByName(@PathVariable String name) {
        return campaignTemplateService.getTemplateByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/campaign-templates/{id}")
    public ResponseEntity<Dnd5eCampaignTemplate> updateCampaignTemplate(@PathVariable Long id, @RequestBody Dnd5eCampaignTemplate updated) {
        try {
            return ResponseEntity.ok(campaignTemplateService.updateTemplate(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/campaign-templates/{id}")
    public ResponseEntity<Void> deleteCampaignTemplate(@PathVariable Long id) {
        campaignTemplateService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/campaign-templates/{id}/toggle-active")
    public ResponseEntity<Void> toggleCampaignTemplateActive(@PathVariable Long id) {
        try {
            campaignTemplateService.toggleTemplateActive(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/campaign-templates/theme/{theme}")
    public List<Dnd5eCampaignTemplate> listCampaignTemplatesByTheme(@PathVariable String theme) {
        return campaignTemplateService.listTemplatesByTheme(theme);
    }

    @GetMapping("/campaign-templates/level/{partyLevel}")
    public List<Dnd5eCampaignTemplate> listCampaignTemplatesByPartyLevel(@PathVariable int partyLevel) {
        return campaignTemplateService.listTemplatesByPartyLevel(partyLevel);
    }

    @GetMapping("/campaign-templates/level-range")
    public List<Dnd5eCampaignTemplate> listCampaignTemplatesByLevelRange(@RequestParam int minLevel, @RequestParam int maxLevel) {
        return campaignTemplateService.listTemplatesByLevelRange(minLevel, maxLevel);
    }

    @GetMapping("/campaign-templates/search")
    public List<Dnd5eCampaignTemplate> searchCampaignTemplates(@RequestParam String keyword) {
        return campaignTemplateService.searchTemplates(keyword);
    }

    @GetMapping("/campaign-templates/{id}/summary")
    public ResponseEntity<Map<String, Object>> getCampaignTemplateSummary(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(campaignTemplateService.getTemplateSummary(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/campaign-templates/themes")
    public List<String> listCampaignTemplateThemes() {
        return campaignTemplateService.listAllThemes();
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

    // ==================== Backgrounds ====================

    // --- Background CRUD ---

    @GetMapping("/backgrounds")
    public List<Dnd5eBackground> listBackgrounds() {
        return backgroundService.listBackgrounds();
    }

    @GetMapping("/backgrounds/all")
    public List<Dnd5eBackground> listAllBackgrounds() {
        return backgroundService.listAllBackgrounds();
    }

    @GetMapping("/backgrounds/{id}")
    public Dnd5eBackground getBackground(@PathVariable Long id) {
        return backgroundService.getBackground(id);
    }

    @GetMapping("/backgrounds/name/{name}")
    public Dnd5eBackground getBackgroundByName(@PathVariable String name) {
        return backgroundService.getBackgroundByName(name);
    }

    @GetMapping("/backgrounds/category/{category}")
    public List<Dnd5eBackground> listBackgroundsByCategory(@PathVariable String category) {
        return backgroundService.listBackgroundsByCategory(category);
    }

    @GetMapping("/backgrounds/skill/{skill}")
    public List<Dnd5eBackground> listBackgroundsBySkill(@PathVariable String skill) {
        return backgroundService.listBackgroundsBySkill(skill);
    }

    @GetMapping("/backgrounds/search")
    public List<Dnd5eBackground> searchBackgrounds(@RequestParam String keyword) {
        return backgroundService.searchBackgrounds(keyword);
    }

    @GetMapping("/backgrounds/categories")
    public List<String> listBackgroundCategories() {
        return backgroundService.listCategories();
    }

    @PostMapping("/backgrounds")
    public Dnd5eBackground createBackground(@RequestParam String name,
                                             @RequestParam String description,
                                             @RequestParam(defaultValue = "Common") String category,
                                             @RequestParam String skillProficiencies,
                                             @RequestParam(required = false) String toolProficiencies,
                                             @RequestParam(required = false) String featureSlots,
                                             @RequestParam String feature,
                                             @RequestParam(required = false) String featureDescription,
                                             @RequestParam(required = false) String equipment) {
        return backgroundService.createBackground(name, description, category,
                skillProficiencies, toolProficiencies, featureSlots, feature,
                featureDescription, equipment);
    }

    @DeleteMapping("/backgrounds/{name}")
    public ResponseEntity<Void> deleteBackground(@PathVariable String name) {
        try {
            backgroundService.deleteBackground(name);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Proficiencies ---

    @GetMapping("/backgrounds/{backgroundName}/skill-proficiencies")
    public List<String> getBackgroundSkillProficiencies(@PathVariable String backgroundName) {
        return backgroundService.getSkillProficiencies(backgroundName);
    }

    @GetMapping("/backgrounds/{backgroundName}/tool-proficiencies")
    public List<String> getBackgroundToolProficiencies(@PathVariable String backgroundName) {
        return backgroundService.getToolProficiencies(backgroundName);
    }

    @GetMapping("/backgrounds/{backgroundName}/feature-slot-options")
    public List<String> getBackgroundFeatureSlotOptions(@PathVariable String backgroundName) {
        return backgroundService.getFeatureSlotOptions(backgroundName);
    }

    // --- Character Backgrounds ---

    @PostMapping("/characters/{characterId}/backgrounds")
    public ResponseEntity<Dnd5eCharacterBackground> assignBackgroundToCharacter(
            @PathVariable Long characterId,
            @RequestParam String backgroundName,
            @RequestParam(required = false) String featureSelections) {
        try {
            return ResponseEntity.ok(backgroundService.assignBackgroundToCharacter(characterId, backgroundName, featureSelections));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/characters/{characterId}/backgrounds/{backgroundName}")
    public ResponseEntity<Void> removeBackgroundFromCharacter(
            @PathVariable Long characterId,
            @PathVariable String backgroundName) {
        try {
            backgroundService.removeBackgroundFromCharacter(characterId, backgroundName);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/characters/{characterId}/backgrounds/{backgroundName}/features")
    public ResponseEntity<Dnd5eCharacterBackground> updateFeatureSelections(
            @PathVariable Long characterId,
            @PathVariable String backgroundName,
            @RequestParam String featureSelections) {
        try {
            return ResponseEntity.ok(backgroundService.updateFeatureSelections(characterId, backgroundName, featureSelections));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/characters/{characterId}/backgrounds")
    public List<Dnd5eCharacterBackground> listCharacterBackgrounds(@PathVariable Long characterId) {
        return backgroundService.listCharacterBackgrounds(characterId);
    }

    @GetMapping("/characters/{characterId}/backgrounds/active")
    public ResponseEntity<Dnd5eCharacterBackground> getActiveCharacterBackground(@PathVariable Long characterId) {
        Dnd5eCharacterBackground bg = backgroundService.getActiveCharacterBackground(characterId);
        if (bg == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bg);
    }

    // ==================== Classes ====================

    // --- Class CRUD ---

    @GetMapping("/classes")
    public List<Dnd5eClass> listClasses() {
        return classService.listClasses();
    }

    @GetMapping("/classes/{id}")
    public Dnd5eClass getClass(@PathVariable Long id) {
        return classService.getClass(id);
    }

    @GetMapping("/classes/name/{name}")
    public Dnd5eClass getClassByName(@PathVariable String name) {
        return classService.getClassByName(name);
    }

    @GetMapping("/classes/hitdie/{hitDie}")
    public List<Dnd5eClass> listClassesByHitDie(@PathVariable String hitDie) {
        return classService.listClassesByHitDie(hitDie);
    }

    @GetMapping("/classes/ability/{primaryAbility}")
    public List<Dnd5eClass> listClassesByPrimaryAbility(@PathVariable String primaryAbility) {
        return classService.listClassesByPrimaryAbility(primaryAbility);
    }

    @GetMapping("/classes/spellcasting")
    public List<Dnd5eClass> listSpellcastingClasses() {
        return classService.listSpellcastingClasses();
    }

    @GetMapping("/classes/spellcastingType/{spellcastingType}")
    public List<Dnd5eClass> listClassesBySpellcastingType(@PathVariable String spellcastingType) {
        return classService.listClassesBySpellcastingType(spellcastingType);
    }

    @GetMapping("/classes/search")
    public List<Dnd5eClass> searchClasses(@RequestParam String keyword) {
        return classService.searchClasses(keyword);
    }

    @PostMapping("/classes")
    public Dnd5eClass createClass(@RequestBody Dnd5eClass cls) {
        return classService.createClass(cls);
    }

    @PatchMapping("/classes/{id}")
    public Dnd5eClass updateClass(@PathVariable Long id, @RequestBody Dnd5eClass cls) {
        return classService.updateClass(id, cls);
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        try {
            classService.deleteClass(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Subclasses ---

    @GetMapping("/subclasses")
    public List<Dnd5eSubclass> listSubclasses() {
        return classService.listSubclasses();
    }

    @GetMapping("/subclasses/class/{className}")
    public List<Dnd5eSubclass> listSubclassesByClass(@PathVariable String className) {
        return classService.listSubclassesByClass(className);
    }

    @GetMapping("/subclasses/name/{name}")
    public Dnd5eSubclass getSubclassByName(@PathVariable String name) {
        return classService.getSubclassByName(name);
    }

    @GetMapping("/subclasses/search")
    public List<Dnd5eSubclass> searchSubclasses(@RequestParam String keyword) {
        return classService.searchSubclasses(keyword);
    }

    @PostMapping("/subclasses")
    public Dnd5eSubclass createSubclass(@RequestBody Dnd5eSubclass subclass) {
        return classService.createSubclass(subclass);
    }

    @DeleteMapping("/subclasses/{id}")
    public ResponseEntity<Void> deleteSubclass(@PathVariable Long id) {
        try {
            classService.deleteSubclass(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Class Features ---

    @GetMapping("/classes/{className}/features")
    public List<Dnd5eClassFeature> listFeaturesByClass(@PathVariable String className) {
        return classService.listFeaturesByClass(className);
    }

    @GetMapping("/classes/{className}/features/level/{level}")
    public List<Dnd5eClassFeature> listFeaturesByClassAndLevel(@PathVariable String className, @PathVariable int level) {
        return classService.listFeaturesByClassAndLevel(className, level);
    }

    @GetMapping("/classes/{className}/features/subclass/{subclass}")
    public List<Dnd5eClassFeature> listFeaturesBySubclass(@PathVariable String className, @PathVariable String subclass) {
        return classService.listFeaturesBySubclass(className, subclass);
    }

    @GetMapping("/classes/{className}/features/upToLevel/{level}")
    public List<Dnd5eClassFeature> listFeaturesUpToLevel(@PathVariable String className, @PathVariable int level) {
        return classService.listFeaturesUpToLevel(className, level);
    }

    @GetMapping("/classes/{className}/features/subclass/{subclass}/upToLevel/{level}")
    public List<Dnd5eClassFeature> listSubclassFeaturesUpToLevel(@PathVariable String className,
                                                                   @PathVariable String subclass,
                                                                   @PathVariable int level) {
        return classService.listSubclassFeaturesUpToLevel(className, subclass, level);
    }

    @GetMapping("/features/search")
    public List<Dnd5eClassFeature> searchFeatures(@RequestParam String keyword) {
        return classService.searchFeatures(keyword);
    }

    @PostMapping("/features")
    public Dnd5eClassFeature createFeature(@RequestBody Dnd5eClassFeature feature) {
        return classService.createFeature(feature);
    }

    @DeleteMapping("/features/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        try {
            classService.deleteFeature(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Character Classes ---

    @PostMapping("/characters/{characterId}/classes")
    public ResponseEntity<Dnd5eCharacterClass> addClassToCharacter(
            @PathVariable Long characterId,
            @RequestParam String className) {
        try {
            return ResponseEntity.ok(classService.addClassToCharacter(characterId, className));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/characters/{characterId}/classes/{className}/level")
    public ResponseEntity<Dnd5eCharacterClass> levelUpClass(
            @PathVariable Long characterId,
            @PathVariable String className,
            @RequestParam int newLevel) {
        try {
            return ResponseEntity.ok(classService.levelUpClass(characterId, className, newLevel));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/characters/{characterId}/classes/{className}/subclass")
    public ResponseEntity<Dnd5eCharacterClass> setSubclass(
            @PathVariable Long characterId,
            @PathVariable String className,
            @RequestParam String subclass) {
        try {
            return ResponseEntity.ok(classService.setSubclass(characterId, className, subclass));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/characters/{characterId}/classes")
    public List<Dnd5eCharacterClass> listCharacterClasses(@PathVariable Long characterId) {
        return classService.listCharacterClasses(characterId);
    }

    @DeleteMapping("/characters/{characterId}/classes/{className}")
    public ResponseEntity<Void> removeClassFromCharacter(
            @PathVariable Long characterId,
            @PathVariable String className) {
        try {
            classService.removeClassFromCharacter(characterId, className);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Spellcaster Level ---

    @GetMapping("/characters/{characterId}/spellcasterLevel")
    public int calculateSpellcasterLevel(@PathVariable Long characterId) {
        return classService.calculateSpellcasterLevel(characterId);
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
