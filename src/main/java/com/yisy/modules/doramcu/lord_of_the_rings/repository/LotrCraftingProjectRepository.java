package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCraftingProjectRepository extends JpaRepository<LotrCraftingProject, Long> {
    List<LotrCraftingProject> findByCharacterId(Long characterId);
}
