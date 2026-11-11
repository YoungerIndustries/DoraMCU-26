package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * AI opponent for air battles.
 */
public class AirBattleAiService {
    public enum Difficulty { EASY, MEDIUM, HARD }

    private final Random random = new Random();
    private final Difficulty difficulty;

    public AirBattleAiService() { this(Difficulty.MEDIUM); }
    public AirBattleAiService(Difficulty difficulty) { this.difficulty = difficulty; }

    public boolean decideAttack(int aiAttack, int enemyDefense) {
        double winChance = calculateWinChance(aiAttack, enemyDefense);
        double threshold = difficulty == Difficulty.EASY ? 0.4 : difficulty == Difficulty.MEDIUM ? 0.5 : 0.6;
        return winChance >= threshold;
    }

    public boolean decideReinforce(int currentAttack, int enemyDefense, int reserves) {
        if (reserves == 0) return false;
        double winChance = calculateWinChance(currentAttack, enemyDefense);
        return winChance < 0.5 && random.nextDouble() < (difficulty == Difficulty.HARD ? 0.8 : 0.5);
    }

    public boolean decideRetreat(int currentAttack, int enemyDefense) {
        double winChance = calculateWinChance(currentAttack, enemyDefense);
        return winChance < (difficulty == Difficulty.EASY ? 0.2 : difficulty == Difficulty.MEDIUM ? 0.15 : 0.1);
    }

    private double calculateWinChance(int attack, int defense) {
        if (defense == 0) return 1.0;
        double ratio = (double) attack / defense;
        return Math.min(1.0, ratio / 3.0);
    }
}
