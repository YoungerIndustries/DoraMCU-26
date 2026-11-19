package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrMonsterRepository extends JpaRepository<LotrMonster, Long> {

    List<LotrMonster> findByNameContainingIgnoreCase(String name);

    List<LotrMonster> findByMonsterType(String monsterType);

    List<LotrMonster> findByFaction(String faction);

    List<LotrMonster> findByIsBossTrue();
}
