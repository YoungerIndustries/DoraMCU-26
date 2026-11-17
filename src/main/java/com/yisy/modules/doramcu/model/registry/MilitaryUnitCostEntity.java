package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for military unit production costs.
 */
@Entity
@Table(name = "military_unit_cost", indexes = {
        @Index(name = "idx_muc_unit", columnList = "unit_code"),
        @Index(name = "idx_muc_cargo", columnList = "cargo_type_code")
})
public class MilitaryUnitCostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "unit_code", nullable = false, length = 50)
    private String unitCode;

    @Column(name = "cargo_type_code", nullable = false, length = 50)
    private String cargoTypeCode;

    @Column(name = "required_amount", nullable = false)
    private int requiredAmount;

    public MilitaryUnitCostEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getCargoTypeCode() {
        return cargoTypeCode;
    }

    public void setCargoTypeCode(String cargoTypeCode) {
        this.cargoTypeCode = cargoTypeCode;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public void setRequiredAmount(int requiredAmount) {
        this.requiredAmount = requiredAmount;
    }
}
