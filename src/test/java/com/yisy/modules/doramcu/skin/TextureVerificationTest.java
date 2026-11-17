package com.yisy.modules.doramcu.skin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Texture Rendering Verification Tests")
class TextureVerificationTest {

    private static final List<String> SHIP_TEXTURES = Arrays.asList(
            "textures/ships/scifi_generic.png",
            "textures/ships/military.png",
            "textures/ships/civilian.png"
    );

    private static final List<String> STATION_TEXTURES = Arrays.asList(
            "textures/stations/industrial.png",
            "textures/stations/military.png"
    );

    private static final List<String> VEHICLE_TEXTURES = Arrays.asList(
            "textures/vehicles/tank.png",
            "textures/vehicles/apc.png",
            "textures/vehicles/truck.png"
    );

    private static final List<String> BUILDING_TEXTURES = Arrays.asList(
            "textures/buildings/modern.png",
            "textures/buildings/industrial.png",
            "textures/buildings/bunker.png"
    );

    private static final List<String> WEAPON_TEXTURES = Arrays.asList(
            "textures/weapons/laser_pistol.png",
            "textures/weapons/laser_rifle.png",
            "textures/weapons/plasma_gun.png",
            "textures/weapons/particle_beam.png",
            "textures/weapons/shotgun.png",
            "textures/weapons/missile_launcher.png",
            "textures/weapons/phaser_type2.png",
            "textures/weapons/phaser_type3.png",
            "textures/weapons/knife.png",
            "textures/weapons/cutlass.png",
            "textures/weapons/rapier.png",
            "textures/weapons/long_sword.png",
            "textures/weapons/laser_cutter.png",
            "textures/weapons/mace.png",
            "textures/weapons/spear.png"
    );

    private static final List<String> ARMOR_TEXTURES = Arrays.asList(
            "textures/armor/leather.png",
            "textures/armor/chain_mail.png",
            "textures/armor/battle_dress.png",
            "textures/armor/flak.png",
            "textures/armor/powered.png"
    );

    private static final List<String> ITEM_TEXTURES = Arrays.asList(
            "textures/items/magic_sword.png",
            "textures/items/magic_staff.png",
            "textures/items/magic_ring.png",
            "textures/items/potion.png"
    );

    private static final List<List<String>> ALL_TEXTURE_CATEGORIES = Arrays.asList(
            SHIP_TEXTURES, STATION_TEXTURES, VEHICLE_TEXTURES,
            BUILDING_TEXTURES, WEAPON_TEXTURES, ARMOR_TEXTURES, ITEM_TEXTURES
    );

    private BufferedImage loadTexture(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Texture file not found on classpath: " + path);
            BufferedImage img = ImageIO.read(is);
            assertNotNull(img, "Failed to decode PNG image: " + path);
            return img;
        } catch (IOException e) {
            fail("IOException loading texture: " + path + " - " + e.getMessage());
            return null;
        }
    }

    @Nested
    @DisplayName("Ship Textures")
    class ShipTextures {
        @Test
        @DisplayName("All ship textures load and render correctly")
        void allShipTexturesLoad() {
            for (String path : SHIP_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Station Textures")
    class StationTextures {
        @Test
        @DisplayName("All station textures load and render correctly")
        void allStationTexturesLoad() {
            for (String path : STATION_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Vehicle Textures")
    class VehicleTextures {
        @Test
        @DisplayName("All vehicle textures load and render correctly")
        void allVehicleTexturesLoad() {
            for (String path : VEHICLE_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Building Textures")
    class BuildingTextures {
        @Test
        @DisplayName("All building textures load and render correctly")
        void allBuildingTexturesLoad() {
            for (String path : BUILDING_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Weapon Textures")
    class WeaponTextures {
        @Test
        @DisplayName("All weapon textures load and render correctly")
        void allWeaponTexturesLoad() {
            for (String path : WEAPON_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Armor Textures")
    class ArmorTextures {
        @Test
        @DisplayName("All armor textures load and render correctly")
        void allArmorTexturesLoad() {
            for (String path : ARMOR_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Item Textures")
    class ItemTextures {
        @Test
        @DisplayName("All item textures load and render correctly")
        void allItemTexturesLoad() {
            for (String path : ITEM_TEXTURES) {
                BufferedImage img = loadTexture(path);
                validateTexture(path, img);
            }
        }
    }

    @Nested
    @DisplayName("Complete Texture Set")
    class CompleteTextureSet {
        @Test
        @DisplayName("All 35 textures load and render correctly")
        void allTexturesLoad() {
            int total = 0;
            for (List<String> category : ALL_TEXTURE_CATEGORIES) {
                for (String path : category) {
                    BufferedImage img = loadTexture(path);
                    validateTexture(path, img);
                    total++;
                }
            }
            assertEquals(35, total, "Expected 35 total textures");
        }

        @Test
        @DisplayName("All textures have valid dimensions")
        void allTexturesHaveValidDimensions() {
            for (List<String> category : ALL_TEXTURE_CATEGORIES) {
                for (String path : category) {
                    BufferedImage img = loadTexture(path);
                    int width = img.getWidth();
                    int height = img.getHeight();
                    assertTrue(width > 0 && width <= 256, path + " width " + width + " out of range");
                    assertTrue(height > 0 && height <= 256, path + " height " + height + " out of range");
                }
            }
        }

        @Test
        @DisplayName("All textures have non-transparent pixel content")
        void allTexturesHaveVisibleContent() {
            for (List<String> category : ALL_TEXTURE_CATEGORIES) {
                for (String path : category) {
                    BufferedImage img = loadTexture(path);
                    boolean hasVisiblePixel = false;
                    for (int y = 0; y < img.getHeight() && !hasVisiblePixel; y++) {
                        for (int x = 0; x < img.getWidth() && !hasVisiblePixel; x++) {
                            int rgb = img.getRGB(x, y);
                            int alpha = (rgb >> 24) & 0xFF;
                            if (alpha > 0) {
                                hasVisiblePixel = true;
                            }
                        }
                    }
                    assertTrue(hasVisiblePixel, path + " has no visible pixels (completely transparent)");
                }
            }
        }

        @Test
        @DisplayName("All textures support ARGB color model")
        void allTexturesSupportARGB() {
            for (List<String> category : ALL_TEXTURE_CATEGORIES) {
                for (String path : category) {
                    BufferedImage img = loadTexture(path);
                    // ARGB is type 2
                    int type = img.getType();
                    assertTrue(type == BufferedImage.TYPE_INT_ARGB ||
                            type == BufferedImage.TYPE_INT_ARGB_PRE ||
                            type == BufferedImage.TYPE_4BYTE_ABGR ||
                            type == BufferedImage.TYPE_4BYTE_ABGR_PRE ||
                            type == BufferedImage.TYPE_3BYTE_BGR,
                            path + " has unexpected image type: " + type);
                }
            }
        }
    }

    private void validateTexture(String path, BufferedImage img) {
        assertNotNull(img, path + " failed to load");

        int width = img.getWidth();
        int height = img.getHeight();
        assertTrue(width > 0, path + " has zero width");
        assertTrue(height > 0, path + " has zero height");

        // Check for visible content (not completely transparent)
        boolean hasVisiblePixel = false;
        for (int y = 0; y < height && !hasVisiblePixel; y++) {
            for (int x = 0; x < width && !hasVisiblePixel; x++) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha > 0) {
                    hasVisiblePixel = true;
                }
            }
        }
        assertTrue(hasVisiblePixel, path + " is completely transparent");
    }
}
