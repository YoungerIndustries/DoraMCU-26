package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a kiosk upgrade purchased by a corporation
 * for their office unit in the Younger Industries HQ.
 */
@Entity
@Table(name = "kiosk_upgrades", indexes = {
        @Index(name = "idx_ku_corp", columnList = "corporation_id"),
        @Index(name = "idx_ku_unit", columnList = "office_unit_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_ku_corp_unit_type", columnNames = {"corporation_id", "office_unit_id", "upgrade_type"})
})
public class KioskUpgrade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Column(name = "office_unit_id", nullable = false)
    private Long officeUnitId;

    @Column(name = "upgrade_type", nullable = false, length = 50)
    private String upgradeType;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    public KioskUpgrade() {
        this.purchasedAt = LocalDateTime.now();
    }

    public KioskUpgrade(Long corporationId, Long officeUnitId, String upgradeType) {
        this();
        this.corporationId = corporationId;
        this.officeUnitId = officeUnitId;
        this.upgradeType = upgradeType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public Long getOfficeUnitId() {
        return officeUnitId;
    }

    public void setOfficeUnitId(Long officeUnitId) {
        this.officeUnitId = officeUnitId;
    }

    public String getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(String upgradeType) {
        this.upgradeType = upgradeType;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    @Override
    public String toString() {
        return "KioskUpgrade{id=" + id + ", corporationId=" + corporationId +
                ", officeUnitId=" + officeUnitId + ", type='" + upgradeType + "'}";
    }
}
