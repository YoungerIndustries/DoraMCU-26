package com.yisy.modules.doramcu.traveller.shipcombat;

import com.yisy.modules.doramcu.traveller.HighGuardRangeBand;
import com.yisy.modules.doramcu.traveller.HighGuardShipCombat;
import com.yisy.modules.doramcu.traveller.HighGuardShipDesign;
import com.yisy.modules.doramcu.traveller.ShipTemplate;
import com.yisy.modules.doramcu.traveller.ShipToShipBattle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core service for ship-to-ship combat in the Traveller module.
 * <p>
 * Manages battle sessions, fleet creation from templates, and round-by-round
 * resolution using either Classic Traveller or High Guard combat rules.
 * </p>
 * <p>
 * Battles are stored in memory (like AirBattleService). No JPA persistence.
 * </p>
 */

@Service
public class ShipCombatService {

    private static final Logger log = LoggerFactory.getLogger(ShipCombatService.class);

    private final Map<String, BattleSession> battles = new ConcurrentHashMap<>();

    /**
     * Creates a new battle between two fleets.
     *
     * @param battleId unique battle ID
     * @param fleet0Name name of fleet 0 (attacker)
     * @param fleet0Templates list of ship template names for fleet 0
     * @param fleet1Name name of fleet 1 (defender)
     * @param fleet1Templates list of ship template names for fleet 1
     * @param rulesType combat rules: CLASSIC_TRAVELLER or HIGH_GUARD
     * @param sector sector name where the battle takes place
     * @return the created BattleSession
     */
    public BattleSession createBattle(String battleId, String fleet0Name, List<String> fleet0Templates,
                                      String fleet1Name, List<String> fleet1Templates,
                                      BattleSession.RulesType rulesType, String sector) {
        // Build fleets from templates
        List<ShipTemplate> templates0 = resolveTemplates(fleet0Templates);
        List<ShipTemplate> templates1 = resolveTemplates(fleet1Templates);

        Fleet fleet0 = buildFleet("fleet-0", 0, fleet0Name, templates0);
        Fleet fleet1 = buildFleet("fleet-1", 1, fleet1Name, templates1);

        // Create battle session
        BattleSession session = BattleSession.builder()
                .id(battleId)
                .rulesType(rulesType)
                .fleet0(fleet0)
                .fleet1(fleet1)
                .sector(sector)
                .build();

        // Initialize the appropriate combat engine
        if (rulesType == BattleSession.RulesType.HIGH_GUARD) {
            initHighGuardCombat(session, templates0, templates1);
        } else {
            initClassicCombat(session, templates0, templates1);
        }

        session.setState(BattleSession.BattleState.IN_PROGRESS);
        battles.put(battleId, session);

        session.addEvent(Map.of(
                "round", 0,
                "type", "BATTLE_STARTED",
                "message", String.format("%s vs %s in %s (%s rules)",
                        fleet0Name, fleet1Name, sector, rulesType.name())
        ));

        log.info("Created battle {}: {} vs {} ({})", battleId, fleet0Name, fleet1Name, rulesType);
        return session;
    }

    /**
     * Resolves one round of combat.
     *
     * @param battleId the battle ID
     * @return the battle session with updated state
     * @throws IllegalArgumentException if battle not found
     * @throws IllegalStateException if battle is not in progress
     */
    public BattleSession resolveRound(String battleId) {
        BattleSession session = getBattle(battleId);
        if (session.getState() != BattleSession.BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Battle is not in progress: " + session.getState());
        }

        if (session.getRulesType() == BattleSession.RulesType.HIGH_GUARD) {
            resolveHighGuardRound(session);
        } else {
            resolveClassicRound(session);
        }

        return session;
    }

    /**
     * Resolves the battle to completion.
     *
     * @param battleId the battle ID
     * @return the battle session with final outcome
     */
    public BattleSession resolveToCompletion(String battleId) {
        while (getBattle(battleId).getState() == BattleSession.BattleState.IN_PROGRESS) {
            resolveRound(battleId);
        }
        return getBattle(battleId);
    }

