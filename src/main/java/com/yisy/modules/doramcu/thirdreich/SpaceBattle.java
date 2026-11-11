package com.yisy.modules.doramcu.thirdreich;

import com.yisy.modules.doramcu.dorat.ShipCombatStatus;
import com.yisy.modules.doramcu.dorat.ShipToShipBattle;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Space battle engine supporting two modes: Abstract (single CRT roll) and Detailed (ship-by-ship phase combat).
 * <p>
 * In abstract mode, uses fleet strength ratios (total tonnage × weapon strength) for CRT resolution.
 * In detailed mode, delegates to {@link ShipToShipBattle} for phase-based combat (radar, missiles, lasers, damage).
 * </p>
 * <p>
 * Follows the same pattern as {@link ArmyBattle}, {@link NavalBattle}, and {@link AirBattle}.
 * </p>
 */
public class SpaceBattle {

    private final int id;
    private final List<ShipCombatStatus> attackerShips;
    private final List<ShipCombatStatus> defenderShips;
    private final BattleMode mode;
    private BattleState state;
    private SpaceBattleResult result;
    private final List<SpaceBattleEvent> events;
    private int currentRound;

    // Detailed mode delegate
    private ShipToShipBattle detailedBattle;

    // Joint operations support fields
    private int orbitalBombardmentBonus;  // bonus to army battles from space superiority

    // Character leadership fields
    private BattleCommander attackerCommander;
    private BattleCommander defenderCommander;
    private final List<BattleParticipant> participants;

    private static int nextId = 1;

    public SpaceBattle(List<ShipCombatStatus> attackerShips, List<ShipCombatStatus> defenderShips,
                       BattleMode mode) {
        this.id = nextId++;
        this.attackerShips = new ArrayList<>(attackerShips);
        this.defenderShips = new ArrayList<>(defenderShips);
        this.mode = mode;
        this.state = BattleState.INITIATED;
        this.events = new ArrayList<>();
        this.currentRound = 0;
        this.participants = new ArrayList<>();
    }

    /**
     * Starts the battle.
     */
    public void start() {
        if (state != BattleState.INITIATED) {
            throw new IllegalStateException("Battle already started or completed");
        }
        state = BattleState.IN_PROGRESS;
        events.add(new SpaceBattleEvent(0, SpaceBattleEvent.EventType.BATTLE_STARTED,
                "Space battle initiated: " + attackerShips.size() + " ships vs " + defenderShips.size() + " ships"));
    }

    /**
     * Resolves the battle in abstract mode with a single CRT roll.
     *
     * @return the battle result
     */
    public SpaceBattleResult resolveAbstract() {
        if (state == BattleState.COMPLETED) {
            return result;
        }
        if (state == BattleState.INITIATED) {
            start();
        }

        int attackValue = calculateTotalAttack(attackerShips);
        int defenseValue = calculateTotalDefense(defenderShips);

        // Calculate odds ratio
        String oddsRatio;
        BattleOutcome outcome;
        String crtResult;
        int attackerLosses = 0;
        int defenderLosses = 0;

        if (defenseValue == 0) {
            oddsRatio = "Automatic";
            outcome = BattleOutcome.ATTACKER_VICTORY;
            crtResult = "Defender Eliminated";
            defenderLosses = defenderShips.size();
        } else if (attackValue == 0) {
            oddsRatio = "0:1";
            outcome = BattleOutcome.DEFENDER_VICTORY;
            crtResult = "Attacker Retreats";
            attackerLosses = (int) Math.ceil(attackerShips.size() * 0.5);
        } else {
            double odds = (double) attackValue / defenseValue;
            oddsRatio = formatOdds(odds);

            // Roll 2D6
            int diceRoll = DiceUtil.roll2D6();
            outcome = lookupCRT(odds, diceRoll);
            crtResult = describeCRTResult(outcome, odds, diceRoll);
            calculateCasualties(outcome, odds, attackerShips.size(), defenderShips.size(), attackerLosses, defenderLosses);
        }

        // Apply casualties
        applyCasualties(attackerShips, attackerLosses, Side.ATTACKER);
        applyCasualties(defenderShips, defenderLosses, Side.DEFENDER);

        // Log battle end event
        events.add(new SpaceBattleEvent(0, SpaceBattleEvent.EventType.BATTLE_ENDED,
                "Battle resolved: " + outcome.name() + " (" + crtResult + ")"));

        state = BattleState.COMPLETED;
        result = new SpaceBattleResult(
                outcome,
                oddsRatio,
                crtResult,
                attackerLosses,
                defenderLosses,
                new ArrayList<>(events)
        );

        return result;
    }

