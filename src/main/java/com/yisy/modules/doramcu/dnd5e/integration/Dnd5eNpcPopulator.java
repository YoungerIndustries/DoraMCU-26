package com.yisy.modules.doramcu.dnd5e.integration;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMonsterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eNpcService;
import com.yisy.modules.doramcu.gamecore.AiNpcPopulator;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * D&D 5E NPC populator that creates persistent AI characters from D&D data.
 * <p>
 * Creates AiCharacter entities from D&D monsters and NPCs from the rules database,
 * saving them to the database via {@link LoreGroupEmpireService}.
 * </p>
 * <p>
 * Each monster becomes an AI character with its CR determining leadership rating,
 * and each NPC becomes an AI character with its role and disposition.
 * </p>
 */
@Component
public class Dnd5eNpcPopulator implements AiNpcPopulator {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eNpcPopulator.class);

    private final Dnd5eMonsterService monsterService;
    private final Dnd5eNpcService npcService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public Dnd5eNpcPopulator(Dnd5eMonsterService monsterService,
                             Dnd5eNpcService npcService,
                             LoreGroupEmpireService loreGroupEmpireService) {
        this.monsterService = monsterService;
        this.npcService = npcService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.DND_5E;
    }

    @Override
    public void populateNpcs(Galaxy galaxy) {
        log.info("Populating D&D 5E NPCs for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();
        int npcCount = 0;

        // Create NPCs from monsters
        List<Dnd5eMonster> monsters = monsterService.listMonsters();
        List<LoreGroupEmpireService.NpcData> monsterNpcs = new ArrayList<>();
        for (Dnd5eMonster monster : monsters) {
            String empireName = determineMonsterEmpire(monster);
            String role = determineMonsterRole(monster);
            String description = String.format(
                    "D&D monster: %s (CR %d, %s, %s)",
                    monster.getName(), monster.getCr(), monster.getSize(), monster.getAlignment()
            );
            monsterNpcs.add(new LoreGroupEmpireService.NpcData(
                    monster.getName(), role, empireName, description
            ));
            npcCount++;
        }
        if (!monsterNpcs.isEmpty()) {
            loreGroupEmpireService.createNpcs(galaxyName, monsterNpcs);
        }

        // Create NPCs from NPC database
        List<Dnd5eNpc> npcs = npcService.listNpcs();
        List<LoreGroupEmpireService.NpcData> npcDataList = new ArrayList<>();
        for (Dnd5eNpc npc : npcs) {
            String empireName = determineNpcEmpire(npc);
            String role = npc.getNpcClass() != null ? npc.getNpcClass() : "NPC";
            String description = String.format(
                    "D&D NPC: %s (Level %d, %s, %s)",
                    npc.getName(), npc.getLevel(), npc.getRace(), npc.getMotive()
            );
            npcDataList.add(new LoreGroupEmpireService.NpcData(
                    npc.getName(), role, empireName, description
            ));
            npcCount++;
        }
        if (!npcDataList.isEmpty()) {
            loreGroupEmpireService.createNpcs(galaxyName, npcDataList);
        }

        log.info("D&D 5E NPC population complete: {} NPCs created for galaxy: {}",
                npcCount, galaxyName);
    }

    /**
     * Determines which empire a monster belongs to based on its type and alignment.
     */
    private String determineMonsterEmpire(Dnd5eMonster monster) {
        String type = monster.getType() != null ? monster.getType() : "";
        String alignment = monster.getAlignment() != null ? monster.getAlignment() : "";

        // Dragons go to dragon empires
        if (type.equalsIgnoreCase("Dragon")) {
            if (alignment.contains("Good")) {
                return "Metallic Dragon Empire";
            } else if (alignment.contains("Evil")) {
                return "Chromatic Dragon Empire";
            }
            return "Gem Dragon Empire";
        }

        // Devils go to Nine Hells
        if (type.equalsIgnoreCase("Fiend") && alignment.contains("Lawful")) {
            return "Nine Hells";
        }

        // Demons go to Abyss
        if (type.equalsIgnoreCase("Fiend") && alignment.contains("Chaotic")) {
            return "The Abyss";
        }

        // Undead go to Underworld
        if (type.equalsIgnoreCase("Undead")) {
            return "The Underworld";
        }

        // Elementals go to elemental planes
        if (type.equalsIgnoreCase("Elemental")) {
            if (monster.getName().toLowerCase().contains("fire")) return "Fire Elemental Plane";
            if (monster.getName().toLowerCase().contains("water") || monster.getName().toLowerCase().contains("ice")) return "Water Elemental Plane";
            if (monster.getName().toLowerCase().contains("earth") || monster.getName().toLowerCase().contains("stone")) return "Earth Elemental Plane";
            if (monster.getName().toLowerCase().contains("air") || monster.getName().toLowerCase().contains("wind")) return "Air Elemental Plane";
            return "Elemental Planes";
        }

        // Drow and underdark creatures
        if (type.equalsIgnoreCase("Humanoid") &&
                (monster.getName().toLowerCase().contains("drow") ||
                 monster.getName().toLowerCase().contains("underdark"))) {
            return "Drow Empire";
        }

        return "Forgotten Realms";
    }

    /**
     * Determines the role of a monster based on its CR and type.
     */
    private String determineMonsterRole(Dnd5eMonster monster) {
        double cr = monster.getCr();
        if (cr >= 20) return "World Boss";
        if (cr >= 15) return "Empire Champion";
        if (cr >= 10) return "Elite Guard";
        if (cr >= 5) return "Veteran Fighter";
        if (cr >= 1) return "Soldier";
        return "Minion";
    }

    /**
     * Determines which empire an NPC belongs to based on its race and type.
     */
    private String determineNpcEmpire(Dnd5eNpc npc) {
        String race = npc.getRace() != null ? npc.getRace() : "";
        String lair = npc.getLairLocation() != null ? npc.getLairLocation() : "";

        if (race.toLowerCase().contains("dragon")) {
            if (npc.getDisposition() != null && npc.getDisposition().equals("Hostile")) {
                return "Chromatic Dragon Empire";
            }
            return "Metallic Dragon Empire";
        }

        if (lair.toLowerCase().contains("underdark") || race.toLowerCase().contains("drow")) {
            return "Drow Empire";
        }

        if (lair.toLowerCase().contains("hell") || race.toLowerCase().contains("devil")) {
            return "Nine Hells";
        }

        if (lair.toLowerCase().contains("abyss") || race.toLowerCase().contains("demon")) {
            return "The Abyss";
        }

        return "Forgotten Realms";
    }
}
