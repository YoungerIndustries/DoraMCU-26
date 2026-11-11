package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackRequest;
import com.yisy.modules.doramcu.dnd5e.dto.Dnd5eAttackResponse;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombatant;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCombatRepository;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for D&D 5e combat resolution.
 */
@Service
public class Dnd5eCombatService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eCombatService.class);

    // Pattern to parse damage formulas like "2d6+3", "1d8", "3d6+2"
    private static final Pattern DAMAGE_PATTERN = Pattern.compile("(\\d+)d(\\d+)(?:\\+(-?\\d+))?");

    private final Dnd5eCombatRepository combatRepository;

    public Dnd5eCombatService(Dnd5eCombatRepository combatRepository) {
        this.combatRepository = combatRepository;
    }

    /**
     * Create a new combat encounter.
     */
    @Transactional
    public Dnd5eCombat createCombat(String encounterName) {
        Dnd5eCombat combat = new Dnd5eCombat(encounterName);
        return combatRepository.save(combat);
    }

    /**
     * Get a combat by ID.
     */
    public Dnd5eCombat getCombat(Long id) {
        return combatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + id));
    }

    /**
     * Add a combatant to a combat.
     */
    @Transactional
    public Dnd5eCombatant addCombatant(Long combatId, String name, int ac, int maxHp, boolean isCharacter) {
        Dnd5eCombat combat = getCombat(combatId);
        if (!"ACTIVE".equals(combat.getStatus())) {
            throw new IllegalStateException("Combat is not active: " + combatId);
        }
        Dnd5eCombatant combatant = new Dnd5eCombatant(name, ac, maxHp);
        combatant.setCharacter(isCharacter);
        combatant.setCombat(combat);
        combat.getCombatants().add(combatant);
        combatRepository.save(combat);
        log.info("Added combatant {} to combat {}", name, combatId);
        return combatant;
    }

    /**
     * Roll initiative for all combatants.
     */
    @Transactional
    public Dnd5eCombat rollInitiative(Long combatId, Map<String, Integer> dexModifiers) {
        Dnd5eCombat combat = getCombat(combatId);
        if (!"ACTIVE".equals(combat.getStatus())) {
            throw new IllegalStateException("Combat is not active: " + combatId);
        }

        List<Dnd5eCombatant> combatants = combat.getCombatants();
        for (Dnd5eCombatant c : combatants) {
            int dexMod = dexModifiers != null ? dexModifiers.getOrDefault(c.getName(), 0) : 0;
            int initiative = DiceUtil.rollD20() + dexMod;
            c.setInitiative(initiative);
        }

        // Sort by initiative descending
        combatants.sort((a, b) -> b.getInitiative() - a.getInitiative());
        for (int i = 0; i < combatants.size(); i++) {
            combatants.get(i).setTurnOrder(i);
        }

        combat.setCurrentRound(1);
        combatRepository.save(combat);
        log.info("Rolled initiative for combat {}: {} combatants", combatId, combatants.size());
        return combat;
    }

    /**
     * Make an attack roll and resolve damage.
     */
    @Transactional
    public Dnd5eAttackResponse makeAttack(Long combatId, Dnd5eAttackRequest request) {
        Dnd5eCombat combat = getCombat(combatId);
        if (!"ACTIVE".equals(combat.getStatus())) {
            throw new IllegalStateException("Combat is not active: " + combatId);
        }

        Dnd5eCombatant attacker = findCombatant(combat, request.getAttackerName());
        Dnd5eCombatant target = findCombatant(combat, request.getTargetName());

        if (!target.isAlive()) {
            return buildMissResponse(request, target, "Target is already down!");
        }

        Dnd5eAttackResponse response = new Dnd5eAttackResponse();
        response.setAttackerName(request.getAttackerName());
        response.setTargetName(request.getTargetName());
        response.setTargetAC(target.getArmorClass());

        // Roll attack
        int roll;
        if (request.isAdvantage()) {
            roll = Math.max(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else if (request.isDisadvantage()) {
            roll = Math.min(DiceUtil.rollD20(), DiceUtil.rollD20());
        } else {
            roll = DiceUtil.rollD20();
        }

        int total = roll + request.getAttackBonus();
        response.setAttackRoll(roll);
        response.setAttackTotal(total);

        // Critical fail
        if (roll == 1) {
            response.setCriticalFail(true);
            response.setHit(false);
            response.setMessage("Natural 1! Critical miss by " + request.getAttackerName() + "!");
            return response;
        }

        // Critical hit
        boolean critical = roll == 20;
        boolean hit = total >= target.getArmorClass();
        response.setCriticalHit(critical);
        response.setHit(hit || critical);

        if (hit || critical) {
            // Roll damage
            int damage = rollDamage(request.getDamageFormula(), critical);
            response.setDamageDealt(damage);
            response.setDamageType(request.getDamageType() != null ? request.getDamageType() : "slashing");

            // Apply damage
            int newHp = Math.max(0, target.getCurrentHp() - damage);
            target.setCurrentHp(newHp);
            response.setRemainingHP(newHp);
            combatRepository.save(combat);

            if (critical) {
                response.setMessage("Natural 20! CRITICAL HIT! " + request.getAttackerName() + " deals " + damage + " damage to " + request.getTargetName() + "!");
            } else {
                response.setMessage(request.getAttackerName() + " hits " + request.getTargetName() + " for " + damage + " damage!");
            }

            if (newHp <= 0) {
                response.setMessage(response.getMessage() + " " + request.getTargetName() + " is down!");
            }
        } else {
            response.setMessage(request.getAttackerName() + " misses " + request.getTargetName() + "! (rolled " + total + " vs AC " + target.getArmorClass() + ")");
        }

        return response;
    }

    /**
     * Heal a combatant.
     */
    @Transactional
    public Dnd5eCombatant healCombatant(Long combatId, String name, int amount) {
        Dnd5eCombat combat = getCombat(combatId);
        Dnd5eCombatant combatant = findCombatant(combat, name);
        int newHp = Math.min(combatant.getMaxHp(), combatant.getCurrentHp() + amount);
        combatant.setCurrentHp(newHp);
        combatRepository.save(combat);
        log.info("Healed {} for {} HP (now {}/{})", name, amount, combatant.getCurrentHp(), combatant.getMaxHp());
        return combatant;
    }

    /**
     * Apply a condition to a combatant.
     */
    @Transactional
    public Dnd5eCombatant applyCondition(Long combatId, String name, String condition) {
        Dnd5eCombat combat = getCombat(combatId);
        Dnd5eCombatant combatant = findCombatant(combat, name);

        Set<String> conditions = parseConditions(combatant.getConditions());
        conditions.add(condition);
        combatant.setConditions(conditionsToJson(conditions));
        combatRepository.save(combat);
        log.info("Applied {} condition to {}", condition, name);
        return combatant;
    }

    /**
     * Remove a condition from a combatant.
     */
    @Transactional
    public Dnd5eCombatant removeCondition(Long combatId, String name, String condition) {
        Dnd5eCombat combat = getCombat(combatId);
        Dnd5eCombatant combatant = findCombatant(combat, name);

        Set<String> conditions = parseConditions(combatant.getConditions());
        conditions.remove(condition);
        combatant.setConditions(conditionsToJson(conditions));
        combatRepository.save(combat);
        log.info("Removed {} condition from {}", condition, name);
        return combatant;
    }

    /**
     * End the current round and start the next.
     */
    @Transactional
    public Dnd5eCombat endRound(Long combatId) {
        Dnd5eCombat combat = getCombat(combatId);
        combat.setCurrentRound(combat.getCurrentRound() + 1);
        combatRepository.save(combat);
        log.info("Round {} ended in combat {}", combat.getCurrentRound() - 1, combatId);
        return combat;
    }

    /**
     * End the combat.
     */
    @Transactional
    public Dnd5eCombat endCombat(Long combatId, String status) {
        Dnd5eCombat combat = getCombat(combatId);
        combat.setStatus(status);
        combatRepository.save(combat);
        log.info("Combat {} ended with status: {}", combatId, status);
        return combat;
    }

    // ==================== Helper Methods ====================

    private Dnd5eCombatant findCombatant(Dnd5eCombat combat, String name) {
        return combat.getCombatants().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Combatant not found: " + name));
    }

    /**
     * Parse and roll a damage formula like "2d6+3" or "1d8".
     * On critical hit, double the dice count.
     */
    public int rollDamage(String formula, boolean critical) {
        if (formula == null || formula.isEmpty()) {
            return 0;
        }
        Matcher matcher = DAMAGE_PATTERN.matcher(formula.trim());
        if (!matcher.matches()) {
            log.warn("Invalid damage formula: {}", formula);
            return 0;
        }
        int dice = Integer.parseInt(matcher.group(1));
        int sides = Integer.parseInt(matcher.group(2));
        int modifier = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

        if (critical) {
            dice *= 2;
        }

        return Math.max(0, DiceUtil.rollNdX(dice, sides) + modifier);
    }

    private Dnd5eAttackResponse buildMissResponse(Dnd5eAttackRequest request, Dnd5eCombatant target, String message) {
        Dnd5eAttackResponse response = new Dnd5eAttackResponse();
        response.setAttackerName(request.getAttackerName());
        response.setTargetName(request.getTargetName());
        response.setTargetAC(target.getArmorClass());
        response.setHit(false);
        response.setRemainingHP(target.getCurrentHp());
        response.setMessage(message);
        return response;
    }

    private Set<String> parseConditions(String json) {
        if (json == null || json.isEmpty()) return new HashSet<>();
        // Simple JSON array parsing: ["condition1","condition2"]
        String inner = json.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
        inner = inner.trim();
        if (inner.isEmpty()) return new HashSet<>();
        Set<String> set = new HashSet<>();
        for (String part : inner.split(",")) {
            String s = part.trim().replace("\"", "");
            if (!s.isEmpty()) set.add(s);
        }
        return set;
    }

    private String conditionsToJson(Set<String> conditions) {
        if (conditions.isEmpty()) return "[]";
        return "[" + conditions.stream().map(c -> "\"" + c + "\"").reduce((a, b) -> a + "," + b).orElse("") + "]";
    }
}
