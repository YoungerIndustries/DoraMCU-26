package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ProcessingRecipeInputEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingRecipeInputRepository extends JpaRepository<ProcessingRecipeInputEntity, Long> {

    List<ProcessingRecipeInputEntity> findByRecipeId(long recipeId);
}
