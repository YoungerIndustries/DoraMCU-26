package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CountryRepository;
import com.yisy.modules.doramcu.fourx.FourxDiplomaticRelation;
import com.yisy.modules.doramcu.fourx.FourxDiplomacyService;
import com.yisy.modules.doramcu.fourx.FourxFaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements FourX diplomacy for G20 countries.
 * <p>
 * Maps Earth2026Country ISO codes to FourX factions, enabling:
 * <ul>
 *   <li>Diplomatic relations between countries</li>
 *   <li>War and peace declarations</li>
 *   <li>Treaties (trade, mutual defense, non-aggression)</li>
 *   <li>Diplomatic events</li>
 * </ul>
 * Country relations affect battle outcomes: allies get +10% combat stats.
 * </p>
 */
@Service
public class CountryDiplomacyService implements FourxDiplomacyService {

    private static final Logger log = LoggerFactory.getLogger(CountryDiplomacyService.class);

    private final Earth2026CountryRepository countryRepository;

    // In-memory diplomacy state
    private final Map<String, FourxFaction> factions = new ConcurrentHashMap<>();
    private final Map<String, FourxDiplomaticRelation> relations = new ConcurrentHashMap<>();

    public CountryDiplomacyService(Earth2026CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
        initializeFactions();
    }

    /**
     * Initialize factions from G20 countries.
     */
    private void initializeFactions() {
        List<Earth2026Country> countries = countryRepository.findAll();
        for (Earth2026Country country : countries) {
            String isoCode = country.getIsoCode();
            FourxFaction faction = new FourxFaction(isoCode, country.getName(), "Earth2026");
            faction.setDescription(country.getGovernmentType() != null ? country.getGovernmentType() : "Unknown");
            faction.setFleetStrength(country.getMilitaryStrength() != null ? country.getMilitaryStrength() : 50);
            faction.setEconomyOutput(country.getEconomicPower() != null ? country.getEconomicPower() : 50);
            faction.setResearchPoints(country.getDiplomaticInfluence() != null ? country.getDiplomaticInfluence() : 50);
            factions.put(isoCode, faction);
        }
        log.info("Initialized {} diplomacy factions from countries", factions.size());
    }

    /**
     * Get relation key for two factions (sorted for consistency).
     */
    private String relationKey(String f1, String f2) {
        return f1.compareTo(f2) < 0 ? f1 + "-" + f2 : f2 + "-" + f1;
    }

    @Override
    public FourxDiplomaticRelation getOrCreateRelation(String faction1Id, String faction2Id) {
        String key = relationKey(faction1Id, faction2Id);
        return relations.computeIfAbsent(key, k -> {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation(faction1Id, faction2Id);
            relation.setRelationScore(0);
            log.debug("Created new diplomatic relation: {} <-> {}", faction1Id, faction2Id);
            return relation;
        });
    }

    @Override
    public FourxDiplomaticRelation modifyRelation(String faction1Id, String faction2Id, int delta) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.setRelationScore(Math.max(-100, Math.min(100, relation.getRelationScore() + delta)));
        log.info("Modified relation {} <-> {}: {} (delta: {})",
                faction1Id, faction2Id, relation.getRelationScore(), delta);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation declareWar(String attackerId, String defenderId) {
        FourxDiplomaticRelation relation = getOrCreateRelation(attackerId, defenderId);
        relation.setAtWar(true);
        relation.setRelationScore(-100);
        log.info("WAR DECLARED: {} declares war on {}", attackerId, defenderId);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation establishPeace(String faction1Id, String faction2Id) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.setAtWar(false);
        relation.setRelationScore(0);
        log.info("PEACE ESTABLISHED: {} <-> {}", faction1Id, faction2Id);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation addTreaty(String faction1Id, String faction2Id, String treatyType) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.addTreaty(treatyType);
        // Treaty bonuses
        switch (treatyType) {
            case "TRADE":
                relation.setRelationScore(Math.min(100, relation.getRelationScore() + 10));
                break;
            case "MUTUAL_DEFENSE":
                relation.setRelationScore(Math.min(100, relation.getRelationScore() + 20));
                break;
            case "NON_AGGRESSION":
                relation.setRelationScore(Math.min(100, relation.getRelationScore() + 5));
                break;
            default:
                relation.setRelationScore(Math.min(100, relation.getRelationScore() + 5));
        }
        log.info("Treaty added: {} <-> {} ({})", faction1Id, faction2Id, treatyType);
        return relation;
    }

