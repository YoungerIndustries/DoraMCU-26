package com.yisy.modules.doramcu.skin;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.Decal;
import com.yisy.modules.doramcu.hex.DamageTier;
import com.yisy.modules.doramcu.hex.DamageOverlay;
import com.yisy.modules.doramcu.hex.SkinType;

import com.yisy.modules.doramcu.hex.BuildingHull;
import com.yisy.modules.doramcu.hex.Compartment;
import com.yisy.modules.doramcu.hex.GroundVehicle;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.spi.SkinLookupSpi;
import com.yisy.modules.doramcu.hex.spi.SkinProviders;
import com.yisy.modules.doramcu.skin.dto.DecalResponse;
import com.yisy.modules.doramcu.skin.dto.SkinVariantResponse;
import com.yisy.modules.doramcu.skin.dto.VisualStateResponse;
import com.yisy.modules.doramcu.skin.model.DecalEntity;
import com.yisy.modules.doramcu.skin.model.FactionSkinThemeEntity;
import com.yisy.modules.doramcu.skin.model.SkinEntity;
import com.yisy.modules.doramcu.skin.model.SkinVariantEntity;
import com.yisy.modules.doramcu.skin.repository.FactionSkinThemeRepository;
import com.yisy.modules.doramcu.skin.repository.SkinRepository;
import com.yisy.modules.doramcu.skin.repository.SkinVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Central service for skin management with JPA persistence.
 * <p>
 * Provides CRUD operations for skins, querying by type and faction,
 * and applying skins to assets. This service is shared across all
 * game modules via the central {@code doramcu.skin} package.
 * </p>
 * <p>
 * Uses JPA entities ({@link SkinEntity}, {@link DecalEntity}, {@link FactionSkinThemeEntity})
 * for persistence and converts to/from domain models ({@link Skin}, {@link Decal},
 * {@link FactionSkinTheme}) for the public API.
 * </p>
 */
@Service
public class SkinService implements SkinLookupSpi {

    /** Static holder for access from non-Spring-managed code. */
    private static volatile SkinLookupSpi instance;

    private final SkinRepository skinRepository;
    private final FactionSkinThemeRepository factionThemeRepository;
    private final SkinVariantRepository variantRepository;
    private final AssetRegistry assetRegistry;

    public SkinService(SkinRepository skinRepository, FactionSkinThemeRepository factionThemeRepository,
                       SkinVariantRepository variantRepository, AssetRegistry assetRegistry) {
        this.skinRepository = skinRepository;
        this.factionThemeRepository = factionThemeRepository;
        this.variantRepository = variantRepository;
        this.assetRegistry = assetRegistry;
    }

    /**
     * Returns the Spring-managed singleton instance for use by non-Spring code.
     *
     * @return the service instance, or null if Spring has not initialized it yet
     */
    public static SkinLookupSpi getInstance() {
        return instance;
    }

    /**
     * Sets the static instance reference after Spring injection.
     */
    @Autowired
    public void setInstance(@Lazy SkinService self) {
        instance = self;
        SkinProviders.setSkinLookup(self);
    }

    // ---- Skin CRUD ----

