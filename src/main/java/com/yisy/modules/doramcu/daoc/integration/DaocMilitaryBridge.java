package com.yisy.modules.doramcu.daoc.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dark Age of Camelot military bridge — maps DaOC units to generic military types.
 */
@Component
public class DaocMilitaryBridge {

    private static final Map<String, GenericUnitType> UNIT_MAPPING = new HashMap<>();

    static {
        // Hibernia classes
        UNIT_MAPPING.put("MARTYR", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("CHAMPION", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("WARRIOR", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("MAGE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("RANGER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("CLERIC", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("BARD", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("NECROMANCER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("DRUID", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ASSASSIN", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("NIGHTSHADE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ANIMIST", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("BANSHEE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ELDITCH", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ENCHANTER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("MENTALIST", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("VAMPIIR", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("VALEWALKER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("HERO", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("WARDEN", GenericUnitType.INFANTRY);
        // Midgard classes
        UNIT_MAPPING.put("BERSERKER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("THANE", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("RUNEMASTER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("SPIRITMASTER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("HEALER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("HUNTER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("SHADOWBLADE", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("SAVAGE", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("SHAMAN", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("SKALD", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("ARMSMAN", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("BLADEMASTER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("SCOUT", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("MINOTAUR", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("REAVER", GenericUnitType.INFANTRY);
        UNIT_MAPPING.put("MERCENARY", GenericUnitType.INFANTRY);
        // Albion classes
        UNIT_MAPPING.put("PALADIN", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("SORCERER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("WIZARD", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("INFILTRATOR", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("MINSTREL", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("FRIAR", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("HERETIC", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("THEURGIST", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("CABALIST", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("BONEDANCER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("MAULER", GenericUnitType.SPECIAL_FORCES);
        UNIT_MAPPING.put("LEGION", GenericUnitType.SPECIAL_FORCES);
    }

    public GenericUnitType mapToGenericType(String daocUnitType) {
        return UNIT_MAPPING.getOrDefault(daocUnitType, GenericUnitType.INFANTRY);
    }

    public Map<String, GenericUnitType> getAllMappings() {
        return new HashMap<>(UNIT_MAPPING);
    }
}
