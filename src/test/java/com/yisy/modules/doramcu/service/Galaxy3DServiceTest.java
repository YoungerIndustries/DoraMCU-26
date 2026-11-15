package com.yisy.modules.doramcu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yisy.modules.doramcu.service.Galaxy3DService.CameraPreset;
import static com.yisy.modules.doramcu.service.Galaxy3DService.CelestialBody3D;
import static com.yisy.modules.doramcu.service.Galaxy3DService.Galaxy3DData;
import static com.yisy.modules.doramcu.service.Galaxy3DService.Position3D;
import static org.junit.jupiter.api.Assertions.*;

class Galaxy3DServiceTest {

    private Galaxy3DService service;

    @BeforeEach
    void setUp() {
        service = new Galaxy3DService();
    }

    @Nested
    class BodyTypes {
        @Test
        void returns_all_body_types() {
            List<Galaxy3DService.BodyType> types = service.getBodyTypes();
            assertEquals(12, types.size());
        }

        @Test
        void has_star_type() {
            assertTrue(service.getBodyTypes().contains(Galaxy3DService.BodyType.STAR));
        }

        @Test
        void has_planet_type() {
            assertTrue(service.getBodyTypes().contains(Galaxy3DService.BodyType.PLANET));
        }

        @Test
        void has_nebula_type() {
            assertTrue(service.getBodyTypes().contains(Galaxy3DService.BodyType.NEBULA));
        }

        @Test
        void has_black_hole_type() {
            assertTrue(service.getBodyTypes().contains(Galaxy3DService.BodyType.BLACK_HOLE));
        }

        @Test
        void body_type_has_id_and_description() {
            Galaxy3DService.BodyType star = Galaxy3DService.BodyType.STAR;
            assertEquals("star", star.getId());
            assertNotNull(star.getDescription());
        }
    }

    @Nested
    class ColorSchemes {
        @Test
        void returns_all_color_schemes() {
            List<Galaxy3DService.ColorScheme> schemes = service.getColorSchemes();
            assertEquals(5, schemes.size());
        }

        @Test
        void has_realistic_scheme() {
            assertTrue(service.getColorSchemes().contains(Galaxy3DService.ColorScheme.REALISTIC));
        }

        @Test
        void has_scifi_scheme() {
            assertTrue(service.getColorSchemes().contains(Galaxy3DService.ColorScheme.SCIFI));
        }

        @Test
        void has_heatmap_scheme() {
            assertTrue(service.getColorSchemes().contains(Galaxy3DService.ColorScheme.HEATMAP));
        }

        @Test
        void color_scheme_has_id_and_description() {
            Galaxy3DService.ColorScheme realistic = Galaxy3DService.ColorScheme.REALISTIC;
            assertEquals("realistic", realistic.getId());
            assertNotNull(realistic.getDescription());
        }
    }

    @Nested
    class Position3DTests {
        @Test
        void creates_position_with_coordinates() {
            Galaxy3DService.Position3D pos = new Galaxy3DService.Position3D(1.0, 2.0, 3.0);
            assertEquals(1.0, pos.getX());
            assertEquals(2.0, pos.getY());
            assertEquals(3.0, pos.getZ());
        }

        @Test
        void calculates_distance_between_positions() {
            Galaxy3DService.Position3D p1 = new Galaxy3DService.Position3D(0, 0, 0);
            Galaxy3DService.Position3D p2 = new Galaxy3DService.Position3D(3, 4, 0);
            assertEquals(5.0, p1.distanceTo(p2), 0.001);
        }

        @Test
        void distance_to_self_is_zero() {
            Galaxy3DService.Position3D pos = new Galaxy3DService.Position3D(1, 2, 3);
            assertEquals(0.0, pos.distanceTo(pos), 0.001);
        }

        @Test
        void to_string_format() {
            Galaxy3DService.Position3D pos = new Galaxy3DService.Position3D(1.5, 2.5, 3.5);
            String str = pos.toString();
            assertTrue(str.contains("1.50"));
            assertTrue(str.contains("2.50"));
            assertTrue(str.contains("3.50"));
        }
    }

