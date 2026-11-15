package com.yisy.modules.doramcu.gamecore;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Default delegating dice service that forwards to a fallback module's DiceService.
 * Uses D&D 5E's d20 system as the default fallback.
 */
@Component
public class DelegatingDiceService implements DiceService {

    private final GameModuleRegistry gameModuleRegistry;

    public DelegatingDiceService(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
    }

    @Override
    public DiceRollResult rollDSides(int sides) {
        DiceService delegate = findDelegate();
        if (delegate != null) return delegate.rollDSides(sides);
        int result = DiceUtil.rollDSides(sides);
        return new DiceRollResult("d" + sides, List.of(result), result, "Rolled d" + sides + ": " + result);
    }

    @Override
    public DiceRollResult rollNdX(int count, int sides) {
        DiceService delegate = findDelegate();
        if (delegate != null) return delegate.rollNdX(count, sides);
        List<Integer> rolls = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < count; i++) {
            int roll = DiceUtil.rollDSides(sides);
            rolls.add(roll);
            total += roll;
        }
        return new DiceRollResult(count + "d" + sides, rolls, total, "Rolled " + count + "d" + sides + ": " + total);
    }

    @Override
    public DiceRollResult rollFormula(String formula) {
        DiceService delegate = findDelegate();
        if (delegate != null) return delegate.rollFormula(formula);
        int result = DiceUtil.roll2D6();
        return new DiceRollResult(formula, List.of(result), result, "Rolled " + formula + ": " + result);
    }

    @Override
    public DiceRollResult rollWithAdvantage(String formula) {
        DiceService delegate = findDelegate();
        if (delegate != null) return delegate.rollWithAdvantage(formula);
        int r1 = DiceUtil.roll2D6();
        int r2 = DiceUtil.roll2D6();
        int result = Math.max(r1, r2);
        return new DiceRollResult(formula + " (advantage)", List.of(r1, r2, result), result,
                "Rolled " + formula + " with advantage: " + r1 + ", " + r2 + " ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ " + result);
    }

    @Override
    public DiceRollResult rollWithDisadvantage(String formula) {
        DiceService delegate = findDelegate();
        if (delegate != null) return delegate.rollWithDisadvantage(formula);
        int r1 = DiceUtil.roll2D6();
        int r2 = DiceUtil.roll2D6();
        int result = Math.min(r1, r2);
        return new DiceRollResult(formula + " (disadvantage)", List.of(r1, r2, result), result,
                "Rolled " + formula + " with disadvantage: " + r1 + ", " + r2 + " ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ " + result);
    }

    @Override
    public DiceRollResult roll2D6WithModifier(int modifier) {
        DiceService delegate = findDelegate();
        if (delegate != null) return delegate.roll2D6WithModifier(modifier);
        int roll = DiceUtil.roll2D6();
        int total = roll + modifier;
        String modStr = modifier >= 0 ? "+" + modifier : String.valueOf(modifier);
        return new DiceRollResult("2D6" + modStr, List.of(roll, total), total,
                "Rolled 2D6" + modStr + ": " + roll + " ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ " + total);
    }

    private DiceService findDelegate() {
        return gameModuleRegistry.findModuleWithDiceService()
                .map(GameModule::getDiceService)
                .orElse(null);
    }
}
