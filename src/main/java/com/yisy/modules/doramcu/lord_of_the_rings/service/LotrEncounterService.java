package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrEncounter;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrEncounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Lord of the Rings encounter management.
 */
@Service
public class LotrEncounterService {

    private static final Logger log = LoggerFactory.getLogger(LotrEncounterService.class);

    private final LotrEncounterRepository repository;

    public LotrEncounterService(LotrEncounterRepository repository) {
        this.repository = repository;
    }

    public List<LotrEncounter> findAll() {
        return repository.findAll();
    }

    public List<LotrEncounter> findActive() {
        return repository.findByActiveTrue();
    }

    public LotrEncounter findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encounter not found: " + id));
    }

    public List<LotrEncounter> findByDifficulty(String difficulty) {
        return repository.findByDifficulty(difficulty);
    }

    public List<LotrEncounter> findByPartyLevel(int level) {
        return repository.findByPartyLevel(level);
    }

    @Transactional
    public LotrEncounter createEncounter(String name, String description, int partyLevel, int partySize) {
        LotrEncounter encounter = new LotrEncounter();
        encounter.setName(name);
        encounter.setDescription(description);
        encounter.setPartyLevel(partyLevel);
        encounter.setPartySize(partySize);
        encounter.setDifficulty("Medium");
        encounter.setActive(true);
        log.info("Created LOTR encounter: {}", name);
        return repository.save(encounter);
    }

    @Transactional
    public LotrEncounter save(LotrEncounter encounter) {
        return repository.save(encounter);
    }

    @Transactional
    public void resolveEncounter(Long id) {
        LotrEncounter encounter = findById(id);
        encounter.setActive(false);
        repository.save(encounter);
    }
}
