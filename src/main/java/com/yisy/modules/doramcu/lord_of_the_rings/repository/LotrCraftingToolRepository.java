package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotrCraftingToolRepository extends JpaRepository<LotrCraftingTool, Long> {
}
