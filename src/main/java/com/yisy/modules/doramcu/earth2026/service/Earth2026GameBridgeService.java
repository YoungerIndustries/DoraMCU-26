package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.CampaignService;
import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.CraftingService;
import com.yisy.modules.doramcu.gamecore.DiceService;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.gamecore.GameModule;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.InventoryService;
import com.yisy.modules.doramcu.gamecore.PsionicsService;
import com.yisy.modules.doramcu.gamecore.RulesService;
import com.yisy.modules.doramcu.gamecore.SpellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Bridge service that gives Earth2026 access to all D&amp;D and Traveller features.
 * <p>
 * This service uses the {@link GameModuleRegistry} to access features from other game modules
 * without duplicating code. Earth2026 characters can use D&amp;D spells, psionics, crafting,
 * dungeon rooms, combat, and more â€” all through this bridge.
 * </p>
 * <p>
 * Key features available through this bridge:
 * <ul>
 *   <li>Dice rolling (from any module)</li>
 *   <li>Combat resolution (D&amp;D tactical or Traveller firearms)</li>
 *   <li>Inventory management (D&amp;D items)</li>
 *   <li>Crafting/manufacturing (D&amp;D recipes)</li>
 *   <li>Dungeon room interiors (D&amp;D engine for building interiors)</li>
 *   <li>Spell casting (D&amp;D spells)</li>
 *   <li>Psionic powers (D&amp;D psionics)</li>
 *   <li>Encounters (D&amp;D random encounters)</li>
 *   <li>Campaigns (D&amp;D campaigns)</li>
 *   <li>Rules database (D&amp;D or Traveller rules)</li>
 * </ul>
 * </p>
 */
