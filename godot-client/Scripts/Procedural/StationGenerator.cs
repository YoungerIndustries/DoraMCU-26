using Godot;

namespace DoraMCU.Procedural
{
    /// <summary>
    /// Procedural station generator — creates space station models.
    /// </summary>
    public static class StationGenerator
    {
        /// <summary>
        /// Create a station model.
        /// </summary>
        public static MeshInstance3D CreateStation(string stationType)
        {
            var station = new MeshInstance3D();

            switch (stationType?.ToLower())
            {
                case "fuel":
                    CreateFuelStation(station);
                    break;
                case "repair":
                    CreateRepairStation(station);
                    break;
                case "trade":
                    CreateTradeStation(station);
                    break;
                default:
                    CreateGenericStation(station);
                    break;
            }

            return station;
        }

        static void CreateFuelStation(MeshInstance3D station)
        {
            var mesh = new CylinderMesh
            {
                TopRadius = 0.5f,
                BottomRadius = 0.5f,
                Height = 0.3f,
                RadialSegments = 8,
                Rings = 1
            };
            station.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.8f, 0.5f, 0.2f),
                Metallic = 0.6f,
                Roughness = 0.4f
            };
            station.MaterialOverride = material;
        }

        static void CreateRepairStation(MeshInstance3D station)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(0.8f, 0.4f, 0.8f)
            };
            station.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.3f, 0.7f, 0.3f),
                Metallic = 0.5f,
                Roughness = 0.5f
            };
            station.MaterialOverride = material;
        }

        static void CreateTradeStation(MeshInstance3D station)
        {
            var mesh = new CylinderMesh
            {
                TopRadius = 0.7f,
                BottomRadius = 0.7f,
                Height = 0.5f,
                RadialSegments = 12,
                Rings = 1
            };
            station.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.6f, 0.4f, 0.8f),
                Metallic = 0.5f,
                Roughness = 0.5f
            };
            station.MaterialOverride = material;
        }

        static void CreateGenericStation(MeshInstance3D station)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(0.6f, 0.3f, 0.6f)
            };
            station.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.5f, 0.5f, 0.6f),
                Metallic = 0.6f,
                Roughness = 0.4f
            };
            station.MaterialOverride = material;
        }
    }
}
