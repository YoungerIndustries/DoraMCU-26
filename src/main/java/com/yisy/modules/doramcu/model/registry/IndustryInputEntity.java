package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for industry inputs — what cargo an industry consumes per turn.
 */
@Entity
@Table(name = "industry_input", indexes = {
        @Index(name = "idx_ii_industry", columnList = "industry_code"),
        @Index(name = "idx_ii_cargo", columnList = "cargo_type_code")
})
public class IndustryInputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "industry_code", nullable = false, length = 50)
    private String industryCode;

    @Column(name = "cargo_type_code", nullable = false, length = 50)
    private String cargoTypeCode;

    @Column(name = "amount_per_turn", nullable = false)
    private int amountPerTurn;

    public IndustryInputEntity() {
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
