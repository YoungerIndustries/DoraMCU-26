using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.Battlefield
{
    /// <summary>
    /// Generates procedural unit markers based on unit type and team.
    /// <para>
    /// Creates different shapes for different unit types (infantry, tanks, artillery)
    /// with team-based coloring and health bars.
    /// </para>
    /// </summary>
    public static class UnitGenerator
    {
        // Team colors
        public static readonly Color AttackerColor = new Color(0.8f, 0.2f, 0.2f, 0.85f);
        public static readonly Color DefenderColor = new Color(0.2f, 0.4f, 0.8f, 0.85f);
        public static readonly Color SelectedColor = new Color(1f, 1f, 0f, 1f);

        /// <summary>
        /// Generates a unit marker GameObject.
        /// </summary>
        public static GameObject GenerateUnit(SceneUnit unitData, Transform parent)
        {
            Color teamColor = unitData.team?.ToLower() == "attacker" ? AttackerColor : DefenderColor;

            var go = new GameObject($"Unit_{unitData.id}")
            {
                transform = 
                {
                    parent = parent,
                    position = unitData.Position,
                    rotation = Quaternion.Euler(0f, unitData.FacingAngle, 0f)
                }
            };

            // Add tag for raycast selection
            go.tag = "BattlefieldUnit";

            // Store unit data for selection
            var unitInfo = go.AddComponent<UnitInfo>();
            unitInfo.UnitId = unitData.id;
            unitInfo.UnitName = unitData.name;
            unitInfo.UnitType = unitData.type;
            unitInfo.Team = unitData.team;
            unitInfo.Stats = unitData.stats;

            // Create marker based on unit type
            CreateUnitMarker(go, unitData.type, teamColor);

            // Create health bar
            CreateHealthBar(go, unitData.stats, teamColor);

            return go;
        }

        /// <summary>
        /// Creates the visual marker for a unit.
        /// </summary>
        private static void CreateUnitMarker(GameObject go, string unitType, Color teamColor)
        {
            string type = unitType?.ToLower() ?? "infantry";

            if (type.Contains("tank"))
            {
                CreateTankMarker(go, teamColor);
            }
            else if (type.Contains("artillery"))
            {
                CreateArtilleryMarker(go, teamColor);
            }
            else if (type.Contains("mechanized") || type.Contains("apc"))
            {
                CreateAPCMarker(go, teamColor);
            }
            else if (type.Contains("anti-air") || type.Contains("aa"))
            {
                CreateAntiAirMarker(go, teamColor);
            }
            else if (type.Contains("recon"))
            {
                CreateReconMarker(go, teamColor);
            }
            else if (type.Contains("engineer"))
            {
                CreateEngineerMarker(go, teamColor);
            }
            else if (type.Contains("air"))
            {
                CreateAircraftMarker(go, teamColor);
            }
            else if (type.Contains("mech") || type.Contains("battlemech"))
            {
                CreateMechMarker(go, teamColor);
            }
            else if (type.Contains("arachnid") || type.Contains("warrior") || type.Contains("bug"))
            {
                CreateArachnidMarker(go, teamColor);
            }
            else if (type.Contains("crawler") || type.Contains("siege"))
            {
                CreateSiegeCrawlerMarker(go, teamColor);
            }
            else if (type.Contains("terran") || type.Contains("infantry squad") || type.Contains("mobile infantry"))
            {
                CreateTerranInfantryMarker(go, teamColor);
            }
            else
            {
                CreateInfantryMarker(go, teamColor);
            }
        }

        /// <summary>
        /// Creates an infantry marker (small capsule).
        /// </summary>
        private static void CreateInfantryMarker(GameObject go, Color color)
        {
            var marker = new GameObject("Marker") { transform = { parent = go.transform } };
            var filter = marker.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveCapsule();
            var renderer = marker.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            renderer.material = mat;
            marker.transform.localScale = new Vector3(0.8f, 1.5f, 0.8f);
            marker.transform.localPosition = Vector3.up * 0.75f;
        }

        /// <summary>
        /// Creates a tank marker (box with turret).
        /// </summary>
        private static void CreateTankMarker(GameObject go, Color color)
        {
            // Hull
            var hull = new GameObject("Hull") { transform = { parent = go.transform } };
            var hullFilter = hull.AddComponent<MeshFilter>();
            hullFilter.mesh = CreatePrimitiveBox();
            var hullRenderer = hull.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            hullRenderer.material = mat;
            hull.transform.localScale = new Vector3(2.5f, 1f, 1.5f);
            hull.transform.localPosition = Vector3.up * 0.5f;

            // Turret
            var turret = new GameObject("Turret") { transform = { parent = go.transform } };
            var turretFilter = turret.AddComponent<MeshFilter>();
            turretFilter.mesh = CreatePrimitiveBox();
            var turretRenderer = turret.AddComponent<MeshRenderer>();
            var turretMat = new Material(Shader.Find("Standard")) { color = color * 0.8f };
            turretRenderer.material = turretMat;
            turret.transform.localScale = new Vector3(1.2f, 0.8f, 1f);
            turret.transform.localPosition = Vector3.up * 1.4f;

            // Barrel
            var barrel = new GameObject("Barrel") { transform = { parent = go.transform } };
            var barrelFilter = barrel.AddComponent<MeshFilter>();
            barrelFilter.mesh = CreatePrimitiveCylinder();
            var barrelRenderer = barrel.AddComponent<MeshRenderer>();
            var barrelMat = new Material(Shader.Find("Standard")) { color = color * 0.7f };
            barrelRenderer.material = barrelMat;
            barrel.transform.localScale = new Vector3(0.2f, 0.2f, 2f);
            barrel.transform.localPosition = new Vector3(0f, 1.4f, 1.2f);
            barrel.transform.rotation = Quaternion.Euler(90f, 0f, 0f);
        }

        /// <summary>
        /// Creates an artillery marker (cylinder with barrel).
        /// </summary>
        private static void CreateArtilleryMarker(GameObject go, Color color)
        {
            // Base
            var base_ = new GameObject("Base") { transform = { parent = go.transform } };
            var baseFilter = base_.AddComponent<MeshFilter>();
            baseFilter.mesh = CreatePrimitiveCylinder();
            var baseRenderer = base_.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            baseRenderer.material = mat;
            base_.transform.localScale = new Vector3(1.5f, 1f, 1.5f);
            base_.transform.localPosition = Vector3.up * 0.5f;

            // Barrel
            var barrel = new GameObject("Barrel") { transform = { parent = go.transform } };
            var barrelFilter = barrel.AddComponent<MeshFilter>();
            barrelFilter.mesh = CreatePrimitiveCylinder();
            var barrelRenderer = barrel.AddComponent<MeshRenderer>();
            var barrelMat = new Material(Shader.Find("Standard")) { color = color * 0.7f };
            barrelRenderer.material = barrelMat;
            barrel.transform.localScale = new Vector3(0.3f, 0.3f, 2.5f);
            barrel.transform.localPosition = new Vector3(0f, 1.2f, 1.5f);
            barrel.transform.rotation = Quaternion.Euler(70f, 0f, 0f);
        }

        /// <summary>
        /// Creates an APC marker (elongated box).
        /// </summary>
        private static void CreateAPCMarker(GameObject go, Color color)
        {
            var marker = new GameObject("Marker") { transform = { parent = go.transform } };
            var filter = marker.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveBox();
            var renderer = marker.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            renderer.material = mat;
            marker.transform.localScale = new Vector3(2f, 1.2f, 1.2f);
            marker.transform.localPosition = Vector3.up * 0.6f;
        }

        /// <summary>
        /// Creates an anti-air marker (box with upward barrel).
        /// </summary>
        private static void CreateAntiAirMarker(GameObject go, Color color)
        {
            var base_ = new GameObject("Base") { transform = { parent = go.transform } };
            var baseFilter = base_.AddComponent<MeshFilter>();
            baseFilter.mesh = CreatePrimitiveBox();
            var baseRenderer = base_.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            baseRenderer.material = mat;
            base_.transform.localScale = new Vector3(1.5f, 1f, 1.5f);
            base_.transform.localPosition = Vector3.up * 0.5f;

            var barrel = new GameObject("Barrel") { transform = { parent = go.transform } };
            var barrelFilter = barrel.AddComponent<MeshFilter>();
            barrelFilter.mesh = CreatePrimitiveCylinder();
            var barrelRenderer = barrel.AddComponent<MeshRenderer>();
            var barrelMat = new Material(Shader.Find("Standard")) { color = color * 0.7f };
            barrelRenderer.material = barrelMat;
            barrel.transform.localScale = new Vector3(0.3f, 2f, 0.3f);
            barrel.transform.localPosition = Vector3.up * 2f;
        }

        /// <summary>
        /// Creates a recon marker (small sphere).
        /// </summary>
        private static void CreateReconMarker(GameObject go, Color color)
        {
            var marker = new GameObject("Marker") { transform = { parent = go.transform } };
            var filter = marker.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveSphere();
            var renderer = marker.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            renderer.material = mat;
            marker.transform.localScale = new Vector3(0.8f, 0.8f, 0.8f);
            marker.transform.localPosition = Vector3.up * 0.8f;
        }

        /// <summary>
        /// Creates an engineer marker (cone).
        /// </summary>
        private static void CreateEngineerMarker(GameObject go, Color color)
        {
            var marker = new GameObject("Marker") { transform = { parent = go.transform } };
            var filter = marker.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveCylinder();
            var renderer = marker.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            renderer.material = mat;
            marker.transform.localScale = new Vector3(1f, 1.5f, 1f);
            marker.transform.localPosition = Vector3.up * 0.75f;
        }

        /// <summary>
        /// Creates an aircraft marker (diamond shape).
        /// </summary>
        private static void CreateAircraftMarker(GameObject go, Color color)
        {
            var marker = new GameObject("Marker") { transform = { parent = go.transform } };
            var filter = marker.AddComponent<MeshFilter>();
            filter.mesh = CreatePrimitiveSphere();
            var renderer = marker.AddComponent<MeshRenderer>();
            var mat = new Material(Shader.Find("Standard")) { color = color };
            mat.EnableKeyword("_EMISSION");
            mat.SetColor("_EmissionColor", color * 0.3f);
            renderer.material = mat;
            marker.transform.localScale = new Vector3(1.5f, 0.5f, 1.5f);
            marker.transform.localPosition = Vector3.up * 3f;
        }

        /// <summary>
        /// Creates a BattleMech marker (bipedal silhouette with torso, head, arms, legs).
        /// </summary>
        private static void CreateMechMarker(GameObject go, Color color)
        {
            var mechColor = color;
            var darkColor = color * 0.7f;

            // Torso (main body)
            var torso = new GameObject("Torso") { transform = { parent = go.transform } };
            var torsoFilter = torso.AddComponent<MeshFilter>();
            torsoFilter.mesh = CreatePrimitiveBox();
            var torsoRenderer = torso.AddComponent<MeshRenderer>();
            var torsoMat = new Material(Shader.Find("Standard")) { color = mechColor };
            torsoRenderer.material = torsoMat;
            torso.transform.localScale = new Vector3(2f, 2.5f, 1.5f);
            torso.transform.localPosition = Vector3.up * 2.5f;

            // Head
            var head = new GameObject("Head") { transform = { parent = go.transform } };
            var headFilter = head.AddComponent<MeshFilter>();
            headFilter.mesh = CreatePrimitiveSphere();
            var headRenderer = head.AddComponent<MeshRenderer>();
            var headMat = new Material(Shader.Find("Standard")) { color = darkColor };
            headRenderer.material = headMat;
            head.transform.localScale = new Vector3(0.8f, 0.8f, 0.8f);
            head.transform.localPosition = new Vector3(0f, 4.2f, 0f);

            // Left Arm
            var leftArm = new GameObject("LeftArm") { transform = { parent = go.transform } };
            var leftArmFilter = leftArm.AddComponent<MeshFilter>();
            leftArmFilter.mesh = CreatePrimitiveCapsule();
            var leftArmRenderer = leftArm.AddComponent<MeshRenderer>();
            var armMat = new Material(Shader.Find("Standard")) { color = darkColor };
            leftArmRenderer.material = armMat;
            leftArm.transform.localScale = new Vector3(0.5f, 2f, 0.5f);
            leftArm.transform.localPosition = new Vector3(-1.3f, 2.5f, 0f);

            // Right Arm
            var rightArm = new GameObject("RightArm") { transform = { parent = go.transform } };
            var rightArmFilter = rightArm.AddComponent<MeshFilter>();
            rightArmFilter.mesh = CreatePrimitiveCapsule();
            var rightArmRenderer = rightArm.AddComponent<MeshRenderer>();
            rightArmRenderer.material = new Material(Shader.Find("Standard")) { color = darkColor };
            rightArm.transform.localScale = new Vector3(0.5f, 2f, 0.5f);
            rightArm.transform.localPosition = new Vector3(1.3f, 2.5f, 0f);

            // Left Leg
            var leftLeg = new GameObject("LeftLeg") { transform = { parent = go.transform } };
            var leftLegFilter = leftLeg.AddComponent<MeshFilter>();
            leftLegFilter.mesh = CreatePrimitiveCapsule();
            var leftLegRenderer = leftLeg.AddComponent<MeshRenderer>();
            var legMat = new Material(Shader.Find("Standard")) { color = mechColor };
            leftLegRenderer.material = legMat;
            leftLeg.transform.localScale = new Vector3(0.6f, 2f, 0.6f);
            leftLeg.transform.localPosition = new Vector3(-0.5f, 0.5f, 0f);

            // Right Leg
            var rightLeg = new GameObject("RightLeg") { transform = { parent = go.transform } };
            var rightLegFilter = rightLeg.AddComponent<MeshFilter>();
            rightLegFilter.mesh = CreatePrimitiveCapsule();
            var rightLegRenderer = rightLeg.AddComponent<MeshRenderer>();
            rightLegRenderer.material = new Material(Shader.Find("Standard")) { color = mechColor };
            rightLeg.transform.localScale = new Vector3(0.6f, 2f, 0.6f);
            rightLeg.transform.localPosition = new Vector3(0.5f, 0.5f, 0f);

            // Emission glow for visibility
            torsoMat.EnableKeyword("_EMISSION");
            torsoMat.SetColor("_EmissionColor", color * 0.15f);
        }

        /// <summary>
        /// Creates a Terran Mobile Infantry marker (small humanoid with rifle silhouette).
        /// </summary>
        private static void CreateTerranInfantryMarker(GameObject go, Color color)
        {
            var bodyColor = color;
            var darkColor = color * 0.7f;

            // Body (torso)
            var body = new GameObject("Body") { transform = { parent = go.transform } };
            var bodyFilter = body.AddComponent<MeshFilter>();
            bodyFilter.mesh = CreatePrimitiveCapsule();
            var bodyRenderer = body.AddComponent<MeshRenderer>();
            var bodyMat = new Material(Shader.Find("Standard")) { color = bodyColor };
            bodyRenderer.material = bodyMat;
            body.transform.localScale = new Vector3(0.6f, 1.2f, 0.6f);
            body.transform.localPosition = Vector3.up * 0.8f;

            // Head
            var head = new GameObject("Head") { transform = { parent = go.transform } };
            var headFilter = head.AddComponent<MeshFilter>();
            headFilter.mesh = CreatePrimitiveSphere();
            var headRenderer = head.AddComponent<MeshRenderer>();
            var headMat = new Material(Shader.Find("Standard")) { color = darkColor };
            headRenderer.material = headMat;
            head.transform.localScale = new Vector3(0.4f, 0.4f, 0.4f);
            head.transform.localPosition = new Vector3(0f, 1.6f, 0f);

            // Rifle (thin cylinder in front)
            var rifle = new GameObject("Rifle") { transform = { parent = go.transform } };
            var rifleFilter = rifle.AddComponent<MeshFilter>();
            rifleFilter.mesh = CreatePrimitiveCylinder();
            var rifleRenderer = rifle.AddComponent<MeshRenderer>();
            var rifleMat = new Material(Shader.Find("Standard")) { color = darkColor };
            rifleRenderer.material = rifleMat;
            rifle.transform.localScale = new Vector3(0.1f, 0.1f, 1.2f);
            rifle.transform.localPosition = new Vector3(0f, 0.9f, 0.5f);
            rifle.transform.rotation = Quaternion.Euler(90f, 0f, 0f);
        }

        /// <summary>
        /// Creates an Arachnid (Bug) marker with multiple legs and segmented body.
        /// </summary>
        private static void CreateArachnidMarker(GameObject go, Color color)
        {
            var bugColor = color;
            var darkColor = color * 0.6f;

            // Abdomen (rear segment)
            var abdomen = new GameObject("Abdomen") { transform = { parent = go.transform } };
            var abdomenFilter = abdomen.AddComponent<MeshFilter>();
            abdomenFilter.mesh = CreatePrimitiveSphere();
            var abdomenRenderer = abdomen.AddComponent<MeshRenderer>();
            var abdomenMat = new Material(Shader.Find("Standard")) { color = bugColor };
            abdomenRenderer.material = abdomenMat;
            abdomen.transform.localScale = new Vector3(1f, 0.8f, 1.2f);
            abdomen.transform.localPosition = new Vector3(0f, 0.8f, -0.5f);

            // Thorax (middle segment)
            var thorax = new GameObject("Thorax") { transform = { parent = go.transform } };
            var thoraxFilter = thorax.AddComponent<MeshFilter>();
            thoraxFilter.mesh = CreatePrimitiveSphere();
            var thoraxRenderer = thorax.AddComponent<MeshRenderer>();
            var thoraxMat = new Material(Shader.Find("Standard")) { color = darkColor };
            thoraxRenderer.material = thoraxMat;
            thorax.transform.localScale = new Vector3(0.8f, 0.7f, 0.8f);
            thorax.transform.localPosition = new Vector3(0f, 0.8f, 0.3f);

            // Head (front segment with mandibles)
            var head = new GameObject("Head") { transform = { parent = go.transform } };
            var headFilter = head.AddComponent<MeshFilter>();
            headFilter.mesh = CreatePrimitiveSphere();
            var headRenderer = head.AddComponent<MeshRenderer>();
            var headMat = new Material(Shader.Find("Standard")) { color = bugColor };
            headRenderer.material = headMat;
            head.transform.localScale = new Vector3(0.5f, 0.5f, 0.5f);
            head.transform.localPosition = new Vector3(0f, 0.8f, 1f);

            // Legs (6 legs, 3 per side)
            float[] legPositions = { -0.8f, 0f, 0.8f };
            for (int i = 0; i < 3; i++)
            {
                // Left legs
                var leftLeg = new GameObject($"LeftLeg_{i}") { transform = { parent = go.transform } };
                var leftLegFilter = leftLeg.AddComponent<MeshFilter>();
                leftLegFilter.mesh = CreatePrimitiveCylinder();
                var leftLegRenderer = leftLeg.AddComponent<MeshRenderer>();
                var legMat = new Material(Shader.Find("Standard")) { color = darkColor };
                leftLegRenderer.material = legMat;
                leftLeg.transform.localScale = new Vector3(0.15f, 0.15f, 1.5f);
                leftLeg.transform.localPosition = new Vector3(-0.6f, 0.3f, legPositions[i]);
                leftLeg.transform.rotation = Quaternion.Euler(70f, 0f, 30f * (i - 1));

                // Right legs
                var rightLeg = new GameObject($"RightLeg_{i}") { transform = { parent = go.transform } };
                var rightLegFilter = rightLeg.AddComponent<MeshFilter>();
                rightLegFilter.mesh = CreatePrimitiveCylinder();
                var rightLegRenderer = rightLeg.AddComponent<MeshRenderer>();
                rightLegRenderer.material = new Material(Shader.Find("Standard")) { color = darkColor };
                rightLeg.transform.localScale = new Vector3(0.15f, 0.15f, 1.5f);
                rightLeg.transform.localPosition = new Vector3(0.6f, 0.3f, legPositions[i]);
                rightLeg.transform.rotation = Quaternion.Euler(70f, 0f, -30f * (i - 1));
            }

            // Emission glow for visibility
            abdomenMat.EnableKeyword("_EMISSION");
            abdomenMat.SetColor("_EmissionColor", color * 0.2f);
        }

        /// <summary>
        /// Creates a Siege Crawler marker (large multi-legged siege vehicle).
        /// </summary>
        private static void CreateSiegeCrawlerMarker(GameObject go, Color color)
        {
            var crawlerColor = color;
            var darkColor = color * 0.6f;

            // Main body (large elongated capsule)
            var body = new GameObject("Body") { transform = { parent = go.transform } };
            var bodyFilter = body.AddComponent<MeshFilter>();
            bodyFilter.mesh = CreatePrimitiveCapsule();
            var bodyRenderer = body.AddComponent<MeshRenderer>();
            var bodyMat = new Material(Shader.Find("Standard")) { color = crawlerColor };
            bodyRenderer.material = bodyMat;
            body.transform.localScale = new Vector3(2f, 1.5f, 3f);
            body.transform.localPosition = Vector3.up * 1.5f;
            body.transform.rotation = Quaternion.Euler(90f, 0f, 0f);

            // Turret/mount on top
            var turret = new GameObject("Turret") { transform = { parent = go.transform } };
            var turretFilter = turret.AddComponent<MeshFilter>();
            turretFilter.mesh = CreatePrimitiveSphere();
            var turretRenderer = turret.AddComponent<MeshRenderer>();
            var turretMat = new Material(Shader.Find("Standard")) { color = darkColor };
            turretRenderer.material = turretMat;
            turret.transform.localScale = new Vector3(1f, 1f, 1f);
            turret.transform.localPosition = new Vector3(0f, 2.5f, 0f);

            // Legs (8 legs, 4 per side)
            float[] legZPositions = { -1.2f, -0.4f, 0.4f, 1.2f };
            for (int i = 0; i < 4; i++)
            {
                // Left legs
                var leftLeg = new GameObject($"LeftLeg_{i}") { transform = { parent = go.transform } };
                var leftLegFilter = leftLeg.AddComponent<MeshFilter>();
                leftLegFilter.mesh = CreatePrimitiveCylinder();
                var leftLegRenderer = leftLeg.AddComponent<MeshRenderer>();
                var legMat = new Material(Shader.Find("Standard")) { color = darkColor };
                leftLegRenderer.material = legMat;
                leftLeg.transform.localScale = new Vector3(0.3f, 0.3f, 1.5f);
                leftLeg.transform.localPosition = new Vector3(-1.2f, 0.5f, legZPositions[i]);
                leftLeg.transform.rotation = Quaternion.Euler(60f, 0f, 20f * (i - 1.5f));

                // Right legs
                var rightLeg = new GameObject($"RightLeg_{i}") { transform = { parent = go.transform } };
                var rightLegFilter = rightLeg.AddComponent<MeshFilter>();
                rightLegFilter.mesh = CreatePrimitiveCylinder();
                var rightLegRenderer = rightLeg.AddComponent<MeshRenderer>();
                rightLegRenderer.material = new Material(Shader.Find("Standard")) { color = darkColor };
                rightLeg.transform.localScale = new Vector3(0.3f, 0.3f, 1.5f);
                rightLeg.transform.localPosition = new Vector3(1.2f, 0.5f, legZPositions[i]);
                rightLeg.transform.rotation = Quaternion.Euler(60f, 0f, -20f * (i - 1.5f));
            }

            // Emission glow for visibility
            bodyMat.EnableKeyword("_EMISSION");
            bodyMat.SetColor("_EmissionColor", color * 0.15f);
        }

        /// <summary>
        /// Creates a health bar above a unit marker.
        /// </summary>
        private static void CreateHealthBar(GameObject parent, UnitStats stats, Color teamColor)
        {
            if (stats == null) return;

            var barGo = new GameObject("HealthBar") { transform = { parent = parent.transform } };
            barGo.transform.localPosition = Vector3.up * 2.5f;

            // Background
            var bg = new GameObject("Background") { transform = { parent = barGo.transform } };
            var bgFilter = bg.AddComponent<MeshFilter>();
            bgFilter.mesh = CreatePrimitiveBox();
            var bgRenderer = bg.AddComponent<MeshRenderer>();
            var bgMat = new Material(Shader.Find("Sprites/Default")) { color = Color.black };
            bgRenderer.material = bgMat;
            bg.transform.localScale = new Vector3(2f, 0.2f, 0.1f);

            // Health fill
            var fill = new GameObject("Fill") { transform = { parent = barGo.transform } };
            var fillFilter = fill.AddComponent<MeshFilter>();
            fillFilter.mesh = CreatePrimitiveBox();
            var fillRenderer = fill.AddComponent<MeshRenderer>();
            var fillMat = new Material(Shader.Find("Sprites/Default")) { color = teamColor };
            fillRenderer.material = fillMat;

            float healthPercent = stats.HealthPercent;
            fill.transform.localScale = new Vector3(2f * healthPercent, 0.15f, 0.15f);
            fill.transform.localPosition = new Vector3(-1f + healthPercent, 0, 0);

            // Store reference for updates
            var healthBar = parent.AddComponent<HealthBarComponent>();
            healthBar.FillTransform = fill.transform;
            healthBar.FillMaterial = fillMat;
            healthBar.TeamColor = teamColor;
        }

        /// <summary>
        /// Updates a unit's health bar.
        /// </summary>
        public static void UpdateHealthBar(GameObject unitGo, float healthPercent)
        {
            var healthBar = unitGo.GetComponent<HealthBarComponent>();
            if (healthBar != null && healthBar.FillTransform != null)
            {
                healthBar.FillTransform.localScale = new Vector3(2f * healthPercent, 0.15f, 0.15f);
                healthBar.FillTransform.localPosition = new Vector3(-1f + healthPercent, 0, 0);

                // Change color based on health
                if (healthPercent < 0.25f)
                {
                    healthBar.FillMaterial.color = Color.red;
                }
                else if (healthPercent < 0.5f)
                {
                    healthBar.FillMaterial.color = Color.yellow;
                }
                else
                {
                    healthBar.FillMaterial.color = healthBar.TeamColor;
                }
            }
        }

        /// <summary>
        /// Highlights a selected unit.
        /// </summary>
        public static void HighlightUnit(GameObject unitGo, bool selected)
        {
            var renderer = unitGo.GetComponentInChildren<MeshRenderer>();
            if (renderer != null)
            {
                if (selected)
                {
                    var mat = renderer.material;
                    mat.EnableKeyword("_EMISSION");
                    mat.SetColor("_EmissionColor", SelectedColor * 0.5f);
                }
                else
                {
                    var mat = renderer.material;
                    mat.DisableKeyword("_EMISSION");
                }
            }
        }

        // Primitive mesh creators
        private static Mesh CreatePrimitiveBox()
        {
            var cube = GameObject.CreatePrimitive(PrimitiveType.Cube);
            var mesh = cube.GetComponent<MeshFilter>().sharedMesh;
            Destroy(cube);
            return mesh;
        }

        private static Mesh CreatePrimitiveCylinder()
        {
            var cyl = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
            var mesh = cyl.GetComponent<MeshFilter>().sharedMesh;
            Destroy(cyl);
            return mesh;
        }

        private static Mesh CreatePrimitiveSphere()
        {
            var sphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            var mesh = sphere.GetComponent<MeshFilter>().sharedMesh;
            Destroy(sphere);
            return mesh;
        }

        private static Mesh CreatePrimitiveCapsule()
        {
            var capsule = GameObject.CreatePrimitive(PrimitiveType.Capsule);
            var mesh = capsule.GetComponent<MeshFilter>().sharedMesh;
            Destroy(capsule);
            return mesh;
        }
    }

    /// <summary>
    /// Stores unit data on a GameObject for selection queries.
    /// </summary>
    public class UnitInfo : MonoBehaviour
    {
        public string UnitId;
        public string UnitName;
        public string UnitType;
        public string Team;
        public UnitStats Stats;
    }

    /// <summary>
    /// Component for updating health bars dynamically.
    /// </summary>
    public class HealthBarComponent : MonoBehaviour
    {
        public Transform FillTransform;
        public Material FillMaterial;
        public Color TeamColor;
    }
}
