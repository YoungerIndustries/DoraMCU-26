package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleBattleAdapter;
import com.yisy.modules.doramcu.hex.planet.military.UniversalBattleUnit;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dark Age of Camelot battle adapter — provides DaOC units to universal battles.
 */
@Component
public class DaocBattleAdapter implements ModuleBattleAdapter {

    private static final Map<String, String> UNIT_TYPES = new HashMap<>();
    private static final Map<String, GenericUnitType> GENERIC_MAP = new HashMap<>();
    private static final Map<String, String> MODEL_PATHS = new HashMap<>();
    private static final Map<String, String> TEXTURE_PATHS = new HashMap<>();

    static {
        UNIT_TYPES.put("MARTYR", "Martyr");
        GENERIC_MAP.put("MARTYR", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MARTYR", "/models/daoc/martyr.obj");
        TEXTURE_PATHS.put("MARTYR", "/textures/daoc/martyr.png");

        UNIT_TYPES.put("CHAMPION", "Champion");
        GENERIC_MAP.put("CHAMPION", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CHAMPION", "/models/daoc/champion.obj");
        TEXTURE_PATHS.put("CHAMPION", "/textures/daoc/champion.png");

        UNIT_TYPES.put("WARRIOR", "Warrior");
        GENERIC_MAP.put("WARRIOR", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("WARRIOR", "/models/daoc/warrior.obj");
        TEXTURE_PATHS.put("WARRIOR", "/textures/daoc/warrior.png");

        UNIT_TYPES.put("MAGE", "Mage");
        GENERIC_MAP.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MAGE", "/models/daoc/mage.obj");
        TEXTURE_PATHS.put("MAGE", "/textures/daoc/mage.png");

        UNIT_TYPES.put("RANGER", "Ranger");
        GENERIC_MAP.put("RANGER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("RANGER", "/models/daoc/ranger.obj");
        TEXTURE_PATHS.put("RANGER", "/textures/daoc/ranger.png");

        UNIT_TYPES.put("CLERIC", "Cleric");
        GENERIC_MAP.put("CLERIC", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CLERIC", "/models/daoc/cleric.obj");
        TEXTURE_PATHS.put("CLERIC", "/textures/daoc/cleric.png");

        UNIT_TYPES.put("BARD", "Bard");
        GENERIC_MAP.put("BARD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("BARD", "/models/daoc/bard.obj");
        TEXTURE_PATHS.put("BARD", "/textures/daoc/bard.png");

        UNIT_TYPES.put("NECROMANCER", "Necromancer");
        GENERIC_MAP.put("NECROMANCER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NECROMANCER", "/models/daoc/necromancer.obj");
        TEXTURE_PATHS.put("NECROMANCER", "/textures/daoc/necromancer.png");

        UNIT_TYPES.put("DRUID", "Druid");
        GENERIC_MAP.put("DRUID", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("DRUID", "/models/daoc/druid.obj");
        TEXTURE_PATHS.put("DRUID", "/textures/daoc/druid.png");

        UNIT_TYPES.put("ASSASSIN", "Assassin");
        GENERIC_MAP.put("ASSASSIN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ASSASSIN", "/models/daoc/assassin.obj");
        TEXTURE_PATHS.put("ASSASSIN", "/textures/daoc/assassin.png");

        // Hibernia classes
        UNIT_TYPES.put("NIGHTSHADE", "Nightshade");
        GENERIC_MAP.put("NIGHTSHADE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("NIGHTSHADE", "/models/daoc/nightshade.obj");
        TEXTURE_PATHS.put("NIGHTSHADE", "/textures/daoc/nightshade.png");

        UNIT_TYPES.put("ANIMIST", "Animist");
        GENERIC_MAP.put("ANIMIST", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ANIMIST", "/models/daoc/animist.obj");
        TEXTURE_PATHS.put("ANIMIST", "/textures/daoc/animist.png");

        UNIT_TYPES.put("BANSHEE", "Banshee");
        GENERIC_MAP.put("BANSHEE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("BANSHEE", "/models/daoc/banshee.obj");
        TEXTURE_PATHS.put("BANSHEE", "/textures/daoc/banshee.png");

        UNIT_TYPES.put("ELDITCH", "Eldritch");
        GENERIC_MAP.put("ELDITCH", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ELDITCH", "/models/daoc/eldritch.obj");
        TEXTURE_PATHS.put("ELDITCH", "/textures/daoc/eldritch.png");

        UNIT_TYPES.put("ENCHANTER", "Enchanter");
        GENERIC_MAP.put("ENCHANTER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("ENCHANTER", "/models/daoc/enchanter.obj");
        TEXTURE_PATHS.put("ENCHANTER", "/textures/daoc/enchanter.png");

        UNIT_TYPES.put("MENTALIST", "Mentalist");
        GENERIC_MAP.put("MENTALIST", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MENTALIST", "/models/daoc/mentalist.obj");
        TEXTURE_PATHS.put("MENTALIST", "/textures/daoc/mentalist.png");

        UNIT_TYPES.put("VAMPIIR", "Vampiir");
        GENERIC_MAP.put("VAMPIIR", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("VAMPIIR", "/models/daoc/vampiir.obj");
        TEXTURE_PATHS.put("VAMPIIR", "/textures/daoc/vampiir.png");

        UNIT_TYPES.put("VALEWALKER", "Valewalker");
        GENERIC_MAP.put("VALEWALKER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("VALEWALKER", "/models/daoc/valewalker.obj");
        TEXTURE_PATHS.put("VALEWALKER", "/textures/daoc/valewalker.png");

        UNIT_TYPES.put("HERO", "Hero");
        GENERIC_MAP.put("HERO", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("HERO", "/models/daoc/hero.obj");
        TEXTURE_PATHS.put("HERO", "/textures/daoc/hero.png");

        UNIT_TYPES.put("WARDEN", "Warden");
        GENERIC_MAP.put("WARDEN", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("WARDEN", "/models/daoc/warden.obj");
        TEXTURE_PATHS.put("WARDEN", "/textures/daoc/warden.png");

        UNIT_TYPES.put("BLADEMASTER", "Blademaster");
        GENERIC_MAP.put("BLADEMASTER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("BLADEMASTER", "/models/daoc/blademaster.obj");
        TEXTURE_PATHS.put("BLADEMASTER", "/textures/daoc/blademaster.png");

        // Midgard classes
        UNIT_TYPES.put("BERSERKER", "Berserker");
        GENERIC_MAP.put("BERSERKER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("BERSERKER", "/models/daoc/berserker.obj");
        TEXTURE_PATHS.put("BERSERKER", "/textures/daoc/berserker.png");

        UNIT_TYPES.put("THANE", "Thane");
        GENERIC_MAP.put("THANE", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("THANE", "/models/daoc/thane.obj");
        TEXTURE_PATHS.put("THANE", "/textures/daoc/thane.png");

        UNIT_TYPES.put("RUNEMASTER", "Runemaster");
        GENERIC_MAP.put("RUNEMASTER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("RUNEMASTER", "/models/daoc/runemaster.obj");
        TEXTURE_PATHS.put("RUNEMASTER", "/textures/daoc/runemaster.png");

        UNIT_TYPES.put("SPIRITMASTER", "Spiritmaster");
        GENERIC_MAP.put("SPIRITMASTER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SPIRITMASTER", "/models/daoc/spiritmaster.obj");
        TEXTURE_PATHS.put("SPIRITMASTER", "/textures/daoc/spiritmaster.png");

        UNIT_TYPES.put("HEALER", "Healer");
        GENERIC_MAP.put("HEALER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("HEALER", "/models/daoc/healer.obj");
        TEXTURE_PATHS.put("HEALER", "/textures/daoc/healer.png");

        UNIT_TYPES.put("HUNTER", "Hunter");
        GENERIC_MAP.put("HUNTER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("HUNTER", "/models/daoc/hunter.obj");
        TEXTURE_PATHS.put("HUNTER", "/textures/daoc/hunter.png");

        UNIT_TYPES.put("SHADOWBLADE", "Shadowblade");
        GENERIC_MAP.put("SHADOWBLADE", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SHADOWBLADE", "/models/daoc/shadowblade.obj");
        TEXTURE_PATHS.put("SHADOWBLADE", "/textures/daoc/shadowblade.png");

        UNIT_TYPES.put("SAVAGE", "Savage");
        GENERIC_MAP.put("SAVAGE", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("SAVAGE", "/models/daoc/savage.obj");
        TEXTURE_PATHS.put("SAVAGE", "/textures/daoc/savage.png");

        UNIT_TYPES.put("SHAMAN", "Shaman");
        GENERIC_MAP.put("SHAMAN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SHAMAN", "/models/daoc/shaman.obj");
        TEXTURE_PATHS.put("SHAMAN", "/textures/daoc/shaman.png");

        UNIT_TYPES.put("SKALD", "Skald");
        GENERIC_MAP.put("SKALD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SKALD", "/models/daoc/skald.obj");
        TEXTURE_PATHS.put("SKALD", "/textures/daoc/skald.png");

        UNIT_TYPES.put("BONEDANCER", "Bonedancer");
        GENERIC_MAP.put("BONEDANCER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("BONEDANCER", "/models/daoc/bonedancer.obj");
        TEXTURE_PATHS.put("BONEDANCER", "/textures/daoc/bonedancer.png");

        // Albion classes
        UNIT_TYPES.put("ARMSMAN", "Armsman");
        GENERIC_MAP.put("ARMSMAN", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("ARMSMAN", "/models/daoc/armsman.obj");
        TEXTURE_PATHS.put("ARMSMAN", "/textures/daoc/armsman.png");

        UNIT_TYPES.put("PALADIN", "Paladin");
        GENERIC_MAP.put("PALADIN", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("PALADIN", "/models/daoc/paladin.obj");
        TEXTURE_PATHS.put("PALADIN", "/textures/daoc/paladin.png");

        UNIT_TYPES.put("WIZARD", "Wizard");
        GENERIC_MAP.put("WIZARD", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("WIZARD", "/models/daoc/wizard.obj");
        TEXTURE_PATHS.put("WIZARD", "/textures/daoc/wizard.png");

        UNIT_TYPES.put("SORCERER", "Sorcerer");
        GENERIC_MAP.put("SORCERER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("SORCERER", "/models/daoc/sorcerer.obj");
        TEXTURE_PATHS.put("SORCERER", "/textures/daoc/sorcerer.png");

        UNIT_TYPES.put("FRIAR", "Friar");
        GENERIC_MAP.put("FRIAR", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("FRIAR", "/models/daoc/friar.obj");
        TEXTURE_PATHS.put("FRIAR", "/textures/daoc/friar.png");

        UNIT_TYPES.put("CABALIST", "Cabalism");
        GENERIC_MAP.put("CABALIST", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("CABALIST", "/models/daoc/cabalism.obj");
        TEXTURE_PATHS.put("CABALIST", "/textures/daoc/cabalism.png");

        UNIT_TYPES.put("HERETIC", "Heretic");
        GENERIC_MAP.put("HERETIC", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("HERETIC", "/models/daoc/heretic.obj");
        TEXTURE_PATHS.put("HERETIC", "/textures/daoc/heretic.png");

        UNIT_TYPES.put("INFILTRATOR", "Infiltrator");
        GENERIC_MAP.put("INFILTRATOR", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("INFILTRATOR", "/models/daoc/infiltrator.obj");
        TEXTURE_PATHS.put("INFILTRATOR", "/textures/daoc/infiltrator.png");

        UNIT_TYPES.put("SCOUT", "Scout");
        GENERIC_MAP.put("SCOUT", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("SCOUT", "/models/daoc/scout.obj");
        TEXTURE_PATHS.put("SCOUT", "/textures/daoc/scout.png");

        UNIT_TYPES.put("REAVER", "Reaver");
        GENERIC_MAP.put("REAVER", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("REAVER", "/models/daoc/reaver.obj");
        TEXTURE_PATHS.put("REAVER", "/textures/daoc/reaver.png");

        UNIT_TYPES.put("MERCENARY", "Mercenary");
        GENERIC_MAP.put("MERCENARY", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("MERCENARY", "/models/daoc/mercenary.obj");
        TEXTURE_PATHS.put("MERCENARY", "/textures/daoc/mercenary.png");

        UNIT_TYPES.put("MINSTREL", "Minstrel");
        GENERIC_MAP.put("MINSTREL", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MINSTREL", "/models/daoc/minstrel.obj");
        TEXTURE_PATHS.put("MINSTREL", "/textures/daoc/minstrel.png");

        UNIT_TYPES.put("THEURGIST", "Theurgist");
        GENERIC_MAP.put("THEURGIST", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("THEURGIST", "/models/daoc/theurgist.obj");
        TEXTURE_PATHS.put("THEURGIST", "/textures/daoc/theurgist.png");

        // Cross-realm and special
        UNIT_TYPES.put("MAULER", "Mauler");
        GENERIC_MAP.put("MAULER", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("MAULER", "/models/daoc/mauler.obj");
        TEXTURE_PATHS.put("MAULER", "/textures/daoc/mauler.png");

        UNIT_TYPES.put("MINOTAUR", "Minotaur");
        GENERIC_MAP.put("MINOTAUR", GenericUnitType.INFANTRY);
        MODEL_PATHS.put("MINOTAUR", "/models/daoc/minotaur.obj");
        TEXTURE_PATHS.put("MINOTAUR", "/textures/daoc/minotaur.png");

        UNIT_TYPES.put("LEGION", "Legion");
        GENERIC_MAP.put("LEGION", GenericUnitType.SPECIAL_FORCES);
        MODEL_PATHS.put("LEGION", "/models/daoc/legion.obj");
        TEXTURE_PATHS.put("LEGION", "/textures/daoc/legion.png");
    }

    @Override
    public String getModuleId() { return "DaOC"; }

    @Override
    public String getModuleName() { return "Dark Age of Camelot"; }

    @Override
    public Map<String, String> getAvailableUnitTypes() { return Collections.unmodifiableMap(UNIT_TYPES); }

    @Override
    public UniversalBattleUnit createUnit(String unitId, String unitName, String moduleUnitType, String faction, int techLevel) {
        GenericUnitType genericType = GENERIC_MAP.getOrDefault(moduleUnitType, GenericUnitType.INFANTRY);
        int scale = Math.max(1, techLevel / 3);
        return UniversalBattleUnit.builder(unitId, unitName, getModuleId(), moduleUnitType, genericType)
                .faction(faction)
                .attackValue(genericType.getBaseAttack() * scale)
                .defenseValue(genericType.getBaseDefense() * scale)
                .model3dPath(MODEL_PATHS.getOrDefault(moduleUnitType, null))
                .texturePath(TEXTURE_PATHS.getOrDefault(moduleUnitType, null))
                .build();
    }

    @Override
    public List<UniversalBattleUnit> createUnits(List<String> unitIds, String moduleUnitType, String faction, int techLevel) {
        return unitIds.stream()
                .map(unitId -> createUnit(unitId, moduleUnitType + "-" + unitId, moduleUnitType, faction, techLevel))
                .collect(Collectors.toList());
    }

    @Override
    public String getModelPath(String moduleUnitType) { return MODEL_PATHS.get(moduleUnitType); }

    @Override
    public String getTexturePath(String moduleUnitType) { return TEXTURE_PATHS.get(moduleUnitType); }

    @Override
    public Map<String, Integer> calculateCombatStats(UniversalBattleUnit unit, int terrainBonus, double commanderBonus) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("attack", unit.getAttackValue() + (int) (unit.getAttackValue() * commanderBonus));
        stats.put("defense", unit.getDefenseValue() + terrainBonus);
        stats.put("morale", Math.min(100, unit.getMorale() + (int) (commanderBonus * 10)));
        return stats;
    }

    @Override
    public String getFactionColor(String faction) {
        if (faction == null) return "#808080";
        switch (faction.toLowerCase()) {
            case "albion": return "#FFD700";
            case "midgard": return "#00FF00";
            case "hibernia": return "#FF0000";
            default: return "#808080";
        }
    }
}
