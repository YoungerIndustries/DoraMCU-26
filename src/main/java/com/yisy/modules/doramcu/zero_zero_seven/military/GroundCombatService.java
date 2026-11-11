package com.yisy.modules.doramcu.zero_zero_seven.military;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondEmpireRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service for ground combat simulation.
 * <p>
 * Wraps combat logic for overt military conflicts between empires.
 * Uses military strength and territory resources to determine outcomes.
 * </p>
 */
@Service
public class GroundCombatService {

    private final BondEmpireRepository empireRepository;
    private final Random random;

    public GroundCombatService(BondEmpireRepository empireRepository) {
        this.empireRepository = empireRepository;
        this.random = new Random();
    }

    /**
     * Simulate a ground battle between two empires.
     *
     * @param attackerId the attacking empire
     * @param defenderId the defending empire
     * @return positive if attacker wins, negative if defender wins, 0 for draw
     */
    public int simulateBattle(Long attackerId, Long defenderId) {
        BondEmpire attacker = empireRepository.findById(attackerId)
                .orElseThrow(() -> new IllegalArgumentException("Attacker not found: " + attackerId));
        BondEmpire defender = empireRepository.findById(defenderId)
                .orElseThrow(() -> new IllegalArgumentException("Defender not found: " + defenderId));

        int attackerPower = attacker.getMilitaryStrength() * 10 + random.nextInt(20);
        int defenderPower = defender.getMilitaryStrength() * 10 + random.nextInt(20) + 5; // Defender bonus

        return attackerPower - defenderPower;
    }

    /**
     * Calculate battle odds for an empire.
     *
     * @param empireId the empire
     * @return battle odds percentage
     */
    public int getBattleOdds(Long empireId) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));
        return empire.getMilitaryStrength() * 10;
    }
}
