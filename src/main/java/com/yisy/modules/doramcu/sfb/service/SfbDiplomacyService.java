package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.fourx.FourxDiplomaticRelation;
import com.yisy.modules.doramcu.fourx.FourxDiplomacyService;
import com.yisy.modules.doramcu.hex.GalaxyServiceRegistry;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;
import com.yisy.modules.doramcu.fourx.FourxFaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SFB implementation of the Diplomacy 4X pillar.
 * <p>
 * Manages faction relations, treaties, war/peace declarations, and diplomatic events
 * between SFB empires (Federation, Klingon, Romulan, Gorn, Tholian, etc.).
 * </p>
 */
@Service
public class SfbDiplomacyService implements FourxDiplomacyService, GalaxyServiceRegistry.ServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(SfbDiplomacyService.class);

    /** All factions in the game. */
    private final Map<String, FourxFaction> factions = new ConcurrentHashMap<>();

    /** Diplomatic relations keyed by "faction1:faction2" (sorted). */
    private final Map<String, FourxDiplomaticRelation> relations = new ConcurrentHashMap<>();

    private final Random random = new Random();

    @PostConstruct
    public void initFactions() {
        // Alpha Octant empires
        addFaction("FEDERATION", "United Federation of Planets", "DIPLOMATIC", FourxFaction.DiplomaticStance.FRIENDLY, 50);
        addFaction("KLINGON", "Klingon Empire", "AGGRESSIVE", FourxFaction.DiplomaticStance.NEUTRAL, 45);
        addFaction("ROMULAN", "Romulan Star Empire", "DEFENSIVE", FourxFaction.DiplomaticStance.NEUTRAL, 40);
        addFaction("GORN", "Gorn Empire", "AGGRESSIVE", FourxFaction.DiplomaticStance.HOSTILE, 30);
        addFaction("THOLIAN", "Tholian Assembly", "DEFENSIVE", FourxFaction.DiplomaticStance.NEUTRAL, 25);
        addFaction("SULIBAN", "Suliban", "GUERRILLA", FourxFaction.DiplomaticStance.HOSTILE, 15);
        addFaction("TALOSIAN", "Talosians", "DEFENSIVE", FourxFaction.DiplomaticStance.NEUTRAL, 20);

        // Andromeda empires
        addFaction("M'HAARI", "M'Haari Empire", "AGGRESSIVE", FourxFaction.DiplomaticStance.NEUTRAL, 35);
        addFaction("PEOPLIAN", "Peoples of Andromeda", "DIPLOMATIC", FourxFaction.DiplomaticStance.FRIENDLY, 30);
        addFaction("ANDROMEDAN", "Andromedan Empire", "AGGRESSIVE", FourxFaction.DiplomaticStance.HOSTILE, 40);

        // Set up initial relations based on SFB lore
        modifyRelation("FEDERATION", "KLINGON", 10); // Fragile alliance
        modifyRelation("FEDERATION", "ROMULAN", -20); // Cold war
        modifyRelation("FEDERATION", "GORN", -40); // Hostile
        modifyRelation("FEDERATION", "THOLIAN", -10); // Tense
        modifyRelation("KLINGON", "ROMULAN", -30); // Rivals
        modifyRelation("KLINGON", "GORN", -50); // Enemies
        modifyRelation("ROMULAN", "SULIBAN", -35); // Hostile
        modifyRelation("M'HAARI", "ANDROMEDAN", -45); // Enemies
        modifyRelation("M'HAARI", "PEOPLIAN", 15); // Allies

        log.info("Initialized {} SFB factions with {} initial relations",
                factions.size(), relations.size());
    }

    private void addFaction(String id, String name, String combatStyle,
                            FourxFaction.DiplomaticStance stance,
                            int fleetStrength) {
        FourxFaction faction = new FourxFaction();
        faction.setId(id);
        faction.setName(name);
        faction.setGameModule("SFB");
        faction.setCombatStyle(combatStyle);
        faction.setDefaultStance(stance);
        faction.setFleetStrength(fleetStrength);
        faction.setControlledSectors(new ArrayList<>());
        factions.put(id, faction);
    }

    private String relationKey(String faction1Id, String faction2Id) {
        String f1 = faction1Id.toUpperCase();
        String f2 = faction2Id.toUpperCase();
        return f1.compareTo(f2) < 0 ? f1 + ":" + f2 : f2 + ":" + f1;
    }

    @Override
    public GalaxyTemplate getSupportedTemplate() {
        return GalaxyTemplate.CUSTOM;
    }

    @Override
    public FourxDiplomaticRelation getOrCreateRelation(String faction1Id, String faction2Id) {
        String key = relationKey(faction1Id, faction2Id);
        return relations.computeIfAbsent(key, k -> {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation();
            relation.setFaction1Name(faction1Id.toUpperCase());
            relation.setFaction2Name(faction2Id.toUpperCase());
            relation.setRelationScore(0);
            relation.setAtWar(false);
            relation.setActiveTreaties(new ArrayList<>());
            return relation;
        });
    }

    @Override
    public FourxDiplomaticRelation modifyRelation(String faction1Id, String faction2Id, int delta) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        int newScore = Math.max(-100, Math.min(100, relation.getRelationScore() + delta));
        relation.setRelationScore(newScore);

        // Auto-declare war if relation drops below threshold
        if (newScore <= -80 && !relation.isAtWar()) {
            relation.setAtWar(true);
            log.info("Auto-war declared: {} vs {} (relation: {})",
                    faction1Id, faction2Id, newScore);
        }

        // Auto-peace if relation rises above threshold
        if (newScore >= 20 && relation.isAtWar()) {
            relation.setAtWar(false);
            log.info("Auto-peace: {} vs {} (relation: {})",
                    faction1Id, faction2Id, newScore);
        }

        return relation;
    }

    @Override
    public FourxDiplomaticRelation declareWar(String attackerId, String defenderId) {
        FourxDiplomaticRelation relation = getOrCreateRelation(attackerId, defenderId);
        relation.setAtWar(true);
        relation.setRelationScore(Math.min(relation.getRelationScore(), -80));
        log.info("War declared: {} vs {}", attackerId, defenderId);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation establishPeace(String faction1Id, String faction2Id) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.setAtWar(false);
        relation.setRelationScore(Math.max(relation.getRelationScore(), -20));
        log.info("Peace established: {} vs {}", faction1Id, faction2Id);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation addTreaty(String faction1Id, String faction2Id, String treatyType) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        List<String> treaties = relation.getActiveTreaties();
        if (!treaties.contains(treatyType)) {
            treaties.add(treatyType);
        }
        // Treaties improve relations
        modifyRelation(faction1Id, faction2Id, 10);
        log.info("Treaty added: {} - {} ({})", faction1Id, faction2Id, treatyType);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation removeTreaty(String faction1Id, String faction2Id, String treatyType) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.getActiveTreaties().remove(treatyType);
        modifyRelation(faction1Id, faction2Id, -10);
        log.info("Treaty removed: {} - {} ({})", faction1Id, faction2Id, treatyType);
        return relation;
    }

    @Override
    public List<FourxDiplomaticRelation> getRelations(String factionId) {
        String id = factionId.toUpperCase();
        List<FourxDiplomaticRelation> result = new ArrayList<>();
        for (FourxDiplomaticRelation relation : relations.values()) {
            if (relation.getFaction1Name().equals(id) || relation.getFaction2Name().equals(id)) {
                result.add(relation);
            }
        }
        return result;
    }

    @Override
    public boolean isAtWar(String faction1Id, String faction2Id) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        return relation.isAtWar();
    }

    @Override
    public List<FourxFaction> getAllFactions() {
        return new ArrayList<>(factions.values());
    }

    @Override
    public FourxFaction getFaction(String factionId) {
        return factions.get(factionId.toUpperCase());
    }

    @Override
    public Map<String, Object> generateDiplomaticEvent(String factionId) {
        Map<String, Object> event = new LinkedHashMap<>();
        FourxFaction faction = getFaction(factionId);
        if (faction == null) {
            event.put("type", "ERROR");
            event.put("message", "Faction not found: " + factionId);
            return event;
        }

        // Pick a random other faction
        List<String> otherFactions = new ArrayList<>(factions.keySet());
        otherFactions.remove(factionId.toUpperCase());
        if (otherFactions.isEmpty()) {
            event.put("type", "NONE");
            event.put("message", "No other factions to interact with.");
            return event;
        }

        String otherId = otherFactions.get(random.nextInt(otherFactions.size()));
        FourxFaction other = factions.get(otherId);
        FourxDiplomaticRelation relation = getOrCreateRelation(factionId, otherId);

        // Generate event based on current relation
        String eventType;
        String description;
        int relationChange = 0;

        if (relation.isAtWar()) {
            eventType = "WAR_EVENT";
            int roll = random.nextInt(4);
            switch (roll) {
                case 0:
                    description = other.getName() + " launches a surprise attack on your border systems.";
                    relationChange = -15;
                    break;
                case 1:
                    description = "Peace feelers from " + other.getName() + " suggest a ceasefire.";
                    relationChange = 10;
                    break;
                case 2:
                    description = "Battle with " + other.getName() + " results in heavy losses on both sides.";
                    relationChange = -5;
                    break;
                default:
                    description = "Your forces repel an incursion by " + other.getName() + ".";
                    relationChange = -5;
                    break;
            }
        } else if (relation.getRelationScore() < -30) {
            eventType = "TENSION";
            int roll = random.nextInt(3);
            switch (roll) {
                case 0:
                    description = other.getName() + " moves fleets toward your border.";
                    relationChange = -10;
                    break;
                case 1:
                    description = "Spy activity from " + other.getName() + " detected in your systems.";
                    relationChange = -5;
                    break;
                default:
                    description = "Diplomatic incident with " + other.getName() + " escalates tensions.";
                    relationChange = -8;
                    break;
            }
        } else if (relation.getRelationScore() > 30) {
            eventType = "ALLIANCE";
            int roll = random.nextInt(3);
            switch (roll) {
                case 0:
                    description = other.getName() + " proposes a trade agreement.";
                    relationChange = 10;
                    break;
                case 1:
                    description = "Joint exercise with " + other.getName() + " strengthens ties.";
                    relationChange = 5;
                    break;
                default:
                    description = other.getName() + " offers mutual defense pact.";
                    relationChange = 15;
                    break;
            }
        } else {
            eventType = "NEUTRAL";
            int roll = random.nextInt(4);
            switch (roll) {
                case 0:
                    description = other.getName() + " sends diplomatic envoys.";
                    relationChange = 5;
                    break;
                case 1:
                    description = "Neutral trade negotiations with " + other.getName() + ".";
                    relationChange = 3;
                    break;
                case 2:
                    description = "Border skirmish with " + other.getName() + " is quickly resolved.";
                    relationChange = -3;
                    break;
                default:
                    description = "No significant diplomatic activity with " + other.getName() + ".";
                    relationChange = 0;
                    break;
            }
        }

        // Apply relation change
        modifyRelation(factionId, otherId, relationChange);

        event.put("type", eventType);
        event.put("faction", factionId);
        event.put("otherFaction", otherId);
        event.put("description", description);
        event.put("relationChange", relationChange);
        event.put("newRelationScore", getOrCreateRelation(factionId, otherId).getRelationScore());

        log.info("Diplomatic event for {}: {} - {}", factionId, eventType, description);
        return event;
    }
}
