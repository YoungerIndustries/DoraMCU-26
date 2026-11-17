package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.ProcessingRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingRecipeRepository extends JpaRepository<ProcessingRecipeEntity, Long> {

    List<ProcessingRecipeEntity> findByOutputResource(String outputResource);

    List<ProcessingRecipeEntity> findByOutputResourceAndRecipeVariant(String outputResource, String recipeVariant);
}
