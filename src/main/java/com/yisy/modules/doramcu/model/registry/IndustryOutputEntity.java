package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for industry outputs — what cargo an industry produces per turn.
 */
@Entity
@Table(name = "industry_output", indexes = {
        @Index(name = "idx_io_industry", columnList = "industry_code"),
        @Index(name = "idx_io_cargo", columnList = "cargo_type_code")
})
public class IndustryOutputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "industry_code", nullable = false, length = 50)
    private String industryCode;

    @Column(name = "cargo_type_code", nullable = false, length = 50)
    private String cargoTypeCode;

    @Column(name = "amount_per_turn", nullable = false)
    private int amountPerTurn;

    public IndustryOutputEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getCargoTypeCode() {
        return cargoTypeCode;
    }

    public void setCargoTypeCode(String cargoTypeCode) {
        this.cargoTypeCode = cargoTypeCode;
    }

    public int getAmountPerTurn() {
        return amountPerTurn;
    }

    public void setAmountPerTurn(int amountPerTurn) {
        this.amountPerTurn = amountPerTurn;
    }
}
