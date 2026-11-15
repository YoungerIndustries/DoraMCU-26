package com.yisy.modules.doramcu.mechwarrior.config;

import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * MechWarrior tech level adapter.
 * <p>
 * Maps generic tech-level modules to MechWarrior/BattleTech lore-appropriate names and stats.
 * MechWarrior supports TL7–TL9 (Interplanetary through Galactic).
 * </p>
 * <p>
 * At TL7, mech technology is emerging with light and medium mechs.
 * At TL8, the full mech era with heavy mechs and advanced weapons.
 * At TL9, Clan-era technology with assault mechs and experimental systems.
 * </p>
 */
@Component
public class MechWarriorTechLevelAdapter implements TechLevelAdapter {

    private static final Map<String, String> LORE_NAMES = new HashMap<>();
    private static final Map<String, Map<String, Object>> LORE_STATS = new HashMap<>();

    static {
        // ==========================================
        // TL7: Interplanetary — Early Mech Era
        // ==========================================

        // VEHICLE: BattleMechs
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "LIGHT_MECH", "Light BattleMech (20-35 tons)",
                Map.of("tonnage", 30, "speed", 65, "armor", 60, "structure", 30,
                        "hardpoints", 3, "heatSinks", 6, "class", "LIGHT"));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MEDIUM_MECH", "Medium BattleMech (40-55 tons)",
                Map.of("tonnage", 50, "speed", 72, "armor", 100, "structure", 50,
                        "hardpoints", 5, "heatSinks", 10, "class", "MEDIUM"));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "MECH_WALKER", "Mech Walker (30 tons)",
                Map.of("tonnage", 30, "speed", 60, "armor", 50, "structure", 25,
                        "hardpoints", 2, "heatSinks", 6, "class", "LIGHT"));

        // VEHICLE: Conventional vehicles
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "IFV", "Infantry Fighting Vehicle",
                Map.of("speed", 80, "armor", 40, "capacity", 10, "cannonDamage", "2d6"));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "TANK_TL7", "Main Battle Tank",
                Map.of("speed", 70, "armor", 80, "cannonDamage", "4d6", "crew", 4));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "TRANSPORT_HELI", "Transport Helicopter",
                Map.of("speed", 150, "capacity", 12, "armor", 20, "flightAltitude", 3000));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "FIGHTER_JET", "OmniFighter",
                Map.of("speed", 2400, "armor", 30, "missileDamage", "3d6", "range", 1500));
        putLore(TechLevel.TL7, ModuleCategory.VEHICLE, "CARGO_DROPSHIP", "Cargo DropShip",
                Map.of("speed", 800, "capacity", 500, "armor", 200, "crew", 20));

        // WEAPON: Mech weapons
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "SMALL_LASER", "Small Laser",
                Map.of("damage", 2, "heat", 1, "range", "6/12/18", "type", "ENERGY", "tons", 1));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "MEDIUM_LASER", "Medium Laser",
                Map.of("damage", 4, "heat", 2, "range", "6/12/18", "type", "ENERGY", "tons", 2));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "LARGE_LASER", "Large Laser",
                Map.of("damage", 7, "heat", 4, "range", "6/14/22", "type", "ENERGY", "tons", 4));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "SRM_2", "SRM-2",
                Map.of("damage", 1, "heat", 1, "range", "2/5/9", "type", "MISSILE", "tons", 2, "ammoTons", 1));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "SRM_4", "SRM-4",
                Map.of("damage", 2, "heat", 2, "range", "2/5/9", "type", "MISSILE", "tons", 3, "ammoTons", 1));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "LRM_5", "LRM-5",
                Map.of("damage", 2, "heat", 2, "range", "6/12/18", "type", "MISSILE", "tons", 4, "ammoTons", 2));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "AC_2", "AC/2",
                Map.of("damage", 2, "heat", 2, "range", "6/12/18", "type", "BALLISTIC", "tons", 4, "ammoTons", 2));
        putLore(TechLevel.TL7, ModuleCategory.WEAPON, "AC_5", "AC/5",
                Map.of("damage", 5, "heat", 3, "range", "6/14/22", "type", "BALLISTIC", "tons", 6, "ammoTons", 3));

        // SETTLEMENT: Mech-era facilities
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "MECH_BAY", "Mech Bay",
                Map.of("hp", 300, "capacity", 4, "liftCapacity", 75, "repairRate", 2));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "MECH_FACTORY", "Mech Factory",
                Map.of("hp", 500, "output", 2, "buildTime", 180, "crewRequired", 50));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "OMNIMECH_FACTORY", "OmniMech Factory",
                Map.of("hp", 800, "output", 1, "buildTime", 365, "crewRequired", 100));
        putLore(TechLevel.TL7, ModuleCategory.SETTLEMENT, "SPACEPORT", "Spaceport",
                Map.of("hp", 400, "capacity", 1000, "dropshipSlots", 4));

        // INDUSTRY: Mech-era industry
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "FUSION_REACTOR", "Fusion Reactor Plant",
                Map.of("output", 200, "power", 500, "crewRequired", 20));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "MECH_PARTS_FACTORY", "Mech Parts Factory",
                Map.of("output", 50, "partsPerMonth", 10, "crewRequired", 30));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "WEAPON_FACTORY", "Weapon Systems Factory",
                Map.of("output", 40, "weaponsPerMonth", 5, "crewRequired", 25));
        putLore(TechLevel.TL7, ModuleCategory.INDUSTRY, "ARMOR_PLATE_MILL", "Armor Plate Mill",
                Map.of("output", 60, "tonsPerMonth", 100, "crewRequired", 15));

        // DEFENSE: Mech-era defenses
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "PLANETARY_DEFENSE_BATTERY", "Planetary Defense Battery",
                Map.of("hp", 200, "defense", 8, "missileDamage", "6d6", "range", 500));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "MECH_GARRISON", "Mech Garrison",
                Map.of("hp", 150, "garrisonSize", 6, "mechSlots", 6));
        putLore(TechLevel.TL7, ModuleCategory.DEFENSE, "ANTI_AIR_BATTERY", "Anti-Air Battery",
                Map.of("hp", 100, "defense", 5, "antiAirBonus", 3, "range", 100));

        // INFRASTRUCTURE
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "FUSION_GRID", "Fusion Power Grid",
                Map.of("powerOutput", 500, "coverageRadius", 50));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "COMMS_RELAY", "Communications Relay",
                Map.of("range", 1000, "encryptionLevel", 3));
        putLore(TechLevel.TL7, ModuleCategory.INFRASTRUCTURE, "ORBITAL_ELEVATOR", "Orbital Elevator",
                Map.of("hp", 1000, "cargoPerDay", 500, "crewRequired", 100));

        // COMMUNICATION
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "COMPUTER_NETWORK", "Computer Network",
                Map.of("range", 100, "bandwidth", "High", "encryptionLevel", 3));
        putLore(TechLevel.TL7, ModuleCategory.COMMUNICATION, "ORBITAL_COMMS", "Orbital Communications Array",
                Map.of("range", 50000, "bandwidth", "Very High", "encryptionLevel", 5));

        // ==========================================
        // TL8: Interstellar — Full Mech Era
        // ==========================================

        // VEHICLE: Advanced BattleMechs
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "HEAVY_MECH", "Heavy BattleMech (60-75 tons)",
                Map.of("tonnage", 70, "speed", 65, "armor", 140, "structure", 70,
                        "hardpoints", 7, "heatSinks", 14, "class", "HEAVY"));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "ASSAULT_MECH", "Assault BattleMech (80-100 tons)",
                Map.of("tonnage", 100, "speed", 56, "armor", 200, "structure", 100,
                        "hardpoints", 10, "heatSinks", 20, "class", "ASSAULT"));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "OMNIMECH", "OmniMech",
                Map.of("tonnage", 65, "speed", 72, "armor", 130, "structure", 65,
                        "hardpoints", 8, "heatSinks", 13, "class", "HEAVY", "configurable", true));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "MECH_WALKER_TL8", "Heavy Walker (50 tons)",
                Map.of("tonnage", 50, "speed", 55, "armor", 80, "structure", 45,
                        "hardpoints", 4, "heatSinks", 10, "class", "MEDIUM"));

        // VEHICLE: Advanced conventional
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "OMNI_TANK", "OmniTank",
                Map.of("speed", 90, "armor", 100, "cannonDamage", "6d6", "crew", 3, "configurable", true));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "COMBAT_HELI", "Combat Helicopter",
                Map.of("speed", 200, "armor", 30, "missileDamage", "2d6", "flightAltitude", 5000));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "WASP_FIGHTER", "Wasp Fighter",
                Map.of("speed", 3000, "armor", 40, "missileDamage", "4d6", "range", 2000));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "ASSAULT_DROPShip", "Assault DropShip",
                Map.of("speed", 600, "capacity", 200, "armor", 300, "crew", 30, "bays", 2));
        putLore(TechLevel.TL8, ModuleCategory.VEHICLE, "JUMPSHIP", "JumpShip",
                Map.of("speed", 0, "jumpRange", 1, "capacity", 5000, "crew", 50, "orbitOnly", true));

        // WEAPON: Advanced mech weapons
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "ER_LARGE_LASER", "ER Large Laser",
                Map.of("damage", 8, "heat", 5, "range", "6/16/26", "type", "ENERGY", "tons", 5));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "PPC", "PPC",
                Map.of("damage", 9, "heat", 6, "range", "6/14/22", "type", "ENERGY", "tons", 7));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "ER_PPC", "ER PPC",
                Map.of("damage", 10, "heat", 7, "range", "6/16/26", "type", "ENERGY", "tons", 8));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "LRM_10", "LRM-10",
                Map.of("damage", 4, "heat", 3, "range", "6/14/22", "type", "MISSILE", "tons", 6, "ammoTons", 3));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "LRM_15", "LRM-15",
                Map.of("damage", 6, "heat", 4, "range", "6/14/22", "type", "MISSILE", "tons", 8, "ammoTons", 4));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "AC_10", "AC/10",
                Map.of("damage", 10, "heat", 4, "range", "6/14/22", "type", "BALLISTIC", "tons", 10, "ammoTons", 5));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "LBX_10", "LBX-10",
                Map.of("damage", 10, "heat", 4, "range", "6/14/22", "type", "BALLISTIC", "tons", 10, "ammoTons", 5,
                        "clusterDamage", 15));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "GAUSS_RIFLE", "Gauss Rifle",
                Map.of("damage", 25, "heat", 10, "range", "6/18/30", "type", "BALLISTIC", "tons", 12, "ammoTons", 6));
        putLore(TechLevel.TL8, ModuleCategory.WEAPON, "FLAME_THROWER", "Flame Thrower",
                Map.of("damage", 3, "heat", 2, "range", "1/2/3", "type", "ENERGY", "tons", 3));

        // SETTLEMENT: Advanced facilities
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "MECH_YARD", "Mech Yard",
                Map.of("hp", 600, "capacity", 8, "liftCapacity", 100, "repairRate", 4));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "CAPITAL_CITY", "Capital City",
                Map.of("hp", 2000, "capacity", 100000, "tradeBonus", 10));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "MILITARY_BASE", "Military Base",
                Map.of("hp", 1000, "garrisonSize", 50, "mechSlots", 12));
        putLore(TechLevel.TL8, ModuleCategory.SETTLEMENT, "STARPORT", "Starport",
                Map.of("hp", 800, "capacity", 5000, "dropshipSlots", 8, "jumpshipSlots", 2));

        // INDUSTRY: Advanced industry
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "OMNI_FACTORY", "OmniFactory",
                Map.of("output", 10, "buildTime", 90, "crewRequired", 200, "configurable", true));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "FUSION_ENGINE_PLANT", "Fusion Engine Plant",
                Map.of("output", 30, "enginesPerMonth", 2, "crewRequired", 50));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "JUMP_DRIVE_FACTORY", "Jump Drive Factory",
                Map.of("output", 5, "drivesPerMonth", 1, "crewRequired", 100));
        putLore(TechLevel.TL8, ModuleCategory.INDUSTRY, "HEAT_SINK_FACTORY", "Heat Sink Factory",
                Map.of("output", 80, "unitsPerMonth", 50, "crewRequired", 20));

        // DEFENSE: Advanced defenses
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "ORBITAL_DEFENSE_GRID", "Orbital Defense Grid",
                Map.of("hp", 500, "defense", 12, "missileDamage", "10d6", "range", 1000));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "MECH_FORTRESS", "Mech Fortress",
                Map.of("hp", 800, "garrisonSize", 12, "mechSlots", 12, "repairRate", 2));
        putLore(TechLevel.TL8, ModuleCategory.DEFENSE, "POINT_DEFENSE", "Point Defense Network",
                Map.of("hp", 200, "defense", 6, "antiAirBonus", 5, "range", 50));

        // INFRASTRUCTURE
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "FUSION_CITY_GRID", "Fusion City Grid",
                Map.of("powerOutput", 2000, "coverageRadius", 200));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "MAGLEV_NETWORK", "Maglev Network",
                Map.of("speed", 500, "coverageRadius", 1000));
        putLore(TechLevel.TL8, ModuleCategory.INFRASTRUCTURE, "ORBITAL_YARD", "Orbital Shipyard",
                Map.of("hp", 2000, "buildCapacity", 1, "crewRequired", 500));

        // COMMUNICATION
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "PLANETARY_NET", "Planetary Network",
                Map.of("range", 10000, "bandwidth", "Ultra High", "encryptionLevel", 5));
        putLore(TechLevel.TL8, ModuleCategory.COMMUNICATION, "COMSTAR_RELAY", "ComStar Relay Station",
                Map.of("range", 50000, "bandwidth", "Ultra High", "encryptionLevel", 7));

        // ==========================================
        // TL9: Galactic — Clan Era / Advanced Tech
        // ==========================================

        // VEHICLE: Clan-era BattleMechs
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "CLAN_LIGHT_MECH", "Clan Light OmniMech",
                Map.of("tonnage", 35, "speed", 81, "armor", 70, "structure", 35,
                        "hardpoints", 5, "heatSinks", 7, "class", "LIGHT", "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "CLAN_HEAVY_MECH", "Clan Heavy OmniMech",
                Map.of("tonnage", 75, "speed", 72, "armor", 150, "structure", 75,
                        "hardpoints", 10, "heatSinks", 15, "class", "HEAVY", "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "CLAN_ASSAULT_MECH", "Clan Assault OmniMech",
                Map.of("tonnage", 100, "speed", 65, "armor", 220, "structure", 100,
                        "hardpoints", 12, "heatSinks", 20, "class", "ASSAULT", "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "EXPERIMENTAL_MECH", "Experimental BattleMech",
                Map.of("tonnage", 100, "speed", 110, "armor", 200, "structure", 100,
                        "hardpoints", 14, "heatSinks", 25, "class", "ASSAULT", "experimental", true));

        // VEHICLE: Clan-era conventional
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "CLAN_OMNI_TANK", "Clan OmniTank",
                Map.of("speed", 110, "armor", 120, "cannonDamage", "8d6", "crew", 2, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "STEALTH_FIGHTER", "Stealth Fighter",
                Map.of("speed", 3500, "armor", 50, "missileDamage", "5d6", "range", 3000, "stealth", true));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "WARSHIP", "WarShip (Corvette)",
                Map.of("speed", 400, "armor", 500, "missileDamage", "20d6", "crew", 100, "orbitOnly", true));
        putLore(TechLevel.TL9, ModuleCategory.VEHICLE, "CLAN_JUMPShip", "Clan JumpShip",
                Map.of("speed", 0, "jumpRange", 2, "capacity", 8000, "crew", 30, "clanTech", true));

        // WEAPON: Clan-era weapons
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "MEDIUM_PULSE_LASER", "Medium Pulse Laser",
                Map.of("damage", 12, "heat", 6, "range", "6/12/18", "type", "ENERGY", "tons", 3, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "LARGE_PULSE_LASER", "Large Pulse Laser",
                Map.of("damage", 21, "heat", 10, "range", "6/14/22", "type", "ENERGY", "tons", 6, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "EXTENDED_PULSE_LASER", "Extended Pulse Laser",
                Map.of("damage", 15, "heat", 8, "range", "6/14/22", "type", "ENERGY", "tons", 4, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "STREAK_SRM_2", "Streak SRM-2",
                Map.of("damage", 1, "heat", 1, "range", "2/5/9", "type", "MISSILE", "tons", 2, "ammoTons", 1,
                        "streak", true, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "STREAK_SRM_4", "Streak SRM-4",
                Map.of("damage", 2, "heat", 2, "range", "2/5/9", "type", "MISSILE", "tons", 3, "ammoTons", 1,
                        "streak", true, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "STREAK_LRM_10", "Streak LRM-10",
                Map.of("damage", 4, "heat", 3, "range", "6/14/22", "type", "MISSILE", "tons", 6, "ammoTons", 3,
                        "streak", true, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "UAC_20", "UAC/20",
                Map.of("damage", 20, "heat", 6, "range", "6/16/26", "type", "BALLISTIC", "tons", 16, "ammoTons", 8,
                        "ultraAuto", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "ROTARY_AC", "Rotary AC",
                Map.of("damage", 1, "heat", 1, "range", "6/12/18", "type", "BALLISTIC", "tons", 8, "ammoTons", 4,
                        "critSeeking", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "AMS", "Anti-Missile System",
                Map.of("damage", 1, "heat", 1, "range", "2/4/6", "type", "BALLISTIC", "tons", 4, "ammoTons", 2,
                        "antiMissile", true));
        putLore(TechLevel.TL9, ModuleCategory.WEAPON, "ECM", "ECM Suite",
                Map.of("type", "ELECTRONIC", "tons", 3, "jammingRange", 9, "clanTech", true));

        // SETTLEMENT: Clan-era facilities
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "CLAN_MECH_YARD", "Clan Mech Yard",
                Map.of("hp", 1000, "capacity", 12, "liftCapacity", 100, "repairRate", 6, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "WARSHIP_YARD", "WarShip Yard",
                Map.of("hp", 3000, "buildTime", 730, "crewRequired", 1000, "orbitOnly", true));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "CLAN_CASTRUM", "Clan Castrum",
                Map.of("hp", 2000, "capacity", 5000, "mechSlots", 24, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.SETTLEMENT, "RESEARCH_FACILITY", "Advanced Research Facility",
                Map.of("hp", 500, "researchRate", 5, "crewRequired", 50));

        // INDUSTRY: Clan-era industry
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "CLAN_OMNI_FACTORY", "Clan OmniFactory",
                Map.of("output", 15, "buildTime", 60, "crewRequired", 150, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "NANOBOT_FACTORY", "Nanobot Factory",
                Map.of("output", 20, "repairRate", 10, "crewRequired", 30, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "FERRINITE_REFINERY", "Ferrite Refinery",
                Map.of("output", 100, "tonsPerMonth", 200, "crewRequired", 20, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.INDUSTRY, "CASE_I_PLANT", "CASE/I Plant",
                Map.of("output", 30, "unitsPerMonth", 5, "crewRequired", 15, "clanTech", true));

        // DEFENSE: Clan-era defenses
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "CLAN_DEFENSE_GRID", "Clan Defense Grid",
                Map.of("hp", 800, "defense", 15, "missileDamage", "15d6", "range", 1500, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "WARSHIP_DEFENSE", "WarShip Defense Battery",
                Map.of("hp", 1000, "defense", 20, "missileDamage", "25d6", "range", 5000, "orbitOnly", true));
        putLore(TechLevel.TL9, ModuleCategory.DEFENSE, "STEALTH_FIELD", "Stealth Field Generator",
                Map.of("hp", 300, "radius", 10, "clanTech", true));

        // INFRASTRUCTURE
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "CLAN_POWER_GRID", "Clan Power Grid",
                Map.of("powerOutput", 5000, "coverageRadius", 500, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.INFRASTRUCTURE, "DEEP_SPACE_YARD", "Deep Space Shipyard",
                Map.of("hp", 5000, "buildCapacity", 2, "crewRequired", 1000, "orbitOnly", true));

        // COMMUNICATION
        putLore(TechLevel.TL9, ModuleCategory.COMMUNICATION, "CLAN_COMMS_NET", "Clan Communications Network",
                Map.of("range", 100000, "bandwidth", "Maximum", "encryptionLevel", 10, "clanTech", true));
        putLore(TechLevel.TL9, ModuleCategory.COMMUNICATION, "FEARLESS_NETWORK", "Fearless Network Node",
                Map.of("range", 100000, "bandwidth", "Maximum", "encryptionLevel", 10, "clanTech", true));
    }

    private static void putLore(TechLevel tl, ModuleCategory cat, String genericType,
                                String loreName, Map<String, Object> stats) {
        String key = key(tl, cat, genericType);
        LORE_NAMES.put(key, loreName);
        LORE_STATS.put(key, stats);
    }

    private static String key(TechLevel tl, ModuleCategory cat, String genericType) {
        return tl.name() + "|" + cat.name() + "|" + genericType;
    }

    @Override
    public String getLoreName(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel)) return null;
        return LORE_NAMES.getOrDefault(key(techLevel, category, genericType), null);
    }

    @Override
    public Map<String, Object> getLoreStats(TechLevel techLevel, ModuleCategory category, String genericType) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyMap();
        return LORE_STATS.getOrDefault(key(techLevel, category, genericType), Collections.emptyMap());
    }

    @Override
    public TechLevel getMaxTechLevel() {
        return TechLevel.TL9;
    }

    @Override
    public TechLevel getMinTechLevel() {
        return TechLevel.TL7;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String key : LORE_NAMES.keySet()) {
            if (key.startsWith(techLevel.name() + "|")) {
                String[] parts = key.split("\\|");
                if (parts.length >= 3) {
                    result.add(parts[2]);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getAvailableModules(TechLevel techLevel, ModuleCategory category) {
        if (!supportsTechLevel(techLevel)) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        String prefix = techLevel.name() + "|" + category.name() + "|";
        for (String key : LORE_NAMES.keySet()) {
            if (key.startsWith(prefix)) {
                result.add(key.substring(prefix.length()));
            }
        }
        return result;
    }
}
