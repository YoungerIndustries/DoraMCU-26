package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ShipTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipTypeRepository extends JpaRepository<ShipTypeEntity, Long> {

    Optional<ShipTypeEntity> findByShipTypeCode(String shipTypeCode);
}
