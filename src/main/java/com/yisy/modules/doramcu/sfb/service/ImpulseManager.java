package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbShip;

import java.util.*;

/**
 * Manages the 32-impulse sequence in SFB.
 * <p>
 * Determines which ships move on which impulse based on their speed
 * using the movement chart from the rules database.
 * </p>
 */
public class ImpulseManager {

    // Pre-computed movement chart: speed -> list of impulses when that speed moves
    private final Map<Integer, List<Integer>> movementChart;

    public ImpulseManager() {
        this.movementChart = computeMovementChart();
    }

    /**
     * Compute the full 32-impulse movement chart.
     * Speed 32 moves on every impulse except 1.
     * Speed 1 moves only on impulse 32.
     */
    private Map<Integer, List<Integer>> computeMovementChart() {
        Map<Integer, List<Integer>> chart = new HashMap<>();

        for (int speed = 1; speed <= 32; speed++) {
            List<Integer> impulses = new ArrayList<>();

            if (speed == 32) {
                // Speed 32 moves on every impulse except 1
                for (int i = 2; i <= 32; i++) {
                    impulses.add(i);
                }
            } else if (speed >= 16) {
                // Speeds 16-31: move on even impulses starting from (32-speed)*2
                int step = Math.max(1, (32 - speed) / 2 + 1);
                for (int i = step; i <= 32; i += step) {
                    if (i > 1) impulses.add(i);
                }
                // Ensure impulse 32 is always included
                if (!impulses.contains(32)) impulses.add(32);
            } else {
                // Speeds 1-15: move on specific impulses
                int movesPerTurn = (32 / speed) + 1;
                int step = 32 / movesPerTurn;
                for (int i = step; i <= 32; i += step) {
                    impulses.add(i);
                }
                if (!impulses.contains(32)) impulses.add(32);
            }

            chart.put(speed, impulses);
        }

        return chart;
    }

    /**
     * Get the list of impulses when a ship at the given speed can move.
     */
    public List<Integer> getMovementImpulses(int speed) {
        speed = Math.max(1, Math.min(32, speed));
        return Collections.unmodifiableList(movementChart.getOrDefault(speed, Collections.emptyList()));
    }

    /**
     * Check if a ship at the given speed moves on the current impulse.
     */
    public boolean movesOnImpulse(int speed, int impulse) {
        return getMovementImpulses(speed).contains(impulse);
    }

    /**
     * Get all ships that move on the given impulse.
     */
    public List<SfbShip> getShipsMovingOnImpulse(List<SfbShip> ships, int impulse) {
        List<SfbShip> moving = new ArrayList<>();
        for (SfbShip ship : ships) {
            if (ship.isInCombat() && movesOnImpulse(ship.getSpeed(), impulse)) {
                moving.add(ship);
            }
        }
        return moving;
    }

    /**
     * Get the number of moves per turn for a given speed.
     */
    public int getMovesPerTurn(int speed) {
        return getMovementImpulses(speed).size();
    }
}
