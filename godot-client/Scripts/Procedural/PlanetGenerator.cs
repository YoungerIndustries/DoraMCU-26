using Godot;
using System;

namespace DoraMCU.Procedural
{
    /// <summary>
    /// Procedural planet generator — creates sphere meshes with biome-based colors.
    /// </summary>
    public static class PlanetGenerator
    {
        // Biome colors
        static readonly Color[] BiomeColors =
        {
            new Color(0.2f, 0.5f, 0.2f),  // Forest
            new Color(0.7f, 0.6f, 0.3f),  // Desert
            new Color(0.3f, 0.4f, 0.7f),  // Ocean
            new Color(0.8f, 0.8f, 0.9f),  // Ice
            new Color(0.5f, 0.3f, 0.1f),  // Rocky
            new Color(0.6f, 0.2f, 0.2f),  // Lava
            new Color(0.4f, 0.6f, 0.4f),  // Jungle
            new Color(0.5f, 0.5f, 0.5f),  // Barren
        };

        /// <summary>
        /// Create a planet mesh instance.
        /// </summary>
        public static MeshInstance3D CreatePlanet(int size, int atmosphere, int temperature, int hydrographics, System.Random rand)
        {
            var meshInstance = new MeshInstance3D();

            // Size affects radius (1-10 maps to 0.5-3.0)
            float radius = 0.5f + (size / 10.0f) * 2.5f;

            // Create sphere
            var sphere = new SphereMesh
            {
                Radius = radius,
                RadialSegments = 16,
                Rings = 12
            };
            meshInstance.Mesh = sphere;

            // Determine biome color
            Color color;
            if (temperature >= 8)
                color = BiomeColors[5]; // Lava
            else if (temperature <= 2)
                color = BiomeColors[3]; // Ice
            else if (hydrographics >= 8)
                color = BiomeColors[2]; // Ocean
            else if (hydrographics >= 5 && atmosphere >= 5)
                color = BiomeColors[rand.Next(0, 3)]; // Forest/Ocean/Jungle
            else if (atmosphere <= 2)
                color = BiomeColors[rand.Next(4, 8)]; // Rocky/Barren
            else
                color = BiomeColors[rand.Next(0, BiomeColors.Length)];

            // Create material
            var material = new StandardMaterial3D
            {
                AlbedoColor = color,
                Metallic = 0.1f,
                Roughness = 0.8f
            };
            meshInstance.MaterialOverride = material;

            return meshInstance;
        }

        /// <summary>
        /// Create an atmosphere glow for a planet.
        /// </summary>
        public static MeshInstance3D CreateAtmosphere(float radius, int atmosphereLevel)
        {
            if (atmosphereLevel < 3) return null;

            var meshInstance = new MeshInstance3D();
            float glowRadius = radius * 1.15f;

            var sphere = new SphereMesh
            {
                Radius = glowRadius,
                RadialSegments = 16,
                Rings = 12
            };
            meshInstance.Mesh = sphere;

            float alpha = (atmosphereLevel / 10.0f) * 0.3f;
            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.5f, 0.7f, 1.0f, alpha),
                
            };
            meshInstance.MaterialOverride = material;

            return meshInstance;
        }
    }
}
