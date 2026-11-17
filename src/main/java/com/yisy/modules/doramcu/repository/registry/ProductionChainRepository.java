package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ProductionChainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionChainRepository extends JpaRepository<ProductionChainEntity, Long> {

    List<ProductionChainEntity> findByChainName(String chainName);

    List<ProductionChainEntity> findByChainLevel(int chainLevel);
}
