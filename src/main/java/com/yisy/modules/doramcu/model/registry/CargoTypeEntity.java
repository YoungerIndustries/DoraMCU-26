package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for planet surface cargo types.
 * <p>
 * Covers 25 cargo types across 4 categories: RAW, PROCESSED, ADVANCED, and MAIL.
 * </p>
 */
@Entity
@Table(name = "cargo_type", indexes = {
        @Index(name = "idx_ct_category", columnList = "category")
})
public class CargoTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "cargo_type_code", nullable = false, unique = true, length = 50)
    private String cargoTypeCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;

    @Column(name = "is_perishable", nullable = false)
    private boolean isPerishable;

    @Column(name = "base_value", nullable = false)
    private int baseValue;

    public CargoTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCargoTypeCode() {
        return cargoTypeCode;
    }

    public void setCargoTypeCode(String cargoTypeCode) {
        this.cargoTypeCode = cargoTypeCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public boolean isPerishable() {
        return isPerishable;
    }

    public void setPerishable(boolean perishable) {
        isPerishable = perishable;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(int baseValue) {
        this.baseValue = baseValue;
    }
}