    @Override
    public FourxDiplomaticRelation removeTreaty(String faction1Id, String faction2Id, String treatyType) {
        FourxDiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.removeTreaty(treatyType);
        log.info("Treaty removed: {} <-> {} ({})", faction1Id, faction2Id, treatyType);
        return relation;
    }

    @Override
    public List<FourxDiplomaticRelation> getRelations(String factionId) {
        List<FourxDiplomaticRelation> result = new ArrayList<>();
        for (FourxDiplomaticRelation relation : relations.values()) {
            if (relation.getFaction1Name().equals(factionId) || relation.getFaction2Name().equals(factionId)) {
                result.add(relation);
            }
        }
        return result;
    }

    @Override
    public boolean isAtWar(String faction1Id, String faction2Id) {
        String key = relationKey(faction1Id, faction2Id);
        FourxDiplomaticRelation relation = relations.get(key);
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
        Random random = new Random();
        List<FourxFaction> otherFactions = factions.values().stream()
                .filter(f -> !f.getId().equals(factionId))
                .toList();

        if (otherFactions.isEmpty()) {
            return Map.of("event", "NO_OTHER_FACTIONS", "faction", factionId);
        }

        FourxFaction target = otherFactions.get(random.nextInt(otherFactions.size()));
        String eventType = randomEventTypes().get(random.nextInt(randomEventTypes().size()));

        Map<String, Object> event = new HashMap<>();
        event.put("event", eventType);
        event.put("faction1", factionId);
        event.put("faction2", target.getId());
        event.put("timestamp", System.currentTimeMillis());

        // Apply event effects
        switch (eventType) {
            case "DIPLOMATIC_MEETING":
                modifyRelation(factionId, target.getId(), random.nextInt(11) - 3); // -3 to +7
                event.put("effect", "Relation improved slightly");
                break;
            case "TRADE_DISPUTE":
                modifyRelation(factionId, target.getId(), random.nextInt(11) - 8); // -8 to -2
                event.put("effect", "Trade tensions increased");
                break;
            case "MILITARY_EXERCISE":
                event.put("effect", "Military exercise near border");
                break;
            case "CULTURAL_EXCHANGE":
                modifyRelation(factionId, target.getId(), random.nextInt(6) + 1); // +1 to +6
                event.put("effect", "Cultural ties strengthened");
                break;
            case "SPY_INCIDENT":
                modifyRelation(factionId, target.getId(), random.nextInt(16) - 10); // -10 to +5
                event.put("effect", "Espionage incident detected");
                break;
            default:
                event.put("effect", "No significant change");
        }

        log.info("Diplomatic event for {}: {} with {}", factionId, eventType, target.getId());
        return event;
    }

    private List<String> randomEventTypes() {
        return List.of(
                "DIPLOMATIC_MEETING",
                "TRADE_DISPUTE",
                "MILITARY_EXERCISE",
                "CULTURAL_EXCHANGE",
                "SPY_INCIDENT",
                "HUMANITARIAN_AID",
                "SANCTIONS",
                "ALLIANCE_PROPOSAL"
        );
    }

    /**
     * Get the battle modifier for a faction based on diplomatic relations.
     * Allies get +10% combat stats, enemies get -10%.
     *
     * @param factionId the faction ID
     * @param allyId the ally faction ID (for checking relations)
     * @return the combat modifier (0.9 to 1.1)
     */
    public double getBattleModifier(String factionId, String allyId) {
        if (isAtWar(factionId, allyId)) {
            return 0.9; // Enemy penalty
        }
        String key = relationKey(factionId, allyId);
        FourxDiplomaticRelation relation = relations.get(key);
        if (relation != null) {
            List<String> treaties = relation.getActiveTreaties();
            if (treaties.contains("MUTUAL_DEFENSE")) {
                return 1.1; // Ally bonus
            }
            if (relation.getRelationScore() > 50) {
                return 1.05; // Friendly bonus
            }
            if (relation.getRelationScore() < -50) {
                return 0.95; // Hostile penalty
            }
        }
        return 1.0; // Neutral
    }
}
