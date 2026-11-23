using Godot;

namespace DoraMCU.Procedural
{
    /// <summary>
    /// Procedural ship generator — creates simple ship models from box/cylinder meshes.
    /// </summary>
    public static class ShipGenerator
    {
        /// <summary>
        /// Create a ship model based on type.
        /// </summary>
        public static MeshInstance3D CreateShip(string shipType)
        {
            var ship = new MeshInstance3D();

            switch (shipType?.ToLower())
            {
                case "fighter":
                    CreateFighter(ship);
                    break;
                case "corvette":
                    CreateCorvette(ship);
                    break;
                case "frigate":
                    CreateFrigate(ship);
                    break;
                case "cruiser":
                    CreateCruiser(ship);
                    break;
                case "dreadnought":
                    CreateDreadnought(ship);
                    break;
                default:
                    CreateFighter(ship);
                    break;
            }

            return ship;
        }

        static void CreateFighter(MeshInstance3D ship)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(0.3f, 0.15f, 0.8f)
            };
            ship.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.6f, 0.6f, 0.7f),
                Metallic = 0.7f,
                Roughness = 0.3f
            };
            ship.MaterialOverride = material;
        }

        static void CreateCorvette(MeshInstance3D ship)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(0.5f, 0.25f, 1.5f)
            };
            ship.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.5f, 0.6f, 0.8f),
                Metallic = 0.6f,
                Roughness = 0.4f
            };
            ship.MaterialOverride = material;
        }

        static void CreateFrigate(MeshInstance3D ship)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(0.8f, 0.4f, 2.5f)
            };
            ship.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.4f, 0.5f, 0.7f),
                Metallic = 0.5f,
                Roughness = 0.5f
            };
            ship.MaterialOverride = material;
        }

        static void CreateCruiser(MeshInstance3D ship)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(1.2f, 0.6f, 4.0f)
            };
            ship.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.3f, 0.4f, 0.6f),
                Metallic = 0.5f,
                Roughness = 0.5f
            };
            ship.MaterialOverride = material;
        }

        static void CreateDreadnought(MeshInstance3D ship)
        {
            var mesh = new BoxMesh
            {
                Size = new Vector3(2.0f, 1.0f, 6.0f)
            };
            ship.Mesh = mesh;

            var material = new StandardMaterial3D
            {
                AlbedoColor = new Color(0.2f, 0.3f, 0.5f),
                Metallic = 0.6f,
                Roughness = 0.4f
            };
            ship.MaterialOverride = material;
        }
    }
}
