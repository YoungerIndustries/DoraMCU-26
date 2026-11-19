package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSpell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrSpellRepository extends JpaRepository<LotrSpell, Long> {

    List<LotrSpell> findByNameContainingIgnoreCase(String name);

    List<LotrSpell> findBySpellLevel(int spellLevel);

    List<LotrSpell> findBySpellSchool(String spellSchool);
}
