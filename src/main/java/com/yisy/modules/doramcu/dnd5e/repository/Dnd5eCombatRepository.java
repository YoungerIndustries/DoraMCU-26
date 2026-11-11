package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCombat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eCombatRepository extends JpaRepository<Dnd5eCombat, Long> {

    List<Dnd5eCombat> findByStatus(String status);
}
