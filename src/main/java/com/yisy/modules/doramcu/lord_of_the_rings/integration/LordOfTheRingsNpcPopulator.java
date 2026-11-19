package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import com.yisy.modules.doramcu.gamecore.AiNpcPopulator;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Faction;
import com.yisy.modules.doramcu.lord_of_the_rings.model.FactionLeader;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Unit;
import com.yisy.modules.doramcu.lord_of_the_rings.service.LordOfTheRingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Lord of the Rings NPC populator that creates persistent AI characters from LOTR data.
 */
@Component
public class LordOfTheRingsNpcPopulator implements AiNpcPopulator {

    private static final Logger log = LoggerFactory.getLogger(LordOfTheRingsNpcPopulator.class);

    private final LordOfTheRingsService lotrService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public LordOfTheRingsNpcPopulator(LordOfTheRingsService lotrService,
                                      LoreGroupEmpireService loreGroupEmpireService) {
        this.lotrService = lotrService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.LORD_OF_THE_RINGS;
    }

    @Override
    public void populateNpcs(Galaxy galaxy) {
        log.info("Populating Lord of the Rings NPCs for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();
        List<LoreGroupEmpireService.NpcData> allNpcs = new ArrayList<>();

        // Create NPCs from faction leaders
        List<Faction> factions = lotrService.getFactions();
        for (Faction faction : factions) {
            if (faction.keyLeaders() != null) {
                for (FactionLeader leader : faction.keyLeaders()) {
                    String description = String.format(
                            "Lord of the Rings faction leader: %s (%s, %s)",
                            leader.name(), leader.role(), faction.name()
                    );
                    allNpcs.add(new LoreGroupEmpireService.NpcData(
                            leader.name(), leader.role(), faction.name(), description
                    ));
                }
            }
        }

        // Create NPCs from character units
        List<Unit> characters = lotrService.getCharacters();
        for (Unit character : characters) {
            String description = String.format(
                    "Lord of the Rings character: %s (%s, %s)",
                    character.name(), character.faction(), character.type()
            );
            allNpcs.add(new LoreGroupEmpireService.NpcData(
                    character.name(), "Character", character.faction(), description
            ));
        }

        if (!allNpcs.isEmpty()) {
            loreGroupEmpireService.createNpcs(galaxyName, allNpcs);
        }

        log.info("Lord of the Rings NPC population complete: {} NPCs created for galaxy: {}",
                allNpcs.size(), galaxyName);
    }
}
