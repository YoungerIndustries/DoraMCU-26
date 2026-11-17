package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<SkillEntity, Long> {

    Optional<SkillEntity> findBySkillCode(String skillCode);

    List<SkillEntity> findByGameModule(String gameModule);
}
