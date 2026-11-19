package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrNpcRepository extends JpaRepository<LotrNpc, Long> {
    List<LotrNpc> findByNameContainingIgnoreCase(String name);
    List<LotrNpc> findByRace(String race);
    List<LotrNpc> findByFaction(String faction);
}
