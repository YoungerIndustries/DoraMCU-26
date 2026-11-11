package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.babylon5.model.B5Race;
import com.yisy.modules.doramcu.fourx.FourxDiplomaticRelation;
import com.yisy.modules.doramcu.fourx.FourxDiplomacyService;
import com.yisy.modules.doramcu.fourx.FourxFaction;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * B5 implementation of the Diplomacy 4X pillar.
 * <p>
 * Manages faction relations, treaties, war/peace declarations, and diplomatic events
 * between B5 races. Uses B5Race socialDM and tradeBonus to influence diplomatic outcomes.
 * </p>
 */
@Service
public class B5DiplomacyService implements FourxDiplomacyService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(B5DiplomacyService.class);

    private final Babylon5DatabaseService databaseService;
    private final Map<String, FourxFaction> factions = new ConcurrentHashMap<>();
    private final Map<String, FourxDiplomaticRelation> relations = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public B5DiplomacyService(Babylon5DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostConstruct
    public void initFactions() {
        for (B5Race race : databaseService.getAllRaces()) {
            FourxFaction faction = new FourxFaction(race.getId(), race.getName(), "Babylon5");
            faction.setDescription(race.getDescription());
            faction.setCombatStyle(race.getCombatStyle());
            faction.setDefaultStance(race.getSocialDM() > 0 ? FourxFaction.DiplomaticStance.FRIENDLY : FourxFaction.DiplomaticStance.NEUTRAL);
            factions.put(race.getId(), faction);
        }
        log.info("Initialized {} B5 factions for diplomacy", factions.size());
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.BABYLON5;
    }

    @Override
    public FourxDiplomaticRelation getOrCreateRelation(String faction1Id, String faction2Id) {
        String key = getRelationKey(faction1Id, faction2Id);
        return relations.computeIfAbsent(key, k -> {
            FourxFaction f1 = factions.get(faction1Id);
            FourxFaction f2 = factions.get(faction2Id);
            if (f1 == null || f2 == null) {
                log.warn("Faction not found for relation: {} / {}", faction1Id, faction2Id);
                return null;
            }
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation(f1.getName(), f2.getName());
            // Set initial relation based on race socialDM
            B5Race race1 = databaseService.getRace(faction1Id).orElse(null);
            B5Race race2 = databaseService.getRace(faction2Id).orElse(null);
            if (race1 != null && race2 != null) {
                int initialScore = race1.getSocialDM() + race2.getSocialDM();
                relation.setRelationScore(initialScore);
            }
            return relation;
        });
    }

    @Override
    public FourxDiplomaticRelation modifyRelation(String faction1Id, String faction2Id, int delta) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        if (relation == null) return null;
        relation.modifyRelation(delta);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation declareWar(String attackerId, String defenderId) {
        FourxDiplomaticRelation relation = getOrCreateRelation(attackerId, defenderId);
        if (relation == null) return null;
        relation.setRelationScore(-100);
        relation.setAtWar(true);
        log.info("War declared: {} vs {}", 
                relation.getFaction1Name(), relation.getFaction2Name());
        return relation;
    }

    @Override
    public FourxDiplomaticRelation establishPeace(String faction1Id, String faction2Id) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        if (relation == null) return null;
        relation.setAtWar(false);
        relation.setRelationScore(Math.max(relation.getRelationScore(), -20));
        relation.addTreaty("PEACE");
        log.info("Peace established: {} and {}", 
                relation.getFaction1Name(), relation.getFaction2Name());
        return relation;
    }

    @Override
    public FourxDiplomaticRelation addTreaty(String faction1Id, String faction2Id, String treatyType) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        if (relation == null) return null;
        relation.addTreaty(treatyType);
        // Trade treaties improve relations
        if ("TRADE".equalsIgnoreCase(treatyType)) {
            relation.modifyRelation(10);
        } else if ("MUTUAL_DEFENSE".equalsIgnoreCase(treatyType)) {
            relation.modifyRelation(20);
        }
        return relation;
    }

    @Override
    public FourxDiplomaticRelation removeTreaty(String faction1Id, String faction2Id, String treatyType) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        if (relation == null) return null;
        relation.removeTreaty(treatyType);
        // Removing treaties worsens relations
        if ("TRADE".equalsIgnoreCase(treatyType)) {
            relation.modifyRelation(-10);
        } else if ("MUTUAL_DEFENSE".equalsIgnoreCase(treatyType)) {
            relation.modifyRelation(-15);
        }
        return relation;
    }

    @Override
    public List<FourxDiplomaticRelation> getRelations(String factionId) {
        List<FourxDiplomaticRelation> result = new ArrayList<>();
        for (FourxDiplomaticRelation relation : relations.values()) {
            if (relation != null && (relation.getFaction1Name().equals(getFactionName(factionId)) ||
                    relation.getFaction2Name().equals(getFactionName(factionId)))) {
                result.add(relation);
            }
        }
        return result;
    }

    @Override
    public boolean isAtWar(String faction1Id, String faction2Id) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        return relation != null && relation.isAtWar();
    }

    @Override
    public List<FourxFaction> getAllFactions() {
        return new ArrayList<>(factions.values());
    }

    @Override
    public FourxFaction getFaction(String factionId) {
        return factions.get(factionId);
    }

    @Override
    public Map<String, Object> generateDiplomaticEvent(String factionId) {
        Map<String, Object> event = new LinkedHashMap<>();
        FourxFaction faction = factions.get(factionId);
        if (faction == null) {
            event.put("error", "Faction not found: " + factionId);
            return event;
        }

        // Pick a random other faction
        List<String> otherFactions = new ArrayList<>(factions.keySet());
        otherFactions.remove(factionId);
        if (otherFactions.isEmpty()) {
            event.put("error", "No other factions available");
            return event;
        }

        String otherId = otherFactions.get(random.nextInt(otherFactions.size()));
        FourxFaction other = factions.get(otherId);

        // Generate event type based on current relation
        FourxDiplomaticRelation relation = getOrCreateRelation(factionId, otherId);
        String eventType;
        int relationChange = 0;

        if (relation != null && relation.isAtWar()) {
            eventType = chooseFrom(
                    "Peace overture received",
                    "Enemy fleet spotted at border",
                    "Ceasefire proposal",
                    "War crimes reported"
            );
            relationChange = random.nextInt(5) - 2; // -2 to +2
        } else if (relation != null && relation.getRelationScore() > 50) {
            eventType = chooseFrom(
                    "Trade delegation arrives",
                    "Joint military exercise proposed",
                    "Cultural exchange program",
                    "Alliance proposal"
            );
            relationChange = random.nextInt(10) + 1; // +1 to +10
        } else if (relation != null && relation.getRelationScore() < -30) {
            eventType = chooseFrom(
                    "Border skirmish reported",
                    "Diplomatic insult exchanged",
                    "Sanctions threatened",
                    "Spy discovered"
            );
            relationChange = random.nextInt(10) - 8; // -8 to +1
        } else {
            eventType = chooseFrom(
                    "Neutral trade offer",
                    "Diplomatic meeting requested",
                    "Territorial dispute arises",
                    "Joint exploration proposal"
            );
            relationChange = random.nextInt(11) - 5; // -5 to +5
        }

        // Apply socialDM bonus
        B5Race race = databaseService.getRace(factionId).orElse(null);
        if (race != null) {
            relationChange += race.getSocialDM();
        }

        if (relation != null) {
            relation.modifyRelation(relationChange);
        }

        event.put("faction", faction.getName());
        event.put("otherFaction", other.getName());
        event.put("eventType", eventType);
        event.put("relationChange", relationChange);
        event.put("newRelationScore", relation != null ? relation.getRelationScore() : 0);
        event.put("timestamp", java.time.LocalDateTime.now());

        log.info("Diplomatic event for {}: {} with {} ({})", 
                faction.getName(), eventType, other.getName(), relationChange);
        return event;
    }

    private String getRelationKey(String f1, String f2) {
        return f1.toLowerCase().compareTo(f2.toLowerCase()) < 0 ? f1 + "-" + f2 : f2 + "-" + f1;
    }

    private String getFactionName(String factionId) {
        FourxFaction faction = factions.get(factionId);
        return faction != null ? faction.getName() : factionId;
    }

    private String chooseFrom(String... options) {
        return options[random.nextInt(options.length)];
    }
}
