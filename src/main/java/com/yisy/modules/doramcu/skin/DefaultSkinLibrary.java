package com.yisy.modules.doramcu.skin;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.Decal;
import com.yisy.modules.doramcu.hex.DamageTier;
import com.yisy.modules.doramcu.hex.DamageOverlay;
import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.hex.DecalType;
import com.yisy.modules.doramcu.hex.DecalPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * Data initializer that provides a default library of pre-built skins.
 * <p>
 * Call {@link #initialize(SkinService)} to populate a {@link SkinService}
 * with default skins for ships, space stations, ground vehicles, and buildings,
 * as well as faction skin themes.
 * </p>
 */
public class DefaultSkinLibrary {

    /**
     * Initializes the given skin service with default skins and faction themes.
     *
     * @param service the skin service to populate
     */
    public static void initialize(SkinService service) {
        // ---- Ship skins ----
        service.registerSkin(createSciFiShipSkin());
        service.registerSkin(createMilitaryShipSkin());
        service.registerSkin(createCivilianShipSkin());

        // ---- Space station skins ----
        service.registerSkin(createIndustrialStationSkin());
        service.registerSkin(createMilitaryStationSkin());

        // ---- Ground vehicle skins ----
        service.registerSkin(createTankSkin());
        service.registerSkin(createApvSkin());
        service.registerSkin(createCivilianTruckSkin());

        // ---- Building skins ----
        service.registerSkin(createModernBuildingSkin());
        service.registerSkin(createIndustrialBuildingSkin());
        service.registerSkin(createMilitaryBunkerSkin());

        // ---- Faction themes ----
        service.registerFactionTheme(createGenericFederationTheme());
        service.registerFactionTheme(createGenericEmpireTheme());
        service.registerFactionTheme(createCivilianCorpTheme());

        // ---- Skin Variants ----
        // Military ship variants
        service.registerVariant(createDesertCamoVariant());
        service.registerVariant(createArcticCamoVariant());
        service.registerVariant(createForestCamoVariant());
        service.registerVariant(createUrbanGreyVariant());

        // Tank vehicle variants
        service.registerVariant(createDesertTankVariant());
        service.registerVariant(createArcticTankVariant());
        service.registerVariant(createForestTankVariant());

        // Building bunker variants
        service.registerVariant(createDesertBunkerVariant());
        service.registerVariant(createForestBunkerVariant());
    }

    // ---- Ship skins ----

    private static Skin createSciFiShipSkin() {
        Skin skin = new Skin("ship_scifi_generic", SkinType.SHIP);
        skin.setName("Generic Sci-Fi Ship");
        skin.setDescription("White/grey hull with blue accent lights");
        skin.setPrimaryColor("#E8E8E8");
        skin.setSecondaryColor("#A0A0A0");
        skin.setAccentColor("#4080FF");
        skin.setTextureReference("textures/ships/scifi_generic.png");

        Decal registryDecal = new Decal("registry", DecalType.TEXT);
        registryDecal.setContent("NCC-XXXX");
        registryDecal.setPosition(DecalPosition.HULL_STARBOARD);
        registryDecal.setColor("#4080FF");
        skin.addDefaultDecal(registryDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "No visible damage");
        overlay.setTierDescription(DamageTier.LIGHT, "Light scorch marks on surface");
        overlay.setTierDescription(DamageTier.MODERATE, "Dented plating with exposed wiring");
        overlay.setTierDescription(DamageTier.HEAVY, "Hull breach with structural deformation");
        overlay.setTierDescription(DamageTier.CRITICAL, "Catastrophic hull fragmentation");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createMilitaryShipSkin() {
        Skin skin = new Skin("ship_military", SkinType.SHIP);
        skin.setName("Military Warship");
        skin.setDescription("Olive drab hull with unit number decals");
        skin.setPrimaryColor("#4A5D23");
        skin.setSecondaryColor("#3B4A1E");
        skin.setAccentColor("#FFD700");
        skin.setTextureReference("textures/ships/military.png");

        Decal unitDecal = new Decal("unit_number", DecalType.TEXT);
        unitDecal.setContent("UNIT-001");
        unitDecal.setPosition(DecalPosition.NOSE);
        unitDecal.setColor("#FFD700");
        skin.addDefaultDecal(unitDecal);

        Decal emblemDecal = new Decal("fleet_emblem", DecalType.FACTION_EMBLEM);
        emblemDecal.setContent("fleet_insignia");
        emblemDecal.setPosition(DecalPosition.DORSAL);
        skin.addDefaultDecal(emblemDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Clean military finish");
        overlay.setTierDescription(DamageTier.LIGHT, "Battle scratches and paint chips");
        overlay.setTierDescription(DamageTier.MODERATE, "Armor plating dented and scorched");
        overlay.setTierDescription(DamageTier.HEAVY, "Armor breach with smoke venting");
        overlay.setTierDescription(DamageTier.CRITICAL, "Hull rupture with fire visible");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createCivilianShipSkin() {
        Skin skin = new Skin("ship_civilian", SkinType.SHIP);
        skin.setName("Civilian Transport");
        skin.setDescription("White hull with company logo decal");
        skin.setPrimaryColor("#FFFFFF");
        skin.setSecondaryColor("#C0C0C0");
        skin.setAccentColor("#FF6600");
        skin.setTextureReference("textures/ships/civilian.png");

        Decal logoDecal = new Decal("company_logo", DecalType.SYMBOL);
        logoDecal.setContent("company_logo");
        logoDecal.setPosition(DecalPosition.HULL_PORT);
        skin.addDefaultDecal(logoDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Pristine civilian finish");
        overlay.setTierDescription(DamageTier.LIGHT, "Minor surface scratches");
        overlay.setTierDescription(DamageTier.MODERATE, "Visible hull dents");
        overlay.setTierDescription(DamageTier.HEAVY, "Structural damage with leaks");
        overlay.setTierDescription(DamageTier.CRITICAL, "Hull breach imminent");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    // ---- Space station skins ----

    private static Skin createIndustrialStationSkin() {
        Skin skin = new Skin("station_industrial", SkinType.SPACE_STATION);
        skin.setName("Industrial Space Station");
        skin.setDescription("Industrial grey with faction ring colors");
        skin.setPrimaryColor("#707070");
        skin.setSecondaryColor("#505050");
        skin.setAccentColor("#00FF00");
        skin.setTextureReference("textures/stations/industrial.png");

        Decal idDecal = new Decal("station_id", DecalType.TEXT);
        idDecal.setContent("STN-001");
        idDecal.setPosition(DecalPosition.CORE);
        idDecal.setColor("#00FF00");
        skin.addDefaultDecal(idDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Fully operational");
        overlay.setTierDescription(DamageTier.LIGHT, "Minor surface wear");
        overlay.setTierDescription(DamageTier.MODERATE, "Panel damage and leaks");
        overlay.setTierDescription(DamageTier.HEAVY, "Section breach detected");
        overlay.setTierDescription(DamageTier.CRITICAL, "Catastrophic structural failure");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createMilitaryStationSkin() {
        Skin skin = new Skin("station_military", SkinType.SPACE_STATION);
        skin.setName("Military Orbital Platform");
        skin.setDescription("Dark grey with red warning lights");
        skin.setPrimaryColor("#404040");
        skin.setSecondaryColor("#303030");
        skin.setAccentColor("#FF0000");
        skin.setTextureReference("textures/stations/military.png");

        Decal emblemDecal = new Decal("defense_emblem", DecalType.FACTION_EMBLEM);
        emblemDecal.setContent("defense_force");
        emblemDecal.setPosition(DecalPosition.RING_OUTER);
        skin.addDefaultDecal(emblemDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Combat ready");
        overlay.setTierDescription(DamageTier.LIGHT, "Light battle damage");
        overlay.setTierDescription(DamageTier.MODERATE, "Armor degradation");
        overlay.setTierDescription(DamageTier.HEAVY, "Critical systems exposed");
        overlay.setTierDescription(DamageTier.CRITICAL, "Station collapse imminent");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    // ---- Ground vehicle skins ----

    private static Skin createTankSkin() {
        Skin skin = new Skin("vehicle_tank", SkinType.GROUND_VEHICLE);
        skin.setName("Main Battle Tank");
        skin.setDescription("Digital camouflage with unit markings");
        skin.setPrimaryColor("#556B2F");
        skin.setSecondaryColor("#3B4A1E");
        skin.setAccentColor("#FFD700");
        skin.setTextureReference("textures/vehicles/tank.png");

        Decal unitDecal = new Decal("tank_unit", DecalType.TEXT);
        unitDecal.setContent("A-1");
        unitDecal.setPosition(DecalPosition.TURRET);
        unitDecal.setColor("#FFFFFF");
        skin.addDefaultDecal(unitDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Full combat readiness");
        overlay.setTierDescription(DamageTier.LIGHT, "Spalling and paint damage");
        overlay.setTierDescription(DamageTier.MODERATE, "Armor penetration attempts");
        overlay.setTierDescription(DamageTier.HEAVY, "Turret damage, smoke visible");
        overlay.setTierDescription(DamageTier.CRITICAL, "Catastrophic ammo explosion");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createApvSkin() {
        Skin skin = new Skin("vehicle_apc", SkinType.GROUND_VEHICLE);
        skin.setName("Armored Personnel Carrier");
        skin.setDescription("Desert tan with unit insignia");
        skin.setPrimaryColor("#C2B280");
        skin.setSecondaryColor("#A0925A");
        skin.setAccentColor("#0000FF");
        skin.setTextureReference("textures/vehicles/apc.png");

        Decal insigniaDecal = new Decal("apc_insignia", DecalType.SYMBOL);
        insigniaDecal.setContent("unit_patch");
        insigniaDecal.setPosition(DecalPosition.HULL_FRONT);
        skin.addDefaultDecal(insigniaDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Operational");
        overlay.setTierDescription(DamageTier.LIGHT, "Light abrasions");
        overlay.setTierDescription(DamageTier.MODERATE, "Armor dents");
        overlay.setTierDescription(DamageTier.HEAVY, "Hull breach");
        overlay.setTierDescription(DamageTier.CRITICAL, "Vehicle destroyed");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createCivilianTruckSkin() {
        Skin skin = new Skin("vehicle_truck", SkinType.GROUND_VEHICLE);
        skin.setName("Civilian Truck");
        skin.setDescription("White delivery truck with company markings");
        skin.setPrimaryColor("#FFFFFF");
        skin.setSecondaryColor("#D0D0D0");
        skin.setAccentColor("#0066CC");
        skin.setTextureReference("textures/vehicles/truck.png");

        Decal logoDecal = new Decal("truck_logo", DecalType.SYMBOL);
        logoDecal.setContent("delivery_logo");
        logoDecal.setPosition(DecalPosition.HULL_LEFT);
        skin.addDefaultDecal(logoDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Clean condition");
        overlay.setTierDescription(DamageTier.LIGHT, "Road wear and scratches");
        overlay.setTierDescription(DamageTier.MODERATE, "Dents and paint damage");
        overlay.setTierDescription(DamageTier.HEAVY, "Structural frame damage");
        overlay.setTierDescription(DamageTier.CRITICAL, "Total loss");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    // ---- Building skins ----

    private static Skin createModernBuildingSkin() {
        Skin skin = new Skin("building_modern", SkinType.BUILDING);
        skin.setName("Modern Office Building");
        skin.setDescription("Glass and steel facade");
        skin.setPrimaryColor("#B0C4DE");
        skin.setSecondaryColor("#708090");
        skin.setAccentColor("#4169E1");
        skin.setTextureReference("textures/buildings/modern.png");

        Decal signDecal = new Decal("building_sign", DecalType.TEXT);
        signDecal.setContent("CORP HQ");
        signDecal.setPosition(DecalPosition.FACADE_FRONT);
        signDecal.setColor("#4169E1");
        skin.addDefaultDecal(signDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "No damage");
        overlay.setTierDescription(DamageTier.LIGHT, "Cracked windows");
        overlay.setTierDescription(DamageTier.MODERATE, "Facade damage");
        overlay.setTierDescription(DamageTier.HEAVY, "Structural cracks");
        overlay.setTierDescription(DamageTier.CRITICAL, "Partial collapse");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createIndustrialBuildingSkin() {
        Skin skin = new Skin("building_industrial", SkinType.BUILDING);
        skin.setName("Industrial Warehouse");
        skin.setDescription("Corrugated metal with warning stripes");
        skin.setPrimaryColor("#808080");
        skin.setSecondaryColor("#606060");
        skin.setAccentColor("#FFD700");
        skin.setTextureReference("textures/buildings/industrial.png");

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Fully operational");
        overlay.setTierDescription(DamageTier.LIGHT, "Surface rust");
        overlay.setTierDescription(DamageTier.MODERATE, "Panel deformation");
        overlay.setTierDescription(DamageTier.HEAVY, "Roof breach");
        overlay.setTierDescription(DamageTier.CRITICAL, "Structural collapse");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    private static Skin createMilitaryBunkerSkin() {
        Skin skin = new Skin("building_bunker", SkinType.BUILDING);
        skin.setName("Military Bunker");
        skin.setDescription("Reinforced concrete with camouflage");
        skin.setPrimaryColor("#6B6B4A");
        skin.setSecondaryColor("#4A4A3A");
        skin.setAccentColor("#FF0000");
        skin.setTextureReference("textures/buildings/bunker.png");

        Decal warningDecal = new Decal("bunker_warning", DecalType.TEXT);
        warningDecal.setContent("RESTRICTED");
        warningDecal.setPosition(DecalPosition.ENTRANCE);
        warningDecal.setColor("#FF0000");
        skin.addDefaultDecal(warningDecal);

        DamageOverlay overlay = new DamageOverlay();
        overlay.setTierDescription(DamageTier.PRISTINE, "Fully fortified");
        overlay.setTierDescription(DamageTier.LIGHT, "Surface scarring");
        overlay.setTierDescription(DamageTier.MODERATE, "Concrete spalling");
        overlay.setTierDescription(DamageTier.HEAVY, "Breach in outer wall");
        overlay.setTierDescription(DamageTier.CRITICAL, "Bunker compromised");
        skin.setDamageOverlay(overlay);

        return skin;
    }

    // ---- Faction themes ----

    private static FactionSkinTheme createGenericFederationTheme() {
        FactionSkinTheme theme = new FactionSkinTheme("federation");
        theme.setName("Generic Federation");
        theme.setDefaultSkinId("ship_scifi_generic");
        theme.setPrimaryColor("#E8E8E8");
        theme.setSecondaryColor("#A0A0A0");
        theme.setAccentColor("#4080FF");
        theme.setEmblemSymbol("federation_insignia");
        return theme;
    }

    private static FactionSkinTheme createGenericEmpireTheme() {
        FactionSkinTheme theme = new FactionSkinTheme("empire");
        theme.setName("Generic Empire");
        theme.setDefaultSkinId("ship_military");
        theme.setPrimaryColor("#404040");
        theme.setSecondaryColor("#202020");
        theme.setAccentColor("#FF0000");
        theme.setEmblemSymbol("empire_crest");
        return theme;
    }

    private static FactionSkinTheme createCivilianCorpTheme() {
        FactionSkinTheme theme = new FactionSkinTheme("civilian_corp");
        theme.setName("Civilian Corporation");
        theme.setDefaultSkinId("ship_civilian");
        theme.setPrimaryColor("#FFFFFF");
        theme.setSecondaryColor("#C0C0C0");
        theme.setAccentColor("#FF6600");
        theme.setEmblemSymbol("corp_logo");
        return theme;
    }

    // ---- Skin Variants ----

    private static SkinVariant createDesertCamoVariant() {
        SkinVariant variant = new SkinVariant("ship_military_desert", "ship_military");
        variant.setName("Desert Camo");
        variant.setDescription("Sand-colored camouflage for desert operations");
        variant.setPrimaryColor("#C2B280");
        variant.setSecondaryColor("#A0925A");
        variant.setAccentColor("#8B7D3C");
        return variant;
    }

    private static SkinVariant createArcticCamoVariant() {
        SkinVariant variant = new SkinVariant("ship_military_arctic", "ship_military");
        variant.setName("Arctic Camo");
        variant.setDescription("White and light grey for polar operations");
        variant.setPrimaryColor("#F0F0F0");
        variant.setSecondaryColor("#D0D0D0");
        variant.setAccentColor("#A0A0A0");
        return variant;
    }

    private static SkinVariant createForestCamoVariant() {
        SkinVariant variant = new SkinVariant("ship_military_forest", "ship_military");
        variant.setName("Forest Camo");
        variant.setDescription("Dark green camouflage for jungle operations");
        variant.setPrimaryColor("#2F4F2F");
        variant.setSecondaryColor("#1A3A1A");
        variant.setAccentColor("#4A5D23");
        return variant;
    }

    private static SkinVariant createUrbanGreyVariant() {
        SkinVariant variant = new SkinVariant("ship_military_urban", "ship_military");
        variant.setName("Urban Grey");
        variant.setDescription("Flat grey for urban environments");
        variant.setPrimaryColor("#808080");
        variant.setSecondaryColor("#606060");
        variant.setAccentColor("#404040");
        return variant;
    }

    private static SkinVariant createDesertTankVariant() {
        SkinVariant variant = new SkinVariant("vehicle_tank_desert", "vehicle_tank");
        variant.setName("Desert Tank");
        variant.setDescription("Sand-colored tank for desert deployment");
        variant.setPrimaryColor("#C2B280");
        variant.setSecondaryColor("#A0925A");
        variant.setAccentColor("#FFD700");
        return variant;
    }

    private static SkinVariant createArcticTankVariant() {
        SkinVariant variant = new SkinVariant("vehicle_tank_arctic", "vehicle_tank");
        variant.setName("Arctic Tank");
        variant.setDescription("White tank for polar deployment");
        variant.setPrimaryColor("#F0F0F0");
        variant.setSecondaryColor("#D0D0D0");
        variant.setAccentColor("#FFD700");
        return variant;
    }

    private static SkinVariant createForestTankVariant() {
        SkinVariant variant = new SkinVariant("vehicle_tank_forest", "vehicle_tank");
        variant.setName("Forest Tank");
        variant.setDescription("Dark green tank for jungle deployment");
        variant.setPrimaryColor("#2F4F2F");
        variant.setSecondaryColor("#1A3A1A");
        variant.setAccentColor("#FFD700");
        return variant;
    }

    private static SkinVariant createDesertBunkerVariant() {
        SkinVariant variant = new SkinVariant("building_bunker_desert", "building_bunker");
        variant.setName("Desert Bunker");
        variant.setDescription("Sand-colored bunker for desert operations");
        variant.setPrimaryColor("#C2B280");
        variant.setSecondaryColor("#A0925A");
        variant.setAccentColor("#FF0000");
        return variant;
    }

    private static SkinVariant createForestBunkerVariant() {
        SkinVariant variant = new SkinVariant("building_bunker_forest", "building_bunker");
        variant.setName("Forest Bunker");
        variant.setDescription("Green-camouflaged bunker for jungle operations");
        variant.setPrimaryColor("#2F4F2F");
        variant.setSecondaryColor("#1A3A1A");
        variant.setAccentColor("#FF0000");
        return variant;
    }
}