    /**
     * Gets a battle by ID.
     */
    public BattleSession getBattle(String battleId) {
        BattleSession session = battles.get(battleId);
        if (session == null) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }
        return session;
    }

    /**
     * Lists all active battles.
     */
    public List<String> listBattles() {
        return new ArrayList<>(battles.keySet());
    }

    /**
     * Gets all available ship templates.
     */
    public List<ShipTemplate> getAllTemplates() {
        return ShipTemplate.getAllTemplates();
    }

    /**
     * Gets ship templates by class.
     */
    public List<ShipTemplate> getTemplatesByClass(ShipTemplate.ShipClass shipClass) {
        return ShipTemplate.getByClass(shipClass);
    }

    // ---- Private Methods ----

    private List<ShipTemplate> resolveTemplates(List<String> templateNames) {
        List<ShipTemplate> all = ShipTemplate.getAllTemplates();
        List<ShipTemplate> resolved = new ArrayList<>();
        for (String name : templateNames) {
            ShipTemplate template = all.stream()
                    .filter(t -> t.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown ship template: " + name));
            resolved.add(template);
        }
        return resolved;
    }

    private Fleet buildFleet(String id, int side, String name, List<ShipTemplate> templates) {
        Fleet.Builder builder = Fleet.builder()
                .id(id)
                .side(side)
                .name(name);

        for (ShipTemplate template : templates) {
            HighGuardShipDesign design = template.toDesign();
            builder.ship(new Fleet.FleetShip(template, design.getHullPoints(), false));
        }

        return builder.build();
    }

    private void initClassicCombat(BattleSession session, List<ShipTemplate> templates0, List<ShipTemplate> templates1) {
        // Classic Traveller ShipToShipBattle uses ShipDesign, but templates produce HighGuardShipDesign.
        // Use HighGuardShipFactory to convert to hex Ships, then build a simplified battle.
        // For template-based battles, High Guard rules are preferred. Fall back to High Guard combat.
        log.warn("Classic Traveller rules requested with High Guard templates. Using High Guard combat engine instead.");
        initHighGuardCombat(session, templates0, templates1);
        session.addEvent(Map.of(
                "round", 0,
                "type", "RULES_OVERRIDE",
                "message", "Classic Traveller rules not compatible with High Guard templates. Using High Guard rules."
        ));
    }

    private void initHighGuardCombat(BattleSession session, List<ShipTemplate> templates0, List<ShipTemplate> templates1) {
        // For High Guard, we use the first ship from each fleet for 1v1 combat
        // Multi-ship High Guard combat would need additional engineering
        if (templates0.isEmpty() || templates1.isEmpty()) {
            throw new IllegalArgumentException("Each fleet must have at least one ship");
        }

        HighGuardShipDesign design0 = templates0.get(0).toDesign();
        HighGuardShipDesign design1 = templates1.get(0).toDesign();

        HighGuardShipCombat combat = new HighGuardShipCombat(design0, design1, HighGuardRangeBand.LONG);
        session.setHighGuardCombat(combat);
    }

    private void resolveClassicRound(BattleSession session) {
        ShipToShipBattle battle = session.getClassicBattle();
        if (battle == null) {
            throw new IllegalStateException("Classic battle not initialized");
        }

        ShipToShipBattle.RoundResult roundResult = battle.fightRound();
        if (roundResult == null) {
            endBattle(session);
            return;
        }

        session.setCurrentRound(roundResult.getRoundNumber());

        // Log events from this round
        for (ShipToShipBattle.BattleEvent event : roundResult.getEvents()) {
            session.addEvent(Map.of(
                    "round", event.getRound(),
                    "phase", event.getPhase().name(),
                    "type", event.getType().name(),
                    "message", event.getMessage()
            ));
        }

        if (battle.isBattleEnded()) {
            endBattle(session);
        }
    }

    private void resolveHighGuardRound(BattleSession session) {
        HighGuardShipCombat combat = session.getHighGuardCombat();
        if (combat == null) {
            throw new IllegalStateException("High Guard combat not initialized");
        }

        boolean ongoing = combat.executeRound();
        session.setCurrentRound(combat.getCurrentRound());

        // Log events from this round
        for (HighGuardShipCombat.CombatEvent event : combat.getEventLog()) {
            if (event.getRound() == combat.getCurrentRound()) {
                session.addEvent(Map.of(
                        "round", event.getRound(),
                        "phase", event.getPhase().name(),
                        "type", event.getType().name(),
                        "message", event.getMessage(),
                        "side", event.getSide()
                ));
            }
        }

        if (!ongoing || combat.isCompleted()) {
            endBattle(session);
        }
    }

    private void endBattle(BattleSession session) {
        if (session.getRulesType() == BattleSession.RulesType.HIGH_GUARD) {
            HighGuardShipCombat combat = session.getHighGuardCombat();
            if (combat != null) {
                HighGuardShipCombat.CombatOutcome outcome = combat.getOutcome();
                session.setOutcome(outcome.name());
                session.setState(BattleSession.BattleState.COMPLETED);

                session.addEvent(Map.of(
                        "round", session.getCurrentRound(),
                        "type", "BATTLE_ENDED",
                        "message", String.format("Battle ended: %s", outcome)
                ));
            }
        } else {
            ShipToShipBattle battle = session.getClassicBattle();
            if (battle != null) {
                ShipToShipBattle.BattleResult result = battle.fightToCompletion();
                ShipToShipBattle.BattleOutcome outcome = result.getOutcome();
                session.setOutcome(outcome.name());

                if (outcome == ShipToShipBattle.BattleOutcome.STALEMATE) {
                    session.setState(BattleSession.BattleState.STALEMATE);
                } else {
                    session.setState(BattleSession.BattleState.COMPLETED);
                }

                session.addEvent(Map.of(
                        "round", result.getTotalRounds(),
                        "type", "BATTLE_ENDED",
                        "message", String.format("Battle ended: %s after %d rounds", outcome, result.getTotalRounds())
                ));
            }
        }

        log.info("Battle {} ended: {}", session.getId(), session.getOutcome());
    }
}
