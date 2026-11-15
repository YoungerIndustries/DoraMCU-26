package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.PropertyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyTransactionRepository extends JpaRepository<PropertyTransaction, Long> {
    List<PropertyTransaction> findByCharacterId(Long characterId);
    List<PropertyTransaction> findByPropertyId(Long propertyId);
    List<PropertyTransaction> findByType(String type);
}
