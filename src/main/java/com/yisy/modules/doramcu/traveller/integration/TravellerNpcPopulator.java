package com.yisy.modules.doramcu.traveller.integration;

import com.yisy.modules.doramcu.gamecore.AiNpcPopulator;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.service.game.LoreGroupEmpireService;
import com.yisy.modules.doramcu.traveller.service.TravellerMonsterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Traveller NPC populator that creates persistent AI characters from Traveller data.
 * <p>
 * Creates AiCharacter entities from Traveller monsters, saving them to the database
 * via {@link LoreGroupEmpireService}.
 * </p>
 */
@Component
public class TravellerNpcPopulator implements AiNpcPopulator {

    private static final Logger log = LoggerFactory.getLogger(TravellerNpcPopulator.class);

    private final TravellerMonsterService monsterService;
    private final LoreGroupEmpireService loreGroupEmpireService;

    public TravellerNpcPopulator(TravellerMonsterService monsterService,
                                 LoreGroupEmpireService loreGroupEmpireService) {
        this.monsterService = monsterService;
        this.loreGroupEmpireService = loreGroupEmpireService;
    }

    @Override
    public GameType getGameType() {
        return GameType.TRAVELLER;
    }

    @Override
    public void populateNpcs(Galaxy galaxy) {
        log.info("Populating Traveller NPCs for galaxy: {}", galaxy.getName());

        String galaxyName = galaxy.getName();
        List<?> monsters = monsterService.listMonsters();

        List<LoreGroupEmpireService.NpcData> npcDataList = new ArrayList<>();
        for (Object monster : monsters) {
            npcDataList.add(new LoreGroupEmpireService.NpcData(
                    monster.toString(), "Monster", "Traveller Galaxy",
                    "Traveller monster: " + monster
            ));
        }

        if (!npcDataList.isEmpty()) {
            loreGroupEmpireService.createNpcs(galaxyName, npcDataList);
        }

        log.info("Traveller NPC population complete: {} NPCs created for galaxy: {}",
                npcDataList.size(), galaxyName);
    }
}
