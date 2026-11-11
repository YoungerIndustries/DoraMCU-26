package com.yisy.modules.doramcu.traveller;

/**
 * Historical Third Reich aircraft presets for conversion to Traveller equivalents.
 *
 * @deprecated Use the new {@code com.yisy.modules.doramcu.thirdreich} package for 3R4 ground combat.
 * This class remains for backward compatibility with the Traveller air combat conversion layer.
 */
@Deprecated
public enum ThirdReichAircraft {
    MESSERSCHMITT_BF109("Messerschmitt Bf 109", "Germany", TravellerSpacecraftType.FIGHTER, 7, 3, 2, 4, 80),
    MESSERSCHMITT_BF110("Messerschmitt Bf 110", "Germany", TravellerSpacecraftType.FIGHTER, 7, 4, 2, 3, 120),
    FOCKE_WULF_FW190("Focke-Wulf Fw 190", "Germany", TravellerSpacecraftType.FIGHTER, 7, 4, 3, 4, 100),
    HEINKEL_HE111("Heinkel He 111", "Germany", TravellerSpacecraftType.BOMBER, 7, 5, 1, 2, 200),
    DO_RADO_DO17("Dornier Do 17", "Germany", TravellerSpacecraftType.BOMBER, 7, 4, 1, 3, 180),
    JU88("Junkers Ju 88", "Germany", TravellerSpacecraftType.BOMBER, 7, 5, 2, 3, 220),
    SPITFIRE("Supermarine Spitfire", "United Kingdom", TravellerSpacecraftType.FIGHTER, 7, 3, 2, 5, 90),
    HURRICANE("Hawker Hurricane", "United Kingdom", TravellerSpacecraftType.FIGHTER, 7, 3, 2, 3, 70),
    WELLINGTON("Vickers Wellington", "United Kingdom", TravellerSpacecraftType.BOMBER, 7, 5, 1, 2, 190),
    LANCASTER("Avro Lancaster", "United Kingdom", TravellerSpacecraftType.BOMBER, 8, 6, 1, 1, 300),
    P_51_MUSTANG("P-51 Mustang", "United States", TravellerSpacecraftType.FIGHTER, 8, 3, 3, 5, 110),
    P_47_THUNDERBOLT("P-47 Thunderbolt", "United States", TravellerSpacecraftType.FIGHTER, 8, 4, 3, 3, 130),
    P_38_LIGHTNING("P-38 Lightning", "United States", TravellerSpacecraftType.INTERCEPTOR, 8, 4, 3, 4, 140),
    B_17_FORTRESS("B-17 Flying Fortress", "United States", TravellerSpacecraftType.BOMBER, 8, 7, 2, 1, 350),
    B_24_LIBERATOR("B-24 Liberator", "United States", TravellerSpacecraftType.BOMBER, 8, 6, 1, 2, 280),
    ME262("Messerschmitt Me 262", "Germany", TravellerSpacecraftType.INTERCEPTOR, 8, 4, 4, 6, 250);

    private final String name;
    private final String nationality;
    private final TravellerSpacecraftType type;
    private final int techLevel;
    private final int attackValue;
    private final int defenseValue;
    private final int movement;
    private final int cost;

    ThirdReichAircraft(String name, String nationality, TravellerSpacecraftType type, int techLevel,
                       int attackValue, int defenseValue, int movement, int cost) {
        this.name = name;
        this.nationality = nationality;
        this.type = type;
        this.techLevel = techLevel;
        this.attackValue = attackValue;
        this.defenseValue = defenseValue;
        this.movement = movement;
        this.cost = cost;
    }

    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public TravellerSpacecraftType getType() { return type; }
    public int getTechLevel() { return techLevel; }
    public int getAttackValue() { return attackValue; }
    public int getDefenseValue() { return defenseValue; }
    public int getMovement() { return movement; }
    public int getCost() { return cost; }
}
