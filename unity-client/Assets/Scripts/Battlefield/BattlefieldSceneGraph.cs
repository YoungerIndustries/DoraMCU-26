using System;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Top-level scene graph returned by the battlefield scene graph API.
    /// <para>
    /// Matches the JSON structure exported by SceneGraphExporter on the server.
    /// </para>
    /// </summary>
    [Serializable]
    public class BattlefieldSceneGraph
    {
        public SceneMetadata metadata;
        public SceneCamera camera;
        public SceneLighting lighting;
        public SceneTerrain terrain;
        public List<SceneBuilding> buildings;
        public List<SceneUnit> attackerUnits;
        public List<SceneUnit> defenderUnits;
        public SceneBiosphere biosphere;
    }

    /// <summary>
    /// Battlefield metadata: location, terrain, tech level, time period.
    /// </summary>
    [Serializable]
    public class SceneMetadata
    {
        public string id;
        public string name;
        public string location;
        public string city;
        public string country;
        public string terrainType;
        public int techLevel;
        public string timePeriod;
    }

    /// <summary>
    /// Camera settings for initial view.
    /// </summary>
    [Serializable]
    public class SceneCamera
    {
        public List<float> position;
        public List<float> target;
        public float fov;

        public Vector3 PositionVector => position != null && position.Count >= 3
            ? new Vector3(position[0], position[1], position[2]) : Vector3.zero;

        public Vector3 TargetVector => target != null && target.Count >= 3
            ? new Vector3(target[0], target[1], target[2]) : Vector3.zero;
    }

    /// <summary>
    /// Lighting settings based on biosphere time of day and weather.
    /// </summary>
    [Serializable]
    public class SceneLighting
    {
        public float ambient;
        public float directional;
        public float sunAngle;
        public float fogDensity;
    }

    /// <summary>
    /// Terrain elevation data: grid dimensions and height values.
    /// </summary>
    [Serializable]
    public class SceneTerrain
    {
        public string type;
        public int width;
        public int height;
        public float cellSize;
        public float baseElevation;
        public List<float> elevations;

        /// <summary>
        /// Gets the elevation at a given grid position.
        /// </summary>
        public float GetElevation(int row, int col)
        {
            if (elevations == null) return baseElevation;
            int index = row * width + col;
            if (index < 0 || index >= elevations.Count) return baseElevation;
            return elevations[index];
        }
    }

    /// <summary>
    /// Building data: type, position, scale, defense properties.
    /// </summary>
    [Serializable]
    public class SceneBuilding
    {
        public string id;
        public string name;
        public string type;
        public int techLevel;
        public float defenseRating;
        public bool providesCover;
        public float coverBonus;
        public bool isStrategic;
        public TransformData transform;
        public string mesh;

        public Vector3 Position => transform?.position != null
            ? new Vector3(transform.position[0], transform.position[1], transform.position[2]) : Vector3.zero;

        public Vector3 Scale => transform?.scale != null
            ? new Vector3(transform.scale[0], transform.scale[1], transform.scale[2]) : Vector3.one;
    }

    /// <summary>
    /// Unit data: type, team, stats, position.
    /// </summary>
    [Serializable]
    public class SceneUnit
    {
        public string id;
        public string name;
        public string type;
        public string team;
        public UnitStats stats;
        public TransformData transform;
        public string mesh;

        public Vector3 Position => transform?.position != null
            ? new Vector3(transform.position[0], transform.position[1], transform.position[2]) : Vector3.zero;

        public float FacingAngle => transform?.rotation != null ? transform.rotation[1] : 0f;
    }

    /// <summary>
    /// Unit combat statistics.
    /// </summary>
    [Serializable]
    public class UnitStats
    {
        public int strength;
        public int maxStrength;
        public int morale;
        public int attack;
        public int defense;
        public int mobility;

        /// <summary>
        /// Health percentage (0-1).
        /// </summary>
        public float HealthPercent => maxStrength > 0 ? (float)strength / maxStrength : 0f;
    }

    /// <summary>
    /// Transform data: position, rotation, scale.
    /// </summary>
    [Serializable]
    public class TransformData
    {
        public List<float> position;
        public List<float> rotation;
        public List<float> scale;
    }

    /// <summary>
    /// Biosphere environmental data: climate, weather, modifiers.
    /// </summary>
    [Serializable]
    public class SceneBiosphere
    {
        public string climateZone;
        public string weather;
        public float temperature;
        public float humidity;
        public float windSpeed;
        public float vegetationDensity;
        public bool isNight;
        public string season;
        public Dictionary<string, int> modifiers;

        public int GetModifier(string key)
        {
            if (modifiers != null && modifiers.TryGetValue(key, out int value))
                return value;
            return 0;
        }
    }
}
