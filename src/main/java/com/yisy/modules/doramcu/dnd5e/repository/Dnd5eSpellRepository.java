package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eSpellRepository extends JpaRepository<Dnd5eSpell, Long> {

    List<Dnd5eSpell> findByLevelAndActiveTrue(int level);

    List<Dnd5eSpell> findBySchoolAndActiveTrue(String school);

    List<Dnd5eSpell> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Dnd5eSpell> findByActiveTrue();
}
