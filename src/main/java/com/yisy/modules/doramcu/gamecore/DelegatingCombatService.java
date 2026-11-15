package com.yisy.modules.doramcu.gamecore;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default delegating combat service that forwards to a fallback module's CombatService.
 * Uses Traveller's combat as the default fallback.
 */
@Component
public class DelegatingCombatService implements CombatService {

    private final GameModuleRegistry gameModuleRegistry;
    private final AtomicLong combatIdCounter = new AtomicLong(1);
    private final Map<Long, CombatInfo> combats = new ConcurrentHashMap<>();

    public DelegatingCombatService(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
    }

    @Override
    public Long createCombat(String encounterName) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.createCombat(encounterName);
        Long id = combatIdCounter.getAndIncrement();
        CombatInfo info = new CombatInfo();
        info.setId(id);
        info.setEncounterName(encounterName);
        info.setStatus("ACTIVE");
        info.setCurrentRound(0);
        info.setCombatants(new ArrayList<>());
        combats.put(id, info);
        return id;
    }

    @Override
    public CombatInfo getCombat(Long combatId) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.getCombat(combatId);
        return combats.get(combatId);
    }

    @Override
    public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.addCombatant(combatId, name, armorClass, maxHp, isCharacter);
        CombatInfo info = combats.get(combatId);
        if (info == null) return null;
        CombatantInfo ci = new CombatantInfo();
        ci.setName(name);
        ci.setArmorClass(armorClass);
        ci.setCurrentHp(maxHp);
        ci.setMaxHp(maxHp);
        ci.setAlive(true);
        ci.setCharacter(isCharacter);
        info.getCombatants().add(ci);
        return ci;
    }

    @Override
    public CombatInfo rollInitiative(Long combatId, Map<String, Integer> dexModifiers) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.rollInitiative(combatId, dexModifiers);
        return combats.get(combatId);
    }

    @Override
    public AttackResult makeAttack(Long combatId, AttackRequest request) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.makeAttack(combatId, request);
        int attackRoll = DiceUtil.roll2D6();
        int attackTotal = attackRoll + request.getAttackBonus();
        boolean hit = attackTotal >= 8;
        AttackResult result = new AttackResult();
        result.setAttackerName(request.getAttackerName());
        result.setTargetName(request.getTargetName());
        result.setAttackRoll(attackRoll);
        result.setAttackTotal(attackTotal);
        result.setHit(hit);
        if (hit) {
            result.setDamageDealt(DiceUtil.rollDSides(6));
            result.setMessage(request.getAttackerName() + " hits for damage!");
        } else {
            result.setDamageDealt(0);
            result.setMessage(request.getAttackerName() + " misses.");
        }
        return result;
    }

    @Override
    public CombatantInfo healCombatant(Long combatId, String name, int amount) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.healCombatant(combatId, name, amount);
        CombatInfo info = combats.get(combatId);
        if (info == null) return null;
        for (CombatantInfo ci : info.getCombatants()) {
            if (ci.getName().equals(name)) {
                ci.setCurrentHp(Math.min(ci.getMaxHp(), ci.getCurrentHp() + amount));
                return ci;
            }
        }
        return null;
    }

    @Override
    public CombatInfo endCombat(Long combatId, String status) {
        CombatService delegate = findDelegate();
        if (delegate != null) return delegate.endCombat(combatId, status);
        CombatInfo info = combats.get(combatId);
        if (info != null) info.setStatus(status);
        return info;
    }

    private CombatService findDelegate() {
        return gameModuleRegistry.findModuleWithCombatService()
                .map(GameModule::getCombatService)
                .orElse(null);
    }
}
