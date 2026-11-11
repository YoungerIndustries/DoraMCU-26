package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Service that orchestrates air battles.
 */
public class AirBattleService {
    private final Map<String, AirBattle> battles = new HashMap<>();
    private final AircraftConversionService conversionService = new AircraftConversionService();
    private final TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();

    public AirBattle createBattle(String id, List<TravellerSpacecraft> attackers,
                                  List<TravellerSpacecraft> defenders,
                                  AirBattle.Altitude altitude, AirBattle.BattleMode mode) {
        AirBattle battle = new AirBattle(id);
        battle.getAttackerUnits().addAll(attackers);
        battle.getDefenderUnits().addAll(defenders);
        battle.setAltitude(altitude);
        battle.setMode(mode);
        battles.put(id, battle);
        return battle;
    }

    public AirBattleResult resolveBattle(String id) {
        AirBattle battle = battles.get(id);
        if (battle == null) throw new IllegalArgumentException("Battle not found: " + id);
        if (battle.getMode() == AirBattle.BattleMode.ABSTRACT) {
            return battle.resolveAbstract();
        } else {
            return battle.fightToCompletion();
        }
    }

    public AirBattle getBattle(String id) {
        return battles.get(id);
    }

    public TravellerSpacecraft createSpacecraft(TravellerSpacecraftType type, int tl) {
        return factory.createSpacecraft(type, tl);
    }

    public TravellerSpacecraft convertAircraft(ThirdReichAircraft aircraft) {
        return conversionService.convert(aircraft);
    }
}
