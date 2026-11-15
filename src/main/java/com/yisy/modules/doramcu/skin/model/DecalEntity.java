package com.yisy.modules.doramcu.skin.model;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.Decal;

import com.yisy.modules.doramcu.hex.DecalType;
import jakarta.persistence.*;

/**
 * JPA entity representing a decal on a skin.
 * <p>
 * Decals provide visual identification markings such as call signs, unit IDs,
 * faction emblems, and insignias. Each decal belongs to a {@link SkinEntity}.
 * </p>
 */
@Entity
@Table(name = "decals", indexes = {
        @Index(name = "idx_decal_skin", columnList = "skin_id"),
        @Index(name = "idx_decal_type", columnList = "type")
})
public class DecalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String decalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecalType type;

    @Column(length = 500)
    private String content = "";

    @Column(length = 50)
    private String position = "";

    @Column(nullable = false)
    private double size = 1.0;

    @Column(nullable = false)
    private double rotation = 0.0;

    @Column(nullable = false)
    private double opacity = 1.0;

    @Column(length = 20)
    private String color = "#000000";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id")
    private SkinEntity skin;

    public DecalEntity() {
    }

    public DecalEntity(String decalId, DecalType type) {
        this.decalId = decalId;
        this.type = type;
    }

    // ---- Accessors ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecalId() {
        return decalId;
    }

    public void setDecalId(String decalId) {
        this.decalId = decalId;
    }

    public DecalType getType() {
        return type;
    }

    public void setType(DecalType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = Math.max(0.1, Math.min(5.0, size));
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation % 360.0;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public SkinEntity getSkin() {
        return skin;
    }

    public void setSkin(SkinEntity skin) {
        this.skin = skin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecalEntity that = (DecalEntity) o;
        return decalId != null && decalId.equals(that.decalId);
    }

    @Override
    public int hashCode() {
        return decalId != null ? decalId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DecalEntity{" +
                "id=" + id +
                ", decalId='" + decalId + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
