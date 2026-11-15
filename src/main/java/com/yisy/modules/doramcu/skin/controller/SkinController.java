package com.yisy.modules.doramcu.skin.controller;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.Decal;
import com.yisy.modules.doramcu.hex.DamageTier;
import com.yisy.modules.doramcu.hex.DamageOverlay;
import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.hex.DecalType;

import com.yisy.modules.doramcu.skin.*;
import com.yisy.modules.doramcu.skin.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for skin management.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting skins,
 * managing decals, and configuring faction skin themes.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/skins}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/skins")
public class SkinController {

    private static final Logger log = LoggerFactory.getLogger(SkinController.class);

    private final SkinService skinService;

    public SkinController(SkinService skinService) {
        this.skinService = skinService;
    }

    // ---- Skin CRUD ----

    /**
     * Returns all registered skins.
     */
    @GetMapping
    public ResponseEntity<List<SkinResponse>> getAllSkins(
            @RequestParam(required = false) SkinType type,
            @RequestParam(required = false) String faction) {
        log.debug("GET /api/v1/skins type={} faction={}", type, faction);
        List<Skin> skins;
        if (type != null) {
            skins = skinService.getSkinsByType(type);
        } else if (faction != null) {
            skins = skinService.getSkinsByFaction(faction);
        } else {
            skins = skinService.getAllSkins();
        }
        return ResponseEntity.ok(toResponseList(skins));
    }

