package com.yisy.modules.doramcu.skin.repository;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.skin.model.SkinVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for skin variant entities.
 */
@Repository
public interface SkinVariantRepository extends JpaRepository<SkinVariantEntity, Long> {

    /**
     * Finds a variant by its unique ID.
     *
     * @param variantId the variant ID
     * @return the variant if found
     */
    Optional<SkinVariantEntity> findByVariantId(String variantId);

    /**
     * Finds all variants for a given base skin.
     *
     * @param baseSkinId the base skin ID
     * @return list of variants for this base skin
     */
    List<SkinVariantEntity> findByBaseSkinId(String baseSkinId);

    /**
     * Checks if a variant with the given ID exists.
     *
     * @param variantId the variant ID
     * @return true if the variant exists
     */
    boolean existsByVariantId(String variantId);

    /**
     * Deletes a variant by its ID.
     *
     * @param variantId the variant ID
     */
    void deleteByVariantId(String variantId);
}