    /**
     * Registers or updates a skin.
     *
     * @param skin the skin to register
     * @return the saved skin
     */
    @Transactional
    public Skin registerSkin(Skin skin) {
        Objects.requireNonNull(skin, "Skin must not be null");
        // Check if an entity with this skinId already exists; if so, update it
        SkinEntity entity = skinRepository.findBySkinIdWithDecals(skin.getId())
                .orElse(null);
        if (entity != null) {
            entity.setType(skin.getType());
            entity.setName(skin.getName());
            entity.setDescription(skin.getDescription());
            entity.setPrimaryColor(skin.getPrimaryColor());
            entity.setSecondaryColor(skin.getSecondaryColor());
            entity.setAccentColor(skin.getAccentColor());
            entity.setTextureReference(skin.getTextureReference());
            entity.setFactionId(skin.getFactionId());
            // Replace damage overlay (set new map instead of clearing immutable proxy)
            DamageOverlay overlay = skin.getDamageOverlay();
            if (overlay != null) {
                entity.setDamageOverlay(new HashMap<>(overlay.getAllDescriptions()));
            } else {
                entity.setDamageOverlay(new HashMap<>());
            }
            // Replace decals (orphanRemoval will delete old ones)
            entity.getDefaultDecals().clear();
            for (Decal decal : skin.getDefaultDecals()) {
                DecalEntity decalEntity = new DecalEntity(decal.getId(), decal.getType());
                decalEntity.setContent(decal.getContent());
                decalEntity.setPosition(decal.getPosition());
                decalEntity.setSize(decal.getSize());
                decalEntity.setRotation(decal.getRotation());
                decalEntity.setOpacity(decal.getOpacity());
                decalEntity.setColor(decal.getColor());
                decalEntity.setSkin(entity);
                entity.getDefaultDecals().add(decalEntity);
            }
        } else {
            entity = toEntity(skin);
        }
        entity = skinRepository.save(entity);
        return toDomain(entity);
    }

    /**
     * Removes a skin by ID.
     *
     * @param id the skin ID
     * @return true if the skin was removed, false if not found
     */
    @Transactional
    public boolean removeSkin(String id) {
        Objects.requireNonNull(id, "Skin ID must not be null");
        if (!skinRepository.existsBySkinId(id)) {
            return false;
        }
        skinRepository.deleteBySkinId(id);
        return true;
    }

    /**
     * Looks up a skin by ID.
     *
     * @param id the skin ID
     * @return the skin, or null if not found
     */
    @Transactional(readOnly = true)
    public Skin getSkin(String id) {
        Objects.requireNonNull(id, "Skin ID must not be null");
        return skinRepository.findBySkinIdWithDecals(id).map(this::toDomain).orElse(null);
    }