    /**
     * Retrieves a skin by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SkinResponse> getSkin(@PathVariable String id) {
        log.debug("GET /api/v1/skins/{}", id);
        Skin skin = skinService.getSkin(id);
        if (skin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(toResponse(skin));
    }

    /**
     * Creates a new skin.
     */
    @PostMapping
    public ResponseEntity<SkinResponse> createSkin(@RequestBody SkinRequest request) {
        log.debug("POST /api/v1/skins - creating skin: {}", request.getId());
        Skin skin = toDomain(request);
        Skin saved = skinService.registerSkin(skin);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * Updates an existing skin.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SkinResponse> updateSkin(@PathVariable String id, @RequestBody SkinRequest request) {
        log.debug("PUT /api/v1/skins/{} - updating skin", id);
        Skin existing = skinService.getSkin(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Skin skin = toDomain(request);
        Skin updated = skinService.registerSkin(skin);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * Deletes a skin by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkin(@PathVariable String id) {
        log.debug("DELETE /api/v1/skins/{}", id);
        boolean deleted = skinService.removeSkin(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ---- Decal endpoints ----

    /**
     * Returns all decals for a skin.
     */
    @GetMapping("/{id}/decals")
    public ResponseEntity<List<DecalResponse>> getDecals(@PathVariable String id) {
        log.debug("GET /api/v1/skins/{}/decals", id);
        Skin skin = skinService.getSkin(id);
        if (skin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(toDecalResponseList(skin.getDefaultDecals()));
    }

    /**
     * Adds a decal to a skin.
     */
    @PostMapping("/{id}/decals")
    public ResponseEntity<DecalResponse> addDecal(@PathVariable String id, @RequestBody DecalRequest request) {
        log.debug("POST /api/v1/skins/{}/decals", id);
        Skin skin = skinService.getSkin(id);
        if (skin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Decal decal = toDecalDomain(request);
        skin.addDefaultDecal(decal);
        Skin updated = skinService.registerSkin(skin);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDecalResponse(decal));
    }

    /**
     * Removes a decal from a skin.
     */
    @DeleteMapping("/{id}/decals/{decalId}")
    public ResponseEntity<Void> removeDecal(@PathVariable String id, @PathVariable String decalId) {
        log.debug("DELETE /api/v1/skins/{}/decals/{}", id, decalId);
        Skin skin = skinService.getSkin(id);
        if (skin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // Rebuild skin without the decal
        Skin updated = new Skin(skin.getId(), skin.getType());
        updated.setName(skin.getName());
        updated.setDescription(skin.getDescription());
        updated.setPrimaryColor(skin.getPrimaryColor());
        updated.setSecondaryColor(skin.getSecondaryColor());
        updated.setAccentColor(skin.getAccentColor());
        updated.setTextureReference(skin.getTextureReference());
        updated.setFactionId(skin.getFactionId());
        updated.setDamageOverlay(skin.getDamageOverlay());
        for (Decal decal : skin.getDefaultDecals()) {
            if (!decal.getId().equals(decalId)) {
                updated.addDefaultDecal(decal);
            }
        }
        skinService.registerSkin(updated);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ---- Faction theme endpoints ----

    /**
     * Returns all faction skin themes.
     */
    @GetMapping("/factions/themes")
    public ResponseEntity<List<FactionThemeResponse>> getAllFactionThemes() {
        log.debug("GET /api/v1/skins/factions/themes");
        List<FactionSkinTheme> themes = skinService.getAllFactionThemes();
        return ResponseEntity.ok(toFactionResponseList(themes));
    }

    /**
     * Returns the faction skin theme for a given faction ID.
     */
    @GetMapping("/factions/themes/{factionId}")
    public ResponseEntity<FactionThemeResponse> getFactionTheme(@PathVariable String factionId) {
        log.debug("GET /api/v1/skins/factions/themes/{}", factionId);
        FactionSkinTheme theme = skinService.getFactionTheme(factionId);
        if (theme == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(toFactionResponse(theme));
    }

    /**
     * Creates or updates a faction skin theme.
     */
    @PostMapping("/factions/themes")
    public ResponseEntity<FactionThemeResponse> createFactionTheme(@RequestBody FactionThemeRequest request) {
        log.debug("POST /api/v1/skins/factions/themes - faction: {}", request.getFactionId());
        FactionSkinTheme theme = toFactionDomain(request);
        FactionSkinTheme saved = skinService.registerFactionTheme(theme);
        return ResponseEntity.status(HttpStatus.CREATED).body(toFactionResponse(saved));
    }

    /**
     * Deletes a faction skin theme.
     */
    @DeleteMapping("/factions/themes/{factionId}")
    public ResponseEntity<Void> deleteFactionTheme(@PathVariable String factionId) {
        log.debug("DELETE /api/v1/skins/factions/themes/{}", factionId);
        boolean deleted = skinService.removeFactionTheme(factionId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ---- Asset assignment endpoints ----

    /**
     * Assigns a skin to a registered asset.
     */
    @PostMapping("/assign")
    public ResponseEntity<Void> assignSkin(@RequestBody SkinAssignRequest request) {
        log.debug("POST /api/v1/skins/assign - assetType={} assetId={} skinId={}",
                request.getAssetType(), request.getAssetId(), request.getSkinId());
        boolean assigned = skinService.assignSkinToAsset(
                request.getAssetType(), request.getAssetId(), request.getSkinId());
        if (!assigned) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Removes the skin from a registered asset.
     */
    @DeleteMapping("/{assetType}/{assetId}/skin")
    public ResponseEntity<Void> removeSkinFromAsset(
            @PathVariable SkinType assetType,
            @PathVariable int assetId) {
        log.debug("DELETE /api/v1/skins/{}/{} /skin", assetType, assetId);
        boolean removed = skinService.removeSkinFromAsset(assetType, assetId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Returns the visual state of a registered asset, including skin, decals, and damage overlays.
     */
    @GetMapping("/{assetType}/{assetId}/visual-state")
    public ResponseEntity<VisualStateResponse> getVisualState(
            @PathVariable SkinType assetType,
            @PathVariable int assetId) {
        log.debug("GET /api/v1/skins/{}/{} /visual-state", assetType, assetId);
        VisualStateResponse state = skinService.getVisualState(assetType, assetId);
        if (state == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(state);
    }

    // ---- Variant endpoints ----

    /**
     * Returns all variants for a given skin.
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<SkinVariantResponse>> getVariants(@PathVariable String id) {
        log.debug("GET /api/v1/skins/{}/variants", id);
        Skin skin = skinService.getSkin(id);
        if (skin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<SkinVariant> variants = skinService.getVariants(id);
        List<SkinVariantResponse> responses = new ArrayList<>();
        for (SkinVariant v : variants) {
            SkinVariantResponse response = new SkinVariantResponse();
            response.setId(v.getId());
            response.setBaseSkinId(v.getBaseSkinId());
            response.setName(v.getName());
            response.setDescription(v.getDescription());
            response.setPrimaryColor(v.getPrimaryColor());
            response.setSecondaryColor(v.getSecondaryColor());
            response.setAccentColor(v.getAccentColor());
            response.setTextureReference(v.getTextureReference());
            responses.add(response);
        }
        return ResponseEntity.ok(responses);
    }

    /**
     * Creates a new variant for a skin.
     */
    @PostMapping("/{id}/variants")
    public ResponseEntity<SkinVariantResponse> createVariant(
            @PathVariable String id,
            @RequestBody SkinVariantRequest request) {
        log.debug("POST /api/v1/skins/{}/variants", id);
        Skin skin = skinService.getSkin(id);
        if (skin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String variantId = request.getId();
        if (variantId == null || variantId.isEmpty()) {
            throw new IllegalArgumentException("Variant ID must not be empty");
        }
        SkinVariant variant = new SkinVariant(variantId, id);
        if (request.getName() != null) {
            variant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            variant.setDescription(request.getDescription());
        }
        variant.setPrimaryColor(request.getPrimaryColor());
        variant.setSecondaryColor(request.getSecondaryColor());
        variant.setAccentColor(request.getAccentColor());
        variant.setTextureReference(request.getTextureReference());
        SkinVariant saved = skinService.registerVariant(variant);
        SkinVariantResponse response = new SkinVariantResponse();
        response.setId(saved.getId());
        response.setBaseSkinId(saved.getBaseSkinId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        response.setPrimaryColor(saved.getPrimaryColor());
        response.setSecondaryColor(saved.getSecondaryColor());
        response.setAccentColor(saved.getAccentColor());
        response.setTextureReference(saved.getTextureReference());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a variant by ID.
     */
    @DeleteMapping("/{id}/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable String id,
            @PathVariable String variantId) {
        log.debug("DELETE /api/v1/skins/{}/variants/{}", id, variantId);
        boolean deleted = skinService.removeVariant(variantId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Resolves a variant to a complete skin with merged colors.
     */
    @GetMapping("/{id}/variants/{variantId}/resolve")
    public ResponseEntity<SkinResponse> resolveVariant(
            @PathVariable String id,
            @PathVariable String variantId) {
        log.debug("GET /api/v1/skins/{}/variants/{}/resolve", id, variantId);
        Skin resolved = skinService.resolveVariant(variantId);
        if (resolved == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(toResponse(resolved));
    }

    // ---- DTO conversion methods ----

    private SkinResponse toResponse(Skin skin) {
        SkinResponse response = new SkinResponse();
        response.setId(skin.getId());
        response.setType(skin.getType());
        response.setName(skin.getName());
        response.setDescription(skin.getDescription());
        response.setPrimaryColor(skin.getPrimaryColor());
        response.setSecondaryColor(skin.getSecondaryColor());
        response.setAccentColor(skin.getAccentColor());
        response.setTextureReference(skin.getTextureReference());
        response.setFactionId(skin.getFactionId());
        response.setDecals(toDecalResponseList(skin.getDefaultDecals()));
        DamageOverlay overlay = skin.getDamageOverlay();
        if (overlay != null) {
            Map<String, String> overlayMap = new HashMap<>();
            for (Map.Entry<DamageTier, String> entry : overlay.getAllDescriptions().entrySet()) {
                overlayMap.put(entry.getKey().name(), entry.getValue());
            }
            response.setDamageOverlay(overlayMap);
        }
        return response;
    }

    private List<SkinResponse> toResponseList(List<Skin> skins) {
        List<SkinResponse> responses = new ArrayList<>();
        for (Skin skin : skins) {
            responses.add(toResponse(skin));
        }
        return responses;
    }

    private Skin toDomain(SkinRequest request) {
        String id = request.getId();
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Skin ID must not be empty");
        }
        SkinType type = request.getType();
        if (type == null) {
            throw new IllegalArgumentException("Skin type must not be null");
        }
        Skin skin = new Skin(id, type);
        if (request.getName() != null) {
            skin.setName(request.getName());
        }
        if (request.getDescription() != null) {
            skin.setDescription(request.getDescription());
        }
        if (request.getPrimaryColor() != null) {
            skin.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            skin.setSecondaryColor(request.getSecondaryColor());
        }
        if (request.getAccentColor() != null) {
            skin.setAccentColor(request.getAccentColor());
        }
        if (request.getTextureReference() != null) {
            skin.setTextureReference(request.getTextureReference());
        }
        skin.setFactionId(request.getFactionId());

        // Damage overlay
        DamageOverlay overlay = new DamageOverlay();
        if (request.getDamageOverlay() != null) {
            for (Map.Entry<String, String> entry : request.getDamageOverlay().entrySet()) {
                try {
                    DamageTier tier = DamageTier.valueOf(entry.getKey());
                    overlay.setTierDescription(tier, entry.getValue());
                } catch (IllegalArgumentException ignored) {
                    log.warn("Unknown damage tier: {}", entry.getKey());
                }
            }
        }
        skin.setDamageOverlay(overlay);

        // Decals
        if (request.getDecals() != null) {
            for (DecalRequest decalReq : request.getDecals()) {
                Decal decal = toDecalDomain(decalReq);
                skin.addDefaultDecal(decal);
            }
        }

        return skin;
    }

    private DecalResponse toDecalResponse(Decal decal) {
        DecalResponse response = new DecalResponse();
        response.setId(decal.getId());
        response.setType(decal.getType());
        response.setContent(decal.getContent());
        response.setPosition(decal.getPosition());
        response.setSize(decal.getSize());
        response.setRotation(decal.getRotation());
        response.setOpacity(decal.getOpacity());
        response.setColor(decal.getColor());
        return response;
    }

    private List<DecalResponse> toDecalResponseList(List<Decal> decals) {
        List<DecalResponse> responses = new ArrayList<>();
        for (Decal decal : decals) {
            responses.add(toDecalResponse(decal));
        }
        return responses;
    }

    private Decal toDecalDomain(DecalRequest request) {
        String id = request.getId();
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Decal ID must not be empty");
        }
        DecalType type = request.getType();
        if (type == null) {
            throw new IllegalArgumentException("Decal type must not be null");
        }
        Decal decal = new Decal(id, type);
        if (request.getContent() != null) {
            decal.setContent(request.getContent());
        }
        if (request.getPosition() != null) {
            decal.setPosition(request.getPosition());
        }
        decal.setSize(request.getSize());
        decal.setRotation(request.getRotation());
        decal.setOpacity(request.getOpacity());
        if (request.getColor() != null) {
            decal.setColor(request.getColor());
        }
        return decal;
    }

    private FactionThemeResponse toFactionResponse(FactionSkinTheme theme) {
        FactionThemeResponse response = new FactionThemeResponse();
        response.setFactionId(theme.getFactionId());
        response.setName(theme.getName());
        response.setDefaultSkinId(theme.getDefaultSkinId());
        response.setPrimaryColor(theme.getPrimaryColor());
        response.setSecondaryColor(theme.getSecondaryColor());
        response.setAccentColor(theme.getAccentColor());
        response.setEmblemSymbol(theme.getEmblemSymbol());
        return response;
    }

    private List<FactionThemeResponse> toFactionResponseList(List<FactionSkinTheme> themes) {
        List<FactionThemeResponse> responses = new ArrayList<>();
        for (FactionSkinTheme theme : themes) {
            responses.add(toFactionResponse(theme));
        }
        return responses;
    }

    private FactionSkinTheme toFactionDomain(FactionThemeRequest request) {
        String factionId = request.getFactionId();
        if (factionId == null || factionId.isEmpty()) {
            throw new IllegalArgumentException("Faction ID must not be empty");
        }
        FactionSkinTheme theme = new FactionSkinTheme(factionId);
        if (request.getName() != null) {
            theme.setName(request.getName());
        }
        if (request.getDefaultSkinId() != null) {
            theme.setDefaultSkinId(request.getDefaultSkinId());
        }
        if (request.getPrimaryColor() != null) {
            theme.setPrimaryColor(request.getPrimaryColor());
        }
        if (request.getSecondaryColor() != null) {
            theme.setSecondaryColor(request.getSecondaryColor());
        }
        if (request.getAccentColor() != null) {
            theme.setAccentColor(request.getAccentColor());
        }
        if (request.getEmblemSymbol() != null) {
            theme.setEmblemSymbol(request.getEmblemSymbol());
        }
        return theme;
    }
}
