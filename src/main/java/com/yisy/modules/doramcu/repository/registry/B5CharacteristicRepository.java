package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.B5CharacteristicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface B5CharacteristicRepository extends JpaRepository<B5CharacteristicEntity, Long> {

    Optional<B5CharacteristicEntity> findByCharCode(String charCode);
}
