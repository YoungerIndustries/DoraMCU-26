package com.yisy.modules.doramcu.lord_of_the_rings.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API for querying Lord of the Rings universe data and RPG gameplay.
 * <p>
 * Exposes all Lord of the Rings rules data: factions, units (ground, starships, vehicles),
 * characters, scenarios, special abilities (Ring powers, wizard abilities), lore, and combat tables.
 * Also provides RPG endpoints for character management, campaigns, combat, encounters,
 * inventory, crafting, monsters, and spells.
 * </p>
 * Endpoints at /api/v1/lord-of-the-rings
 */
@RestController
@RequestMapping("/api/v1/lord-of-the-rings")
public class LordOfTheRingsRestController {

    private final LordOfTheRingsService service;
    private final LotrCharacterService characterService;
    private final LotrCampaignService campaignService;
    private final LotrCombatService combatService;
    private final LotrEncounterService encounterService;
    private final LotrInventoryService inventoryService;
    private final LotrCraftingService craftingService;
    private final LotrMonsterService monsterService;
    private final LotrSpellService spellService;
    private final LotrDiceService diceService;
    private final LotrGameplayService gameplayService;
    private final LotrNpcService npcService;
    private final LotrVillainService villainService;
    private final LotrMagicItemService magicItemService;
    private final LotrCallingService callingService;
    private final LotrRulesService rulesService;
    private final LotrClassService classService;
    private final LotrBackgroundService backgroundService;
    private final LotrFeatService featService;
    private final LotrSpeciesService speciesService;
    private final LotrHeroicInspirationService heroicInspirationService;
    private final LotrExhaustionService exhaustionService;
    private final LotrBastionService bastionService;
    private final LotrAdventureHookService adventureHookService;
    private final LotrDungeonRoomService dungeonRoomService;
    private final LotrTensionPoolService tensionPoolService;
    private final LotrCampaignTemplateService campaignTemplateService;
    private final LotrModuleService moduleService;
    private final LotrHorrorService horrorService;
    private final LotrPsionService psionService;
    private final LotrGrowingMagicItemService growingMagicItemService;
    private final LotrToolProficiencyService toolProficiencyService;

    public LordOfTheRingsRestController(LordOfTheRingsService service,
                                          LotrCharacterService characterService,
                                          LotrCampaignService campaignService,
                                          LotrCombatService combatService,
                                          LotrEncounterService encounterService,
                                          LotrInventoryService inventoryService,
                                          LotrCraftingService craftingService,
                                          LotrMonsterService monsterService,
                                          LotrSpellService spellService,
                                          LotrDiceService diceService,
                                          LotrGameplayService gameplayService,
                                          LotrNpcService npcService,
                                          LotrVillainService villainService,
                                          LotrMagicItemService magicItemService,
                                          LotrCallingService callingService,
                                          LotrRulesService rulesService,
                                          LotrClassService classService,
                                          LotrBackgroundService backgroundService,
                                          LotrFeatService featService,
                                          LotrSpeciesService speciesService,
                                          LotrHeroicInspirationService heroicInspirationService,
                                          LotrExhaustionService exhaustionService,
                                          LotrBastionService bastionService,
                                          LotrAdventureHookService adventureHookService,
                                          LotrDungeonRoomService dungeonRoomService,
                                          LotrTensionPoolService tensionPoolService,
                                          LotrCampaignTemplateService campaignTemplateService,
                                          LotrModuleService moduleService,
                                          LotrHorrorService horrorService,
                                          LotrPsionService psionService,
                                          LotrGrowingMagicItemService growingMagicItemService,
                                          LotrToolProficiencyService toolProficiencyService) {
        this.service = service;
        this.characterService = characterService;
        this.campaignService = campaignService;
        this.combatService = combatService;
        this.encounterService = encounterService;
        this.inventoryService = inventoryService;
        this.craftingService = craftingService;
        this.monsterService = monsterService;
        this.spellService = spellService;
        this.diceService = diceService;
        this.gameplayService = gameplayService;
        this.npcService = npcService;
        this.villainService = villainService;
        this.magicItemService = magicItemService;
        this.callingService = callingService;
        this.rulesService = rulesService;
        this.classService = classService;
        this.backgroundService = backgroundService;
        this.featService = featService;
        this.speciesService = speciesService;
        this.heroicInspirationService = heroicInspirationService;
        this.exhaustionService = exhaustionService;
        this.bastionService = bastionService;
        this.adventureHookService = adventureHookService;
        this.dungeonRoomService = dungeonRoomService;
        this.tensionPoolService = tensionPoolService;
        this.campaignTemplateService = campaignTemplateService;
        this.moduleService = moduleService;
        this.horrorService = horrorService;
        this.psionService = psionService;
        this.growingMagicItemService = growingMagicItemService;
        this.toolProficiencyService = toolProficiencyService;
    }

