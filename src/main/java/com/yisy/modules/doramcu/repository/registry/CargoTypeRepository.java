package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.CargoTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoTypeRepository extends JpaRepository<CargoTypeEntity, Long> {

    Optional<CargoTypeEntity> findByCargoTypeCode(String cargoTypeCode);

    List<CargoTypeEntity> findByCategory(String category);
}