    @Nested
    class CelestialBody3DTests {
        @Test
        void creates_star_body() {
            Galaxy3DService.CelestialBody3D body = Galaxy3DService.CelestialBody3D.builder("star-1", Galaxy3DService.BodyType.STAR)
                    .name("Alpha Centauri")
                    .position(100, 50, 200)
                    .size(5.0)
                    .color("#ffffcc")
                    .property("temperature", 5778)
                    .build();

            assertEquals("star-1", body.getId());
            assertEquals("Alpha Centauri", body.getName());
            assertEquals(Galaxy3DService.BodyType.STAR, body.getType());
            assertEquals(5.0, body.getSize());
            assertEquals("#ffffcc", body.getColor());
            assertEquals(5778, body.getProperties().get("temperature"));
        }

        @Test
        void creates_planet_with_parent() {
            Galaxy3DService.CelestialBody3D body = Galaxy3DService.CelestialBody3D.builder("planet-1", Galaxy3DService.BodyType.PLANET)
                    .name("Earth")
                    .position(100, 0, 100)
                    .size(1.0)
                    .color("#4488ff")
                    .parentId("star-1")
                    .build();

            assertEquals("star-1", body.getParentId());
            assertEquals(Galaxy3DService.BodyType.PLANET, body.getType());
        }

        @Test
        void default_values() {
            Galaxy3DService.CelestialBody3D body = Galaxy3DService.CelestialBody3D.builder("test", Galaxy3DService.BodyType.ASTEROID)
                    .build();

            assertEquals("", body.getName());
            assertEquals(1.0, body.getSize());
            assertEquals("#ffffff", body.getColor());
            assertNull(body.getParentId());
        }

        @Test
        void position_builder_shortcut() {
            Galaxy3DService.CelestialBody3D body = Galaxy3DService.CelestialBody3D.builder("test", Galaxy3DService.BodyType.STAR)
                    .position(10, 20, 30)
                    .build();

            assertEquals(10, body.getPosition().getX());
            assertEquals(20, body.getPosition().getY());
            assertEquals(30, body.getPosition().getZ());
        }
    }

    @Nested
    class CameraPresetTests {
        @Test
        void creates_camera_preset() {
            Galaxy3DService.CameraPreset preset = Galaxy3DService.CameraPreset.builder("top_down", "Top Down")
                    .position(0, 1000, 0)
                    .target(0, 0, 0)
                    .fov(45.0)
                    .minDistance(1.0)
                    .maxDistance(5000.0)
                    .build();

            assertEquals("top_down", preset.getId());
            assertEquals("Top Down", preset.getName());
            assertEquals(45.0, preset.getFov());
            assertEquals(1.0, preset.getMinDistance());
            assertEquals(5000.0, preset.getMaxDistance());
        }

        @Test
        void default_values() {
            Galaxy3DService.CameraPreset preset = Galaxy3DService.CameraPreset.builder("test", "Test")
                    .build();

            assertEquals(60.0, preset.getFov());
            assertEquals(1.0, preset.getMinDistance());
            assertEquals(10000.0, preset.getMaxDistance());
        }

        @Test
        void position_builder_shortcut() {
            Galaxy3DService.CameraPreset preset = Galaxy3DService.CameraPreset.builder("test", "Test")
                    .position(100, 200, 300)
                    .target(0, 0, 0)
                    .build();

            assertEquals(100, preset.getPosition().getX());
            assertEquals(200, preset.getPosition().getY());
            assertEquals(300, preset.getPosition().getZ());
        }
    }

    @Nested
    class Galaxy3DDataTests {
        @Test
        void creates_galaxy_data() {
            List<Galaxy3DService.CelestialBody3D> bodies = Arrays.asList(
                    Galaxy3DService.CelestialBody3D.builder("star-1", Galaxy3DService.BodyType.STAR).build(),
                    Galaxy3DService.CelestialBody3D.builder("planet-1", Galaxy3DService.BodyType.PLANET).build()
            );

            Galaxy3DService.Galaxy3DData data = Galaxy3DService.Galaxy3DData.builder("Milky Way")
                    .bodies(bodies)
                    .colorScheme(Galaxy3DService.ColorScheme.REALISTIC)
                    .build();

            assertEquals("Milky Way", data.getGalaxyName());
            assertEquals(2, data.getTotalBodies());
            assertEquals(Galaxy3DService.ColorScheme.REALISTIC, data.getColorScheme());
        }

