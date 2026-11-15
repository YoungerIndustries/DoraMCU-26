package com.yisy.modules.doramcu.starshiptroopers.integration;

import com.yisy.modules.doramcu.hex.planet.military.GenericUnitType;
import com.yisy.modules.doramcu.hex.planet.military.ModuleUnitMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/** Bridges Starship Troopers units to the generic military system. */
@Component
public class StarshipTroopersMilitaryBridge implements ModuleUnitMapper {
    private static final Map<String, GenericUnitType> MAP = new HashMap<>();
    static {
        MAP.put("MOBILE_INFANTRY", GenericUnitType.INFANTRY);
        MAP.put("Cavalry", GenericUnitType.ARMORED);
        MAP.put("CADET", GenericUnitType.INFANTRY);
        MAP.put("BRAIN_BUG", GenericUnitType.SPECIAL_FORCES);
        MAP.put("WARRIOR_BUG", GenericUnitType.ARMORED);
        MAP.put("PLASMA_BUG", GenericUnitType.ARTILLERY);
    }
    @Override public String getModuleId() { return "starshiptroopers"; }
    @Override public GenericUnitType mapToGeneric(String t) { return MAP.getOrDefault(t, GenericUnitType.INFANTRY); }
    @Override public List<String> mapFromGeneric(GenericUnitType g) { List<String> r = new ArrayList<>(); for (var e : MAP.entrySet()) if (e.getValue() == g) r.add(e.getKey()); return r; }
    @Override public List<String> getAvailableUnitTypes(int tl) { return new ArrayList<>(MAP.keySet()); }
    @Override public String getUnitDisplayName(String t) { return t; }
}