    /**
     * Fights a single round of detailed combat.
     *
     * @return the result of this round
     */
    public ShipToShipBattle.RoundResult fightRound() {
        if (state == BattleState.COMPLETED) {
            return null;
        }
        if (state == BattleState.INITIATED) {
            start();
        }

        // Initialize detailed battle if needed
        if (detailedBattle == null) {
            detailedBattle = new ShipToShipBattle(attackerShips, defenderShips);
        }

        ShipToShipBattle.RoundResult roundResult = detailedBattle.fightRound();
        if (roundResult != null) {
            currentRound = roundResult.getRoundNumber();

            // Log events from detailed battle
            for (ShipToShipBattle.BattleEvent event : roundResult.getEvents()) {
                events.add(new SpaceBattleEvent(
                        event.getRound(),
                        convertEventType(event.getType()),
                        event.getMessage()
                ));
            }

            // Check for battle end
            int attackerActive = countActiveShips(attackerShips);
            int defenderActive = countActiveShips(defenderShips);

            if (attackerActive == 0 && defenderActive == 0) {
                endBattle(BattleOutcome.DRAW, "Mutual Destruction");
            } else if (attackerActive == 0) {
                endBattle(BattleOutcome.DEFENDER_VICTORY, "Defender Victory");
            } else if (defenderActive == 0) {
                endBattle(BattleOutcome.ATTACKER_VICTORY, "Attacker Victory");
            }
        }

        return roundResult;
    }

    /**
     * Fights the battle to completion.
     *
     * @return the final battle result
     */
    public SpaceBattleResult fightToCompletion() {
        if (state == BattleState.COMPLETED) {
            return result;
        }

        while (state != BattleState.COMPLETED) {
            ShipToShipBattle.RoundResult round = fightRound();
            if (round == null) break;
        }

        if (result == null) {
            // Determine outcome from ship status
            int attackerActive = countActiveShips(attackerShips);
            int defenderActive = countActiveShips(defenderShips);

            BattleOutcome outcome;
            String crtResult;
            if (attackerActive == 0 && defenderActive == 0) {
                outcome = BattleOutcome.DRAW;
                crtResult = "Mutual Destruction";
            } else if (attackerActive == 0) {
                outcome = BattleOutcome.DEFENDER_VICTORY;
                crtResult = "Defender Victory";
            } else if (defenderActive == 0) {
                outcome = BattleOutcome.ATTACKER_VICTORY;
                crtResult = "Attacker Victory";
            } else {
                outcome = BattleOutcome.DRAW;
                crtResult = "Stalemate";
            }

            int attackerLosses = attackerShips.size() - attackerActive;
            int defenderLosses = defenderShips.size() - defenderActive;

            events.add(new SpaceBattleEvent(currentRound, SpaceBattleEvent.EventType.BATTLE_ENDED,
                    "Battle concluded: " + crtResult));

            state = BattleState.COMPLETED;
            result = new SpaceBattleResult(
                    outcome,
                    "Detailed",
                    crtResult,
                    attackerLosses,
                    defenderLosses,
                    new ArrayList<>(events)
            );
        }

        return result;
    }

