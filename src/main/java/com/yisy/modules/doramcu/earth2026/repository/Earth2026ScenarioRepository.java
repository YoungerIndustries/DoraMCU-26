package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026ScenarioRepository extends JpaRepository<Earth2026Scenario, Long> {

    List<Earth2026Scenario> findByNameContainingIgnoreCase(String name);

    List<Earth2026Scenario> findByStatus(Earth2026Scenario.ScenarioStatus status);

    List<Earth2026Scenario> findByActiveTrue();

    List<Earth2026Scenario> findBySupernaturalTrue();

    List<Earth2026Scenario> findByMinLevelLessThanEqualAndActiveTrue(int level);
}
