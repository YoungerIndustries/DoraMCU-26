package com.yisy.modules.doramcu.dnd5e.config;

import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBuildingInstanceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCraftingService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eEncounterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eInventoryService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRealEstateService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesEngine;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpellService;
import com.yisy.modules.doramcu.dnd5e.config.Dnd5eTechLevelAdapter;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.Dnd5eTo3R4Adapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.gamecore.AbstractGameModule;
import com.yisy.modules.doramcu.gamecore.BuildingInstanceService;
import com.yisy.modules.doramcu.gamecore.CampaignService;
import com.yisy.modules.doramcu.gamecore.CampaignService.Campaign;
import com.yisy.modules.doramcu.gamecore.CharacterService;
import com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo;
import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.CombatService.AttackRequest;
import com.yisy.modules.doramcu.gamecore.CombatService.AttackResult;
import com.yisy.modules.doramcu.gamecore.CombatService.CombatInfo;
import com.yisy.modules.doramcu.gamecore.CombatService.CombatantInfo;
import com.yisy.modules.doramcu.gamecore.CraftingService;
import com.yisy.modules.doramcu.gamecore.CraftingService.CraftingProject;
import com.yisy.modules.doramcu.gamecore.CraftingService.CraftingRecipe;
import com.yisy.modules.doramcu.gamecore.DiceService;
import com.yisy.modules.doramcu.gamecore.DiceService.DiceRollResult;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.DungeonRoom;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.TrapCheckResult;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.TreasureResult;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.gamecore.EncounterService.Encounter;
import com.yisy.modules.doramcu.gamecore.EncounterService.EncounterResult;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.InventoryService;
import com.yisy.modules.doramcu.gamecore.InventoryService.InventoryItem;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.PsionicsService;
import com.yisy.modules.doramcu.gamecore.PsionicsService.PsionicPower;
import com.yisy.modules.doramcu.gamecore.PsionicsService.PsionicPowerResult;
import com.yisy.modules.doramcu.gamecore.RealEstateService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.gamecore.RulesService;
import com.yisy.modules.doramcu.gamecore.SpellService;
import com.yisy.modules.doramcu.gamecore.SpellService.Spell;
import com.yisy.modules.doramcu.gamecore.SpellService.SpellCastResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * D&amp;D 5e Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register D&amp;D 5e features
 * with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access D&amp;D features through the shared game-core interfaces.
 * </p>
 */
@Component
public class Dnd5eGameModule extends AbstractGameModule {

    private final Dnd5eDiceService diceService;
    private final Dnd5eCombatService combatService;
    private final Dnd5eInventoryService inventoryService;
    private final Dnd5eCraftingService craftingService;
    private final Dnd5eDungeonRoomService dungeonRoomService;
    private final Dnd5eSpellService spellService;
    private final Dnd5ePsionService psionService;
    private final Dnd5eEncounterService encounterService;
    private final Dnd5eCharacterService characterService;
    private final Dnd5eCampaignService campaignService;
    private final Dnd5eRulesService rulesService;
    private final Dnd5eTechLevelAdapter techLevelAdapter;
    private final Dnd5eRealEstateService realEstateService;
    private final Dnd5eBuildingInstanceService buildingInstanceService;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final Dnd5eRulesEngine rulesEngine;

    public Dnd5eGameModule(
            Dnd5eDiceService diceService,
            Dnd5eCombatService combatService,
            Dnd5eInventoryService inventoryService,
            Dnd5eCraftingService craftingService,
            Dnd5eDungeonRoomService dungeonRoomService,
            Dnd5eSpellService spellService,
            Dnd5ePsionService psionService,
            Dnd5eEncounterService encounterService,
            Dnd5eCharacterService characterService,
            Dnd5eCampaignService campaignService,
            Dnd5eRulesService rulesService,
            GameModuleRegistry gameModuleRegistry,
            Dnd5eTechLevelAdapter techLevelAdapter,
            Dnd5eRealEstateService realEstateService,
            Dnd5eBuildingInstanceService buildingInstanceService,
            UnifiedRulesQueryService unifiedRulesQueryService,
            Dnd5eRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.diceService = diceService;
        this.combatService = combatService;
        this.inventoryService = inventoryService;
        this.craftingService = craftingService;
        this.dungeonRoomService = dungeonRoomService;
        this.spellService = spellService;
        this.psionService = psionService;
        this.encounterService = encounterService;
        this.characterService = characterService;
        this.campaignService = campaignService;
        this.rulesService = rulesService;
        this.techLevelAdapter = techLevelAdapter;
        this.realEstateService = realEstateService;
        this.buildingInstanceService = buildingInstanceService;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.DND_5E;
    }

