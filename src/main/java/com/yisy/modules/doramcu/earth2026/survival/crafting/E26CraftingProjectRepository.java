package com.yisy.modules.doramcu.earth2026.survival.crafting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface E26CraftingProjectRepository extends JpaRepository<E26CraftingProject, Long> {

    List<E26CraftingProject> findByCharacterId(Long characterId);

    List<E26CraftingProject> findByCharacterIdAndStatus(Long characterId, String status);

    List<E26CraftingProject> findByStatus(String status);
}
