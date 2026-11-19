using Godot;
using System;
using System.Collections.Generic;
using System.Linq;

namespace DoraMCU.Core
{
    /// <summary>
    /// Demo mode manager - generates a procedural galaxy without needing a backend server.
    /// Creates stars, planets, ships, and a starfield background.
    /// Press D to toggle demo on/off at runtime.
    /// </summary>
    public partial class DemoModeManager : Node3D
    {
        [Export]
        public int SectorCount = 25;

        [Export]
        public int StarsPerSector = 3;

        [Export]
        public int PlanetsPerStar = 4;

        [Export]
        public int ShipsPerSystem = 2;

        [Export]
        public int StarfieldCount = 200;

        private Random _random = new Random();
        private List<Node3D> _demoObjects = new List<Node3D>();
        private bool _isActive;

        public override void _Ready()
        {
            GD.Print("[DemoModeManager] Ready");
        }

        public override void _Input(InputEvent @event)
        {
            if (@event is InputEventKey keyEvent && keyEvent.Pressed && !keyEvent.Echo)
            {
                if (keyEvent.KeyCode == Key.D)
                {
                    ToggleDemo();
                }
            }
        }

        public void ToggleDemo()
        {
            _isActive = !_isActive;
            if (_isActive)
            {
                GenerateGalaxy();
                GD.Print("[DemoModeManager] Demo galaxy generated");
            }
            else
            {
                ClearGalaxy();
                GD.Print("[DemoModeManager] Demo galaxy cleared");
            }
        }

        public void StartDemo()
        {
            _isActive = true;
            GenerateGalaxy();
            GD.Print("[DemoModeManager] Demo started automatically");
        }

        private void GenerateGalaxy()
        {
            // Create central sun
            CreateSun();

            // Create starfield background
            CreateStarfield();

            // Create sectors with stars, planets, and ships
            for (int i = 0; i < SectorCount; i++)
            {
                CreateSector(i);
            }
        }

        private void CreateSun()
        {
            var sun = new MeshInstance3D
            {
                Name = "Sun"
            };

            var sphere = new SphereMesh
            {
                Radius = 5.0f,
                RadiusSegments = 32,
                HeightSegments = 16
            };
            sun.SetMesh(sphere);

            var material = new StandardMaterial3D
            {
                AlbedoColor = Colors.Yellow,
                Emission = Colors.Orange,
                EmissionEnergyMultiplier = 2.0f
            };
            sun.MaterialOverride = material;

            AddChild(sun);
            _demoObjects.Add(sun);

            // Add point light for the sun
            var light = new PointLight3D
            {
                Color = Colors.Yellow,
                Energy = 5.0f,
                OmniRange = 100.0f
            };
            sun.AddChild(light);
        }

        private void CreateStarfield()
        {
            for (int i = 0; i < StarfieldCount; i++)
            {
                var star = new MeshInstance3D
                {
                    Name = $"Star_{i}"
                };

                var sphere = new SphereMesh
                {
                    Radius = 0.1f + _random.NextDouble() * 0.2f,
                    RadiusSegments = 8,
                    HeightSegments = 4
                };
                star.SetMesh(sphere);

                var material = new StandardMaterial3D
                {
                    AlbedoColor = Colors.White,
                    Emission = Colors.White,
                    EmissionEnergyMultiplier = 1.0f
                };
                star.MaterialOverride = material;

                // Random position on a large sphere
                var theta = _random.NextDouble() * MathF.PI * 2;
                var phi = MathF.Acos(2.0f * (float)_random.NextDouble() - 1.0f);
                var radius = 200.0f;

                star.Position = new Vector3(
                    radius * MathF.Sin(phi) * MathF.Cos(theta),
                    radius * MathF.Sin(phi) * MathF.Sin(theta),
                    radius * MathF.Cos(phi)
                );

                AddChild(star);
                _demoObjects.Add(star);
            }
        }

        private void CreateSector(int sectorIndex)
        {
            var sector = new Node3D
            {
                Name = $"Sector_{sectorIndex}"
            };

            // Random position for sector
            var angle = (sectorIndex * 137.508f) * MathF.PI / 180f; // Golden angle
            var distance = 20.0f + _random.NextDouble() * 60.0f;

            sector.Position = new Vector3(
                MathF.Cos(angle) * distance,
                (_random.NextDouble() - 0.5f) * 20.0f,
                MathF.Sin(angle) * distance
            );

            AddChild(sector);
            _demoObjects.Add(sector);

            // Create stars in this sector
            for (int s = 0; s < StarsPerSector; s++)
            {
                CreateStar(sector, s);
            }
        }

