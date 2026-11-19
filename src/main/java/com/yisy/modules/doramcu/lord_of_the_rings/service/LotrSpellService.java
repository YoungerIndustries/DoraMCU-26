package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpell;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrSpellRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Lord of the Rings spell management.
 */
@Service
public class LotrSpellService {

    private static final Logger log = LoggerFactory.getLogger(LotrSpellService.class);

    private final LotrSpellRepository repository;

    public LotrSpellService(LotrSpellRepository repository) {
        this.repository = repository;
    }

    public List<LotrSpell> findAll() {
        return repository.findAll();
    }

    public LotrSpell findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Spell not found: " + id));
    }

    public List<LotrSpell> findByLevel(int level) {
        return repository.findBySpellLevel(level);
    }

    public List<LotrSpell> findBySchool(String school) {
        return repository.findBySpellSchool(school);
    }

    @Transactional
    public LotrSpell createSpell(String name, String description, int spellLevel,
                                   String spellSchool, String range, String duration) {
        LotrSpell spell = new LotrSpell();
        spell.setName(name);
        spell.setDescription(description);
        spell.setSpellLevel(spellLevel);
        spell.setSpellSchool(spellSchool);
        spell.setRange(range);
        spell.setDuration(duration);
        spell.setCastingTime("1 action");
        spell.setRequiresFocus(true);
        log.info("Created LOTR spell: {} (level {})", name, spellLevel);
        return repository.save(spell);
    }

    @Transactional
    public LotrSpell save(LotrSpell spell) {
        return repository.save(spell);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