@Service
public class Earth2026GameBridgeService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026GameBridgeService.class);

    private final GameModuleRegistry gameModuleRegistry;

    public Earth2026GameBridgeService(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
        log.info("Earth2026 Game Bridge initialized with {} registered modules",
                gameModuleRegistry.getAllModules().size());
    }

    // ==================== Dice Rolling ====================

    /**
     * Roll dice using any available module's dice service.
     * Falls back to DiceUtil if no module is available.
     */
    public DiceService.DiceRollResult rollDice(String formula) {
        DiceService diceService = gameModuleRegistry.getDiceService(GameType.DND_5E);
        if (diceService != null) {
            return diceService.rollFormula(formula);
        }
        // Fallback: use DiceUtil directly
        return rollDiceFallback(formula);
    }

    private DiceService.DiceRollResult rollDiceFallback(String formula) {
        // Simple fallback using DiceUtil
        int total = com.yisy.modules.doramcu.util.DiceUtil.rollD20();
        return new DiceService.DiceRollResult(formula, List.of(total), total,
                formula + " = " + total);
    }

    // ==================== Combat ====================

    /**
     * Get the combat service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public CombatService getCombatService() {
        CombatService combat = gameModuleRegistry.getCombatService(GameType.DND_5E);
        if (combat != null) {
            return combat;
        }
        log.warn("No combat service available - D&amp;D module not registered");
        return null;
    }

    // ==================== Inventory ====================

    /**
     * Get the inventory service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public InventoryService getInventoryService() {
        InventoryService inventory = gameModuleRegistry.getInventoryService(GameType.DND_5E);
        if (inventory != null) {
            return inventory;
        }
        log.warn("No inventory service available - D&amp;D module not registered");
        return null;
    }

    // ==================== Crafting ====================

    /**
     * Get the crafting service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public CraftingService getCraftingService() {
        CraftingService crafting = gameModuleRegistry.getCraftingService(GameType.DND_5E);
        if (crafting != null) {
            return crafting;
        }
        log.warn("No crafting service available - D&amp;D module not registered");
        return null;
    }

    // ==================== Dungeon Rooms ====================

    /**
     * Get the dungeon room service from D&amp;D module.
     * Used for creating building interiors, 3D city rooms, and structure layouts.
     * Returns null if D&amp;D module is not available.
     */
    public DungeonRoomService getDungeonRoomService() {
        DungeonRoomService dungeon = gameModuleRegistry.getDungeonRoomService(GameType.DND_5E);
        if (dungeon != null) {
            return dungeon;
        }
        log.warn("No dungeon room service available - D&amp;D module not registered");
        return null;
    }

    /**
     * Create interior rooms for an Earth2026 building using D&amp;D dungeon engine.
     *
     * @param buildingId the building/structure identifier
     * @param rooms list of rooms to create
     * @return the created rooms
     */
    public List<DungeonRoomService.DungeonRoom> createBuildingInterior(String buildingId, List<DungeonRoomService.DungeonRoom> rooms) {
        DungeonRoomService dungeonService = getDungeonRoomService();
        if (dungeonService == null) {
            log.warn("Cannot create building interior - dungeon room service not available");
            return List.of();
        }

        List<DungeonRoomService.DungeonRoom> created = new java.util.ArrayList<>();
        for (DungeonRoomService.DungeonRoom room : rooms) {
            room.setDungeonId(buildingId);
            DungeonRoomService.DungeonRoom createdRoom = dungeonService.createRoom(room);
            created.add(createdRoom);
        }

        // Create circular connections for non-linear exploration
        dungeonService.createCircularConnections(buildingId);
        log.info("Created {} interior rooms for building {}", created.size(), buildingId);
        return created;
    }

    // ==================== Spells ====================

    /**
     * Get the spell service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public SpellService getSpellService() {
        SpellService spell = gameModuleRegistry.getSpellService(GameType.DND_5E);
        if (spell != null) {
            return spell;
        }
        log.warn("No spell service available - D&amp;D module not registered");
        return null;
    }

    /**
     * List all available spells from D&amp;D module.
     */
    public List<SpellService.Spell> listAllSpells() {
        SpellService spellService = getSpellService();
        return spellService != null ? spellService.listSpells() : List.of();
    }

    /**
     * Cast a spell using D&amp;D spell system.
     */
    public SpellService.SpellCastResult castSpell(Long characterId, Long spellId, Long targetId) {
        SpellService spellService = getSpellService();
        if (spellService == null) {
            return new SpellService.SpellCastResult(spellId, "Unknown", false,
                    "Spell service not available", 0);
        }
        return spellService.castSpell(characterId, spellId, targetId);
    }

    // ==================== Psionics ====================

    /**
     * Get the psionics service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public PsionicsService getPsionicsService() {
        PsionicsService psionics = gameModuleRegistry.getPsionicsService(GameType.DND_5E);
        if (psionics != null) {
            return psionics;
        }
        log.warn("No psionics service available - D&amp;D module not registered");
        return null;
    }

    /**
     * List all available psionic powers.
     */
    public List<PsionicsService.PsionicPower> listAllPsionicPowers() {
        PsionicsService psionicsService = getPsionicsService();
        return psionicsService != null ? psionicsService.listPowers() : List.of();
    }

    /**
     * Use a psionic power.
     */
    public PsionicsService.PsionicPowerResult usePsionicPower(Long characterId, Long powerId, Long targetId) {
        PsionicsService psionicsService = getPsionicsService();
        if (psionicsService == null) {
            return new PsionicsService.PsionicPowerResult(powerId, "Unknown", false,
                    "Psionics service not available", 0);
        }
        return psionicsService.usePower(characterId, powerId, targetId);
    }

    // ==================== Encounters ====================

    /**
     * Get the encounter service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public EncounterService getEncounterService() {
        EncounterService encounter = gameModuleRegistry.getEncounterService(GameType.DND_5E);
        if (encounter != null) {
            return encounter;
        }
        log.warn("No encounter service available - D&amp;D module not registered");
        return null;
    }

    /**
     * Generate a random encounter for an Earth2026 location.
     */
    public EncounterService.Encounter generateRandomEncounter(String locationId, int level) {
        EncounterService encounterService = getEncounterService();
        if (encounterService == null) {
            return null;
        }
        return encounterService.generateRandomEncounter(locationId, level);
    }

    // ==================== Campaigns ====================

    /**
     * Get the campaign service from D&amp;D module.
     * Returns null if D&amp;D module is not available.
     */
    public CampaignService getCampaignService() {
        CampaignService campaign = gameModuleRegistry.getCampaignService(GameType.DND_5E);
        if (campaign != null) {
            return campaign;
        }
        log.warn("No campaign service available - D&amp;D module not registered");
        return null;
    }

    // ==================== Rules ====================

    /**
     * Get the rules service from the specified module.
     * Defaults to D&amp;D if not specified.
     */
    public RulesService getRulesService(GameType gameType) {
        RulesService rules = gameModuleRegistry.getRulesService(gameType != null ? gameType : GameType.DND_5E);
        if (rules != null) {
            return rules;
        }
        log.warn("No rules service available for {}", gameType);
        return null;
    }

    /**
     * Search rules across all available modules.
     */
    public List<com.yisy.modules.doramcu.shared.model.RulesEntry> searchRules(String keyword) {
        List<com.yisy.modules.doramcu.shared.model.RulesEntry> results = new java.util.ArrayList<>();
        for (GameType gameType : GameType.values()) {
            RulesService rules = gameModuleRegistry.getRulesService(gameType);
            if (rules != null) {
                results.addAll(rules.searchRules(keyword));
            }
        }
        return results;
    }

    // ==================== Module Status ====================

    /**
     * Get the status of all available game modules.
     */
    public List<GameModule> getAvailableModules() {
        return gameModuleRegistry.getAllModules();
    }

    /**
     * Check if a specific module is available.
     */
    public boolean isModuleAvailable(GameType gameType) {
        return gameModuleRegistry.isModuleEnabled(gameType);
    }

    /**
     * Get summary of available features.
     */
    public String getFeatureSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Earth2026 Game Bridge - Available Features:\n");

        for (GameModule module : gameModuleRegistry.getAllModules()) {
            sb.append("  Module: ").append(module.getModuleName()).append(" (").append(module.getGameType()).append(")\n");
            sb.append("    Enabled: ").append(module.isEnabled()).append("\n");
            sb.append("    Dice: ").append(module.getDiceService() != null).append("\n");
            sb.append("    Combat: ").append(module.getCombatService() != null).append("\n");
            sb.append("    Inventory: ").append(module.getInventoryService() != null).append("\n");
            sb.append("    Crafting: ").append(module.getCraftingService() != null).append("\n");
            sb.append("    Dungeon Rooms: ").append(module.getDungeonRoomService() != null).append("\n");
            sb.append("    Spells: ").append(module.getSpellService() != null).append("\n");
            sb.append("    Psionics: ").append(module.getPsionicsService() != null).append("\n");
            sb.append("    Encounters: ").append(module.getEncounterService() != null).append("\n");
            sb.append("    Campaigns: ").append(module.getCampaignService() != null).append("\n");
            sb.append("    Rules: ").append(module.getRulesService() != null).append("\n");
        }
        return sb.toString();
    }
}