        @Test
        void counts_body_types() {
            List<Galaxy3DService.CelestialBody3D> bodies = Arrays.asList(
                    Galaxy3DService.CelestialBody3D.builder("star-1", Galaxy3DService.BodyType.STAR).build(),
                    Galaxy3DService.CelestialBody3D.builder("star-2", Galaxy3DService.BodyType.STAR).build(),
                    Galaxy3DService.CelestialBody3D.builder("planet-1", Galaxy3DService.BodyType.PLANET).build()
            );

            Galaxy3DService.Galaxy3DData data = Galaxy3DService.Galaxy3DData.builder("Test")
                    .bodies(bodies)
                    .build();

            Map<Galaxy3DService.BodyType, Integer> counts = data.getBodyCounts();
            assertEquals(2, counts.get(Galaxy3DService.BodyType.STAR).intValue());
            assertEquals(1, counts.get(Galaxy3DService.BodyType.PLANET).intValue());
            assertEquals(0, counts.get(Galaxy3DService.BodyType.NEBULA).intValue());
        }

        @Test
        void bodies_list_is_copy() {
            List<Galaxy3DService.CelestialBody3D> bodies = new ArrayList<>();
            Galaxy3DService.Galaxy3DData data = Galaxy3DService.Galaxy3DData.builder("Test")
                    .bodies(bodies)
                    .build();

            bodies.add(Galaxy3DService.CelestialBody3D.builder("added", Galaxy3DService.BodyType.STAR).build());
            assertEquals(0, data.getTotalBodies());
        }
    }

    @Nested
    class GetGalaxy3DData {
        @Test
        void returns_galaxy_data_for_valid_name() {
            Optional<Galaxy3DService.Galaxy3DData> result = service.getGalaxy3DData("Milky Way", Galaxy3DService.ColorScheme.REALISTIC);
            assertTrue(result.isPresent());
            assertEquals("Milky Way", result.get().getGalaxyName());
        }

        @Test
        void returns_empty_for_null_name() {
            Optional<Galaxy3DService.Galaxy3DData> result = service.getGalaxy3DData(null, Galaxy3DService.ColorScheme.REALISTIC);
            assertFalse(result.isPresent());
        }

        @Test
        void returns_empty_for_blank_name() {
            Optional<Galaxy3DService.Galaxy3DData> result = service.getGalaxy3DData("  ", Galaxy3DService.ColorScheme.REALISTIC);
            assertFalse(result.isPresent());
        }

        @Test
        void returns_data_with_bodies() {
            Optional<Galaxy3DService.Galaxy3DData> result = service.getGalaxy3DData("Test Galaxy", Galaxy3DService.ColorScheme.REALISTIC);
            assertTrue(result.isPresent());
            assertTrue(result.get().getTotalBodies() > 0);
        }

        @Test
        void returns_data_with_camera_presets() {
            Optional<Galaxy3DService.Galaxy3DData> result = service.getGalaxy3DData("Test Galaxy", Galaxy3DService.ColorScheme.REALISTIC);
            assertTrue(result.isPresent());
            assertEquals(5, result.get().getCameraPresets().size());
        }

        @Test
        void uses_default_color_scheme_for_null() {
            Optional<Galaxy3DService.Galaxy3DData> result = service.getGalaxy3DData("Test Galaxy", null);
            assertTrue(result.isPresent());
            assertEquals(Galaxy3DService.ColorScheme.REALISTIC, result.get().getColorScheme());
        }

        @Test
        void caches_galaxy_data() {
            service.getGalaxy3DData("Cached Galaxy", Galaxy3DService.ColorScheme.REALISTIC);
            Map<String, Object> stats = service.getStatistics();
            assertEquals(1, stats.get("cachedGalaxies"));
        }

        @Test
        void returns_cached_data_on_second_call() {
            Optional<Galaxy3DService.Galaxy3DData> result1 = service.getGalaxy3DData("Cached", Galaxy3DService.ColorScheme.REALISTIC);
            Optional<Galaxy3DService.Galaxy3DData> result2 = service.getGalaxy3DData("Cached", Galaxy3DService.ColorScheme.REALISTIC);
            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertEquals(result1.get().getTotalBodies(), result2.get().getTotalBodies());
        }