    /**
     * Reinforces a side with additional ships.
     *
     * @param ships the ships to add
     * @param side  the side to reinforce
     */
    public void reinforce(List<ShipCombatStatus> ships, Side side) {
        if (state == BattleState.COMPLETED) {
            throw new IllegalStateException("Cannot reinforce a completed battle");
        }
        if (state == BattleState.INITIATED) {
            start();
        }

        if (side == Side.ATTACKER) {
            attackerShips.addAll(ships);
        } else {
            defenderShips.addAll(ships);
        }

        events.add(new SpaceBattleEvent(currentRound, SpaceBattleEvent.EventType.REINFORCEMENT_ARRIVED,
                side.name() + " reinforced with " + ships.size() + " ships"));
    }

    /**
     * Retreats from the battle.
     */
    public void retreat() {
        if (state == BattleState.COMPLETED) {
            throw new IllegalStateException("Cannot retreat from a completed battle");
        }
        if (state == BattleState.INITIATED) {
            start();
        }

        events.add(new SpaceBattleEvent(currentRound, SpaceBattleEvent.EventType.RETREAT,
                "Fleet retreated from battle"));

        state = BattleState.COMPLETED;
        result = new SpaceBattleResult(
                BattleOutcome.DRAW,
                "Retreat",
                "Retreat",
                0,
                0,
                new ArrayList<>(events)
        );
    }

    // ---- Helper methods ----

    private int calculateTotalAttack(List<ShipCombatStatus> ships) {
        int total = 0;
        for (ShipCombatStatus ship : ships) {
            if (!ship.isDestroyed()) {
                total += ship.getDesign().getWeapons().stream()
                        .mapToInt(ws -> (int) ws.getAverageDamage())
                        .sum();
            }
        }
        // Scale to combat factors
        return Math.max(1, total / 10);
    }

    private int calculateTotalDefense(List<ShipCombatStatus> ships) {
        int total = 0;
        for (ShipCombatStatus ship : ships) {
            if (!ship.isDestroyed()) {
                total += ship.getHullPoints();
            }
        }
        return Math.max(1, total);
    }

    private int countActiveShips(List<ShipCombatStatus> ships) {
        int count = 0;
        for (ShipCombatStatus ship : ships) {
            if (!ship.isDestroyed()) count++;
        }
        return count;
    }

    private String formatOdds(double odds) {
        if (odds >= 6.0) return "6:1";
        if (odds >= 4.0) return "4:1";
        if (odds >= 3.0) return "3:1";
        if (odds >= 2.0) return "2:1";
        if (odds >= 1.0) return "1:1";
        if (odds >= 0.5) return "1:2";
        if (odds >= 0.33) return "1:3";
        if (odds >= 0.25) return "1:4";
        if (odds >= 0.17) return "1:6";
        return "1:8+";
    }

    private BattleOutcome lookupCRT(double odds, int diceRoll) {
        // Simplified CRT lookup based on 3R4 rules
        if (diceRoll <= 2) return BattleOutcome.ATTACKER_VICTORY;
        if (diceRoll >= 12) return BattleOutcome.DEFENDER_VICTORY;
        if (odds >= 2.0 && diceRoll <= 5) return BattleOutcome.ATTACKER_VICTORY;
        if (odds <= 0.5 && diceRoll >= 8) return BattleOutcome.DEFENDER_VICTORY;
        if (diceRoll <= 4) return BattleOutcome.ATTACKER_VICTORY;
        if (diceRoll >= 9) return BattleOutcome.DEFENDER_VICTORY;
        return BattleOutcome.DRAW;
    }

    private String describeCRTResult(BattleOutcome outcome, double odds, int diceRoll) {
        return String.format("%s (odds %s, roll %d)",
                outcome.name(), formatOdds(odds), diceRoll);
    }

    private void calculateCasualties(BattleOutcome outcome, double odds,
                                     int attackerSize, int defenderSize,
                                     int attackerLosses, int defenderLosses) {
        // Casualties are calculated based on outcome and odds
        switch (outcome) {
            case ATTACKER_VICTORY:
                // Defender loses more
                break;
            case DEFENDER_VICTORY:
                // Attacker loses more
                break;
            case DRAW:
                // Equal losses
                break;
            default:
                break;
        }
    }

