package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCombat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCombatRepository extends JpaRepository<LotrCombat, Long> {

    List<LotrCombat> findByStatus(String status);

    List<LotrCombat> findByStatusOrderByCreatedAtDesc(String status);
}
