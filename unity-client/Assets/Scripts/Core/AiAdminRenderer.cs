using System;
using System.Collections.Generic;
using UnityEngine;
using DoraMCU.Client.Core;
using DoraMCU.Client.Network;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Renders AI admin characters as 3D markers in the Unity client.
    /// Supports both galaxy view (small markers) and planet surface view (character models).
    /// </summary>
    public class AiAdminRenderer : Singleton<AiAdminRenderer>
    {
        [Header("Visual Settings")]
        [SerializeField] private float markerScale = 0.3f;
        [SerializeField] private float labelOffsetY = 0.8f;
        [SerializeField] private float maxLabelDistance = 100f;

        [Header("Colors")]
        [Tooltip("Color for Galaxy Overseer")]
        [SerializeField] private Color overseerColor = new Color(1f, 0.84f, 0f, 1f); // Gold
        [Tooltip("Color for Leaders")]
        [SerializeField] private Color leaderColor = new Color(0.25f, 0.41f, 0.88f, 1f); // Blue
        [Tooltip("Color for Governors")]
        [SerializeField] private Color governorColor = new Color(0.2f, 0.8f, 0.2f, 1f); // Green
        [Tooltip("Color for Office Managers")]
        [SerializeField] private Color managerColor = new Color(1f, 1f, 0f, 1f); // Yellow
        [Tooltip("Color for Entity-linked AIs")]
        [SerializeField] private Color entityColor = new Color(0.5f, 0.2f, 0.8f, 1f); // Purple
        [Tooltip("Default color for unknown roles")]
        [SerializeField] private Color defaultColor = new Color(0.7f, 0.7f, 0.7f, 1f); // Gray

        // Parent transforms for organization
        private Transform galaxyParent;
        private Transform planetParent;

        // Dictionary to track AI admin GameObjects by ID
        private Dictionary<long, GameObject> adminObjects = new Dictionary<long, GameObject>();
        // Dictionary to track AI admin data by ID for click detection
        private Dictionary<long, AiAdminData> adminDataMap = new Dictionary<long, AiAdminData>();

        protected override void Awake()
        {
            base.Awake();
            DontDestroyOnLoad(gameObject);

            // Create parent GameObjects for organization
            galaxyParent = new GameObject("AiAdmins_Galaxy").transform;
            galaxyParent.SetParent(transform);
            DontDestroyOnLoad(galaxyParent.gameObject);

            planetParent = new GameObject("AiAdmins_Planet").transform;
            planetParent.SetParent(transform);
            DontDestroyOnLoad(planetParent.gameObject);

            // Subscribe to NetworkManager events
            NetworkManager.Instance.OnAiAdminsFetched += OnAiAdminsFetched;
        }

        void OnDestroy()
        {
            if (NetworkManager.Instance != null)
            {
                NetworkManager.Instance.OnAiAdminsFetched -= OnAiAdminsFetched;
            }
        }

        /// <summary>
        /// Handle AI admin data fetched from the server.
        /// </summary>
        private void OnAiAdminsFetched(List<AiAdminData> admins)
        {
            if (admins == null || admins.Count == 0) return;
            UpdateAiAdminPositions(admins);
            // Update data map for click detection
            foreach (var admin in admins)
            {
                adminDataMap[admin.id] = admin;
            }
        }

        /// <summary>
        /// Render AI admin markers in the galaxy view.
        /// </summary>
        public void RenderAiAdminsInGalaxy(List<AiAdminData> admins)
        {
            if (admins == null || admins.Count == 0) return;

            // Clear existing galaxy markers
            ClearGalaxyMarkers();

            foreach (var admin in admins)
            {
                if (!admin.hasPosition) continue;

                var go = CreateAiAdminMarker(admin, galaxyParent);
                if (go != null)
                {
                    adminObjects[admin.id] = go;
                }
            }
        }

        /// <summary>
        /// Render AI admin markers on a planet surface.
        /// </summary>
        public void RenderAiAdminsOnPlanet(List<AiAdminData> admins)
        {
            if (admins == null || admins.Count == 0) return;

            // Clear existing planet markers
            ClearPlanetMarkers();

            foreach (var admin in admins)
            {
                if (!admin.hasPosition) continue;

                var go = CreateAiAdminMarker(admin, planetParent);
                if (go != null)
                {
                    adminObjects[admin.id] = go;
                }
            }
        }

        /// <summary>
        /// Update positions of existing AI admin markers.
        /// </summary>
        public void UpdateAiAdminPositions(List<AiAdminData> admins)
        {
            if (admins == null || admins.Count == 0) return;

            var adminIds = new HashSet<long>();

            foreach (var admin in admins)
            {
                adminIds.Add(admin.id);

                if (adminObjects.TryGetValue(admin.id, out var go))
                {
                    // Update existing marker position
                    go.transform.position = admin.Position;
                }
                else if (admin.hasPosition)
                {
                    // Create new marker
                    var newGo = CreateAiAdminMarker(admin, galaxyParent);
                    if (newGo != null)
                    {
                        adminObjects[admin.id] = newGo;
                    }
                }
            }

            // Remove markers for admins that are no longer in the list
            var keysToRemove = new List<long>();
            foreach (var kvp in adminObjects)
            {
                if (!adminIds.Contains(kvp.Key))
                {
                    Destroy(kvp.Value);
                    keysToRemove.Add(kvp.Key);
                }
            }
            foreach (var key in keysToRemove)
            {
                adminObjects.Remove(key);
            }
        }

        /// <summary>
        /// Create a marker for an AI admin.
        /// </summary>
        private GameObject CreateAiAdminMarker(AiAdminData admin, Transform parent)
        {
            var go = new GameObject($"AiAdmin_{admin.id}_{admin.name}");
            go.transform.SetParent(parent);
            go.transform.position = admin.Position;

            // Create capsule marker
            var capsule = go.AddComponent<CapsuleCollider>();
            capsule.height = markerScale * 2f;
            capsule.radius = markerScale * 0.5f;

            var renderer = go.AddComponent<MeshRenderer>();
            var filter = go.AddComponent<MeshFilter>();
            var mesh = new Mesh();
            // Create a simple capsule-like mesh using a sphere
            var sphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            filter.sharedMesh = sphere.GetComponent<MeshFilter>().sharedMesh;
            Destroy(sphere);

            // Scale the marker
            go.transform.localScale = Vector3.one * markerScale;

            // Set color based on role
            var material = new Material(Shader.Find("Standard"));
            material.color = GetColorForRole(admin);
            renderer.sharedMaterial = material;

            // Add a label
            CreateLabel(go, admin);

            return go;
        }

        /// <summary>
        /// Create a floating label above an AI admin marker.
        /// </summary>
        private void CreateLabel(GameObject parent, AiAdminData admin)
        {
            // Try to use TextMeshPro if available, otherwise use a simple sprite
            try
            {
                var labelGo = new GameObject("Label");
                labelGo.transform.SetParent(parent.transform);
                labelGo.transform.localPosition = new Vector3(0, labelOffsetY / markerScale, 0);

                // Create a simple text display using a GUI Text alternative
                var textMesh = labelGo.AddComponent<TextMesh>();
                textMesh.text = $"{admin.name}\n({admin.role})";
                textMesh.fontSize = 12;
                textMesh.anchor = TextAnchor.MiddleCenter;
                textMesh.alignment = TextAlignment.Center;
                textMesh.characterSize = 0.05f;
                textMesh.color = Color.white;
            }
            catch (Exception e)
            {
                Debug.LogWarning($"Could not create label for {admin.name}: {e.Message}");
            }
        }

        /// <summary>
        /// Get the color for an AI admin based on their role.
        /// </summary>
        private Color GetColorForRole(AiAdminData admin)
        {
            if (admin.isGalaxyOverseer) return overseerColor;

            var role = admin.role?.ToLower() ?? "";
            if (role.Contains("leader")) return leaderColor;
            if (role.Contains("governor")) return governorColor;
            if (role.Contains("manager")) return managerColor;
            if (admin.characterType != null && admin.characterType.Contains("ENTITY")) return entityColor;

            return defaultColor;
        }

        /// <summary>
        /// Clear all galaxy view markers.
        /// </summary>
        public void ClearGalaxyMarkers()
        {
            ClearParent(galaxyParent);
            adminObjects.Clear();
        }

        /// <summary>
        /// Clear all planet surface markers.
        /// </summary>
        public void ClearPlanetMarkers()
        {
            ClearParent(planetParent);
        }

        /// <summary>
        /// Clear all AI admin markers.
        /// </summary>
        public void ClearAll()
        {
            ClearGalaxyMarkers();
            ClearPlanetMarkers();
        }

        private void ClearParent(Transform parent)
        {
            if (parent == null) return;
            while (parent.childCount > 0)
            {
                Destroy(parent.GetChild(0).gameObject);
            }
        }

        /// <summary>
        /// Get the AI admin data for a clicked marker.
        /// </summary>
        public AiAdminData GetAdminAtPosition(Ray ray, float maxDistance = 100f)
        {
            RaycastHit hit;
            if (Physics.Raycast(ray, out hit, maxDistance))
            {
                var go = hit.collider.gameObject;
                // Find the parent AI admin object
                while (go != null)
                {
                    foreach (var kvp in adminObjects)
                    {
                        if (kvp.Value == go)
                        {
                            // Return the admin data from the data map
                            if (adminDataMap.TryGetValue(kvp.Key, out var data))
                            {
                                return data;
                            }
                            return null;
                        }
                    }
                    go = go.transform.parent?.gameObject;
                }
            }
            return null;
        }

        /// <summary>
        /// Handle click input and return the clicked admin data.
        /// Call this from InputHandler or GameManager on mouse click.
        /// </summary>
        public AiAdminData OnClick(Vector3 mousePosition)
        {
            if (Camera.main == null) return null;
            Ray ray = Camera.main.ScreenPointToRay(mousePosition);
            return GetAdminAtPosition(ray, maxLabelDistance);
        }
    }
}