        @Test
        void different_color_schemes_cached_separately() {
            service.getGalaxy3DData("Multi", Galaxy3DService.ColorScheme.REALISTIC);
            service.getGalaxy3DData("Multi", Galaxy3DService.ColorScheme.SCIFI);
            Map<String, Object> stats = service.getStatistics();
            assertEquals(2, stats.get("cachedGalaxies"));
        }

        @Test
        void deterministic_generation_for_same_name() {
            Optional<Galaxy3DService.Galaxy3DData> result1 = service.getGalaxy3DData("Deterministic", Galaxy3DService.ColorScheme.REALISTIC);
            service.clearCache();
            Optional<Galaxy3DService.Galaxy3DData> result2 = service.getGalaxy3DData("Deterministic", Galaxy3DService.ColorScheme.REALISTIC);
            assertEquals(result1.get().getTotalBodies(), result2.get().getTotalBodies());
        }
    }

    @Nested
    class CameraPresets {
        @Test
        void returns_default_presets() {
            List<Galaxy3DService.CameraPreset> presets = service.getCameraPresets();
            assertEquals(5, presets.size());
        }

        @Test
        void has_top_down_preset() {
            List<Galaxy3DService.CameraPreset> presets = service.getCameraPresets();
            assertTrue(presets.stream().anyMatch(p -> p.getId().equals("top_down")));
        }

        @Test
        void has_galactic_plane_preset() {
            List<Galaxy3DService.CameraPreset> presets = service.getCameraPresets();
            assertTrue(presets.stream().anyMatch(p -> p.getId().equals("galactic_plane")));
        }

        @Test
        void has_close_up_preset() {
            List<Galaxy3DService.CameraPreset> presets = service.getCameraPresets();
            assertTrue(presets.stream().anyMatch(p -> p.getId().equals("close_up")));
        }

        @Test
        void has_wide_angle_preset() {
            List<Galaxy3DService.CameraPreset> presets = service.getCameraPresets();
            assertTrue(presets.stream().anyMatch(p -> p.getId().equals("wide_angle")));
        }

        @Test
        void has_sector_view_preset() {
            List<Galaxy3DService.CameraPreset> presets = service.getCameraPresets();
            assertTrue(presets.stream().anyMatch(p -> p.getId().equals("sector_view")));
        }
    }

    @Nested
    class GetBodiesByType {
        @Test
        void returns_stars_for_star_type() {
            List<Galaxy3DService.CelestialBody3D> bodies = service.getBodiesByType("Test", Galaxy3DService.BodyType.STAR);
            assertTrue(bodies.stream().allMatch(b -> b.getType() == Galaxy3DService.BodyType.STAR));
        }

        @Test
        void returns_planets_for_planet_type() {
            List<Galaxy3DService.CelestialBody3D> bodies = service.getBodiesByType("Test", Galaxy3DService.BodyType.PLANET);
            assertTrue(bodies.stream().allMatch(b -> b.getType() == Galaxy3DService.BodyType.PLANET));
        }

        @Test
        void returns_list_for_comet_type() {
            List<Galaxy3DService.CelestialBody3D> bodies = service.getBodiesByType("Test", Galaxy3DService.BodyType.COMET);
            assertNotNull(bodies);
        }
    }

    @Nested
    class GetBodiesNearPosition {
        @Test
        void returns_bodies_near_origin() {
            Galaxy3DService.Position3D origin = new Galaxy3DService.Position3D(0, 0, 0);
            List<Galaxy3DService.CelestialBody3D> bodies = service.getBodiesNearPosition("Test", origin, 1000);
            assertTrue(bodies.size() > 0);
        }

        @Test
        void returns_fewer_bodies_with_smaller_radius() {
            Galaxy3DService.Position3D origin = new Galaxy3DService.Position3D(0, 0, 0);
            List<Galaxy3DService.CelestialBody3D> large = service.getBodiesNearPosition("Test", origin, 1000);
            List<Galaxy3DService.CelestialBody3D> small = service.getBodiesNearPosition("Test", origin, 1);
            assertTrue(small.size() <= large.size());
        }

