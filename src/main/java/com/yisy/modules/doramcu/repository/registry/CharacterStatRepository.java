package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.CharacterStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterStatRepository extends JpaRepository<CharacterStatEntity, Long> {

    Optional<CharacterStatEntity> findByStatCode(String statCode);
}
