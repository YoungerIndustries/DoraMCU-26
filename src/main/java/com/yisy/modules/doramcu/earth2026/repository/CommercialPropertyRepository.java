package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.CommercialProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommercialPropertyRepository extends JpaRepository<CommercialProperty, Long> {

    List<CommercialProperty> findByStatus(String status);

    List<CommercialProperty> findByPropertyType(String propertyType);

    List<CommercialProperty> findByOwnerId(Long ownerId);

    List<CommercialProperty> findByStatusAndPropertyType(String status, String propertyType);

    List<CommercialProperty> findByPlanetId(Long planetId);
}