        private void CreateStar(Node3D parent, int starIndex)
        {
            var star = new Node3D
            {
                Name = $"Star_{starIndex}"
            };

            star.Position = new Vector3(
                (_random.NextDouble() - 0.5f) * 10.0f,
                (_random.NextDouble() - 0.5f) * 5.0f,
                (_random.NextDouble() - 0.5f) * 10.0f
            );

            parent.AddChild(star);

            // Star mesh
            var starMesh = new MeshInstance3D
            {
                Name = "StarMesh"
            };

            var sphere = new SphereMesh
            {
                Radius = 1.0f + _random.NextDouble() * 1.5f,
                RadiusSegments = 16,
                HeightSegments = 8
            };
            starMesh.SetMesh(sphere);

            // Random star color
            var colors = new Color[] { Colors.White, Colors.Yellow, Colors.Orange, Colors.Blue, Colors.Cyan };
            var starColor = colors[_random.Next(colors.Length)];

            var material = new StandardMaterial3D
            {
                AlbedoColor = starColor,
                Emission = starColor,
                EmissionEnergyMultiplier = 1.5f
            };
            starMesh.MaterialOverride = material;

            star.AddChild(starMesh);
            _demoObjects.Add(starMesh);

            // Point light
            var light = new PointLight3D
            {
                Color = starColor,
                Energy = 2.0f,
                OmniRange = 15.0f
            };
            star.AddChild(light);

            // Create orbiting planets
            for (int p = 0; p < PlanetsPerStar; p++)
            {
                CreatePlanet(star, p);
            }

            // Create ships
            for (int sh = 0; sh < ShipsPerSystem; sh++)
            {
                CreateShip(star, sh);
            }
        }

        private void CreatePlanet(Node3D parent, int planetIndex)
        {
            var planet = new MeshInstance3D
            {
                Name = $"Planet_{planetIndex}"
            };

            var radius = 0.3f + _random.NextDouble() * 0.5f;
            var sphere = new SphereMesh
            {
                Radius = radius,
                RadiusSegments = 16,
                HeightSegments = 8
            };
            planet.SetMesh(sphere);

            // Random planet color based on biome
            var biomeColors = new Color[]
            {
                Colors.Blue,    // Ocean
                Colors.Green,   // Forest
                Colors.Brown,   // Desert
                Colors.LightBlue, // Ice
                Colors.Red      // Lava
            };
            var biomeColor = biomeColors[_random.Next(biomeColors.Length)];

            var material = new StandardMaterial3D
            {
                AlbedoColor = biomeColor
            };
            planet.MaterialOverride = material;

            // Orbit position
            var orbitRadius = 3.0f + planetIndex * 1.5f;
            var orbitAngle = _random.NextDouble() * MathF.PI * 2;

            planet.Position = new Vector3(
                MathF.Cos(orbitAngle) * orbitRadius,
                (_random.NextDouble() - 0.5f) * 0.5f,
                MathF.Sin(orbitAngle) * orbitRadius
            );

            parent.AddChild(planet);
            _demoObjects.Add(planet);
        }

        private void CreateShip(Node3D parent, int shipIndex)
        {
            var ship = new MeshInstance3D
            {
                Name = $"Ship_{shipIndex}"
            };

            // Simple ship shape (box)
            var box = new BoxMesh
            {
                Size = new Vector3(0.5f, 0.2f, 1.0f)
            };
            ship.SetMesh(box);

            var material = new StandardMaterial3D
            {
                AlbedoColor = Colors.Gray
            };
            ship.MaterialOverride = material;

            // Random position near star
            ship.Position = new Vector3(
                (_random.NextDouble() - 0.5f) * 8.0f,
                (_random.NextDouble() - 0.5f) * 3.0f,
                (_random.NextDouble() - 0.5f) * 8.0f
            );

            ship.Rotation = new Vector3(
                (_random.NextDouble() - 0.5f) * MathF.PI,
                _random.NextDouble() * MathF.PI * 2,
                (_random.NextDouble() - 0.5f) * MathF.PI
            );

            parent.AddChild(ship);
            _demoObjects.Add(ship);
        }

        private void ClearGalaxy()
        {
            foreach (var obj in _demoObjects)
            {
                if (obj.IsInsideTree())
                {
                    obj.QueueFree();
                }
            }
            _demoObjects.Clear();
        }

        public override void _ExitTree()
        {
            ClearGalaxy();
        }
    }
}
