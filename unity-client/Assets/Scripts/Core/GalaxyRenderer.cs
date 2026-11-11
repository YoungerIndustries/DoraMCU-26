using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Rendering
{
    /// <summary>
    /// Renders the full galaxy map in 3D: sector grid, hyperlanes, and stars.
    /// <para>
    /// Provides an overview of the entire galaxy with clickable sectors and systems.
    /// </para>
    /// </summary>
    public class GalaxyRenderer : MonoBehaviour
    {
        [Header("Visual Settings")]
        [SerializeField] private float sectorSpacing = 10f;
        [SerializeField] private float starSize = 0.5f;
        [SerializeField] private Color sectorColor = new Color(0.1f, 0.2f, 0.4f, 0.3f);
        [SerializeField] private Color hyperlaneColor = new Color(0.3f, 0.6f, 1f, 0.5f);
        [SerializeField] private Color starColor = Color.white;

        [Header("Materials")]
        [SerializeField] private Material sectorMaterial;
        [SerializeField] private Material hyperlaneMaterial;
        [SerializeField] private Material starMaterial;

        // Rendered objects
        private Dictionary<string, GameObject> sectorObjects;
        private Dictionary<string, GameObject> starObjects;
        private List<LineRenderer> hyperlanes;

        public Dictionary<string, GameObject> SectorObjects => sectorObjects;
        public Dictionary<string, GameObject> StarObjects => starObjects;

        private void Awake()
        {
            sectorObjects = new Dictionary<string, GameObject>();
            starObjects = new Dictionary<string, GameObject>();
            hyperlanes = new List<LineRenderer>();
        }

        /// <summary>
        /// Renders the galaxy overview with sectors and stars.
        /// </summary>
        public void RenderGalaxy(GalaxyData data)
        {
            Clear();

            if (data == null) return;

            // Render sectors
            if (data.sectors != null)
            {
                foreach (var sector in data.sectors)
                {
                    CreateSector(sector);
                }
            }

            // Render stars
            if (data.stars != null)
            {
                foreach (var star in data.stars)
                {
                    CreateStar(star);
                }
            }

            // Render hyperlanes
            if (data.hyperlanes != null)
            {
                foreach (var lane in data.hyperlanes)
                {
                    CreateHyperlane(lane.from, lane.to);
                }
            }

            Debug.Log($"[GalaxyRenderer] Rendered {sectorObjects.Count} sectors, {starObjects.Count} stars, {hyperlanes.Count} hyperlanes");
        }

        /// <summary>
        /// Creates a 3D sector representation.
        /// </summary>
        private void CreateSector(SectorData sector)
        {
            var go = new GameObject($"Sector_{sector.id}");
            go.transform.SetParent(transform);

            // Sector cube
            var cube = go.AddComponent<Cube>();
            var mat = sectorMaterial ?? new Material(Shader.Find("Sprites/Default"));
            mat.color = sectorColor;
            go.GetComponent<MeshRenderer>().material = mat;

            // Position based on sector coordinates
            Vector3 pos = new Vector3(
                sector.x * sectorSpacing,
                0,
                sector.z * sectorSpacing
            );
            go.transform.position = pos;
            go.transform.localScale = Vector3.one * sectorSpacing * 0.8f;

            // Label
            var label = new GameObject("Label");
            var textMesh = label.AddComponent<TextMesh>();
            textMesh.text = sector.name;
            textMesh.fontSize = 14;
            textMesh.anchor = TextAnchor.MiddleCenter;
            label.transform.SetParent(go.transform);
            label.transform.localPosition = Vector3.up * 0.6f;

            sectorObjects[sector.id] = go;
        }

        /// <summary>
        /// Creates a 3D star representation.
        /// </summary>
        private void CreateStar(StarData star)
        {
            var go = new GameObject($"Star_{star.id}");
            go.transform.SetParent(transform);

            // Star sphere
            var sphere = go.AddComponent<Sphere>();
            var mat = starMaterial ?? new Material(Shader.Find("Sprites/Default"));
            mat.color = starColor;
            go.GetComponent<MeshRenderer>().material = mat;

            // Position within sector
            Vector3 pos = new Vector3(
                star.sectorX * sectorSpacing + star.localX,
                0,
                star.sectorZ * sectorSpacing + star.localZ
            );
            go.transform.position = pos;
            go.transform.localScale = Vector3.one * starSize;

            starObjects[star.id] = go;
        }

        /// <summary>
        /// Creates a hyperlane line between two points.
        /// </summary>
        private void CreateHyperlane(Vector3 from, Vector3 to)
        {
            var go = new GameObject("Hyperlane");
            go.transform.SetParent(transform);

            var line = go.AddComponent<LineRenderer>();
            var mat = hyperlaneMaterial ?? new Material(Shader.Find("Sprites/Default"));
            mat.color = hyperlaneColor;
            line.material = mat;
            line.startWidth = 0.1f;
            line.endWidth = 0.1f;
            line.positionCount = 2;
            line.SetPosition(0, from);
            line.SetPosition(1, to);

            hyperlanes.Add(line);
        }

        /// <summary>
        /// Clears all rendered objects.
        /// </summary>
        public void Clear()
        {
            foreach (var go in sectorObjects.Values)
            {
                Destroy(go);
            }
            sectorObjects.Clear();

            foreach (var go in starObjects.Values)
            {
                Destroy(go);
            }
            starObjects.Clear();

            foreach (var line in hyperlanes)
            {
                Destroy(line.gameObject);
            }
            hyperlanes.Clear();
        }

        private void OnDestroy()
        {
            Clear();
        }
    }

    /// <summary>
    /// Data structure for galaxy rendering.
    /// </summary>
    public class GalaxyData
    {
        public List<SectorData> sectors;
        public List<StarData> stars;
        public List<HyperlaneData> hyperlanes;
    }

    public class SectorData
    {
        public string id;
        public string name;
        public int x;
        public int z;
    }

    public class StarData
    {
        public string id;
        public int sectorX;
        public int sectorZ;
        public float localX;
        public float localZ;
    }

    public class HyperlaneData
    {
        public Vector3 from;
        public Vector3 to;
    }
}
