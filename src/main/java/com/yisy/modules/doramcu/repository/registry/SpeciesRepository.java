package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.SpeciesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<SpeciesEntity, Long> {

    Optional<SpeciesEntity> findBySpeciesCode(String speciesCode);

    List<SpeciesEntity> findByGameModule(String gameModule);
}
