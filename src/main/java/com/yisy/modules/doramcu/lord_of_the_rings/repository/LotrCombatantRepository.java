package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombatant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCombatantRepository extends JpaRepository<LotrCombatant, Long> {

    List<LotrCombatant> findByCombatId(Long combatId);

    List<LotrCombatant> findByCombatIdAndAliveTrue(Long combatId);
}
