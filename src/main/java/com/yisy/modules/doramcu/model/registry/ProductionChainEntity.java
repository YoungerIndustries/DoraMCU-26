package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for production chains from raw resources to final products.
 */
@Entity
@Table(name = "production_chain", indexes = {
        @Index(name = "idx_pc_name", columnList = "chain_name"),
        @Index(name = "idx_pc_final_product", columnList = "final_product_object_id"),
        @Index(name = "idx_pc_level", columnList = "chain_level")
})
public class ProductionChainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "chain_name", nullable = false, length = 100)
    private String chainName;

    @Column(name = "final_product_object_id")
    private Long finalProductObjectId;

    @Column(name = "chain_level", nullable = false)
    private int chainLevel;

    @Column(columnDefinition = "TEXT")
    private String description;

    public ProductionChainEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public Long getFinalProductObjectId() {
        return finalProductObjectId;
    }

    public void setFinalProductObjectId(Long finalProductObjectId) {
        this.finalProductObjectId = finalProductObjectId;
    }

    public int getChainLevel() {
        return chainLevel;
    }

    public void setChainLevel(int chainLevel) {
        this.chainLevel = chainLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
