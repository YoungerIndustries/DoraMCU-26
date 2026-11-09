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
        private boolean inDockyard;

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
        public boolean isInDockyard() { return inDockyard; }
        public void setSectorPosition(int row, int col) { this.sectorRow = row; this.sectorCol = col; }
        public void setInDockyard(boolean inDockyard) { this.inDockyard = inDockyard; }
        public void addShip(String shipId) { this.shipIds.add(shipId); }
        public void removeShip(String shipId) { this.shipIds.remove(shipId); }
        public int getShipCount() { return shipIds.size(); }
    }

    private final Map<String, Fleet> fleets = new HashMap<>();
    private final Map<String, SfbScenario> activeBattles = new HashMap<>();
    private final Map<String, SfbShip> shipyard = new HashMap<>(); // Available ships by ID
    private final SfbShipLoader shipLoader;

    public SfbCampaignEngine() {
        this.shipLoader = new SfbShipLoader();
    }

    public SfbCampaignEngine(SfbShipLoader shipLoader) {
        this.shipLoader = shipLoader;
    }

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
     * Build a new ship and add it to a fleet.
     *
     * @param fleetId The fleet to add the ship to
     * @param empire The empire/faction of the ship
     * @param shipClass The ship class name
     * @return The created ship, or null if the fleet was not found
     */
    public SfbShip buildShip(String fleetId, String empire, String shipClass) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet not found: " + fleetId);
        }

        SfbShip ship = shipLoader.createShip(empire, shipClass, UUID.randomUUID().toString().substring(0, 8), fleet.getOwnerSide());
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setSide(fleet.getOwnerSide());

        fleet.addShip(ship.getId());
        shipyard.put(ship.getId(), ship);

        return ship;
    }

    /**
     * Add an existing ship to a fleet.
     */
    public void addShipToFleet(String fleetId, SfbShip ship) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet == null) {
            throw new IllegalArgumentException("Fleet not found: " + fleetId);
        }
        fleet.addShip(ship.getId());
        shipyard.put(ship.getId(), ship);
    }

    /**
     * Remove a ship from a fleet (destroyed or reassigned).
     */
    public void removeShipFromFleet(String fleetId, String shipId) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet != null) {
            fleet.removeShip(shipId);
            shipyard.remove(shipId);
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
        
        // Add ships from both fleets to the scenario
        for (String shipId : attacker.getShipIds()) {
            SfbShip ship = shipyard.get(shipId);
            if (ship != null) {
                scenario.addShip(ship);
            }
        }
        for (String shipId : defender.getShipIds()) {
            SfbShip ship = shipyard.get(shipId);
            if (ship != null) {
                scenario.addShip(ship);
            }
        }

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
     * Complete a battle and process results.
     * Removes destroyed ships from fleets and tracks battle outcome.
     *
     * @param battleId The battle ID
     * @param winningSide The winning side (0 or 1), -1 for stalemate
     */
    public void completeBattle(String battleId, int winningSide) {
        SfbScenario scenario = activeBattles.remove(battleId);
        if (scenario == null) return;

        // Remove destroyed ships from fleets
        for (SfbShip ship : scenario.getShips()) {
            if (ship.isDestroyed()) {
                for (Fleet fleet : fleets.values()) {
                    if (fleet.getOwnerSide() == ship.getSide()) {
                        fleet.removeShip(ship.getId());
                    }
                }
                shipyard.remove(ship.getId());
            }
        }
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
     * Get a fleet by ID.
     */
    public Fleet getFleet(String fleetId) {
        return fleets.get(fleetId);
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
     * Restores shield boxes and repairs non-crippled systems based on DamCon rating.
     *
     * @param fleetId The fleet to repair
     * @param dockyardBonus Additional repair bonus if in a dockyard (0-5)
     */
    public void repairFleet(String fleetId, int dockyardBonus) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet == null) return;

        for (String shipId : fleet.getShipIds()) {
            SfbShip ship = shipyard.get(shipId);
            if (ship == null || ship.isDestroyed()) continue;

            // Repair shields: restore boxes based on DamCon + dockyard bonus
            int repairAmount = ship.getSsd().getDamConRating() + dockyardBonus;
            for (SfbShield shield : ship.getShields()) {
                if (shield != null) {
                    shield.repair(repairAmount);
                }
            }

            // Repair excess damage
            if (ship.getSsd().getExcessDamage() > 0) {
                int repaired = Math.min(ship.getSsd().getExcessDamage(), repairAmount * 2);
                ship.getSsd().setExcessDamage(ship.getSsd().getExcessDamage() - repaired);
            }

            // Uncripple if excess damage is cleared
            if (ship.isCrippled() && ship.getSsd().getExcessDamage() <= 0) {
                ship.setCrippled(false);
            }
        }
    }

    /**
     * Repair ships in a fleet with no dockyard bonus.
     */
    public void repairFleet(String fleetId) {
        repairFleet(fleetId, 0);
    }

    /**
     * Get all available ships in the shipyard.
     */
    public Map<String, SfbShip> getShipyard() {
        return Collections.unmodifiableMap(shipyard);
    }

    /**
     * Get the number of active battles.
     */
    public int getActiveBattleCount() {
        return activeBattles.size();
    }

    /**
     * Get the total number of fleets.
     */
    public int getFleetCount() {
        return fleets.size();
    }
}
