package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.E26Monster;
import com.yisy.modules.doramcu.earth2026.repository.E26MonsterRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Service for Earth 2026 Monster encounters.
 */
@Service
public class E26MonsterService {

    private final E26MonsterRepository repository;

    @Autowired
    public E26MonsterService(E26MonsterRepository repository) {
        this.repository = repository;
    }

    /**
     * Find all monsters.
     */
    public List<E26Monster> findAll() {
        return repository.findAll();
    }

    /**
     * Find monster by ID.
     */
    public Optional<E26Monster> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Find monsters by threat level.
     */
    public List<E26Monster> findByThreatLevel(int threatLevel) {
        return repository.findByThreatLevel(threatLevel);
    }

    /**
     * Find monsters by type.
     */
    public List<E26Monster> findByType(String type) {
        return repository.findByType(type);
    }

    /**
     * Find monsters by size.
     */
    public List<E26Monster> findBySize(String size) {
        return repository.findBySize(size);
    }

    /**
     * Find monsters by threat level and type.
     */
    public List<E26Monster> findByThreatLevelAndType(int threatLevel, String type) {
        return repository.findByThreatLevelAndType(threatLevel, type);
    }

    /**
     * Find monsters by threat level range.
     */
    public List<E26Monster> findByThreatLevelRange(int minTL, int maxTL) {
        return repository.findByThreatLevelRange(minTL, maxTL);
    }

    /**
     * Find monsters by subtype.
     */
    public List<E26Monster> findBySubtype(String subtype) {
        return repository.findBySubtype(subtype);
    }

    /**
     * Save a monster.
     */
    public E26Monster save(E26Monster monster) {
        return repository.save(monster);
    }
}