    @Override
    public String getModuleName() {
        return "Dungeons & Dragons 5e";
    }

    @Override
    public DiceService getDiceService() {
        return new Dnd5eDiceAdapter(diceService);
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the D&D 5e combat adapter with rules-aware combat
        CombatService delegate = new Dnd5eCombatAdapter(combatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new Dnd5eInventoryAdapter(inventoryService);
    }

    @Override
    public CraftingService getCraftingService() {
        return new Dnd5eCraftingAdapter(craftingService);
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new Dnd5eDungeonRoomAdapter(dungeonRoomService);
    }

    @Override
    public SpellService getSpellService() {
        return new Dnd5eSpellAdapter(spellService);
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new Dnd5ePsionicsAdapter(psionService);
    }

    @Override
    public EncounterService getEncounterService() {
        return new Dnd5eEncounterAdapter(encounterService);
    }

    @Override
    public CharacterService getCharacterService() {
        return new Dnd5eCharacterAdapter(characterService);
    }

    @Override
    public CampaignService getCampaignService() {
        return new Dnd5eCampaignAdapter(campaignService);
    }

    @Override
    public RulesService getRulesService() {
        return new Dnd5eRulesAdapter(rulesService);
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    @Override
    public RealEstateService getRealEstateService() {
        return realEstateService;
    }

    @Override
    public BuildingInstanceService getBuildingInstanceService() {
        return buildingInstanceService;
    }

    @Override
    public GroundCombatAdapter getGroundCombatAdapter() {
        return new Dnd5eTo3R4Adapter();
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.COMBAT,
                MechanicType.CRAFTING,
                MechanicType.INVENTORY,
                MechanicType.RPG,
                MechanicType.TACTICAL,
                MechanicType.CHARACTER,
                MechanicType.ENCOUNTER,
                MechanicType.CAMPAIGN,
                MechanicType.SUPERNATURAL,
                MechanicType.EXPLORATION
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts Dnd5eDiceService to DiceService interface.
     */
    private static class Dnd5eDiceAdapter implements DiceService {
        private final Dnd5eDiceService delegate;

        Dnd5eDiceAdapter(Dnd5eDiceService delegate) {
            this.delegate = delegate;
        }

        @Override
        public DiceRollResult rollDSides(int sides) {
            var r = delegate.rollDice("d" + sides);
            return new DiceRollResult(r.getFormula(), r.getIndividualRolls(), r.getTotal(), r.getDescription());
        }

        @Override
        public DiceRollResult rollNdX(int count, int sides) {
            var r = delegate.rollDice(count + "d" + sides);
            return new DiceRollResult(r.getFormula(), r.getIndividualRolls(), r.getTotal(), r.getDescription());
        }

        @Override
        public DiceRollResult rollFormula(String formula) {
            var r = delegate.rollDice(formula);
            return new DiceRollResult(r.getFormula(), r.getIndividualRolls(), r.getTotal(), r.getDescription());
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            var r = delegate.rollWithAdvantage(formula);
            return new DiceRollResult(r.getFormula(), r.getIndividualRolls(), r.getTotal(), r.getDescription());
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            var r = delegate.rollWithDisadvantage(formula);
            return new DiceRollResult(r.getFormula(), r.getIndividualRolls(), r.getTotal(), r.getDescription());
        }

        @Override
        public DiceRollResult roll2D6WithModifier(int modifier) {
            String formula = modifier >= 0 ? "2d6+" + modifier : "2d6" + modifier;
            var r = delegate.rollDice(formula);
            return new DiceRollResult(r.getFormula(), r.getIndividualRolls(), r.getTotal(), r.getDescription());
        }
    }

    /**
     * Adapts Dnd5eCombatService to CombatService interface.
     */
    private static class Dnd5eCombatAdapter implements CombatService {
        private final Dnd5eCombatService delegate;

        Dnd5eCombatAdapter(Dnd5eCombatService delegate) {
            this.delegate = delegate;
        }

        @Override
        public Long createCombat(String encounterName) {
            return delegate.createCombat(encounterName).getId();
        }

        @Override
        public CombatInfo getCombat(Long combatId) {
            var combat = delegate.getCombat(combatId);
            CombatInfo info = new CombatInfo();
            info.setId(combat.getId());
            info.setEncounterName(combat.getEncounterName());
            info.setStatus(combat.getStatus());
            info.setCurrentRound(combat.getCurrentRound());
            return info;
        }

        @Override
        public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
            var combatant = delegate.addCombatant(combatId, name, armorClass, maxHp, isCharacter);
            CombatantInfo info = new CombatantInfo();
            info.setName(combatant.getName());
            info.setArmorClass(combatant.getArmorClass());
            info.setCurrentHp(combatant.getCurrentHp());
            info.setMaxHp(combatant.getMaxHp());
            info.setAlive(combatant.isAlive());
            return info;
        }

        @Override
        public CombatInfo rollInitiative(Long combatId, Map<String, Integer> dexModifiers) {
            var combat = delegate.rollInitiative(combatId, dexModifiers);
            CombatInfo info = new CombatInfo();
            info.setId(combat.getId());
            info.setEncounterName(combat.getEncounterName());
            info.setStatus(combat.getStatus());
            info.setCurrentRound(combat.getCurrentRound());
            return info;
        }

        @Override
        public AttackResult makeAttack(Long combatId, AttackRequest request) {
            var dndRequest = new com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackRequest();
            dndRequest.setAttackerName(request.getAttackerName());
            dndRequest.setTargetName(request.getTargetName());
            dndRequest.setAttackBonus(request.getAttackBonus());
            dndRequest.setDamageFormula(request.getDamageFormula());
            dndRequest.setDamageType(request.getDamageType());
            dndRequest.setAdvantage(request.isAdvantage());
            dndRequest.setDisadvantage(request.isDisadvantage());

            var dndResponse = delegate.makeAttack(combatId, dndRequest);

            AttackResult result = new AttackResult();
            result.setAttackerName(dndResponse.getAttackerName());
            result.setTargetName(dndResponse.getTargetName());
            result.setAttackRoll(dndResponse.getAttackRoll());
            result.setAttackTotal(dndResponse.getAttackTotal());
            result.setHit(dndResponse.isHit());
            result.setCriticalHit(dndResponse.isCriticalHit());
            result.setCriticalFail(dndResponse.isCriticalFail());
            result.setDamageDealt(dndResponse.getDamageDealt());
            result.setDamageType(dndResponse.getDamageType());
            result.setRemainingHp(dndResponse.getRemainingHP());
            result.setMessage(dndResponse.getMessage());
            return result;
        }

        @Override
        public CombatantInfo healCombatant(Long combatId, String name, int amount) {
            var combatant = delegate.healCombatant(combatId, name, amount);
            CombatantInfo info = new CombatantInfo();
            info.setName(combatant.getName());
            info.setCurrentHp(combatant.getCurrentHp());
            info.setMaxHp(combatant.getMaxHp());
            info.setAlive(combatant.isAlive());
            return info;
        }

        @Override
        public CombatInfo endCombat(Long combatId, String status) {
            var combat = delegate.endCombat(combatId, status);
            CombatInfo info = new CombatInfo();
            info.setId(combat.getId());
            info.setStatus(combat.getStatus());
            return info;
        }
    }

    /**
     * Adapts Dnd5eInventoryService to InventoryService interface.
     */
    private static class Dnd5eInventoryAdapter implements InventoryService {
        private final Dnd5eInventoryService delegate;

        Dnd5eInventoryAdapter(Dnd5eInventoryService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            // Dnd5eInventoryService uses different method signatures
            return List.of();
        }

        @Override
        public InventoryItem addItem(Long characterId, InventoryItem item) {
            return item;
        }

        @Override
        public boolean removeItem(Long itemId) {
            return false;
        }

        @Override
        public String useItem(Long characterId, Long itemId) {
            return "Item usage not directly available through bridge";
        }
    }

    /**
     * Adapts Dnd5eCraftingService to CraftingService interface.
     */
    private static class Dnd5eCraftingAdapter implements CraftingService {
        private final Dnd5eCraftingService delegate;

        Dnd5eCraftingAdapter(Dnd5eCraftingService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<CraftingRecipe> listRecipes() {
            return delegate.listRecipes().stream().map(recipe -> {
                CraftingRecipe cr = new CraftingRecipe();
                cr.setId(recipe.getId());
                cr.setName(recipe.getName());
                cr.setDescription(recipe.getDescription());
                cr.setCategory(recipe.getCategory());
                return cr;
            }).toList();
        }

        @Override
        public CraftingRecipe getRecipe(Long recipeId) {
            var recipe = delegate.getRecipe(recipeId);
            if (recipe == null) return null;
            CraftingRecipe cr = new CraftingRecipe();
            cr.setId(recipe.getId());
            cr.setName(recipe.getName());
            return cr;
        }

        @Override
        public CraftingProject startProject(Long characterId, Long recipeId) {
            var request = new com.yisy.modules.doramcu.dnd5e.dto.StartCraftingProjectRequest();
            request.setCharacterId(characterId);
            request.setRecipeId(recipeId);
            var project = delegate.startProject(request);
            CraftingProject cp = new CraftingProject();
            cp.setId(project.getId());
            cp.setCharacterId(project.getCharacterId());
            cp.setRecipeId(project.getRecipeId());
            cp.setStatus(project.getStatus());
            return cp;
        }

        @Override
        public CraftingProject progressProject(Long projectId) {
            return new CraftingProject() {{
                setId(projectId);
                setStatus("IN_PROGRESS");
            }};
        }

        @Override
        public CraftingProject completeProject(Long projectId) {
            return new CraftingProject() {{
                setId(projectId);
                setStatus("COMPLETED");
            }};
        }
    }

    /**
     * Adapts Dnd5eDungeonRoomService to DungeonRoomService interface.
     */
    private static class Dnd5eDungeonRoomAdapter implements DungeonRoomService {
        private final Dnd5eDungeonRoomService delegate;

        Dnd5eDungeonRoomAdapter(Dnd5eDungeonRoomService delegate) {
            this.delegate = delegate;
        }

        @Override
        public DungeonRoom createRoom(DungeonRoom room) {
            var dndRoom = new com.yisy.modules.doramcu.dnd5e.model.Dnd5eDungeonRoom();
            dndRoom.setName(room.getName());
            dndRoom.setDescription(room.getDescription());
            dndRoom.setDungeonId(room.getDungeonId());
            dndRoom.setTheme(room.getTheme());
            dndRoom.setSize(room.getSize());
            dndRoom.setTrap(room.getTrap());
            dndRoom.setTrapDc(room.getTrapDc());
            dndRoom.setTrapDamage(room.getTrapDamage());
            dndRoom.setTrapDamageExpr(room.getTrapDamageExpr());
            dndRoom.setTreasure(room.getTreasure());
            dndRoom.setGoldPieces(room.getGoldPieces());
            dndRoom.setMagicRarity(room.getMagicRarity());
            dndRoom.setEncounter(room.getEncounter());
            dndRoom.setEncounterDifficulty(room.getEncounterDifficulty());
            dndRoom.setXpReward(room.getXpReward());
            dndRoom.setMinLevel(room.getMinLevel());
            dndRoom.setMaxLevel(room.getMaxLevel());
            dndRoom.setDmSecrets(room.getDmSecrets());

            var created = delegate.createRoom(dndRoom);

            DungeonRoom dr = new DungeonRoom();
            dr.setId(created.getId());
            dr.setName(created.getName());
            dr.setDescription(created.getDescription());
            dr.setDungeonId(created.getDungeonId());
            dr.setTheme(created.getTheme());
            return dr;
        }

        @Override
        public DungeonRoom getRoom(Long id) {
            return delegate.getRoom(id).map(room -> {
                DungeonRoom dr = new DungeonRoom();
                dr.setId(room.getId());
                dr.setName(room.getName());
                dr.setDescription(room.getDescription());
                dr.setDungeonId(room.getDungeonId());
                dr.setTheme(room.getTheme());
                return dr;
            }).orElse(null);
        }

        @Override
        public List<DungeonRoom> listRoomsByDungeon(String dungeonId) {
            return delegate.listRoomsByDungeon(dungeonId).stream().map(room -> {
                DungeonRoom dr = new DungeonRoom();
                dr.setId(room.getId());
                dr.setName(room.getName());
                dr.setDungeonId(room.getDungeonId());
                dr.setTheme(room.getTheme());
                return dr;
            }).toList();
        }

        @Override
        public List<DungeonRoom> listRoomsByTheme(String theme) {
            return delegate.listRoomsByTheme(theme).stream().map(room -> {
                DungeonRoom dr = new DungeonRoom();
                dr.setId(room.getId());
                dr.setName(room.getName());
                dr.setTheme(room.getTheme());
                return dr;
            }).toList();
        }

        @Override
        public DungeonRoom discoverRoom(Long id) {
            var room = delegate.discoverRoom(id);
            DungeonRoom dr = new DungeonRoom();
            dr.setId(room.getId());
            dr.setName(room.getName());
            dr.setDiscovered(true);
            return dr;
        }

        @Override
        public void addConnection(Long fromId, Long toId) {
            delegate.addConnection(fromId, toId);
        }

        @Override
        public List<DungeonRoom> getConnectedRooms(Long roomId) {
            return delegate.getConnectedRooms(roomId).stream().map(room -> {
                DungeonRoom dr = new DungeonRoom();
                dr.setId(room.getId());
                dr.setName(room.getName());
                return dr;
            }).toList();
        }

        @Override
        public void createCircularConnections(String dungeonId) {
            delegate.createCircularConnections(dungeonId);
        }

        @Override
        public TrapCheckResult makeTrapCheck(Long roomId, int dexModifier, int proficiencyBonus, int roll) {
            var result = delegate.makeTrapCheck(roomId, dexModifier, proficiencyBonus, roll);
            return new TrapCheckResult(result.getRoomId(), result.getRoomName(), result.getDc(),
                    result.isSuccess(), result.isCriticalFailure(), result.getDamageMultiplier(),
                    result.getDamageType(), result.getDamageExpr());
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            var result = delegate.discoverTreasure(roomId);
            return new TreasureResult(result.getRoomId(), result.getRoomName(), result.getTreasure(),
                    result.getGoldPieces(), result.getMagicRarity());
        }
    }

    /**
     * Adapts Dnd5eSpellService to SpellService interface.
     */
    private static class Dnd5eSpellAdapter implements SpellService {
        private final Dnd5eSpellService delegate;

        Dnd5eSpellAdapter(Dnd5eSpellService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Spell> listSpells() {
            return delegate.getAllSpells().stream().map(spell -> {
                Spell s = new Spell();
                s.setId(spell.getId());
                s.setName(spell.getName());
                s.setLevel(spell.getLevel());
                s.setSchool(spell.getSchool());
                s.setDescription(spell.getDescription());
                return s;
            }).toList();
        }

        @Override
        public Spell getSpell(Long spellId) {
            var allSpells = delegate.getAllSpells();
            for (var spell : allSpells) {
                if (spell.getId().equals(spellId)) {
                    Spell s = new Spell();
                    s.setId(spell.getId());
                    s.setName(spell.getName());
                    s.setLevel(spell.getLevel());
                    s.setSchool(spell.getSchool());
                    s.setDescription(spell.getDescription());
                    return s;
                }
            }
            return null;
        }

        @Override
        public List<Spell> listSpellsByLevel(int level) {
            return delegate.getSpellsByLevel(level).stream().map(spell -> {
                Spell s = new Spell();
                s.setId(spell.getId());
                s.setName(spell.getName());
                s.setLevel(spell.getLevel());
                s.setSchool(spell.getSchool());
                return s;
            }).toList();
        }

        @Override
        public SpellCastResult castSpell(Long characterId, Long spellId, Long targetId) {
            // Spell casting requires combat context — not directly available
            var spell = getSpell(spellId);
            return new SpellCastResult(spellId, spell != null ? spell.getName() : "Unknown", false,
                    "Spell casting requires combat context", 0);
        }
    }

    /**
     * Adapts Dnd5ePsionService to PsionicsService interface.
     */
    private static class Dnd5ePsionicsAdapter implements PsionicsService {
        private final Dnd5ePsionService delegate;

        Dnd5ePsionicsAdapter(Dnd5ePsionService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<PsionicPower> listPowers() {
            // Dnd5ePsionService manages psions, not individual powers directly
            // Return empty list — psion powers are accessed through psion subclasses
            return List.of();
        }

        @Override
        public PsionicPower getPower(Long powerId) {
            return null;
        }

        @Override
        public PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId) {
            return new PsionicPowerResult(powerId, "Unknown", false,
                    "Psionics not directly available — use psion subclass", 0);
        }
    }

    /**
     * Adapts Dnd5eEncounterService to EncounterService interface.
     */
    private static class Dnd5eEncounterAdapter implements EncounterService {
        private final Dnd5eEncounterService delegate;

        Dnd5eEncounterAdapter(Dnd5eEncounterService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Encounter> listEncounters() {
            return delegate.listEncounters().stream().map(enc -> {
                Encounter e = new Encounter();
                e.setId(enc.getId());
                e.setName(enc.getName());
                e.setDescription(enc.getDescription());
                e.setDifficulty(enc.getDifficulty());
                return e;
            }).toList();
        }

        @Override
        public Encounter getEncounter(Long encounterId) {
            return delegate.getEncounter(encounterId).map(enc -> {
                Encounter e = new Encounter();
                e.setId(enc.getId());
                e.setName(enc.getName());
                e.setDescription(enc.getDescription());
                e.setDifficulty(enc.getDifficulty());
                return e;
            }).orElse(null);
        }

        @Override
        public Encounter generateRandomEncounter(String areaId, int level) {
            var encounters = delegate.listEncountersByLevel(level);
            if (encounters.isEmpty()) {
                encounters = delegate.listEncounters();
            }
            if (encounters.isEmpty()) return null;
            var enc = encounters.get(com.yisy.modules.doramcu.util.DiceUtil.rollIndex(encounters.size()));
            Encounter e = new Encounter();
            e.setId(enc.getId());
            e.setName(enc.getName());
            e.setDescription(enc.getDescription());
            e.setDifficulty(enc.getDifficulty());
            e.setAreaId(areaId);
            return e;
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            var encOpt = delegate.getEncounter(encounterId);
            if (encOpt.isEmpty()) {
                return new EncounterResult(encounterId, "Unknown", false, "Encounter not found", 0);
            }
            var enc = encOpt.get();
            boolean success = characterIds.size() >= 2;
            return new EncounterResult(enc.getId(), enc.getName(), success,
                    success ? "Encounter resolved successfully" : "Encounter failed", 0);
        }
    }

    /**
     * Adapts Dnd5eCharacterService to CharacterService interface.
     */
    private static class Dnd5eCharacterAdapter implements CharacterService {
        private final Dnd5eCharacterService delegate;

        Dnd5eCharacterAdapter(Dnd5eCharacterService delegate) {
            this.delegate = delegate;
        }

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            try {
                var character = delegate.getCharacter(characterId);
            } catch (IllegalArgumentException e) {
                return null;
            }
            return null;
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            return delegate.listCharacters().stream().map(character -> {
                CharacterInfo info = new CharacterInfo();
                info.setId(character.getId());
                info.setName(character.getName());
                info.setLevel(character.getLevel());
                info.setExperiencePoints(character.getExperiencePoints());
                info.setHitPoints(character.getHitPoints());
                info.setMaxHitPoints(character.getMaxHitPoints());
                info.setStrScore(character.getStrScore());
                info.setDexScore(character.getDexScore());
                info.setConScore(character.getConScore());
                info.setIntScore(character.getIntScore());
                info.setWisScore(character.getWisScore());
                info.setChaScore(character.getChaScore());
                return info;
            }).toList();
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            try {
                var character = delegate.getCharacter(characterId);
                var request = new com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest();
                request.setStrScore(str);
                request.setDexScore(dex);
                request.setConScore(con);
                request.setIntScore(intelligence);
                request.setWisScore(wis);
                request.setChaScore(cha);
                delegate.updateCharacter(characterId, request);
            } catch (IllegalArgumentException e) {
                // Character not found, ignore
            }
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            try {
                var character = delegate.getCharacter(characterId);
                var request = new com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest();
                request.setExperiencePoints(character.getExperiencePoints() + xp);
                var updated = delegate.updateCharacter(characterId, request);
                CharacterInfo info = new CharacterInfo();
                info.setId(updated.getId());
                info.setName(updated.getName());
                info.setExperiencePoints(updated.getExperiencePoints());
                info.setLevel(updated.getLevel());
                return info;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    /**
     * Adapts Dnd5eCampaignService to CampaignService interface.
     */
    private static class Dnd5eCampaignAdapter implements CampaignService {
        private final Dnd5eCampaignService delegate;

        Dnd5eCampaignAdapter(Dnd5eCampaignService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Campaign> listCampaigns() {
            return delegate.listCampaigns().stream().map(camp -> {
                Campaign c = new Campaign();
                c.setId(camp.getId());
                c.setName(camp.getName());
                c.setDescription(camp.getDescription());
                return c;
            }).toList();
        }

        @Override
        public Campaign getCampaign(Long campaignId) {
            var camp = delegate.getCampaign(campaignId);
            if (camp == null) return null;
            Campaign c = new Campaign();
            c.setId(camp.getId());
            c.setName(camp.getName());
            return c;
        }

        @Override
        public Campaign createCampaign(String name, String description) {
            var request = new com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest();
            request.setName(name);
            request.setDescription(description);
            var camp = delegate.createCampaign(request);
            Campaign c = new Campaign();
            c.setId(camp.getId());
            c.setName(camp.getName());
            return c;
        }

        @Override
        public void addCharacterToCampaign(Long campaignId, Long characterId) {
            var request = new com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest();
            request.setCharacterId(characterId);
            delegate.addCharacterToCampaign(campaignId, request);
        }
    }

    /**
     * Adapts Dnd5eRulesService to RulesService interface.
     */
    private static class Dnd5eRulesAdapter implements RulesService {
        private final Dnd5eRulesService delegate;

        Dnd5eRulesAdapter(Dnd5eRulesService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<RulesEntry> listRules() {
            return delegate.getAllRules().stream().map(entry -> {
                RulesEntry r = new RulesEntry();
                r.setId(entry.getId());
                r.setTitle(entry.getTitle());
                r.setCategory(entry.getCategory());
                r.setText(entry.getText());
                r.setSource(entry.getSource());
                return r;
            }).toList();
        }

        @Override
        public List<RulesEntry> searchRules(String keyword) {
            return delegate.searchRules(keyword).stream().map(entry -> {
                RulesEntry r = new RulesEntry();
                r.setId(entry.getId());
                r.setTitle(entry.getTitle());
                r.setCategory(entry.getCategory());
                r.setText(entry.getText());
                r.setSource(entry.getSource());
                return r;
            }).toList();
        }

        @Override
        public RulesEntry getRule(Long id) {
            var allRules = delegate.getAllRules();
            for (var entry : allRules) {
                if (entry.getId().equals(id)) {
                    RulesEntry r = new RulesEntry();
                    r.setId(entry.getId());
                    r.setTitle(entry.getTitle());
                    r.setCategory(entry.getCategory());
                    r.setText(entry.getText());
                    r.setSource(entry.getSource());
                    return r;
                }
            }
            return null;
        }
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }
}
