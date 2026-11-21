package com.yisy.modules.doramcu.sfb.service;
import com.yisy.modules.doramcu.sfb.model.SfbShield;
import com.yisy.modules.doramcu.sfb.model.SfbSSD;
import com.yisy.modules.doramcu.sfb.model.SfbCrewStationType;
import com.yisy.modules.doramcu.sfb.model.SfbCharacterAssignment;
import com.yisy.modules.doramcu.sfb.model.SfbEnergyAllocation;

import com.yisy.modules.doramcu.sfb.model.SfbBattleOutcome;
import com.yisy.modules.doramcu.sfb.model.SfbHexPosition;
import com.yisy.modules.doramcu.sfb.model.SfbScenario;
import com.yisy.modules.doramcu.sfb.model.SfbShip;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final SfbCampaignHistory campaignHistory;

    public SfbCampaignEngine() {
        this.shipLoader = new SfbShipLoader();
        this.campaignHistory = new SfbCampaignHistory();
    }

    public SfbCampaignEngine(SfbShipLoader shipLoader) {
        this.shipLoader = shipLoader;
        this.campaignHistory = new SfbCampaignHistory();
    }

    /**
     * Get the campaign history service.
     *
     * @return the campaign history tracker
     */
    public SfbCampaignHistory getCampaignHistory() {
        return campaignHistory;
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
     * Removes destroyed ships from fleets and tracks battle outcome in campaign history.
     *
     * @param battleId The battle ID
     * @param winningSide The winning side (0 or 1), -1 for stalemate
     * @return the recorded battle outcome
     */
    public SfbBattleOutcome completeBattle(String battleId, int winningSide) {
        SfbScenario scenario = activeBattles.remove(battleId);
        if (scenario == null) return null;

        // Count destroyed ships per side
        int shipsLostSide0 = 0;
        int shipsLostSide1 = 0;
        List<String> destroyedShipIds = new ArrayList<>();

        for (SfbShip ship : scenario.getShips()) {
            if (ship.isDestroyed()) {
                destroyedShipIds.add(ship.getId());
                if (ship.getSide() == 0) {
                    shipsLostSide0++;
                } else {
                    shipsLostSide1++;
                }
                // Remove from fleets
                for (Fleet fleet : fleets.values()) {
                    if (fleet.getOwnerSide() == ship.getSide()) {
                        fleet.removeShip(ship.getId());
                    }
                }
                shipyard.remove(ship.getId());
            }
        }

        // Determine result
        SfbBattleOutcome.Result result;
        if (winningSide == -1) {
            result = SfbBattleOutcome.Result.STALEMATE;
        } else if (shipsLostSide0 > 0 && shipsLostSide1 > 0) {
            result = SfbBattleOutcome.Result.MUTUAL_DESTRUCTION;
        } else if (winningSide == 0) {
            result = SfbBattleOutcome.Result.SIDE_0_VICTORY;
        } else {
            result = SfbBattleOutcome.Result.SIDE_1_VICTORY;
        }

        // Get sector position from attacker fleet for terrain tracking
        int sectorRow = 0;
        int sectorCol = 0;
        String terrainEffect = "NONE";
        if (!scenario.getShips().isEmpty()) {
            SfbShip firstShip = scenario.getShips().get(0);
            if (firstShip.getPosition() != null) {
                sectorRow = firstShip.getPosition().getQ();
                sectorCol = firstShip.getPosition().getR();
            }
        }

        // Build and record the battle outcome
        SfbBattleOutcome.Builder outcomeBuilder = SfbBattleOutcome.builder()
                .battleId(battleId)
                .result(result)
                .winningSide(winningSide)
                .sectorPosition(sectorRow, sectorCol)
                .terrainEffect(terrainEffect)
                .shipsLostSide0(shipsLostSide0)
                .shipsLostSide1(shipsLostSide1);

        for (String destroyedId : destroyedShipIds) {
            outcomeBuilder.destroyedShipId(destroyedId);
        }

        SfbBattleOutcome outcome = outcomeBuilder.build();
        campaignHistory.recordBattleOutcome(outcome);
        return outcome;
    }

    /**
     * Get fleet composition summary for a fleet.
     * Returns the breakdown of ship classes and their counts.
     *
     * @param fleetId the fleet ID
     * @return map of ship class to count, or empty map if fleet not found
     */
    public Map<String, Integer> getFleetComposition(String fleetId) {
        Fleet fleet = fleets.get(fleetId);
        if (fleet == null) {
            return Collections.emptyMap();
        }

        return fleet.getShipIds().stream()
                .map(shipyard::get)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        SfbShip::getShipClass,
                        Collectors.summingInt(s -> 1)
                ));
    }

    /**
     * Get fleet composition summary for all fleets of a side.
     *
     * @param side the side (0 or 1)
     * @return map of fleet ID to composition map
     */
    public Map<String, Map<String, Integer>> getAllFleetCompositions(int side) {
        Map<String, Map<String, Integer>> compositions = new HashMap<>();
        for (Fleet fleet : getFleets(side)) {
            compositions.put(fleet.getId(), getFleetComposition(fleet.getId()));
        }
        return compositions;
    }

    /**
     * Transfer ships from one fleet to another.
     *
     * @param sourceFleetId the source fleet
     * @param targetFleetId the target fleet
     * @param shipIds the ship IDs to transfer
     * @return the number of ships transferred
     */
    public int transferShips(String sourceFleetId, String targetFleetId, List<String> shipIds) {
        Fleet source = fleets.get(sourceFleetId);
        Fleet target = fleets.get(targetFleetId);

        if (source == null || target == null) {
            throw new IllegalArgumentException("Fleet not found");
        }

        int transferred = 0;
        for (String shipId : shipIds) {
            if (source.getShipIds().remove(shipId)) {
                target.getShipIds().add(shipId);
                transferred++;
            }
        }
        return transferred;
    }

    /**
     * Split a fleet into two fleets, distributing ships evenly.
     *
     * @param sourceFleetId the fleet to split
     * @param newFleetId the ID for the new fleet
     * @param newFleetName the name for the new fleet
     * @return the newly created fleet
     */
    public Fleet splitFleet(String sourceFleetId, String newFleetId, String newFleetName) {
        Fleet source = fleets.get(sourceFleetId);
        if (source == null) {
            throw new IllegalArgumentException("Source fleet not found: " + sourceFleetId);
        }

        if (source.getShipIds().size() < 2) {
            throw new IllegalStateException("Cannot split fleet with fewer than 2 ships");
        }

        Fleet newFleet = createFleet(newFleetId, newFleetName, source.getOwnerSide());
        newFleet.setSectorPosition(source.getSectorRow(), source.getSectorCol());
        newFleet.setInDockyard(source.isInDockyard());

        // Move half the ships to the new fleet
        int halfSize = source.getShipIds().size() / 2;
        List<String> shipsToMove = new ArrayList<>(source.getShipIds().subList(0, halfSize));
        source.getShipIds().removeAll(shipsToMove);
        newFleet.getShipIds().addAll(shipsToMove);

        return newFleet;
    }

    /**
     * Merge two fleets into one.
     *
     * @param primaryFleetId the fleet to keep
     * @param secondaryFleetId the fleet to merge into the primary
     * @return the merged fleet
     */
    public Fleet mergeFleets(String primaryFleetId, String secondaryFleetId) {
        Fleet primary = fleets.get(primaryFleetId);
        Fleet secondary = fleets.get(secondaryFleetId);

        if (primary == null || secondary == null) {
            throw new IllegalArgumentException("Fleet not found");
        }

        // Move all ships from secondary to primary
        primary.getShipIds().addAll(secondary.getShipIds());

        // Remove the secondary fleet
        fleets.remove(secondaryFleetId);

        return primary;
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
