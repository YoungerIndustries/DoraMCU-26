package com.yisy.modules.doramcu.warhammer40k.service;

import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RulesEngine for Warhammer 40K using d10-based resolution.
 * <p>
 * Task attempts: Roll d10, must meet or exceed the skill value.
 * Combat: Roll d10 to hit (vs BS/WS), roll d10 to wound (vs toughness),
 * target rolls d10 + armor save.
 * </p>
 */
@Service
public class Warhammer40kRulesEngine implements RulesEngine {

    @Override
    public GameSystem getGameSystem() {
        return GameSystem.WARHAMMER_40K;
    }

    @Override
    public int rollDice(int sides, int count) {
        return DiceUtil.rollNdX(count, sides);
    }

    @Override
    public RulesEngine.TaskResult taskAttempt(int difficulty, int skillLevel) {
        int roll = DiceUtil.rollDSides(10);
        int threshold = Math.max(2, Math.min(10, difficulty + 1));
        boolean success = roll >= threshold;
        boolean critical = roll == 10;
        boolean fumble = roll == 1;

        if (fumble) {
            return new Wh40kTaskResult(false, roll, threshold, "Fumble - catastrophic failure");
        }
        if (critical) {
            return new Wh40kTaskResult(true, roll, threshold, "Critical success - exceptional performance");
        }
        return new Wh40kTaskResult(success, roll, threshold,
                success ? "Success" : "Failure");
    }

    @Override
    public RulesEngine.CombatResult combatRound(Map<String, Object> attacker, Map<String, Object> defender) {
        int attacks = getInt(attacker, "attacks", 1);
        int bs = getInt(attacker, "bs", 4);
        int strength = getInt(attacker, "strength", 4);
        int toughness = getInt(defender, "toughness", 4);
        int armor = getInt(defender, "armor", 0);

        int hits = 0;
        int wounds = 0;
        int saved = 0;

        for (int i = 0; i < attacks; i++) {
            int toHit = DiceUtil.rollDSides(10);
            if (toHit >= bs) {
                hits++;
                int toWound = DiceUtil.rollDSides(10);
                int woundThreshold = Math.max(2, Math.min(10, 11 - Math.abs(strength - toughness)));
                if (toWound >= woundThreshold) {
                    wounds++;
                    int saveRoll = DiceUtil.rollDSides(10) + armor;
                    if (saveRoll < 8) {
                        saved++;
                    }
                }
            }
        }

        int damage = Math.max(0, wounds - saved);
        return new Wh40kCombatResult(
                damage > 0,
                damage,
                hits,
                wounds,
                saved,
                attacks
        );
    }

    @Override
    public List<RulesEntry> queryRules(String category) {
        List<RulesEntry> rules = new ArrayList<>();
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.WARHAMMER_40K);
        entry.setCategory(category);
        entry.setSource("Warhammer 40K Rulebook");
        entry.setChapter(category);
        switch (category.toLowerCase()) {
            case "combat":
                entry.setTitle("d10 Combat System");
                entry.setText("Roll d10 >= BS/WS to hit, roll d10 to wound, target rolls d10 + armor save. Roll 10 = critical, roll 1 = fumble.");
                break;
            case "task":
                entry.setTitle("Task Resolution");
                entry.setText("Roll d10 >= difficulty threshold. Roll 10 = critical success, roll 1 = fumble.");
                break;
            default:
                entry.setTitle("Unknown Category");
                entry.setText("No rules found for category: " + category);
        }
        rules.add(entry);
        return rules;
    }

    @Override
    public List<RulesEntry> queryRules(String category, GameSystem gameSystem) {
        return queryRules(category);
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || !map.containsKey(key)) return defaultValue;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    private record Wh40kTaskResult(boolean success, int roll, int threshold, String message)
            implements RulesEngine.TaskResult {
        @Override public boolean isSuccess() { return success; }
        @Override public int getRoll() { return roll; }
        @Override public int getTarget() { return threshold; }
        @Override public String getDescription() { return message; }
    }

    private record Wh40kCombatResult(boolean hit, int damage, int hits, int wounds, int saved, int attacks)
            implements RulesEngine.CombatResult {
        @Override public boolean isHit() { return hit; }
        @Override public int getDamage() { return damage; }
        @Override public String getDescription() {
            return String.format("%d hits, %d wounds, %d saved, %d damage", hits, wounds, saved, damage);
        }
    }
}
