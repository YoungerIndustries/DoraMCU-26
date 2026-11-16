package com.yisy.modules.doramcu.starwars.integration;

import com.yisy.modules.doramcu.gamecore.AiNpcPopulator;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.service.LoreGroupEmpireService;
import com.yisy.modules.doramcu.starwars.model.Faction;
import com.yisy.modules.doramcu.starwars.model.FactionLeader;
import com.yisy.modules.doramcu.starwars.model.Unit;
import com.yisy.modules.doramcu.starwars.service.StarWarsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Star Wars NPC populator that creates persistent AI characters from Star Wars data.
 * <p>
 * Creates AiCharacter entities for faction leaders and character units,
 * saving them to the database via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class StarWarsNpcPopulator implements AiNpcPopulator {

    private static final Logger log = LoggerFactory.getLogger(StarWarsNpcPopulator.class);

    private final StarWarsService starWarsService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public StarWarsNpcPopulator(StarWarsService starWarsService,
                                LoreGroupEmpireService loreGroupEmpireService) {
        this.starWarsService = starWarsService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.STAR_WARS;
    }

    @Override
    public void populateNpcs(Galaxy galaxy) {
        log.info("Populating Star Wars NPCs for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();
        List<LoreGroupEmpireService.NpcData> allNpcs = new ArrayList<>();

        // Create NPCs from faction leaders
        List<Faction> factions = starWarsService.getFactions();
        for (Faction faction : factions) {
            if (faction.keyLeaders() != null) {
                for (FactionLeader leader : faction.keyLeaders()) {
                    String description = String.format(
                            "Star Wars faction leader: %s (%s, %s)",
                            leader.name(), leader.role(), faction.name()
                    );
                    allNpcs.add(new LoreGroupEmpireService.NpcData(
                            leader.name(), leader.role(), faction.name(), description
                    ));
                }
            }
        }

        // Create NPCs from character units
        List<Unit> characters = starWarsService.getCharacters();
        for (Unit character : characters) {
            String description = String.format(
                    "Star Wars character: %s (%s, %s)",
                    character.name(), character.faction(), character.getClass().getSimpleName()
            );
            allNpcs.add(new LoreGroupEmpireService.NpcData(
                    character.name(), "Character", character.faction(), description
            ));
        }

        if (!allNpcs.isEmpty()) {
            loreGroupEmpireService.createNpcs(galaxyName, allNpcs);
        }

        log.info("Star Wars NPC population complete: {} NPCs created for galaxy: {}",
                allNpcs.size(), galaxyName);
    }
}