    /**
     * Returns all registered skins.
     *
     * @return list of all skins
     */
    @Transactional(readOnly = true)
    public List<Skin> getAllSkins() {
        return skinRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Returns all skins for the given asset type.
     *
     * @param type the skin type
     * @return list of skins matching the type
     */
    @Transactional(readOnly = true)
    public List<Skin> getSkinsByType(SkinType type) {
        Objects.requireNonNull(type, "Skin type must not be null");
        return skinRepository.findByType(type).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Returns all skins associated with the given faction.
     *
     * @param factionId the faction ID
     * @return list of skins for the faction
     */
    @Transactional(readOnly = true)
    public List<Skin> getSkinsByFaction(String factionId) {
        Objects.requireNonNull(factionId, "Faction ID must not be null");
        return skinRepository.findByFactionId(factionId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ---- Faction theme management ----

    /**
     * Registers or updates a faction skin theme.
     *
     * @param theme the theme to register
     * @return the saved theme
     */
    @Transactional
    public FactionSkinTheme registerFactionTheme(FactionSkinTheme theme) {
        Objects.requireNonNull(theme, "Theme must not be null");
        FactionSkinThemeEntity entity = toFactionEntity(theme);
        entity = factionThemeRepository.save(entity);
        return toFactionDomain(entity);
    }

    /**
     * Removes a faction skin theme by faction ID.
     *
     * @param factionId the faction ID
     * @return true if the theme was removed, false if not found
     */
    @Transactional
    public boolean removeFactionTheme(String factionId) {
        Objects.requireNonNull(factionId, "Faction ID must not be null");
        if (!factionThemeRepository.existsByFactionId(factionId)) {
            return false;
        }
        factionThemeRepository.deleteByFactionId(factionId);
        return true;
    }

    /**
     * Looks up a faction skin theme by faction ID.
     *
     * @param factionId the faction ID
     * @return the theme, or null if not found
     */
    @Transactional(readOnly = true)
    public FactionSkinTheme getFactionTheme(String factionId) {
        Objects.requireNonNull(factionId, "Faction ID must not be null");
        return factionThemeRepository.findByFactionId(factionId)
                .map(this::toFactionDomain)
                .orElse(null);
    }

    /**
     * Returns all registered faction themes.
     *
     * @return list of all faction themes
     */
    @Transactional(readOnly = true)
    public List<FactionSkinTheme> getAllFactionThemes() {
        return factionThemeRepository.findAll().stream()
                .map(this::toFactionDomain)
                .collect(Collectors.toList());
    }

    /**
     * Returns the default skin for a faction, or null if not configured.
     *
     * @param factionId the faction ID
     * @return the default skin, or null
     */
    @Transactional(readOnly = true)
    public Skin getDefaultSkinForFaction(String factionId) {
        Objects.requireNonNull(factionId, "Faction ID must not be null");
        FactionSkinTheme theme = getFactionTheme(factionId);
        if (theme == null || theme.getDefaultSkinId().isEmpty()) {
            return null;
        }
        return getSkin(theme.getDefaultSkinId());
    }

    // ---- Asset assignment and visual state ----

    /**
     * Assigns a skin to a registered asset.
     *
     * @param assetType the asset type (SHIP, SPACE_STATION, etc.)
     * @param assetId the asset ID
     * @param skinId the skin ID to assign
     * @return true if the skin was assigned, false if asset or skin not found
     */
    public boolean assignSkinToAsset(SkinType assetType, int assetId, String skinId) {
        Objects.requireNonNull(assetType, "Asset type must not be null");
        Objects.requireNonNull(skinId, "Skin ID must not be null");
        Skin skin = getSkin(skinId);
        if (skin == null) {
            return false;
        }
        Object asset = assetRegistry.get(assetType, assetId);
        if (asset == null) {
            return false;
        }
        if (asset instanceof Ship) {
            ((Ship) asset).setSkin(skin);
        } else if (asset instanceof SpaceStation) {
            ((SpaceStation) asset).setSkin(skin);
        } else if (asset instanceof GroundVehicle) {
            ((GroundVehicle) asset).setSkin(skin);
        } else if (asset instanceof BuildingHull) {
            ((BuildingHull) asset).setSkin(skin);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Removes the skin from a registered asset.
     *
     * @param assetType the asset type
     * @param assetId the asset ID
     * @return true if the skin was removed, false if asset not found
     */
    public boolean removeSkinFromAsset(SkinType assetType, int assetId) {
        Objects.requireNonNull(assetType, "Asset type must not be null");
        Object asset = assetRegistry.get(assetType, assetId);
        if (asset == null) {
            return false;
        }
        if (asset instanceof Ship) {
            ((Ship) asset).removeSkin();
        } else if (asset instanceof SpaceStation) {
            ((SpaceStation) asset).removeSkin();
        } else if (asset instanceof GroundVehicle) {
            ((GroundVehicle) asset).removeSkin();
        } else if (asset instanceof BuildingHull) {
            ((BuildingHull) asset).removeSkin();
        } else {
            return false;
        }
        return true;
    }

    /**
     * Returns the visual state of a registered asset, including skin, decals, and damage overlays.
     *
     * @param assetType the asset type
     * @param assetId the asset ID
     * @return the visual state response, or null if asset not found
     */
    public VisualStateResponse getVisualState(SkinType assetType, int assetId) {
        Objects.requireNonNull(assetType, "Asset type must not be null");
        Object asset = assetRegistry.get(assetType, assetId);
        if (asset == null) {
            return null;
        }
        VisualStateResponse response = new VisualStateResponse();
        response.setAssetId(assetId);
        response.setAssetType(assetType);

        if (asset instanceof Ship ship) {
            response.setAssetName(ship.getName());
            Skin skin = ship.getSkin();
            fillSkinInfo(response, skin);
            response.setDecals(toDecalResponseList(ship.getDecals()));
            fillCompartmentDamage(response, ship.getHullModules(), skin);
        } else if (asset instanceof SpaceStation station) {
            response.setAssetName(station.getName());
            Skin skin = station.getSkin();
            fillSkinInfo(response, skin);
            response.setDecals(toDecalResponseList(station.getDecals()));
            HullModule hull = station.getHullModule();
            if (hull != null) {
                fillCompartmentDamage(response, List.of(hull), skin);
            }
        } else if (asset instanceof GroundVehicle vehicle) {
            response.setAssetName(vehicle.getName());
            Skin skin = vehicle.getSkin();
            fillSkinInfo(response, skin);
            response.setDecals(toDecalResponseList(vehicle.getDecals()));
            fillCompartmentDamage(response, vehicle.getHullModules(), skin);
        } else if (asset instanceof BuildingHull building) {
            response.setAssetName(building.getName());
            Skin skin = building.getSkin();
            fillSkinInfo(response, skin);
            response.setDecals(toDecalResponseList(building.getDecals()));
            fillCompartmentDamageFromMap(response, building.getCompartments(), skin);
        }
        return response;
    }

    private void fillSkinInfo(VisualStateResponse response, Skin skin) {
        if (skin != null) {
            response.setSkinId(skin.getId());
            response.setSkinName(skin.getName());
            response.setPrimaryColor(skin.getPrimaryColor());
            response.setSecondaryColor(skin.getSecondaryColor());
            response.setAccentColor(skin.getAccentColor());
        }
    }

    private void fillCompartmentDamage(VisualStateResponse response, List<HullModule> hullModules, Skin skin) {
        Map<String, String> compartmentDamage = new HashMap<>();
        DamageTier overallWorst = DamageTier.PRISTINE;
        for (HullModule module : hullModules) {
            for (Compartment compartment : module.getCompartments()) {
                DamageTier worst = compartment.getWorstDamageTier();
                compartmentDamage.put(compartment.getName(), worst.name());
                if (worst.ordinal() > overallWorst.ordinal()) {
                    overallWorst = worst;
                }
            }
        }
        response.setCompartmentDamage(compartmentDamage);
        response.setOverallWorstDamage(overallWorst.name());
        if (skin != null && skin.getDamageOverlay() != null) {
            String description = skin.getDamageOverlay().getDescription(overallWorst);
            response.setDamageOverlayDescription(description);
        }
    }

    private void fillCompartmentDamageFromMap(VisualStateResponse response, Map<String, Compartment> compartments, Skin skin) {
        Map<String, String> compartmentDamage = new HashMap<>();
        DamageTier overallWorst = DamageTier.PRISTINE;
        for (Map.Entry<String, Compartment> entry : compartments.entrySet()) {
            Compartment compartment = entry.getValue();
            DamageTier worst = compartment.getWorstDamageTier();
            compartmentDamage.put(entry.getKey(), worst.name());
            if (worst.ordinal() > overallWorst.ordinal()) {
                overallWorst = worst;
            }
        }
        response.setCompartmentDamage(compartmentDamage);
        response.setOverallWorstDamage(overallWorst.name());
        if (skin != null && skin.getDamageOverlay() != null) {
            String description = skin.getDamageOverlay().getDescription(overallWorst);
            response.setDamageOverlayDescription(description);
        }
    }

    private List<DecalResponse> toDecalResponseList(List<Decal> decals) {
        List<DecalResponse> responses = new ArrayList<>();
        for (Decal decal : decals) {
            DecalResponse response = new DecalResponse();
            response.setId(decal.getId());
            response.setType(decal.getType());
            response.setContent(decal.getContent());
            response.setPosition(decal.getPosition());
            response.setSize(decal.getSize());
            response.setRotation(decal.getRotation());
            response.setOpacity(decal.getOpacity());
            response.setColor(decal.getColor());
            responses.add(response);
        }
        return responses;
    }

    // ---- Entity <-> Domain conversion ----

    private SkinEntity toEntity(Skin skin) {
        SkinEntity entity = new SkinEntity(skin.getId(), skin.getType());
        entity.setName(skin.getName());
        entity.setDescription(skin.getDescription());
        entity.setPrimaryColor(skin.getPrimaryColor());
        entity.setSecondaryColor(skin.getSecondaryColor());
        entity.setAccentColor(skin.getAccentColor());
        entity.setTextureReference(skin.getTextureReference());
        entity.setFactionId(skin.getFactionId());

        // Convert damage overlay
        DamageOverlay overlay = skin.getDamageOverlay();
        if (overlay != null) {
            entity.setDamageOverlay(overlay.getAllDescriptions());
        }

        // Convert decals
        for (Decal decal : skin.getDefaultDecals()) {
            DecalEntity decalEntity = new DecalEntity(decal.getId(), decal.getType());
            decalEntity.setContent(decal.getContent());
            decalEntity.setPosition(decal.getPosition());
            decalEntity.setSize(decal.getSize());
            decalEntity.setRotation(decal.getRotation());
            decalEntity.setOpacity(decal.getOpacity());
            decalEntity.setColor(decal.getColor());
            decalEntity.setSkin(entity);
            entity.getDefaultDecals().add(decalEntity);
        }

        return entity;
    }

    private Skin toDomain(SkinEntity entity) {
        Skin skin = new Skin(entity.getSkinId(), entity.getType());
        skin.setName(entity.getName());
        skin.setDescription(entity.getDescription());
        skin.setPrimaryColor(entity.getPrimaryColor());
        skin.setSecondaryColor(entity.getSecondaryColor());
        skin.setAccentColor(entity.getAccentColor());
        skin.setTextureReference(entity.getTextureReference());
        skin.setFactionId(entity.getFactionId());

        // Convert damage overlay
        DamageOverlay overlay = new DamageOverlay();
        Map<DamageTier, String> overlayMap = entity.getDamageOverlay();
        if (overlayMap != null) {
            for (Map.Entry<DamageTier, String> entry : overlayMap.entrySet()) {
                overlay.setTierDescription(entry.getKey(), entry.getValue());
            }
        }
        skin.setDamageOverlay(overlay);

        // Convert decals
        for (DecalEntity decalEntity : entity.getDefaultDecals()) {
            Decal decal = new Decal(decalEntity.getDecalId(), decalEntity.getType());
            decal.setContent(decalEntity.getContent());
            decal.setPosition(decalEntity.getPosition());
            decal.setSize(decalEntity.getSize());
            decal.setRotation(decalEntity.getRotation());
            decal.setOpacity(decalEntity.getOpacity());
            decal.setColor(decalEntity.getColor());
            skin.addDefaultDecal(decal);
        }

        return skin;
    }

    private FactionSkinThemeEntity toFactionEntity(FactionSkinTheme theme) {
        FactionSkinThemeEntity entity = new FactionSkinThemeEntity(theme.getFactionId());
        entity.setName(theme.getName());
        entity.setDefaultSkinId(theme.getDefaultSkinId());
        entity.setPrimaryColor(theme.getPrimaryColor());
        entity.setSecondaryColor(theme.getSecondaryColor());
        entity.setAccentColor(theme.getAccentColor());
        entity.setEmblemSymbol(theme.getEmblemSymbol());
        return entity;
    }

    private FactionSkinTheme toFactionDomain(FactionSkinThemeEntity entity) {
        FactionSkinTheme theme = new FactionSkinTheme(entity.getFactionId());
        theme.setName(entity.getName());
        theme.setDefaultSkinId(entity.getDefaultSkinId());
        theme.setPrimaryColor(entity.getPrimaryColor());
        theme.setSecondaryColor(entity.getSecondaryColor());
        theme.setAccentColor(entity.getAccentColor());
        theme.setEmblemSymbol(entity.getEmblemSymbol());
        return theme;
    }

    // ---- Skin Variant management ----

    /**
     * Creates or updates a skin variant.
     *
     * @param variant the variant to register
     * @return the saved variant
     */
    @Transactional
    public SkinVariant registerVariant(SkinVariant variant) {
        Objects.requireNonNull(variant, "Variant must not be null");
        // Verify base skin exists
        if (!skinRepository.existsBySkinId(variant.getBaseSkinId())) {
            throw new IllegalArgumentException("Base skin '" + variant.getBaseSkinId() + "' not found");
        }
        SkinVariantEntity entity = variantRepository.findByVariantId(variant.getId())
                .orElse(null);
        if (entity != null) {
            entity.setName(variant.getName());
            entity.setDescription(variant.getDescription());
            entity.setPrimaryColor(variant.getPrimaryColor());
            entity.setSecondaryColor(variant.getSecondaryColor());
            entity.setAccentColor(variant.getAccentColor());
            entity.setTextureReference(variant.getTextureReference());
        } else {
            entity = toVariantEntity(variant);
        }
        entity = variantRepository.save(entity);
        return toVariantDomain(entity);
    }

    /**
     * Removes a skin variant by ID.
     *
     * @param variantId the variant ID
     * @return true if the variant was removed, false if not found
     */
    @Transactional
    public boolean removeVariant(String variantId) {
        Objects.requireNonNull(variantId, "Variant ID must not be null");
        if (!variantRepository.existsByVariantId(variantId)) {
            return false;
        }
        variantRepository.deleteByVariantId(variantId);
        return true;
    }

    /**
     * Looks up a variant by ID.
     *
     * @param variantId the variant ID
     * @return the variant, or null if not found
     */
    @Transactional(readOnly = true)
    public SkinVariant getVariant(String variantId) {
        Objects.requireNonNull(variantId, "Variant ID must not be null");
        return variantRepository.findByVariantId(variantId)
                .map(this::toVariantDomain)
                .orElse(null);
    }

    /**
     * Returns all variants for a given base skin.
     *
     * @param baseSkinId the base skin ID
     * @return list of variants for this base skin
     */
    @Transactional(readOnly = true)
    public List<SkinVariant> getVariants(String baseSkinId) {
        Objects.requireNonNull(baseSkinId, "Base skin ID must not be null");
        return variantRepository.findByBaseSkinId(baseSkinId).stream()
                .map(this::toVariantDomain)
                .collect(Collectors.toList());
    }

    /**
     * Resolves a variant against its base skin to produce a complete Skin.
     * The resolved skin inherits all properties from the base skin, with overrides
     * applied for any non-null values in the variant.
     *
     * @param variantId the variant ID
     * @return the resolved skin, or null if variant or base skin not found
     */
    @Transactional(readOnly = true)
    public Skin resolveVariant(String variantId) {
        Objects.requireNonNull(variantId, "Variant ID must not be null");
        SkinVariant variant = getVariant(variantId);
        if (variant == null) {
            return null;
        }
        Skin baseSkin = getSkin(variant.getBaseSkinId());
        if (baseSkin == null) {
            return null;
        }
        return variant.resolve(baseSkin);
    }

    // ---- Variant Entity <-> Domain conversion ----

    private SkinVariantEntity toVariantEntity(SkinVariant variant) {
        SkinVariantEntity entity = new SkinVariantEntity(variant.getId(), variant.getBaseSkinId());
        entity.setName(variant.getName());
        entity.setDescription(variant.getDescription());
        entity.setPrimaryColor(variant.getPrimaryColor());
        entity.setSecondaryColor(variant.getSecondaryColor());
        entity.setAccentColor(variant.getAccentColor());
        entity.setTextureReference(variant.getTextureReference());
        return entity;
    }

    private SkinVariant toVariantDomain(SkinVariantEntity entity) {
        SkinVariant variant = new SkinVariant(entity.getVariantId(), entity.getBaseSkinId());
        variant.setName(entity.getName());
        variant.setDescription(entity.getDescription());
        variant.setPrimaryColor(entity.getPrimaryColor());
        variant.setSecondaryColor(entity.getSecondaryColor());
        variant.setAccentColor(entity.getAccentColor());
        variant.setTextureReference(entity.getTextureReference());
        return variant;
    }

    /**
     * Converts a variant entity to a response DTO.
     */
    public SkinVariantResponse toVariantResponse(SkinVariantEntity entity) {
        SkinVariantResponse response = new SkinVariantResponse();
        response.setId(entity.getVariantId());
        response.setBaseSkinId(entity.getBaseSkinId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setPrimaryColor(entity.getPrimaryColor());
        response.setSecondaryColor(entity.getSecondaryColor());
        response.setAccentColor(entity.getAccentColor());
        response.setTextureReference(entity.getTextureReference());
        return response;
    }
}
