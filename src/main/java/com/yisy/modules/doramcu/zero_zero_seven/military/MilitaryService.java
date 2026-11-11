package com.yisy.modules.doramcu.zero_zero_seven.military;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondEmpireRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing military units and strength.
 * <p>
 * Handles military strength calculations and army management.
 * </p>
 */
@Service
public class MilitaryService {

    private final BondEmpireRepository empireRepository;

    public MilitaryService(BondEmpireRepository empireRepository) {
        this.empireRepository = empireRepository;
    }

    /**
     * Get military strength for an empire.
     *
     * @param empireId the empire
     * @return military strength
     */
    public int getMilitaryStrength(Long empireId) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));
        return empire.getMilitaryStrength();
    }

    /**
     * Increase military strength.
     *
     * @param empireId the empire
     * @param amount amount to increase
     */
    public void increaseMilitaryStrength(Long empireId, int amount) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));
        empire.setMilitaryStrength(Math.min(10, empire.getMilitaryStrength() + amount));
        empireRepository.save(empire);
    }

    /**
     * Decrease military strength.
     *
     * @param empireId the empire
     * @param amount amount to decrease
     */
    public void decreaseMilitaryStrength(Long empireId, int amount) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));
        empire.setMilitaryStrength(Math.max(1, empire.getMilitaryStrength() - amount));
        empireRepository.save(empire);
    }
}
