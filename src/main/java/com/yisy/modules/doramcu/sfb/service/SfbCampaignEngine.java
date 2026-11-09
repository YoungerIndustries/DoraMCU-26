package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;

import java.util.*;

/**
 * Strategic campaign management for SFB.
 * <p>
 * Handles fleet deployment, strategic map management, shipbuilding,
 * repairs, and Zone of Control.
 * </p>
 */
public class SfbCampaignEngine {

    /**
     * Represents a fleet in the campaign.
     */
    public static class Fleet {
        private final String id;
        private final String name;
        private final int ownerSide;
        private final List<String> shipIds = new ArrayList<>();
        private int sectorRow;
        private int sectorCol;

        public Fleet(String id, String name, int ownerSide) {
            this.id = id;
            this.name = name;
            this.ownerSide = ownerSide;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getOwnerSide() { return ownerSide; }
        public List<String> getShipIds() { return shipIds; }
        public int getSectorRow() { return sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorPosition(int row, int col) { this.sectorRow = row; this.sectorCol = col; }
        public void addShip(String shipId) { this.shipIds.add(shipId); }
    }

    private final Map<String, Fleet> fleets = new HashMap<>();
    private final Map<String, SfbScenario> activeBattles = new HashMap<>();

    /**
     * Create a new fleet.
     */
    public Fleet createFleet(String id, String name, int ownerSide) {
        Fleet fleet = new Fleet(id, name, ownerSide);
        fleets.put(id, fleet);
        return fleet;
    }

    /**
     * Deploy a fleet to a sector.
     */
    public void deployFleet(String fleetId, int sectorRow, int sectorCol) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet != null) {
            fleet.setSectorPosition(sectorRow, sectorCol);
        }
    }

    /**
     * Start a tactical battle between fleets.
     */
    public SfbScenario startBattle(String battleId, String attackerFleetId, String defenderFleetId) {
        Fleet attacker = fleets.get(attackerFleetId);
        Fleet defender = fleets.get(defenderFleetId);

        if (attacker == null || defender == null) {
            throw new IllegalArgumentException("Fleet not found");
        }

        SfbScenario scenario = new SfbScenario(battleId);
        activeBattles.put(battleId, scenario);
        return scenario;
    }

    /**
     * Get active battle.
     */
    public SfbScenario getActiveBattle(String battleId) {
        return activeBattles.get(battleId);
    }

    /**
     * Complete a battle and remove it from active battles.
     */
    public void completeBattle(String battleId, int winningSide) {
        activeBattles.remove(battleId);
    }

    /**
     * Get all fleets for a side.
     */
    public List<Fleet> getFleets(int side) {
        return fleets.values().stream()
                .filter(f -> f.getOwnerSide() == side)
                .toList();
    }

    /**
     * Check Zone of Control: fleets in adjacent sectors can engage.
     */
    public boolean canEngage(String fleet1Id, String fleet2Id) {
        Fleet f1 = fleets.get(fleet1Id);
        Fleet f2 = fleets.get(fleet2Id);

        if (f1 == null || f2 == null) return false;

        int rowDiff = Math.abs(f1.getSectorRow() - f2.getSectorRow());
        int colDiff = Math.abs(f1.getSectorCol() - f2.getSectorCol());

        return rowDiff <= 1 && colDiff <= 1; // Adjacent sectors
    }

    /**
     * Repair ships in a fleet (between battles).
     */
    public void repairFleet(String fleetId) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet == null) return;

        // In production, this would repair the actual ships
        // For now, just mark the fleet as repaired
    }
}
