package com.yisy.modules.doramcu.sfb.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Records the outcome of a completed SFB battle for campaign history tracking.
 * <p>
 * Stores the winning side, destroyed ships, damage statistics, and the sector
 * where the battle took place.
 * </p>
 */
public class SfbBattleOutcome {

    public enum Result {
        /** Side 0 won the battle. */
        SIDE_0_VICTORY,
        /** Side 1 won the battle. */
        SIDE_1_VICTORY,
        /** Both sides destroyed each other. */
        MUTUAL_DESTRUCTION,
        /** Battle ended in a stalemate with no clear winner. */
        STALEMATE
    }

    private final String battleId;
    private final Result result;
    private final int winningSide;
    private final Instant timestamp;
    private final int sectorRow;
    private final int sectorCol;
    private final String terrainEffect;
    private final List<String> destroyedShipIds = new ArrayList<>();
    private final int shipsLostSide0;
    private final int shipsLostSide1;
    private final int damageDealtSide0;
    private final int damageDealtSide1;

    /**
     * Builder for constructing SfbBattleOutcome instances.
     */
    public static class Builder {
        private String battleId;
        private Result result;
        private int winningSide;
        private int sectorRow;
        private int sectorCol;
        private String terrainEffect;
        private final List<String> destroyedShipIds = new ArrayList<>();
        private int shipsLostSide0;
        private int shipsLostSide1;
        private int damageDealtSide0;
        private int damageDealtSide1;

        public Builder battleId(String id) { this.battleId = id; return this; }
        public Builder result(Result result) { this.result = result; return this; }
        public Builder winningSide(int side) { this.winningSide = side; return this; }
        public Builder sectorPosition(int row, int col) { this.sectorRow = row; this.sectorCol = col; return this; }
        public Builder terrainEffect(String effect) { this.terrainEffect = effect; return this; }
        public Builder destroyedShipId(String id) { this.destroyedShipIds.add(id); return this; }
        public Builder shipsLostSide0(int count) { this.shipsLostSide0 = count; return this; }
        public Builder shipsLostSide1(int count) { this.shipsLostSide1 = count; return this; }
        public Builder damageDealtSide0(int damage) { this.damageDealtSide0 = damage; return this; }
        public Builder damageDealtSide1(int damage) { this.damageDealtSide1 = damage; return this; }

        public SfbBattleOutcome build() {
            return new SfbBattleOutcome(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private SfbBattleOutcome(Builder b) {
        this.battleId = b.battleId;
        this.result = b.result;
        this.winningSide = b.winningSide;
        this.timestamp = Instant.now();
        this.sectorRow = b.sectorRow;
        this.sectorCol = b.sectorCol;
        this.terrainEffect = b.terrainEffect;
        this.destroyedShipIds.addAll(b.destroyedShipIds);
        this.shipsLostSide0 = b.shipsLostSide0;
        this.shipsLostSide1 = b.shipsLostSide1;
        this.damageDealtSide0 = b.damageDealtSide0;
        this.damageDealtSide1 = b.damageDealtSide1;
    }

    public String getBattleId() { return battleId; }
    public Result getResult() { return result; }
    public int getWinningSide() { return winningSide; }
    public Instant getTimestamp() { return timestamp; }
    public int getSectorRow() { return sectorRow; }
    public int getSectorCol() { return sectorCol; }
    public String getTerrainEffect() { return terrainEffect; }
    public List<String> getDestroyedShipIds() { return List.copyOf(destroyedShipIds); }
    public int getShipsLostSide0() { return shipsLostSide0; }
    public int getShipsLostSide1() { return shipsLostSide1; }
    public int getDamageDealtSide0() { return damageDealtSide0; }
    public int getDamageDealtSide1() { return damageDealtSide1; }
}
