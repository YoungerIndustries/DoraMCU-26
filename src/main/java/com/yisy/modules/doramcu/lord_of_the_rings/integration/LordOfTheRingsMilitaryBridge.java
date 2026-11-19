package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/** Bridges Lord of the Rings units to the generic military system. */
@Component
public class LordOfTheRingsMilitaryBridge implements ModuleUnitMapper {
    private static final Map<String, GenericUnitType> MAP = new HashMap<>();
    static {
        MAP.put("ELVEN_GUARD", GenericUnitType.INFANTRY);
        MAP.put("ROHIRRIM", GenericUnitType.ARMORED);
        MAP.put("GONDOR_SOLDIER", GenericUnitType.INFANTRY);
        MAP.put("DWARVEN_WARRIOR", GenericUnitType.INFANTRY);
        MAP.put("HOBBIT", GenericUnitType.INFANTRY);
        MAP.put("ORC", GenericUnitType.INFANTRY);
        MAP.put("URUK_HAI", GenericUnitType.INFANTRY);
        MAP.put("WIZARD", GenericUnitType.SPECIAL_FORCES);
        MAP.put("ENT", GenericUnitType.ARMORED);
        MAP.put("NAZGUL", GenericUnitType.SPECIAL_FORCES);
        MAP.put("BALROG", GenericUnitType.ARTILLERY);
        MAP.put("GIANT_SPIDER", GenericUnitType.ARTILLERY);
        MAP.put("WARG", GenericUnitType.ARMORED);
        MAP.put("EASTERLING", GenericUnitType.INFANTRY);
        MAP.put("HARADRIM", GenericUnitType.INFANTRY);
    }
    @Override public String getModuleId() { return "lordoftherings"; }
    @Override public GenericUnitType mapToGeneric(String t) { return MAP.getOrDefault(t, GenericUnitType.INFANTRY); }
    @Override public List<String> mapFromGeneric(GenericUnitType g) { List<String> r = new ArrayList<>(); for (var e : MAP.entrySet()) if (e.getValue() == g) r.add(e.getKey()); return r; }
    @Override public List<String> getAvailableUnitTypes(int tl) { return new ArrayList<>(MAP.keySet()); }
    @Override public String getUnitDisplayName(String t) { return t; }
}
