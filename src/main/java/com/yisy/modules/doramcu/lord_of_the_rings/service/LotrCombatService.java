package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombatant;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCombatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Lord of the Rings combat encounters.
 */
@Service
public class LotrCombatService {

    private static final Logger log = LoggerFactory.getLogger(LotrCombatService.class);

    private final LotrCombatRepository repository;

    public LotrCombatService(LotrCombatRepository repository) {
        this.repository = repository;
    }

    public List<LotrCombat> findAll() {
        return repository.findAll();
    }

    public List<LotrCombat> findActive() {
        return repository.findByStatus("ACTIVE");
    }

    public LotrCombat findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Combat not found: " + id));
    }

    @Transactional
    public LotrCombat createCombat(String encounterName, String location) {
        LotrCombat combat = new LotrCombat(encounterName);
        combat.setLocation(location);
        log.info("Created LOTR combat: {}", encounterName);
        return repository.save(combat);
    }

    @Transactional
    public LotrCombat save(LotrCombat combat) {
        return repository.save(combat);
    }

    @Transactional
    public void endCombat(Long id) {
        LotrCombat combat = findById(id);
        combat.setStatus("ENDED");
        repository.save(combat);
    }

    @Transactional
    public void nextRound(Long id) {
        LotrCombat combat = findById(id);
        combat.setCurrentRound(combat.getCurrentRound() + 1);
        repository.save(combat);
    }
}