        @Test
        void returns_empty_for_far_position() {
            Galaxy3DService.Position3D far = new Galaxy3DService.Position3D(10000, 10000, 10000);
            List<Galaxy3DService.CelestialBody3D> bodies = service.getBodiesNearPosition("Test", far, 1);
            assertEquals(0, bodies.size());
        }
    }

    @Nested
    class Statistics {
        @Test
        void returns_initial_statistics() {
            Map<String, Object> stats = service.getStatistics();
            assertEquals(0, stats.get("cachedGalaxies"));
            assertEquals(1, stats.get("cameraPresets"));
            assertEquals(12, stats.get("bodyTypes"));
            assertEquals(5, stats.get("colorSchemes"));
        }

        @Test
        void cached_galaxies_increases_after_query() {
            service.getGalaxy3DData("Test", Galaxy3DService.ColorScheme.REALISTIC);
            Map<String, Object> stats = service.getStatistics();
            assertEquals(1, stats.get("cachedGalaxies"));
        }
    }

    @Nested
    class CacheManagement {
        @Test
        void clear_cache_removes_all() {
            service.getGalaxy3DData("Test1", Galaxy3DService.ColorScheme.REALISTIC);
            service.getGalaxy3DData("Test2", Galaxy3DService.ColorScheme.REALISTIC);
            service.clearCache();

            Map<String, Object> stats = service.getStatistics();
            assertEquals(0, stats.get("cachedGalaxies"));
        }

        @Test
        void remove_galaxy_cache_removes_specific() {
            service.getGalaxy3DData("RemoveMe", Galaxy3DService.ColorScheme.REALISTIC);
            service.getGalaxy3DData("KeepMe", Galaxy3DService.ColorScheme.REALISTIC);
            service.removeGalaxyCache("RemoveMe");

            Map<String, Object> stats = service.getStatistics();
            assertEquals(1, stats.get("cachedGalaxies"));
        }

        @Test
        void remove_galaxy_cache_removes_all_schemes() {
            service.getGalaxy3DData("RemoveMe", Galaxy3DService.ColorScheme.REALISTIC);
            service.getGalaxy3DData("RemoveMe", Galaxy3DService.ColorScheme.SCIFI);
            service.removeGalaxyCache("RemoveMe");

            Map<String, Object> stats = service.getStatistics();
            assertEquals(0, stats.get("cachedGalaxies"));
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void full_workflow_query_and_filter() {
            Optional<Galaxy3DService.Galaxy3DData> data = service.getGalaxy3DData("Milky Way", Galaxy3DService.ColorScheme.REALISTIC);
            assertTrue(data.isPresent());

            List<Galaxy3DService.CelestialBody3D> stars = service.getBodiesByType("Milky Way", Galaxy3DService.BodyType.STAR);
            assertTrue(stars.size() > 0);

            Galaxy3DService.Position3D origin = new Galaxy3DService.Position3D(0, 0, 0);
            List<Galaxy3DService.CelestialBody3D> near = service.getBodiesNearPosition("Milky Way", origin, 100);
            assertNotNull(near);
        }

        @Test
        void color_scheme_affects_colors() {
            service.clearCache();
            Optional<Galaxy3DService.Galaxy3DData> realistic = service.getGalaxy3DData("ColorTest", Galaxy3DService.ColorScheme.REALISTIC);
            service.clearCache();
            Optional<Galaxy3DService.Galaxy3DData> scifi = service.getGalaxy3DData("ColorTest", Galaxy3DService.ColorScheme.SCIFI);

            assertTrue(realistic.isPresent());
            assertTrue(scifi.isPresent());
            assertEquals(Galaxy3DService.ColorScheme.REALISTIC, realistic.get().getColorScheme());
            assertEquals(Galaxy3DService.ColorScheme.SCIFI, scifi.get().getColorScheme());
        }

        @Test
        void multiple_galaxies_cached_independently() {
            service.getGalaxy3DData("Galaxy1", Galaxy3DService.ColorScheme.REALISTIC);
            service.getGalaxy3DData("Galaxy2", Galaxy3DService.ColorScheme.REALISTIC);
            service.getGalaxy3DData("Galaxy3", Galaxy3DService.ColorScheme.REALISTIC);

            Map<String, Object> stats = service.getStatistics();
            assertEquals(3, stats.get("cachedGalaxies"));
        }
    }
}
