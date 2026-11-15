package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.SfbBattleOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Maintains the history of SFB campaign events, primarily battle outcomes.
 * <p>
 * Provides query methods for retrieving battle history by side, sector,
 * and result type. This service integrates with {@link SfbCampaignEngine}
 * to record battle outcomes when battles are completed.
 * </p>
 */
public class SfbCampaignHistory {

    private final List<SfbBattleOutcome> battleOutcomes = new ArrayList<>();
    private final Map<String, SfbBattleOutcome> outcomesByBattleId = new ConcurrentHashMap<>();

    /**
     * Record a battle outcome in the campaign history.
     *
     * @param outcome the battle outcome to record
     */
    public void recordBattleOutcome(SfbBattleOutcome outcome) {
        battleOutcomes.add(outcome);
        outcomesByBattleId.put(outcome.getBattleId(), outcome);
    }

    /**
     * Get all recorded battle outcomes in chronological order.
     *
     * @return list of all battle outcomes
     */
    public List<SfbBattleOutcome> getAllOutcomes() {
        return List.copyOf(battleOutcomes);
    }

    /**
     * Get battle outcome by battle ID.
     *
     * @param battleId the battle ID
     * @return the battle outcome, or null if not found
     */
    public SfbBattleOutcome getOutcome(String battleId) {
        return outcomesByBattleId.get(battleId);
    }

    /**
     * Get battle outcomes for a specific side.
     *
     * @param side the side (0 or 1)
     * @return list of outcomes where the specified side participated
     */
    public List<SfbBattleOutcome> getOutcomesForSide(int side) {
        return battleOutcomes.stream()
                .filter(o -> o.getWinningSide() == side || o.getResult() == SfbBattleOutcome.Result.MUTUAL_DESTRUCTION
                        || o.getResult() == SfbBattleOutcome.Result.STALEMATE)
                .collect(Collectors.toList());
    }

    /**
     * Get battle outcomes for a specific sector.
     *
     * @param row the sector row
     * @param col the sector column
     * @return list of outcomes in the specified sector
     */
    public List<SfbBattleOutcome> getOutcomesInSector(int row, int col) {
        return battleOutcomes.stream()
                .filter(o -> o.getSectorRow() == row && o.getSectorCol() == col)
                .collect(Collectors.toList());
    }

    /**
     * Get battle outcomes filtered by result type.
     *
     * @param result the result type to filter by
     * @return list of outcomes with the specified result
     */
    public List<SfbBattleOutcome> getOutcomesByResult(SfbBattleOutcome.Result result) {
        return battleOutcomes.stream()
                .filter(o -> o.getResult() == result)
                .collect(Collectors.toList());
    }

    /**
     * Get the total number of recorded battle outcomes.
     *
     * @return the number of recorded outcomes
     */
    public int getOutcomeCount() {
        return battleOutcomes.size();
    }

    /**
     * Get summary statistics for a specific side.
     *
     * @param side the side (0 or 1)
     * @return map containing wins, losses, ships lost, and damage dealt
     */
    public Map<String, Integer> getSideStatistics(int side) {
        int wins = 0;
        int losses = 0;
        int totalShipsLost = 0;
        int totalDamageDealt = 0;

        for (SfbBattleOutcome outcome : battleOutcomes) {
            if (outcome.getResult() == SfbBattleOutcome.Result.MUTUAL_DESTRUCTION
                    || outcome.getResult() == SfbBattleOutcome.Result.STALEMATE) {
                // Count mutual destruction and stalemate as partial for both sides
                if (side == 0) {
                    totalShipsLost += outcome.getShipsLostSide0();
                    totalDamageDealt += outcome.getDamageDealtSide0();
                } else {
                    totalShipsLost += outcome.getShipsLostSide1();
                    totalDamageDealt += outcome.getDamageDealtSide1();
                }
                continue;
            }

            if (outcome.getWinningSide() == side) {
                wins++;
                if (side == 0) {
                    totalShipsLost += outcome.getShipsLostSide0();
                    totalDamageDealt += outcome.getDamageDealtSide0();
                } else {
                    totalShipsLost += outcome.getShipsLostSide1();
                    totalDamageDealt += outcome.getDamageDealtSide1();
                }
            } else {
                losses++;
                if (side == 0) {
                    totalShipsLost += outcome.getShipsLostSide0();
                    totalDamageDealt += outcome.getDamageDealtSide0();
                } else {
                    totalShipsLost += outcome.getShipsLostSide1();
                    totalDamageDealt += outcome.getDamageDealtSide1();
                }
            }
        }

        return Map.of(
                "wins", wins,
                "losses", losses,
                "totalShipsLost", totalShipsLost,
                "totalDamageDealt", totalDamageDealt
        );
    }

    /**
     * Clear all recorded battle outcomes.
     */
    public void clear() {
        battleOutcomes.clear();
        outcomesByBattleId.clear();
    }
}
