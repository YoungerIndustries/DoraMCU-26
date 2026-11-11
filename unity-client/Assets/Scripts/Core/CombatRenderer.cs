using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Rendering
{
    /// <summary>
    /// Renders space combat: ship models, weapons fire, and explosions.
    /// <para>
    /// Visualizes combat encounters with animated ships, laser beams,
    /// and particle effects for explosions.
    /// </para>
    /// </summary>
    public class CombatRenderer : MonoBehaviour
    {
        [Header("Combat Settings")]
        [SerializeField] private float shipScale = 2f;
        [SerializeField] private float laserSpeed = 50f;
        [SerializeField] private float explosionDuration = 1f;

        [Header("Colors")]
        [SerializeField] private Color attackerColor = new Color(0.8f, 0.2f, 0.2f);
        [SerializeField] private Color defenderColor = new Color(0.2f, 0.4f, 0.8f);
        [SerializeField] private Color laserColor = new Color(1f, 0.5f, 0.2f);
        [SerializeField] private Color explosionColor = new Color(1f, 0.8f, 0.3f);

        // Rendered objects
        private Dictionary<string, GameObject> attackerShips;
        private Dictionary<string, GameObject> defenderShips;
        private List<GameObject> activeLasers;
        private List<GameObject> activeExplosions;

        public Dictionary<string, GameObject> AttackerShips => attackerShips;
        public Dictionary<string, GameObject> DefenderShips => defenderShips;

        private void Awake()
        {
            attackerShips = new Dictionary<string, GameObject>();
            defenderShips = new Dictionary<string, GameObject>();
            activeLasers = new List<GameObject>();
            activeExplosions = new List<GameObject>();
        }

        /// <summary>
        /// Renders a combat scene.
        /// </summary>
        public void RenderCombat(CombatData data)
        {
            Clear();

            if (data == null) return;

            // Render attacker ships
            if (data.attackerFleet != null)
            {
                foreach (var ship in data.attackerFleet)
                {
                    CreateShip(ship, attackerColor, true);
                }
            }

            // Render defender ships
            if (data.defenderFleet != null)
            {
                foreach (var ship in data.defenderFleet)
                {
                    CreateShip(ship, defenderColor, false);
                }
            }

            Debug.Log($"[CombatRenderer] Rendered combat: {attackerShips.Count} attackers vs {defenderShips.Count} defenders");
        }

        /// <summary>
        /// Creates a ship model for combat.
        /// </summary>
        private void CreateShip(CombatShipData shipData, Color color, bool isAttacker)
        {
            var go = new GameObject($"Ship_{shipData.id}");
            go.transform.SetParent(transform);

            // Ship body (capsule-like shape using scaled cube)
            var body = new GameObject("Body");
            body.transform.SetParent(go.transform);
            var filter = body.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitive.Cube();
            var renderer = body.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard"));
            mat.color = color;
            renderer.material = mat;
            body.transform.localScale = new Vector3(2f, 0.5f, 1f) * shipScale;

            // Ship wings
            var wings = new GameObject("Wings");
            wings.transform.SetParent(go.transform);
            var wingFilter = wings.AddComponent<MeshFilter>();
            wingFilter.mesh = CreatePrimitive.Cube();
            var wingRenderer = wings.AddComponent<MeshRenderer>();
            var wingMat = new Material(Shader.Find("Standard"));
            wingMat.color = color * 0.8f;
            wingRenderer.material = wingMat;
            wings.transform.localScale = new Vector3(0.5f, 0.1f, 2f) * shipScale;
            wings.transform.localPosition = new Vector3(-0.5f, 0, 0) * shipScale;

            // Position
            go.transform.position = shipData.position;
            go.transform.rotation = shipData.rotation;

            // Health bar
            CreateHealthBar(go, shipData.health, shipData.maxHealth);

            if (isAttacker)
            {
                attackerShips[shipData.id] = go;
            }
            else
            {
                defenderShips[shipData.id] = go;
            }
        }

        /// <summary>
        /// Creates a health bar above a ship.
        /// </summary>
        private void CreateHealthBar(GameObject parent, float health, float maxHealth)
        {
            var barGo = new GameObject("HealthBar");
            barGo.transform.SetParent(parent.transform);
            barGo.transform.localPosition = Vector3.up * 2f * shipScale;

            // Background
            var bg = new GameObject("Background");
            bg.transform.SetParent(barGo.transform);
            var bgFilter = bg.AddComponent<MeshFilter>();
            bgFilter.mesh = CreatePrimitive.Cube();
            var bgRenderer = bg.AddComponent<MeshRenderer>();
            var bgMat = new Material(Shader.Find("Sprites/Default"));
            bgMat.color = Color.gray;
            bgRenderer.material = bgMat;
            bg.transform.localScale = new Vector3(2f, 0.2f, 0.1f);

            // Health fill
            var fill = new GameObject("Fill");
            fill.transform.SetParent(barGo.transform);
            var fillFilter = fill.AddComponent<MeshFilter>();
            fillFilter.mesh = CreatePrimitive.Cube();
            var fillRenderer = fill.AddComponent<MeshRenderer>();
            var fillMat = new Material(Shader.Find("Sprites/Default"));
            fillMat.color = Color.green;
            fillRenderer.material = fillMat;
            float healthPercent = maxHealth > 0 ? health / maxHealth : 0;
            fill.transform.localScale = new Vector3(2f * healthPercent, 0.15f, 0.15f);
            fill.transform.localPosition = new Vector3(-1f + healthPercent, 0, 0);
        }

        /// <summary>
        /// Creates a laser beam effect.
        /// </summary>
        public void CreateLaser(Vector3 from, Vector3 to)
        {
            var go = new GameObject("Laser");
            go.transform.SetParent(transform);

            var line = go.AddComponent<LineRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = laserColor;
            line.material = mat;
            line.startWidth = 0.1f;
            line.endWidth = 0.1f;
            line.positionCount = 2;
            line.SetPosition(0, from);
            line.SetPosition(1, to);

            activeLasers.Add(go);

            // Auto-remove after short duration
            Destroy(go, 0.5f);
        }

        /// <summary>
        /// Creates an explosion effect.
        /// </summary>
        public void CreateExplosion(Vector3 position, float size = 1f)
        {
            var go = new GameObject("Explosion");
            go.transform.SetParent(transform);
            go.transform.position = position;

            // Explosion sphere
            var sphere = go.AddComponent<Sphere>();
            var renderer = go.GetComponent<MeshRenderer>();
            if (renderer == null) renderer = go.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Particles/Alpha Blended"));
            mat.color = explosionColor;
            renderer.material = mat;
            go.transform.localScale = Vector3.one * size;

            activeExplosions.Add(go);

            // Animate and destroy
            StartCoroutine(AnimateExplosion(go, size));
        }

        /// <summary>
        /// Animates an explosion effect.
        /// </summary>
        private System.Collections.IEnumerator AnimateExplosion(GameObject go, float size)
        {
            float elapsed = 0;
            var renderer = go.GetComponent<MeshRenderer>();

            while (elapsed < explosionDuration)
            {
                elapsed += Time.deltaTime;
                float progress = elapsed / explosionDuration;

                // Expand and fade
                go.transform.localScale = Vector3.one * size * (1 + progress * 2);
                if (renderer && renderer.material)
                {
                    renderer.material.color = explosionColor * (1 - progress);
                }

                yield return null;
            }

            Destroy(go);
            if (activeExplosions.Contains(go))
            {
                activeExplosions.Remove(go);
            }
        }

        /// <summary>
        /// Updates ship positions during combat.
        /// </summary>
        public void UpdateShipPosition(string shipId, Vector3 position, Quaternion rotation)
        {
            if (attackerShips.ContainsKey(shipId))
            {
                attackerShips[shipId].transform.position = position;
                attackerShips[shipId].transform.rotation = rotation;
            }
            else if (defenderShips.ContainsKey(shipId))
            {
                defenderShips[shipId].transform.position = position;
                defenderShips[shipId].transform.rotation = rotation;
            }
        }

        /// <summary>
        /// Removes a ship from the combat scene.
        /// </summary>
        public void RemoveShip(string shipId)
        {
            if (attackerShips.ContainsKey(shipId))
            {
                CreateExplosion(attackerShips[shipId].transform.position);
                Destroy(attackerShips[shipId]);
                attackerShips.Remove(shipId);
            }
            else if (defenderShips.ContainsKey(shipId))
            {
                CreateExplosion(defenderShips[shipId].transform.position);
                Destroy(defenderShips[shipId]);
                defenderShips.Remove(shipId);
            }
        }

        /// <summary>
        /// Clears all combat objects.
        /// </summary>
        public void Clear()
        {
            foreach (var ship in attackerShips.Values)
            {
                Destroy(ship);
            }
            attackerShips.Clear();

            foreach (var ship in defenderShips.Values)
            {
                Destroy(ship);
            }
            defenderShips.Clear();

            foreach (var laser in activeLasers)
            {
                if (laser != null) Destroy(laser);
            }
            activeLasers.Clear();

            foreach (var explosion in activeExplosions)
            {
                if (explosion != null) Destroy(explosion);
            }
            activeExplosions.Clear();
        }

        private void OnDestroy()
        {
            Clear();
        }
    }

    /// <summary>
    /// Data structure for combat rendering.
    /// </summary>
    public class CombatData
    {
        public List<CombatShipData> attackerFleet;
        public List<CombatShipData> defenderFleet;
    }

    public class CombatShipData
    {
        public string id;
        public Vector3 position;
        public Quaternion rotation;
        public float health;
        public float maxHealth;
    }
}
