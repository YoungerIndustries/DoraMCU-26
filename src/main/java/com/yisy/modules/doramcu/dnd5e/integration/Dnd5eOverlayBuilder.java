package com.yisy.modules.doramcu.dnd5e.integration;

import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMonsterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpeciesService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.gamecore.GameOverlayBuilder;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.service.LoreGroupEmpireService;
import com.yisy.modules.doramcu.service.LoreGroupEmpireService.LoreGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * D&D 5E overlay builder that creates persistent empires and AI characters
 * for the Forgotten Realms and other D&D settings.
 * <p>
 * Creates the following empire structures:
 * <ul>
 *   <li><b>Dragon Empires</b> — Tiamat rules the Chromatic Dragons, Bahamut rules the Metallic Dragons</li>
 *   <li><b>Planes of Hell</b> — Nine Hells (Asmodeus), Abyss (Demogorgon), Nine Hells lords</li>
 *   <li><b>Underdark</b> — Drow, Duergar, and other underground races</li>
 *   <li><b>Elemental Planes</b> — Fire, Water, Earth, Air elemental lords</li>
 *   <li><b>Species Empires</b> — Elven, Dwarven, Halfling, and other surface races</li>
 * </ul>
 * </p>
 * <p>
 * Each empire has AI character leaders saved to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class Dnd5eOverlayBuilder implements GameOverlayBuilder {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eOverlayBuilder.class);

    private final Dnd5eMonsterService monsterService;
    private final Dnd5eSpeciesService speciesService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public Dnd5eOverlayBuilder(Dnd5eMonsterService monsterService,
                               Dnd5eSpeciesService speciesService,
                               LoreGroupEmpireService loreGroupEmpireService) {
        this.monsterService = monsterService;
        this.speciesService = speciesService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.DND_5E;
    }

    @Override
    public void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config) {
        log.info("Building D&D 5E overlay for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();

        // Phase 1: Dragon Empires — Tiamat and Bahamut
        createDragonEmpires(galaxyName);

        // Phase 2: Planes of Hell and the Underworld
        createPlaneEmpires(galaxyName);

        // Phase 3: Underdark empires
        createUnderdarkEmpires(galaxyName);

        // Phase 4: Elemental Planes
        createElementalEmpires(galaxyName);

        // Phase 5: Species-based empires from database
        createSpeciesEmpires(galaxyName);

        log.info("D&D 5E overlay complete for galaxy: {}", galaxy.getName());
    }

    /**
     * Creates the two great dragon empires:
     * Chromatic Dragons led by Tiamat, and Metallic Dragons led by Bahamut.
     */
    private void createDragonEmpires(String galaxyName) {
        log.info("Creating D&D Dragon Empires for galaxy: {}", galaxyName);

        List<LoreGroup> dragonEmpires = new ArrayList<>();

        // Chromatic Dragon Empire — evil dragons
        dragonEmpires.add(new LoreGroup(
                "Chromatic Dragon Empire",
                "Dragon God",
                new String[]{"Tiamat"},
                "The five-headed goddess of evil dragons, ruling the chromatic dragonkind from her lair in the Abyss.",
                "Dragon Magic"
        ));

        // Metallic Dragon Empire — good dragons
        dragonEmpires.add(new LoreGroup(
                "Metallic Dragon Empire",
                "Dragon God",
                new String[]{"Bahamut"},
                "The silver dragon god of good dragons, justice, and metalworkers, ruling from the Celestial Planes.",
                "Dragon Magic"
        ));

        // Gem Dragon Empire — neutral dragons
        dragonEmpires.add(new LoreGroup(
                "Gem Dragon Empire",
                "Dragon Lord",
                new String[]{"Asmodeus"},
                "The gem dragons serve Asmodeus in the Nine Hells, acting as enforcers and diplomats.",
                "Diplomacy"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, dragonEmpires);

        // Add dragon NPC leaders
        List<LoreGroupEmpireService.NpcData> dragonNpcs = new ArrayList<>();
        dragonNpcs.add(new LoreGroupEmpireService.NpcData(
                "Crimson Scorch", "Chromatic Dragon Warchief", "Chromatic Dragon Empire",
                "Red dragon warchief serving Tiamat"
        ));
        dragonNpcs.add(new LoreGroupEmpireService.NpcData(
                "Verdant Breath", "Chromatic Dragon Warchief", "Chromatic Dragon Empire",
                "Green dragon warchief serving Tiamat"
        ));
        dragonNpcs.add(new LoreGroupEmpireService.NpcData(
                "Platinum Guardian", "Metallic Dragon Champion", "Metallic Dragon Empire",
                "Platinum dragon champion serving Bahamut"
        ));
        dragonNpcs.add(new LoreGroupEmpireService.NpcData(
                "Bronze Sentinel", "Metallic Dragon Champion", "Metallic Dragon Empire",
                "Bronze dragon sentinel serving Bahamut"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, dragonNpcs);
    }

    /**
     * Creates empires for the planes of hell, abyss, and the underworld.
     */
    private void createPlaneEmpires(String galaxyName) {
        log.info("Creating D&D Plane Empires for galaxy: {}", galaxyName);

        List<LoreGroup> planeEmpires = new ArrayList<>();

        // Nine Hells
        planeEmpires.add(new LoreGroup(
                "Nine Hells",
                "Archdevil",
                new String[]{"Asmodeus"},
                "The ruler of the Nine Hells, the most powerful devil and lord of the first layer, Avernus.",
                "Infernal Magic"
        ));

        // The Abyss
        planeEmpires.add(new LoreGroup(
                "The Abyss",
                "Demon Prince",
                new String[]{"Demogorgon"},
                "The two-headed demon prince of knowledge and hunting, ruling layers of the Abyss.",
                "Demonic Power"
        ));

        // The Nine Hells lords
        planeEmpires.add(new LoreGroup(
                "Nine Hells - Dis",
                "Archdevil",
                new String[]{"Mammon"},
                "Lord of Dis, the second layer of the Nine Hells, master of greed and wealth.",
                "Wealth"
        ));

        planeEmpires.add(new LoreGroup(
                "Nine Hells - Maladomini",
                "Archdevil",
                new String[]{"Mephistopheles"},
                "Lord of Maladomini, the third layer, master of deception and contracts.",
                "Deception"
        ));

        // The Underworld
        planeEmpires.add(new LoreGroup(
                "The Underworld",
                "Lord of the Dead",
                new String[]{"Kelemvor"},
                "The god of the dead, overseeing the passage of souls to the afterlife.",
                "Death Magic"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, planeEmpires);

        // Plane NPCs
        List<LoreGroupEmpireService.NpcData> planeNpcs = new ArrayList<>();
        planeNpcs.add(new LoreGroupEmpireService.NpcData(
                "Dispater", "Archdevil of Avernus", "Nine Hells",
                "Former ruler of Avernus, overthrown by Asmodeus"
        ));
        planeNpcs.add(new LoreGroupEmpireService.NpcData(
                "Glasya", "Devil Commander", "Nine Hells",
                "A powerful devil who serves in the Nine Hells"
        ));
        planeNpcs.add(new LoreGroupEmpireService.NpcData(
                "Yeenoghu", "Demon Prince", "The Abyss",
                "Demon prince of goblins and hobgoblins"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, planeNpcs);
    }

    /**
     * Creates empires for the Underdark races.
     */
    private void createUnderdarkEmpires(String galaxyName) {
        log.info("Creating D&D Underdark Empires for galaxy: {}", galaxyName);

        List<LoreGroup> underdarkEmpires = new ArrayList<>();

        // Drow Empire
        underdarkEmpires.add(new LoreGroup(
                "Drow Empire",
                "Matron Mother",
                new String[]{"Lolth"},
                "The goddess of spiders and darkness, ruling the drow from her web in the Underdark.",
                "Spider Magic"
        ));

        // Duergar Empire
        underdarkEmpires.add(new LoreGroup(
                "Duergar Empire",
                "Clan Lord",
                new String[]{"Kymil"},
                "The duergar (gray dwarves) rule the deep Underdark with discipline and industry.",
                "Mining"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, underdarkEmpires);

        // Underdark NPCs
        List<LoreGroupEmpireService.NpcData> underdarkNpcs = new ArrayList<>();
        underdarkNpcs.add(new LoreGroupEmpireService.NpcData(
                "Viconia DeVir", "Drow Sorceress", "Drow Empire",
                "A drow outcast sorceress"
        ));
        underdarkNpcs.add(new LoreGroupEmpireService.NpcData(
                "Drizzt Do'Urden", "Drow Ranger", "Drow Empire",
                "A good drow ranger who left the Underdark"
        ));
        underdarkNpcs.add(new LoreGroupEmpireService.NpcData(
                "Gruumsh", "Goblin God", "Duergar Empire",
                "The one-eyed god of goblins and orcs"
        ));
        loreGroupEmpireService.createNpcs(galaxyName, underdarkNpcs);
    }

    /**
     * Creates empires for the four elemental planes.
     */
    private void createElementalEmpires(String galaxyName) {
        log.info("Creating D&D Elemental Empires for galaxy: {}", galaxyName);

        List<LoreGroup> elementalEmpires = new ArrayList<>();

        // Fire Elemental Plane
        elementalEmpires.add(new LoreGroup(
                "Fire Elemental Plane",
                "Elemental Lord",
                new String[]{"Imix"},
                "The primordial fire giant god, ruler of the Elemental Plane of Fire.",
                "Fire Magic"
        ));

        // Water Elemental Plane
        elementalEmpires.add(new LoreGroup(
                "Water Elemental Plane",
                "Elemental Lord",
                new String[]{"Othea"},
                "The goddess of water and the sea, ruler of the Elemental Plane of Water.",
                "Water Magic"
        ));

        // Earth Elemental Plane
        elementalEmpires.add(new LoreGroup(
                "Earth Elemental Plane",
                "Elemental Lord",
                new String[]{"Moradin"},
                "The dwarven creator god, ruler of the Elemental Plane of Earth.",
                "Earth Magic"
        ));

        // Air Elemental Plane
        elementalEmpires.add(new LoreGroup(
                "Air Elemental Plane",
                "Elemental Lord",
                new String[]{"Aerandis"},
                "The elven goddess of air and freedom, ruler of the Elemental Plane of Air.",
                "Air Magic"
        ));

        loreGroupEmpireService.createEmpireLeaders(galaxyName, elementalEmpires);
    }

    /**
     * Creates empires based on D&D species from the rules database.
     */
    private void createSpeciesEmpires(String galaxyName) {
        log.info("Creating D&D Species Empires for galaxy: {}", galaxyName);

        List<?> allSpecies = speciesService.listSpecies();
        for (Object sp : allSpecies) {
            log.info("  D&D species empire: {} ({})", sp, galaxyName);
        }

        // Add monster-based lore
        List<?> monsters = monsterService.listMonsters();
        log.info("  D&D monsters available for lore: {}", monsters.size());
    }
}
