package com.yisy.modules.doramcu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a promo code.
 * <p>
 * Promo codes are divided into two types:
 * <ul>
 *   <li>PLAYER — codes entered during player registration for bonuses</li>
 *   <li>ADMIN  — codes used by admins to trigger system actions (e.g., RESET)</li>
 * </ul>
 * Each promo code can have usage limits, expiry dates, and associated bonuses.
 * </p>
 */
@Entity
@Table(name = "promo_codes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_promo_code_code", columnNames = "code")
        })
@EntityListeners(CdcEntityListener.class)
public class PromoCode {

    /**
     * The type of promo code, determining how it is consumed.
     */
    public enum CodeType {
        /** Player registration promo codes — entered on the landing page */
        PLAYER,
        /** Admin action promo codes — used via the admin API to trigger system operations */
        ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The promo code string (e.g., "WELCOME2026").
     * Stored in uppercase for case-insensitive matching.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable description of the promo code.
     */
    @Column(length = 255)
    private String description;

    /**
     * Type of the promo code: PLAYER (registration) or ADMIN (system actions).
     * Defaults to PLAYER for backward compatibility.
     */
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private CodeType codeType = CodeType.PLAYER;

    /**
     * Maximum number of times this code can be used.
     * Null means unlimited uses.
     */
    @Column
    private Integer maxUses;

    /**
     * Number of times this code has been used.
     */
    @Column(nullable = false)
    private int usedCount = 0;

    /**
     * When the promo code expires. Null means no expiry.
     */
    @Column
    private LocalDateTime expiryDate;

    /**
     * Whether this promo code is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Bonus resources or benefits granted when this code is used.
     * Stored as JSON or key-value pairs.
     */
    @Column(length = 1000)
    private String bonuses;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeType codeType) {
        this.codeType = codeType;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBonuses() {
        return bonuses;
    }

    public void setBonuses(String bonuses) {
        this.bonuses = bonuses;
    }

    /**
     * Checks if this promo code has reached its usage limit.
     *
     * @return true if the code has been used the maximum number of times
     */
    public boolean isUsageLimitReached() {
        return maxUses != null && usedCount >= maxUses;
    }

    /**
     * Checks if this promo code has expired.
     *
     * @return true if the expiry date has passed
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    @Override
    public String toString() {
        return "PromoCode{id=" + id + ", code='" + code + "', type=" + codeType + ", active=" + active +
                ", used=" + usedCount + "/" + (maxUses != null ? maxUses : "∞") + "}";
    }
}
