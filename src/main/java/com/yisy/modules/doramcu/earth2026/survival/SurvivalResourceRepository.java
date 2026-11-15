package com.yisy.modules.doramcu.earth2026.survival;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurvivalResourceRepository extends JpaRepository<SurvivalResource, Long> {

    List<SurvivalResource> findByCategory(String category);

    List<SurvivalResource> findByCategoryAndRarity(String category, String rarity);

    Optional<SurvivalResource> findByName(String name);

    List<SurvivalResource> findByConsumableTrue();

    List<SurvivalResource> findByIsMaterialTrue();

    List<SurvivalResource> findByIsToolTrue();
}
