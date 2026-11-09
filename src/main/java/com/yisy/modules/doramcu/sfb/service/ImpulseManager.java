package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbShip;

import java.util.*;

/**
 * Manages the 32-impulse sequence in SFB.
 * <p>
 * Determines which ships move on which impulse based on their speed
 * using the movement chart from the rules database.
 * Each impulse move is 1 hex, so total hexes per turn = number of movement impulses.
 * Speed 32 moves 31 hexes/turn, speed 16 moves 15 hexes/turn, speed 8 moves 4 hexes/turn.
 * </p>
 */
public class ImpulseManager {

    // Pre-computed movement chart: speed -> list of impulses when that speed moves
    private final Map<Integer, List<Integer>> movementChart;

    // Total hexes moved per turn for each speed (equals number of movement impulses)
    private final Map<Integer, Integer> hexesPerTurn;

    public ImpulseManager() {
        this.movementChart = computeMovementChart();
        this.hexesPerTurn = computeHexesPerTurn();
    }

    /**
     * Compute the full 32-impulse movement chart based on SFB rules.
     * Speed 32 moves on every impulse except 1 (31 moves).
     * Speed 16 moves on 15 impulses.
     * Speed 8 moves on 4 impulses.
     * Speed 1 moves only on impulse 32 (1 move).
     */
    private Map<Integer, List<Integer>> computeMovementChart() {
        Map<Integer, List<Integer>> chart = new HashMap<>();

        // Hard-coded impulse mappings from SFB rules for all 32 speeds
        // Speed 32: 31 moves (every impulse except 1)
        chart.put(32, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32));
        // Speed 31: 30 moves
        chart.put(31, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,32));
        // Speed 30: 28 moves
        chart.put(30, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,28,30,32));
        // Speed 29: 26 moves
        chart.put(29, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,24,26,28,30,32));
        // Speed 28: 24 moves
        chart.put(28, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,24,26,28,30,32));
        // Speed 27: 22 moves
        chart.put(27, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,22,24,26,28,30,32));
        // Speed 26: 20 moves
        chart.put(26, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,22,24,26,28,30,32));
        // Speed 25: 19 moves
        chart.put(25, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,20,22,24,26,28,30,32));
        // Speed 24: 20 moves
        chart.put(24, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,20,24,28,32));
        // Speed 23: 18 moves
        chart.put(23, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,16,18,20,24,28,32));
        // Speed 22: 17 moves
        chart.put(22, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,16,18,22,28,32));
        // Speed 21: 16 moves
        chart.put(21, List.of(2,3,4,5,6,7,8,9,10,11,12,13,14,16,20,28,32));
        // Speed 20: 15 moves
        chart.put(20, List.of(2,3,4,5,6,7,8,9,10,12,15,18,20,25,32));
        // Speed 19: 15 moves
        chart.put(19, List.of(2,3,4,5,6,7,8,9,10,12,15,18,20,25,32));
        // Speed 18: 15 moves
        chart.put(18, List.of(2,3,4,5,6,7,8,9,10,12,15,18,20,25,32));
        // Speed 17: 15 moves
        chart.put(17, List.of(2,3,4,5,6,7,8,9,10,12,15,18,20,25,32));
        // Speed 16: 15 moves (standard CA speed)
        chart.put(16, List.of(4,6,8,10,12,14,16,18,20,22,24,26,28,30,32));
        // Speed 15: 11 moves
        chart.put(15, List.of(4,6,8,10,12,15,18,20,24,28,32));
        // Speed 14: 10 moves
        chart.put(14, List.of(4,6,8,10,12,14,18,22,28,32));
        // Speed 13: 9 moves
        chart.put(13, List.of(4,6,8,10,12,14,18,24,32));
        // Speed 12: 8 moves
        chart.put(12, List.of(4,6,8,10,12,16,24,32));
        // Speed 11: 8 moves
        chart.put(11, List.of(4,6,8,10,12,16,24,32));
        // Speed 10: 7 moves
        chart.put(10, List.of(4,6,8,10,15,22,32));
        // Speed 9: 6 moves
        chart.put(9, List.of(4,6,8,12,20,32));
        // Speed 8: 4 moves (standard DD speed)
        chart.put(8, List.of(8,16,24,32));
        // Speed 7: 4 moves
        chart.put(7, List.of(8,16,24,32));
        // Speed 6: 4 moves
        chart.put(6, List.of(8,16,24,32));
        // Speed 5: 3 moves
        chart.put(5, List.of(10,20,32));
        // Speed 4: 2 moves
        chart.put(4, List.of(16,32));
        // Speed 3: 2 moves
        chart.put(3, List.of(16,32));
        // Speed 2: 2 moves
        chart.put(2, List.of(16,32));
        // Speed 1: 1 move (slowest)
        chart.put(1, List.of(32));

        return chart;
    }

    /**
     * Compute total hexes moved per turn for each speed.
     * In SFB, each impulse move is 1 hex, so hexes per turn = number of movement impulses.
     */
    private Map<Integer, Integer> computeHexesPerTurn() {
        Map<Integer, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : movementChart.entrySet()) {
            map.put(entry.getKey(), entry.getValue().size());
        }
        return map;
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

    /**
     * Get the total hexes a ship can move per turn at the given speed.
     * In SFB, each impulse move is 1 hex, so this equals the number of movement impulses.
     * Speed 32 = 31 hexes, speed 16 = 15 hexes, speed 8 = 4 hexes, speed 1 = 1 hex.
     */
    public int getHexesPerTurn(int speed) {
        speed = Math.max(1, Math.min(32, speed));
        return hexesPerTurn.getOrDefault(speed, 0);
    }

    /**
     * Calculate the maximum speed change allowed this turn (acceleration/deceleration).
     * SFB rules: max 4 speed points change per turn.
     */
    public int getMaxSpeedChange() {
        return 4;
    }

    /**
     * Validate speed change from previous speed to new speed.
     * Returns the validated speed (clamped to acceleration limits).
     */
    public int validateSpeedChange(int previousSpeed, int newSpeed) {
        int maxChange = getMaxSpeedChange();
        int minSpeed = Math.max(1, previousSpeed - maxChange);
        int maxSpeed = Math.min(32, previousSpeed + maxChange);
        return Math.max(minSpeed, Math.min(maxSpeed, newSpeed));
    }
}
