package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ModuleTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleTypeRepository extends JpaRepository<ModuleTypeEntity, Long> {

    Optional<ModuleTypeEntity> findByModuleCode(String moduleCode);

    List<ModuleTypeEntity> findByCategory(String category);
}