    // ==========================================
    // Factions
    // ==========================================

    @GetMapping("/factions")
    public List<Faction> getAllFactions() {
        return service.getFactions();
    }

    @GetMapping("/factions/{id}")
    public ResponseEntity<Faction> getFaction(@PathVariable String id) {
        Optional<Faction> faction = service.getFactionById(id);
        return faction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/factions/{id}/allies")
    public List<Faction> getAlliesOf(@PathVariable String id) {
        return service.getAlliesOf(id);
    }

    // ==========================================
    // Units
    // ==========================================

    @GetMapping("/units")
    public List<Unit> getAllUnits() {
        return service.getAllUnits();
    }

    @GetMapping("/units/{id}")
    public ResponseEntity<Unit> getUnit(@PathVariable String id) {
        Optional<Unit> unit = service.getUnitById(id);
        return unit.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/units/faction/{factionId}")
    public List<Unit> getUnitsByFaction(@PathVariable String factionId) {
        return service.getUnitsByFaction(factionId);
    }

    @GetMapping("/units/ground")
    public List<Unit> getGroundUnits() {
        return service.getGroundUnits();
    }

    @GetMapping("/units/starships")
    public List<Unit> getStarships() {
        return service.getStarships();
    }

    @GetMapping("/units/vehicles")
    public List<Unit> getGroundVehicles() {
        return service.getGroundVehicles();
    }

    @GetMapping("/units/combat")
    public List<Unit> getCombatUnits() {
        return service.getCombatUnits();
    }

    // ==========================================
    // Characters
    // ==========================================

    @GetMapping("/characters")
    public List<Unit> getAllCharacters() {
        return service.getCharacters();
    }

    @GetMapping("/characters/ring-bearers")
    public List<Unit> getRingBearers() {
        return service.getRingBearers();
    }

    // ==========================================
    // Scenarios
    // ==========================================

    @GetMapping("/scenarios")
    public List<Scenario> getAllScenarios() {
        return service.getScenarios();
    }

    @GetMapping("/scenarios/{id}")
    public ResponseEntity<Scenario> getScenario(@PathVariable String id) {
        Optional<Scenario> scenario = service.getScenarioById(id);
        return scenario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/scenarios/difficulty")
    public List<Scenario> getScenariosByDifficulty() {
        return service.getScenariosByDifficulty();
    }

    @GetMapping("/scenarios/era/{era}")
    public List<Scenario> getScenariosByEra(@PathVariable String era) {
        return service.getScenariosByEra(era);
    }

    @GetMapping("/scenarios/faction/{faction}")
    public List<Scenario> getScenariosByFaction(@PathVariable String faction) {
        return service.getScenariosByFaction(faction);
    }

    // ==========================================
    // Special Abilities
    // ==========================================

    @GetMapping("/abilities")
    public List<SpecialAbility> getAllAbilities() {
        return service.getSpecialAbilities();
    }

    @GetMapping("/abilities/{id}")
    public ResponseEntity<SpecialAbility> getAbility(@PathVariable String id) {
        Optional<SpecialAbility> ability = service.getAbilityById(id);
        return ability.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/abilities/ring-powers")
    public List<SpecialAbility> getRingPowers() {
        return service.getRingPowers();
    }

    @GetMapping("/abilities/wizard")
    public List<SpecialAbility> getWizardAbilities() {
        return service.getWizardAbilities();
    }

    @GetMapping("/abilities/racial-traits")
    public List<SpecialAbility> getRacialTraits() {
        return service.getRacialTraits();
    }

    @GetMapping("/abilities/keywords")
    public List<SpecialAbility> getUnitKeywords() {
        return service.getUnitKeywords();
    }

    // ==========================================
    // Lore
    // ==========================================

    @GetMapping("/lore")
    public List<Lore> getAllLore() {
        return service.getLore();
    }

    @GetMapping("/lore/category/{category}")
    public List<Lore> getLoreByCategory(@PathVariable String category) {
        return service.getLoreByCategory(category);
    }

    @GetMapping("/lore/realms")
    public List<Lore> getRealms() {
        return service.getRealms();
    }

    @GetMapping("/lore/constellations")
    public List<Lore> getConstellations() {
        return service.getConstellations();
    }

    @GetMapping("/lore/valar-planets")
    public List<Lore> getValarPlanets() {
        return service.getValarPlanets();
    }

    @GetMapping("/lore/species")
    public List<Lore> getSpecies() {
        return service.getSpecies();
    }

    @GetMapping("/lore/events")
    public List<Lore> getEvents() {
        return service.getEvents();
    }

    @GetMapping("/lore/rings")
    public List<Lore> getRingInfo() {
        return service.getRingInfo();
    }

    @GetMapping("/lore/artifacts")
    public List<Lore> getArtifacts() {
        return service.getArtifacts();
    }

    // ==========================================
    // Combat Tables
    // ==========================================

    @GetMapping("/combat/ground-table")
    public Map<String, Map<String, String>> getGroundCombatTable() {
        return service.getGroundCombatTable();
    }

    @GetMapping("/combat/space-table")
    public Map<String, Map<String, String>> getSpaceCombatTable() {
        return service.getSpaceCombatTable();
    }

    @GetMapping("/combat/outcomes")
    public Map<String, String> getCombatOutcomes() {
        return service.getCombatOutcomes();
    }

    // ==========================================
    // Combat Resolution
    // ==========================================

    @GetMapping("/combat/resolve/ground")
    public String resolveGroundCombat(@RequestParam String odds, @RequestParam int roll) {
        return service.resolveGroundCombat(odds, roll);
    }

    @GetMapping("/combat/resolve/space")
    public String resolveSpaceCombat(@RequestParam String odds, @RequestParam int roll) {
        return service.resolveSpaceCombat(odds, roll);
    }

    @GetMapping("/combat/odds")
    public String calculateOdds(@RequestParam int attack, @RequestParam int defense) {
        return service.calculateOddsRatio(attack, defense);
    }

    // ==========================================
    // Cache Management
    // ==========================================

    @PostMapping("/cache/clear")
    public void clearCache() {
        service.clearCache();
    }

    // ==========================================
    // RPG: Character Management
    // ==========================================

    @GetMapping("/rpg/characters")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter> getAllRpgCharacters() {
        return characterService.findAll();
    }

    @GetMapping("/rpg/characters/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter> getRpgCharacter(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(characterService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rpg/characters/race/{race}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter> getCharactersByRace(@PathVariable String race) {
        return characterService.findByRace(race);
    }

    @GetMapping("/rpg/characters/calling/{calling}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter> getCharactersByCalling(@PathVariable String calling) {
        return characterService.findByCalling(calling);
    }

    @GetMapping("/rpg/characters/ring-bearers")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter> getRpgRingBearers() {
        return characterService.findRingBearers();
    }

    @PostMapping("/rpg/characters")
    public com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter createCharacter(
            @RequestParam String name, @RequestParam String race, @RequestParam String calling) {
        return characterService.createCharacter(name, race, calling);
    }

    @PostMapping("/rpg/characters/roll-ability")
    public int rollAbilityScore() {
        return characterService.roll4d6DropLowest();
    }

    // ==========================================
    // RPG: Campaigns
    // ==========================================

    @GetMapping("/rpg/campaigns")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign> getAllCampaigns() {
        return campaignService.findAll();
    }

    @GetMapping("/rpg/campaigns/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign> getActiveCampaigns() {
        return campaignService.findActive();
    }

    @GetMapping("/rpg/campaigns/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign> getCampaign(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(campaignService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/rpg/campaigns")
    public com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign createCampaign(
            @RequestParam String name, @RequestParam(defaultValue = "") String description,
            @RequestParam String narratorName, @RequestParam(defaultValue = "Quest") String questType) {
        return campaignService.createCampaign(name, description, narratorName, questType);
    }

    // ==========================================
    // RPG: Combat
    // ==========================================

    @GetMapping("/rpg/combat")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat> getAllCombats() {
        return combatService.findAll();
    }

    @GetMapping("/rpg/combat/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat> getActiveCombats() {
        return combatService.findActive();
    }

    @GetMapping("/rpg/combat/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat> getCombat(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(combatService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/rpg/combat")
    public com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat createCombat(
            @RequestParam String encounterName, @RequestParam(defaultValue = "") String location) {
        return combatService.createCombat(encounterName, location);
    }

    @PostMapping("/rpg/combat/{id}/end")
    public void endCombat(@PathVariable Long id) {
        combatService.endCombat(id);
    }

    @PostMapping("/rpg/combat/{id}/next-round")
    public void nextRound(@PathVariable Long id) {
        combatService.nextRound(id);
    }

    // ==========================================
    // RPG: Encounters
    // ==========================================

    @GetMapping("/rpg/encounters")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter> getAllEncounters() {
        return encounterService.findAll();
    }

    @GetMapping("/rpg/encounters/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter> getActiveEncounters() {
        return encounterService.findActive();
    }

    @GetMapping("/rpg/encounters/difficulty/{difficulty}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter> getEncountersByDifficulty(@PathVariable String difficulty) {
        return encounterService.findByDifficulty(difficulty);
    }

    @GetMapping("/rpg/encounters/level/{level}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter> getEncountersByLevel(@PathVariable int level) {
        return encounterService.findByPartyLevel(level);
    }

    // ==========================================
    // RPG: Inventory
    // ==========================================

    @GetMapping("/rpg/inventory/{characterId}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrInventoryItem> getCharacterInventory(@PathVariable Long characterId) {
        return inventoryService.findByCharacterId(characterId);
    }

    @GetMapping("/rpg/inventory/{characterId}/equipped")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrInventoryItem> getEquippedItems(@PathVariable Long characterId) {
        return inventoryService.findEquipped(characterId);
    }

    // ==========================================
    // RPG: Crafting
    // ==========================================

    @GetMapping("/rpg/crafting")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingRecipe> getAllCraftingRecipes() {
        return craftingService.findAll();
    }

    @GetMapping("/rpg/crafting/skill/{skill}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingRecipe> getRecipesBySkill(@PathVariable String skill) {
        return craftingService.findBySkill(skill);
    }

    // ==========================================
    // RPG: Monsters
    // ==========================================

    @GetMapping("/rpg/monsters")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster> getAllMonsters() {
        return monsterService.findAll();
    }

    @GetMapping("/rpg/monsters/type/{type}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster> getMonstersByType(@PathVariable String type) {
        return monsterService.findByType(type);
    }

    @GetMapping("/rpg/monsters/faction/{faction}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster> getMonstersByFaction(@PathVariable String faction) {
        return monsterService.findByFaction(faction);
    }

    @GetMapping("/rpg/monsters/bosses")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster> getBossMonsters() {
        return monsterService.findBosses();
    }

    // ==========================================
    // RPG: Spells
    // ==========================================

    @GetMapping("/rpg/spells")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpell> getAllSpells() {
        return spellService.findAll();
    }

    @GetMapping("/rpg/spells/level/{level}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpell> getSpellsByLevel(@PathVariable int level) {
        return spellService.findByLevel(level);
    }

    @GetMapping("/rpg/spells/school/{school}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpell> getSpellsBySchool(@PathVariable String school) {
        return spellService.findBySchool(school);
    }

    // ==========================================
    // RPG: Dice Roller
    // ==========================================

    @PostMapping("/rpg/dice/roll")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.DiceRollResponse rollDice(@RequestParam String formula) {
        return diceService.rollDice(formula);
    }

    @GetMapping("/rpg/dice/eye-of-sauron")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.DiceRollResponse rollEyeOfSauron() {
        return diceService.rollEyeOfSauron();
    }

    @PostMapping("/rpg/dice/roll-with-hope")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.DiceRollResponse rollWithHope(@RequestParam(defaultValue = "20") int sides) {
        return diceService.rollWithAdvantage(sides);
    }

    @PostMapping("/rpg/dice/roll-with-shadow")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.DiceRollResponse rollWithShadow(@RequestParam(defaultValue = "20") int sides) {
        return diceService.rollWithDisadvantage(sides);
    }

    // ==========================================
    // RPG: Gameplay
    // ==========================================

    @PostMapping("/rpg/gameplay/skill-check")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.SkillCheckResponse skillCheck(
            @RequestParam Long characterId, @RequestParam String skill, @RequestParam int dc,
            @RequestParam(defaultValue = "false") boolean hope, @RequestParam(defaultValue = "false") boolean shadow) {
        return gameplayService.skillCheck(characterId, skill, dc, hope, shadow);
    }

    @PostMapping("/rpg/gameplay/saving-throw")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.SkillCheckResponse savingThrow(
            @RequestParam Long characterId, @RequestParam String ability, @RequestParam int dc,
            @RequestParam(defaultValue = "false") boolean hope, @RequestParam(defaultValue = "false") boolean shadow) {
        return gameplayService.savingThrow(characterId, ability, dc, hope, shadow);
    }

    @PostMapping("/rpg/gameplay/cast-spell")
    public com.yisy.modules.doramcu.lord_of_the_rings.dto.LotrSpellCastResponse castSpell(
            @RequestParam Long characterId, @RequestParam Long spellId) {
        return gameplayService.castSpell(characterId, spellId);
    }

    @PostMapping("/rpg/gameplay/short-rest/{characterId}")
    public Map<String, String> shortRest(@PathVariable Long characterId) {
        return Map.of("message", gameplayService.shortRest(characterId));
    }

    @PostMapping("/rpg/gameplay/long-rest/{characterId}")
    public Map<String, String> longRest(@PathVariable Long characterId) {
        return Map.of("message", gameplayService.longRest(characterId));
    }

    @PostMapping("/rpg/gameplay/corruption/{characterId}")
    public Map<String, Integer> corruptionCheck(@PathVariable Long characterId, @RequestParam(defaultValue = "1") int amount) {
        return Map.of("corruption", gameplayService.corruptionCheck(characterId, amount));
    }

    @PostMapping("/rpg/gameplay/award-xp/{characterId}")
    public Map<String, String> awardXP(@PathVariable Long characterId, @RequestParam int xp) {
        return Map.of("message", gameplayService.awardXP(characterId, xp));
    }

    // ==========================================
    // RPG: NPCs
    // ==========================================

    @GetMapping("/rpg/npcs")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc> getAllNpcs() {
        return npcService.findAll();
    }

    @GetMapping("/rpg/npcs/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc> getNpc(@PathVariable Long id) {
        return npcService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rpg/npcs/race/{race}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc> getNpcsByRace(@PathVariable String race) {
        return npcService.findByRace(race);
    }

    @GetMapping("/rpg/npcs/faction/{faction}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc> getNpcsByFaction(@PathVariable String faction) {
        return npcService.findByFaction(faction);
    }

    @PostMapping("/rpg/npcs/generate")
    public com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc generateNpc(@RequestParam(required = false) String faction) {
        return faction != null ? npcService.generateNpcFromFaction(faction) : npcService.generateRandomNpc();
    }

    @PostMapping("/rpg/npcs/{id}/damage")
    public Map<String, String> damageNpc(@PathVariable Long id, @RequestParam int damage) {
        return Map.of("message", npcService.damageNpc(id, damage));
    }

    @PostMapping("/rpg/npcs/{id}/heal")
    public Map<String, String> healNpc(@PathVariable Long id, @RequestParam int healing) {
        return Map.of("message", npcService.healNpc(id, healing));
    }

    // ==========================================
    // RPG: Villains
    // ==========================================

    @GetMapping("/rpg/villains")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillain> getAllVillains() {
        return villainService.findAll();
    }

    @GetMapping("/rpg/villains/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillain> getActiveVillains() {
        return villainService.findActiveVillains();
    }

    @GetMapping("/rpg/villains/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillain> getVillain(@PathVariable Long id) {
        return villainService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rpg/villains/{id}/powers")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillainPower> getVillainPowers(@PathVariable Long id) {
        return villainService.getVillainPowers(id);
    }

    @PostMapping("/rpg/villains/{id}/damage")
    public Map<String, String> damageVillain(@PathVariable Long id, @RequestParam int damage) {
        return Map.of("message", villainService.damageVillain(id, damage));
    }

    @PostMapping("/rpg/villains/{id}/heal")
    public Map<String, String> healVillain(@PathVariable Long id, @RequestParam int healing) {
        return Map.of("message", villainService.healVillain(id, healing));
    }

    @PostMapping("/rpg/villains/{id}/corruption")
    public Map<String, String> increaseVillainCorruption(@PathVariable Long id, @RequestParam(defaultValue = "1") int amount) {
        return Map.of("message", villainService.increaseCorruption(id, amount));
    }

    // ==========================================
    // RPG: Magic Items
    // ==========================================

    @GetMapping("/rpg/magic-items")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMagicItem> getAllMagicItems() {
        return magicItemService.findAll();
    }

    @GetMapping("/rpg/magic-items/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMagicItem> getActiveMagicItems() {
        return magicItemService.findActiveItems();
    }

    @GetMapping("/rpg/magic-items/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMagicItem> getMagicItem(@PathVariable Long id) {
        return magicItemService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rpg/magic-items/type/{type}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMagicItem> getMagicItemsByType(@PathVariable String type) {
        return magicItemService.findByType(type);
    }

    @GetMapping("/rpg/magic-items/rarity/{rarity}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMagicItem> getMagicItemsByRarity(@PathVariable String rarity) {
        return magicItemService.findByRarity(rarity);
    }

    @PostMapping("/rpg/magic-items/{id}/use")
    public Map<String, String> useMagicItem(@PathVariable Long id) {
        return Map.of("message", magicItemService.useMagicItem(id));
    }

    @PostMapping("/rpg/magic-items/{id}/recharge")
    public Map<String, String> rechargeMagicItem(@PathVariable Long id) {
        return Map.of("message", magicItemService.rechargeMagicItem(id));
    }

    // ==========================================
    // RPG: Callings
    // ==========================================

    @GetMapping("/rpg/callings")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCalling> getAllCallings() {
        return callingService.findAll();
    }

    @GetMapping("/rpg/callings/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCalling> getCalling(@PathVariable Long id) {
        return callingService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rpg/callings/name/{name}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCalling> getCallingByName(@PathVariable String name) {
        return callingService.findByName(name).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==========================================
    // RPG: Rules
    // ==========================================

    @GetMapping("/rpg/rules")
    public Map<String, String> getAllRules() {
        return rulesService.getAllRules();
    }

    @GetMapping("/rpg/rules/{ruleName}")
    public String getRule(@PathVariable String ruleName) {
        return rulesService.getRule(ruleName);
    }

    @GetMapping("/rpg/rules/summary")
    public String getRulesSummary() {
        return rulesService.getRulesSummary();
    }

    @GetMapping("/rpg/rules/xp-thresholds")
    public Map<Integer, Integer> getXpThresholds() {
        return rulesService.getXpThresholds();
    }

    @GetMapping("/rpg/rules/dc-table")
    public Map<Integer, String> getDcTable() {
        return rulesService.getDcTable();
    }

    @GetMapping("/rpg/rules/corruption-levels")
    public Map<String, String> getCorruptionLevels() {
        return rulesService.getCorruptionLevels();
    }

    @GetMapping("/rpg/rules/eye-of-sauron")
    public Map<String, String> getEyeOfSauronTable() {
        return rulesService.getEyeOfSauronTable();
    }

    // ==========================================
    // RPG: Classes
    // ==========================================

    @GetMapping("/rpg/classes")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrClass> getAllClasses() {
        return classService.findAll();
    }

    @GetMapping("/rpg/classes/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrClass> getActiveClasses() {
        return classService.findActiveClasses();
    }

    @GetMapping("/rpg/classes/caster")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrClass> getCasterClasses() {
        return classService.findCasterClasses();
    }

    // ==========================================
    // RPG: Backgrounds
    // ==========================================

    @GetMapping("/rpg/backgrounds")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBackground> getAllBackgrounds() {
        return backgroundService.findAll();
    }

    @GetMapping("/rpg/backgrounds/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBackground> getActiveBackgrounds() {
        return backgroundService.findActiveBackgrounds();
    }

    // ==========================================
    // RPG: Feats
    // ==========================================

    @GetMapping("/rpg/feats")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrFeat> getAllFeats() {
        return featService.findAll();
    }

    @GetMapping("/rpg/feats/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrFeat> getActiveFeats() {
        return featService.findActiveFeats();
    }

    // ==========================================
    // RPG: Species
    // ==========================================

    @GetMapping("/rpg/species")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpecies> getAllSpecies() {
        return speciesService.findAll();
    }

    @GetMapping("/rpg/species/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpecies> getActiveSpecies() {
        return speciesService.findActiveSpecies();
    }

    // ==========================================
    // RPG: Heroic Inspiration
    // ==========================================

    @GetMapping("/rpg/heroic-inspiration/{characterId}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrHeroicInspiration> getHeroicInspiration(@PathVariable Long characterId) {
        return heroicInspirationService.findByCharacterId(characterId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/rpg/heroic-inspiration/{characterId}/award")
    public Map<String, String> awardHeroicInspiration(@PathVariable Long characterId, @RequestParam(defaultValue = "1") int points) {
        return Map.of("message", heroicInspirationService.awardPoints(characterId, points));
    }

    @PostMapping("/rpg/heroic-inspiration/{characterId}/spend")
    public Map<String, String> spendHeroicInspiration(@PathVariable Long characterId, @RequestParam(defaultValue = "1") int points) {
        return Map.of("message", heroicInspirationService.spendPoints(characterId, points));
    }

    // ==========================================
    // RPG: Exhaustion
    // ==========================================

    @GetMapping("/rpg/exhaustion")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrExhaustion> getAllExhaustionLevels() {
        return exhaustionService.findAll();
    }

    @GetMapping("/rpg/exhaustion/level/{level}")
    public String getExhaustionEffects(@PathVariable int level) {
        return exhaustionService.getExhaustionEffects(level);
    }

    // ==========================================
    // RPG: Bastions
    // ==========================================

    @GetMapping("/rpg/bastions")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBastion> getAllBastions() {
        return bastionService.findAll();
    }

    @GetMapping("/rpg/bastions/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBastion> getActiveBastions() {
        return bastionService.findActiveBastions();
    }

    @GetMapping("/rpg/bastions/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrBastion> getBastion(@PathVariable Long id) {
        return bastionService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/rpg/bastions/{id}/damage")
    public Map<String, String> damageBastion(@PathVariable Long id, @RequestParam int damage) {
        return Map.of("message", bastionService.damageBastion(id, damage));
    }

    @PostMapping("/rpg/bastions/{id}/repair")
    public Map<String, String> repairBastion(@PathVariable Long id, @RequestParam int repair) {
        return Map.of("message", bastionService.repairBastion(id, repair));
    }

    // ==========================================
    // RPG: Adventure Hooks
    // ==========================================

    @GetMapping("/rpg/adventure-hooks")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrAdventureHook> getAllAdventureHooks() {
        return adventureHookService.findAll();
    }

    @GetMapping("/rpg/adventure-hooks/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrAdventureHook> getActiveAdventureHooks() {
        return adventureHookService.findActiveHooks();
    }

    @GetMapping("/rpg/adventure-hooks/level/{level}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrAdventureHook> getAdventureHooksByLevel(@PathVariable int level) {
        return adventureHookService.findByLevel(level);
    }

    @GetMapping("/rpg/adventure-hooks/random/{level}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrAdventureHook> getRandomAdventureHook(@PathVariable int level) {
        return adventureHookService.getRandomHook(level).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==========================================
    // RPG: Dungeon Rooms
    // ==========================================

    @GetMapping("/rpg/dungeon-rooms")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDungeonRoom> getAllDungeonRooms() {
        return dungeonRoomService.findAll();
    }

    @GetMapping("/rpg/dungeon-rooms/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDungeonRoom> getActiveDungeonRooms() {
        return dungeonRoomService.findActiveRooms();
    }

    @GetMapping("/rpg/dungeon-rooms/level/{level}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDungeonRoom> getDungeonRoomsByLevel(@PathVariable int level) {
        return dungeonRoomService.findByLevel(level);
    }

    @GetMapping("/rpg/dungeon-rooms/random/{level}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDungeonRoom> getRandomDungeonRoom(@PathVariable int level) {
        return dungeonRoomService.getRandomRoom(level).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==========================================
    // RPG: Tension Pools
    // ==========================================

    @GetMapping("/rpg/tension-pool/{campaignId}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrTensionPool> getTensionPool(@PathVariable Long campaignId) {
        return tensionPoolService.findByCampaignId(campaignId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/rpg/tension-pool/{campaignId}/increase")
    public Map<String, String> increaseTension(@PathVariable Long campaignId, @RequestParam(defaultValue = "1") int amount) {
        return Map.of("message", tensionPoolService.increaseTension(campaignId, amount));
    }

    @PostMapping("/rpg/tension-pool/{campaignId}/decrease")
    public Map<String, String> decreaseTension(@PathVariable Long campaignId, @RequestParam(defaultValue = "1") int amount) {
        return Map.of("message", tensionPoolService.decreaseTension(campaignId, amount));
    }

    // ==========================================
    // RPG: Campaign Templates
    // ==========================================

    @GetMapping("/rpg/campaign-templates")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaignTemplate> getAllCampaignTemplates() {
        return campaignTemplateService.findAll();
    }

    @GetMapping("/rpg/campaign-templates/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaignTemplate> getActiveCampaignTemplates() {
        return campaignTemplateService.findActiveTemplates();
    }

    // ==========================================
    // RPG: Modules
    // ==========================================

    @GetMapping("/rpg/modules")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrModule> getAllModules() {
        return moduleService.findAll();
    }

    @GetMapping("/rpg/modules/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrModule> getActiveModules() {
        return moduleService.findActiveModules();
    }

    @GetMapping("/rpg/modules/type/{type}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrModule> getModulesByType(@PathVariable String type) {
        return moduleService.findByType(type);
    }

    // ==========================================
    // RPG: Horror (Dark Gifts & Fear)
    // ==========================================

    @GetMapping("/rpg/horror/dark-gifts")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDarkGift> getAllDarkGifts() {
        return horrorService.findActiveDarkGifts();
    }

    @GetMapping("/rpg/horror/fear/{characterId}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrFearDread> getFear(@PathVariable Long characterId) {
        return horrorService.findByCharacterId(characterId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/rpg/horror/fear/{characterId}/increase")
    public Map<String, String> increaseFear(@PathVariable Long characterId, @RequestParam(defaultValue = "1") int amount) {
        return Map.of("message", horrorService.increaseFear(characterId, amount));
    }

    @PostMapping("/rpg/horror/fear/{characterId}/decrease")
    public Map<String, String> decreaseFear(@PathVariable Long characterId, @RequestParam(defaultValue = "1") int amount) {
        return Map.of("message", horrorService.decreaseFear(characterId, amount));
    }

    // ==========================================
    // RPG: Psion (Maia Magic)
    // ==========================================

    @GetMapping("/rpg/psion/{characterId}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrPsion> getPsion(@PathVariable Long characterId) {
        return psionService.findByCharacterId(characterId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/rpg/psion/{characterId}/spend")
    public Map<String, String> spendPsiPoints(@PathVariable Long characterId, @RequestParam int points) {
        return Map.of("message", psionService.spendPsiPoints(characterId, points));
    }

    @PostMapping("/rpg/psion/{characterId}/recover")
    public Map<String, String> recoverPsiPoints(@PathVariable Long characterId, @RequestParam int points) {
        return Map.of("message", psionService.recoverPsiPoints(characterId, points));
    }

    // ==========================================
    // RPG: Growing Magic Items
    // ==========================================

    @GetMapping("/rpg/growing-magic-items")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrGrowingMagicItem> getAllGrowingMagicItems() {
        return growingMagicItemService.findAll();
    }

    @GetMapping("/rpg/growing-magic-items/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrGrowingMagicItem> getActiveGrowingMagicItems() {
        return growingMagicItemService.findActiveItems();
    }

    @PostMapping("/rpg/growing-magic-items/{id}/level-up")
    public Map<String, String> levelUpGrowingItem(@PathVariable Long id) {
        return Map.of("message", growingMagicItemService.levelUpItem(id));
    }

    // ==========================================
    // RPG: Tool Proficiencies
    // ==========================================

    @GetMapping("/rpg/tools")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool> getAllTools() {
        return toolProficiencyService.findAllTools();
    }

    @GetMapping("/rpg/tools/active")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool> getActiveTools() {
        return toolProficiencyService.findActiveTools();
    }

    @GetMapping("/rpg/tools/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool> getTool(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toolProficiencyService.findToolById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rpg/tools/category/{category}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool> getToolsByCategory(@PathVariable String category) {
        return toolProficiencyService.findToolsByCategory(category);
    }

    @PostMapping("/rpg/tools")
    public com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool createTool(
            @RequestParam String name, @RequestParam String description,
            @RequestParam String category, @RequestParam(defaultValue = "0") int proficiencyBonus) {
        return toolProficiencyService.createTool(name, description, category, proficiencyBonus);
    }

    @DeleteMapping("/rpg/tools/{id}")
    public Map<String, String> deleteTool(@PathVariable Long id) {
        toolProficiencyService.deleteTool(id);
        return Map.of("message", "Tool deleted: " + id);
    }

    @GetMapping("/rpg/tool-proficiencies/{characterId}")
    public List<com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacterToolProficiency> getCharacterToolProficiencies(@PathVariable Long characterId) {
        return toolProficiencyService.getCharacterToolProficiencies(characterId);
    }

    @PostMapping("/rpg/tool-proficiencies")
    public com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacterToolProficiency addToolProficiency(
            @RequestParam Long characterId, @RequestParam Long toolId) {
        return toolProficiencyService.addProficiency(characterId, toolId);
    }

    @DeleteMapping("/rpg/tool-proficiencies/{id}")
    public Map<String, String> removeToolProficiency(@PathVariable Long id) {
        toolProficiencyService.removeProficiency(id);
        return Map.of("message", "Tool proficiency removed: " + id);
    }

    @PostMapping("/rpg/tool-check")
    public com.yisy.modules.doramcu.lord_of_the_rings.service.LotrToolProficiencyService.ToolCheckResult makeToolCheck(
            @RequestParam Long characterId, @RequestParam Long toolId, @RequestParam(defaultValue = "15") int dc) {
        return toolProficiencyService.makeToolCheck(characterId, toolId, dc);
    }

    // ==================== Sessions ====================

    @PostMapping("/campaigns/{id}/sessions")
    public LotrSession createSession(@PathVariable Long id, @RequestParam String sessionName,
                                      @RequestParam int sessionNumber,
                                      @RequestParam(required = false) LocalDate sessionDate,
                                      @RequestParam(required = false) String notes) {
        return campaignService.createSession(id, sessionName, sessionNumber, sessionDate, notes);
    }

    @GetMapping("/campaigns/{id}/sessions")
    public List<LotrSession> listSessions(@PathVariable Long id) {
        return campaignService.listSessions(id);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<LotrSession> getSession(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(campaignService.findSessionById(sessionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Monster Abilities ====================

    @GetMapping("/monsters/{monsterName}/abilities")
    public List<LotrMonsterAbility> listAbilities(@PathVariable String monsterName) {
        return monsterService.listAbilities(monsterName);
    }

    @GetMapping("/monsters/{monsterName}/abilities/type/{type}")
    public List<LotrMonsterAbility> listAbilitiesByType(@PathVariable String monsterName, @PathVariable String type) {
        return monsterService.listAbilitiesByType(monsterName, type);
    }

    @GetMapping("/monsters/{monsterName}/abilities/subtype/{subtype}")
    public List<LotrMonsterAbility> listAbilitiesBySubtype(@PathVariable String monsterName, @PathVariable String subtype) {
        return monsterService.listAbilitiesBySubtype(monsterName, subtype);
    }

    @GetMapping("/monsters/{monsterName}/abilities/search")
    public List<LotrMonsterAbility> searchAbilities(@PathVariable String monsterName, @RequestParam String keyword) {
        return monsterService.searchAbilities(monsterName, keyword);
    }

    @PostMapping("/monsters/abilities")
    public LotrMonsterAbility createAbility(@RequestParam String monsterName, @RequestParam String name,
                                             @RequestParam String type, @RequestParam String description,
                                             @RequestParam int attackBonus, @RequestParam String damageDice,
                                             @RequestParam String damageType, @RequestParam(required = false) String saveAbility,
                                             @RequestParam(defaultValue = "10") int saveDc) {
        return monsterService.createAbility(monsterName, name, type, description, attackBonus, damageDice,
                damageType, saveAbility, saveDc);
    }

    @DeleteMapping("/monsters/abilities/{id}")
    public Map<String, String> deleteAbility(@PathVariable Long id) {
        monsterService.deleteAbility(id);
        return Map.of("message", "Ability deleted: " + id);
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
