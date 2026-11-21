package com.yisy.modules.doramcu.service.naval;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a naval battle between two fleets.
 */
public class NavalBattle {

    public enum BattleOutcome {
        ATTACKER_VICTORY, DEFENDER_VICTORY, DRAW
    }

    public static class BattleRound {
        private final int fleet1DamageDealt;
        private final int fleet2DamageDealt;
        private final int fleet1ShipsLost;
        private final int fleet2ShipsLost;

        public BattleRound(int fleet1DamageDealt, int fleet2DamageDealt, int fleet1ShipsLost, int fleet2ShipsLost) {
            this.fleet1DamageDealt = fleet1DamageDealt;
            this.fleet2DamageDealt = fleet2DamageDealt;
            this.fleet1ShipsLost = fleet1ShipsLost;
            this.fleet2ShipsLost = fleet2ShipsLost;
        }

        public int getFleet1DamageDealt() { return fleet1DamageDealt; }
        public int getFleet2DamageDealt() { return fleet2DamageDealt; }
        public int getFleet1ShipsLost() { return fleet1ShipsLost; }
        public int getFleet2ShipsLost() { return fleet2ShipsLost; }
    }

    private final NavalFleet fleet1;
    private final NavalFleet fleet2;
    private final List<BattleRound> rounds = new ArrayList<>();
    private final List<String> battleLog = new ArrayList<>();
    private boolean active = true;
    private BattleOutcome outcome = BattleOutcome.DRAW;

    public NavalBattle(NavalFleet fleet1, NavalFleet fleet2, BattleOutcome outcome) {
        this.fleet1 = fleet1;
        this.fleet2 = fleet2;
        if (outcome != null) this.outcome = outcome;
    }

    public boolean isActive() { return active; }
    public BattleOutcome getOutcome() { return outcome; }
    public List<BattleRound> getRounds() { return rounds; }
    public List<String> getBattleLog() { return battleLog; }

    public void executeRound(FleetAction action1, FleetAction action2) {
        BattleRound round = new BattleRound(0, 0, 0, 0);
        rounds.add(round);
        active = false;
    }
}
