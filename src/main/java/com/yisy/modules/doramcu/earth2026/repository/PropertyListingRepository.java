package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.PropertyListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyListingRepository extends JpaRepository<PropertyListing, Long> {
    List<PropertyListing> findByStatus(String status);
    List<PropertyListing> findByPropertyTypeAndStatus(String propertyType, String status);
    List<PropertyListing> findByBuildingIdAndStatus(Long buildingId, String status);
    List<PropertyListing> findByOwnerId(Long ownerId);
}
