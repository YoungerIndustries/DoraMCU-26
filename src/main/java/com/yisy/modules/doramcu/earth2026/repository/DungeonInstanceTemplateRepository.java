package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DungeonInstanceTemplateRepository extends JpaRepository<DungeonInstanceTemplate, Long> {
    Optional<DungeonInstanceTemplate> findByTemplateId(String templateId);
    List<DungeonInstanceTemplate> findByActiveTrue();
    List<DungeonInstanceTemplate> findByTheme(String theme);
}
