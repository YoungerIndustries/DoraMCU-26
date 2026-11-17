package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ProductionChainStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionChainStepRepository extends JpaRepository<ProductionChainStepEntity, Long> {

    List<ProductionChainStepEntity> findByChainIdOrderByStepOrder(long chainId);
}
