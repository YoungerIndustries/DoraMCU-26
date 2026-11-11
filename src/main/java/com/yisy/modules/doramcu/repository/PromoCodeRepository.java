package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing promo codes in the database.
 */
@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    /**
     * Finds a promo code by its code string (case-insensitive).
     *
     * @param code the promo code string
     * @return optional promo code if found
     */
    Optional<PromoCode> findByCode(String code);

    /**
     * Checks if a promo code exists.
     *
     * @param code the promo code string
     * @return true if the code exists
     */
    boolean existsByCode(String code);

    /**
     * Finds all promo codes of a given type.
     *
     * @param codeType the type of promo code (PLAYER or ADMIN)
     * @return list of promo codes of the specified type
     */
    List<PromoCode> findByCodeType(PromoCode.CodeType codeType);

    /**
     * Finds an active promo code by code and type.
     *
     * @param code the promo code string
     * @param codeType the type of promo code
     * @return optional promo code if found
     */
    Optional<PromoCode> findByCodeAndCodeType(String code, PromoCode.CodeType codeType);
}
