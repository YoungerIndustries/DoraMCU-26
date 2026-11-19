package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrVillain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrVillainRepository extends JpaRepository<LotrVillain, Long> {
    List<LotrVillain> findByNameContainingIgnoreCase(String name);
    List<LotrVillain> findByFaction(String faction);
    List<LotrVillain> findByIsActiveTrue();
}