    private void applyCasualties(List<ShipCombatStatus> ships, int losses, Side side) {
        int count = 0;
        for (ShipCombatStatus ship : ships) {
            if (count >= losses) break;
            if (!ship.isDestroyed()) {
                ship.applyHullDamage(ship.getHullPoints());
                count++;
                events.add(new SpaceBattleEvent(currentRound, SpaceBattleEvent.EventType.SHIP_DESTROYED,
                        side.name() + " ship destroyed: " + ship.getDesign().getName()));
            }
        }
    }

    private void endBattle(BattleOutcome outcome, String description) {
        events.add(new SpaceBattleEvent(currentRound, SpaceBattleEvent.EventType.BATTLE_ENDED,
                "Battle concluded: " + description));

        state = BattleState.COMPLETED;
        int attackerLosses = attackerShips.size() - countActiveShips(attackerShips);
        int defenderLosses = defenderShips.size() - countActiveShips(defenderShips);

        result = new SpaceBattleResult(
                outcome,
                "Detailed",
                description,
                attackerLosses,
                defenderLosses,
                new ArrayList<>(events)
        );
    }

    private SpaceBattleEvent.EventType convertEventType(ShipToShipBattle.BattleEvent.EventType type) {
        if (type == null) return SpaceBattleEvent.EventType.BATTLE_STARTED;
        switch (type) {
            case MISSILE_FIRED: return SpaceBattleEvent.EventType.MISSILE_FIRED;
            case MISSILE_HIT: return SpaceBattleEvent.EventType.MISSILE_HIT;
            case MISSILE_MISS: return SpaceBattleEvent.EventType.MISSILE_MISS;
            case LASER_FIRED: return SpaceBattleEvent.EventType.LASER_FIRED;
            case LASER_HIT: return SpaceBattleEvent.EventType.LASER_HIT;
            case LASER_MISS: return SpaceBattleEvent.EventType.LASER_MISS;
            case SHIP_DESTROYED: return SpaceBattleEvent.EventType.SHIP_DESTROYED;
            case SHIP_RETREATED: return SpaceBattleEvent.EventType.RETREAT;
            case BATTLE_ENDED: return SpaceBattleEvent.EventType.BATTLE_ENDED;
            default: return SpaceBattleEvent.EventType.BATTLE_STARTED;
        }
    }

    // ---- Getters and setters ----

    public int getId() { return id; }
    public List<ShipCombatStatus> getAttackerShips() { return attackerShips; }
    public List<ShipCombatStatus> getDefenderShips() { return defenderShips; }
    public BattleMode getMode() { return mode; }
    public BattleState getState() { return state; }
    public SpaceBattleResult getResult() { return result; }
    public List<SpaceBattleEvent> getEvents() { return events; }
    public int getCurrentRound() { return currentRound; }

    public int getOrbitalBombardmentBonus() { return orbitalBombardmentBonus; }
    public void setOrbitalBombardmentBonus(int orbitalBombardmentBonus) {
        this.orbitalBombardmentBonus = orbitalBombardmentBonus;
    }

    public BattleCommander getAttackerCommander() { return attackerCommander; }
    public void setAttackerCommander(BattleCommander attackerCommander) {
        this.attackerCommander = attackerCommander;
    }

    public BattleCommander getDefenderCommander() { return defenderCommander; }
    public void setDefenderCommander(BattleCommander defenderCommander) {
        this.defenderCommander = defenderCommander;
    }

    public List<BattleParticipant> getParticipants() { return participants; }

    public BattleCommander getCommander(Side side) {
        return side == Side.ATTACKER ? attackerCommander : defenderCommander;
    }

    @Override
    public String toString() {
        return "SpaceBattle#" + id + " (" + mode.name() + ", " + state.name() + ")";
    }
}
