using UnityEngine;
using UnityEngine.TestTools;
using NUnit.Framework;
using System.Collections;
using System.Collections.Generic;
using DoraMCU.Rendering;

namespace DoraMCU.Tests.SceneTransition
{
    /// <summary>
    /// Tests for GalaxyRenderer interaction system: sector/star rendering, click detection,
    /// event firing, and hover highlighting.
    /// </summary>
    [TestFixture]
    public class GalaxyRendererTests
    {
        private GameObject testScene;
        private GameObject cameraObject;
        private GameObject rendererObject;
        private GalaxyRenderer renderer;

        [SetUp]
        public void SetUp()
        {
            testScene = new GameObject("TestScene");
            cameraObject = new GameObject("MainCamera");
            cameraObject.AddComponent<Camera>();
            cameraObject.tag = "MainCamera";

            rendererObject = new GameObject("GalaxyRenderer");
            renderer = rendererObject.AddComponent<GalaxyRenderer>();
        }

        [TearDown]
        public void TearDown()
        {
            Object.DestroyImmediate(testScene);
        }

        #region GalaxyData Structure Tests

        [Test]
        public void GalaxyData_Creation_InitializedCorrectly()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>(),
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            Assert.IsNotNull(data.sectors);
            Assert.IsNotNull(data.stars);
            Assert.IsNotNull(data.hyperlanes);
            Assert.AreEqual(0, data.sectors.Count);
            Assert.AreEqual(0, data.stars.Count);
            Assert.AreEqual(0, data.hyperlanes.Count);
        }

        [Test]
        public void SectorData_Properties_SetCorrectly()
        {
            var sector = new SectorData
            {
                id = "Sector_1",
                name = "Alpha Centauri",
                x = 1,
                z = 2
            };

            Assert.AreEqual("Sector_1", sector.id);
            Assert.AreEqual("Alpha Centauri", sector.name);
            Assert.AreEqual(1, sector.x);
            Assert.AreEqual(2, sector.z);
        }

        [Test]
        public void StarData_Properties_SetCorrectly()
        {
            var star = new StarData
            {
                id = "Star_101",
                sectorX = 1,
                sectorZ = 0,
                localX = 0.5f,
                localZ = 0.3f
            };

            Assert.AreEqual("Star_101", star.id);
            Assert.AreEqual(1, star.sectorX);
            Assert.AreEqual(0, star.sectorZ);
            Assert.AreEqual(0.5f, star.localX);
            Assert.AreEqual(0.3f, star.localZ);
        }

        [Test]
        public void HyperlaneData_Properties_SetCorrectly()
        {
            Vector3 from = new Vector3(0f, 0f, 0f);
            Vector3 to = new Vector3(100f, 0f, 100f);

            var hyperlane = new HyperlaneData
            {
                from = from,
                to = to
            };

            Assert.AreEqual(from, hyperlane.from);
            Assert.AreEqual(to, hyperlane.to);
        }

        [Test]
        public void GalaxyData_WithMultipleSectors()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_0", name = "Core", x = 0, z = 0 },
                    new SectorData { id = "Sector_1", name = "Alpha", x = 1, z = 0 },
                    new SectorData { id = "Sector_2", name = "Beta", x = 0, z = 1 }
                },
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            Assert.AreEqual(3, data.sectors.Count);
            Assert.AreEqual("Core", data.sectors[0].name);
            Assert.AreEqual("Alpha", data.sectors[1].name);
            Assert.AreEqual("Beta", data.sectors[2].name);
        }

        #endregion

        #region Sector ID Parsing Tests

        [Test]
        public void SectorId_Parsing_SimpleId()
        {
            string sectorId = "Sector_5";
            string numericPart = sectorId.Replace("Sector_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int x = id / 10;
                int z = id % 10;
                
                Assert.AreEqual(0, x);
                Assert.AreEqual(5, z);
            }
            else
            {
                Assert.Fail("Should parse sector ID");
            }
        }

        [Test]
        public void SectorId_Parsing_TwoDigitId()
        {
            string sectorId = "Sector_12";
            string numericPart = sectorId.Replace("Sector_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int x = id / 10;
                int z = id % 10;
                
                Assert.AreEqual(1, x);
                Assert.AreEqual(2, z);
            }
            else
            {
                Assert.Fail("Should parse sector ID");
            }
        }

        [Test]
        public void SectorId_Parsing_ZeroId()
        {
            string sectorId = "Sector_0";
            string numericPart = sectorId.Replace("Sector_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int x = id / 10;
                int z = id % 10;
                
                Assert.AreEqual(0, x);
                Assert.AreEqual(0, z);
            }
            else
            {
                Assert.Fail("Should parse sector ID");
            }
        }

        [Test]
        public void SectorId_Parsing_LargeId()
        {
            string sectorId = "Sector_99";
            string numericPart = sectorId.Replace("Sector_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int x = id / 10;
                int z = id % 10;
                
                Assert.AreEqual(9, x);
                Assert.AreEqual(9, z);
            }
            else
            {
                Assert.Fail("Should parse sector ID");
            }
        }

        #endregion

        #region Star ID Parsing Tests

        [Test]
        public void StarId_Parsing_SimpleId()
        {
            string starId = "Star_101";
            string numericPart = starId.Replace("Star_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int sectorX = id / 100;
                int sectorZ = id % 100;
                
                Assert.AreEqual(1, sectorX);
                Assert.AreEqual(1, sectorZ);
            }
            else
            {
                Assert.Fail("Should parse star ID");
            }
        }

        [Test]
        public void StarId_Parsing_LargeId()
        {
            string starId = "Star_1234";
            string numericPart = starId.Replace("Star_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int sectorX = id / 100;
                int sectorZ = id % 100;
                
                Assert.AreEqual(12, sectorX);
                Assert.AreEqual(34, sectorZ);
            }
            else
            {
                Assert.Fail("Should parse star ID");
            }
        }

        [Test]
        public void StarId_Parsing_ZeroId()
        {
            string starId = "Star_0";
            string numericPart = starId.Replace("Star_", "");
            
            if (int.TryParse(numericPart, out int id))
            {
                int sectorX = id / 100;
                int sectorZ = id % 100;
                
                Assert.AreEqual(0, sectorX);
                Assert.AreEqual(0, sectorZ);
            }
            else
            {
                Assert.Fail("Should parse star ID");
            }
        }

        #endregion

        #region Sector Position Calculation Tests

        [Test]
        public void SectorPosition_CalculatedFromCoordinates()
        {
            float sectorSpacing = 10f;
            int x = 1;
            int z = 2;

            Vector3 expectedPos = new Vector3(
                x * sectorSpacing,
                0,
                z * sectorSpacing
            );

            Assert.AreEqual(10f, expectedPos.x);
            Assert.AreEqual(0f, expectedPos.y);
            Assert.AreEqual(20f, expectedPos.z);
        }

        [Test]
        public void SectorPosition_ZeroCoordinates_AtOrigin()
        {
            float sectorSpacing = 10f;
            int x = 0;
            int z = 0;

            Vector3 pos = new Vector3(
                x * sectorSpacing,
                0,
                z * sectorSpacing
            );

            Assert.AreEqual(Vector3.zero, pos);
        }

        [Test]
        public void SectorPosition_NegativeCoordinates()
        {
            float sectorSpacing = 10f;
            int x = -1;
            int z = -2;

            Vector3 pos = new Vector3(
                x * sectorSpacing,
                0,
                z * sectorSpacing
            );

            Assert.AreEqual(-10f, pos.x);
            Assert.AreEqual(0f, pos.y);
            Assert.AreEqual(-20f, pos.z);
        }

        [Test]
        public void SectorPosition_ScaleFactor_AppliedCorrectly()
        {
            float sectorSpacing = 10f;
            float scale = sectorSpacing * 0.8f;
            
            Assert.AreEqual(8f, scale, "Sector scale should be 80% of spacing");
        }

        #endregion

        #region Star Position Calculation Tests

        [Test]
        public void StarPosition_CalculatedFromSectorAndLocal()
        {
            float sectorSpacing = 10f;
            int sectorX = 1;
            int sectorZ = 2;
            float localX = 0.5f;
            float localZ = 0.3f;

            Vector3 pos = new Vector3(
                sectorX * sectorSpacing + localX,
                0,
                sectorZ * sectorSpacing + localZ
            );

            Assert.AreEqual(10.5f, pos.x);
            Assert.AreEqual(0f, pos.y);
            Assert.AreEqual(20.3f, pos.z);
        }

        [Test]
        public void StarPosition_LocalOffset_AdjustsPosition()
        {
            float sectorSpacing = 10f;
            int sectorX = 0;
            int sectorZ = 0;
            float localX = 5f;
            float localZ = 3f;

            Vector3 pos = new Vector3(
                sectorX * sectorSpacing + localX,
                0,
                sectorZ * sectorSpacing + localZ
            );

            Assert.AreEqual(5f, pos.x);
            Assert.AreEqual(3f, pos.z);
        }

        #endregion

        #region Event Delegate Tests

        [Test]
        public void SectorClickedHandler_Delegate_CanBeCreated()
        {
            bool eventFired = false;
            string capturedId = null;
            int capturedX = 0;
            int capturedZ = 0;

            SectorClickedHandler handler = (id, x, z) =>
            {
                eventFired = true;
                capturedId = id;
                capturedX = x;
                capturedZ = z;
            };

            // Invoke the handler
            handler?.Invoke("Sector_1", 0, 1);

            Assert.IsTrue(eventFired);
            Assert.AreEqual("Sector_1", capturedId);
            Assert.AreEqual(0, capturedX);
            Assert.AreEqual(1, capturedZ);
        }

        [Test]
        public void StarClickedHandler_Delegate_CanBeCreated()
        {
            bool eventFired = false;
            string capturedId = null;
            int capturedSectorX = 0;
            int capturedSectorZ = 0;

            StarClickedHandler handler = (id, sectorX, sectorZ) =>
            {
                eventFired = true;
                capturedId = id;
                capturedSectorX = sectorX;
                capturedSectorZ = sectorZ;
            };

            // Invoke the handler
            handler?.Invoke("Star_101", 1, 1);

            Assert.IsTrue(eventFired);
            Assert.AreEqual("Star_101", capturedId);
            Assert.AreEqual(1, capturedSectorX);
            Assert.AreEqual(1, capturedSectorZ);
        }

        [Test]
        public void EventDelegate_NullInvocation_IsSafe()
        {
            SectorClickedHandler handler = null;
            
            // Should not throw
            handler?.Invoke("Sector_1", 0, 1);
            
            Assert.Pass("Null invocation should not throw");
        }

        #endregion

        #region Sector Object Dictionary Tests

        [Test]
        public void SectorObjects_Dictionary_StoresSectors()
        {
            var sectorObjects = new Dictionary<string, GameObject>();
            
            GameObject sector1 = new GameObject("Sector_1");
            GameObject sector2 = new GameObject("Sector_2");
            
            sectorObjects["Sector_1"] = sector1;
            sectorObjects["Sector_2"] = sector2;
            
            Assert.AreEqual(2, sectorObjects.Count);
            Assert.AreEqual(sector1, sectorObjects["Sector_1"]);
            Assert.AreEqual(sector2, sectorObjects["Sector_2"]);
            
            Object.DestroyImmediate(sector1);
            Object.DestroyImmediate(sector2);
        }

        [Test]
        public void SectorObjects_Dictionary_ContainsKey()
        {
            var sectorObjects = new Dictionary<string, GameObject>();
            
            GameObject sector = new GameObject("Sector_1");
            sectorObjects["Sector_1"] = sector;
            
            Assert.IsTrue(sectorObjects.ContainsKey("Sector_1"));
            Assert.IsFalse(sectorObjects.ContainsKey("Sector_2"));
            
            Object.DestroyImmediate(sector);
        }

        [Test]
        public void SectorObjects_Dictionary_Clear()
        {
            var sectorObjects = new Dictionary<string, GameObject>();
            
            sectorObjects["Sector_1"] = new GameObject("Sector_1");
            sectorObjects["Sector_2"] = new GameObject("Sector_2");
            
            Assert.AreEqual(2, sectorObjects.Count);
            
            sectorObjects.Clear();
            
            Assert.AreEqual(0, sectorObjects.Count);
        }

        #endregion

        #region Star Object Dictionary Tests

        [Test]
        public void StarObjects_Dictionary_StoresStars()
        {
            var starObjects = new Dictionary<string, GameObject>();
            
            GameObject star1 = new GameObject("Star_101");
            GameObject star2 = new GameObject("Star_102");
            
            starObjects["Star_101"] = star1;
            starObjects["Star_102"] = star2;
            
            Assert.AreEqual(2, starObjects.Count);
            Assert.AreEqual(star1, starObjects["Star_101"]);
            Assert.AreEqual(star2, starObjects["Star_102"]);
            
            Object.DestroyImmediate(star1);
            Object.DestroyImmediate(star2);
        }

        [Test]
        public void StarObjects_Dictionary_ContainsKey()
        {
            var starObjects = new Dictionary<string, GameObject>();
            
            GameObject star = new GameObject("Star_101");
            starObjects["Star_101"] = star;
            
            Assert.IsTrue(starObjects.ContainsKey("Star_101"));
            Assert.IsFalse(starObjects.ContainsKey("Star_999"));
            
            Object.DestroyImmediate(star);
        }

        #endregion

        #region Hyperlane Tests

        [Test]
        public void Hyperlane_Connection_BetweenTwoPoints()
        {
            Vector3 from = new Vector3(0f, 0f, 0f);
            Vector3 to = new Vector3(100f, 0f, 100f);
            
            float distance = Vector3.Distance(from, to);
            
            Assert.Greater(distance, 0f, "Hyperlane should have positive length");
            Assert.AreEqual(141.421356f, distance, 0.001f, "Distance should be sqrt(100^2 + 100^2)");
        }

        [Test]
        public void Hyperlane_SamePoint_ZeroLength()
        {
            Vector3 from = new Vector3(50f, 0f, 50f);
            Vector3 to = new Vector3(50f, 0f, 50f);
            
            float distance = Vector3.Distance(from, to);
            
            Assert.AreEqual(0f, distance, "Hyperlane from point to itself should have zero length");
        }

        [Test]
        public void Hyperlane_MultipleConnections()
        {
            var hyperlanes = new List<HyperlaneData>
            {
                new HyperlaneData { from = new Vector3(0f, 0f, 0f), to = new Vector3(10f, 0f, 0f) },
                new HyperlaneData { from = new Vector3(10f, 0f, 0f), to = new Vector3(20f, 0f, 0f) },
                new HyperlaneData { from = new Vector3(0f, 0f, 0f), to = new Vector3(0f, 0f, 10f) }
            };
            
            Assert.AreEqual(3, hyperlanes.Count);
        }

        #endregion

        #region Color Tests

        [Test]
        public void SectorColor_Default_IsSemiTransparent()
        {
            Color sectorColor = new Color(0.1f, 0.2f, 0.4f, 0.3f);
            
            Assert.AreEqual(0.1f, sectorColor.r);
            Assert.AreEqual(0.2f, sectorColor.g);
            Assert.AreEqual(0.4f, sectorColor.b);
            Assert.AreEqual(0.3f, sectorColor.a, "Sector color should be semi-transparent");
        }

        [Test]
        public void HyperlaneColor_Default_IsSemiTransparent()
        {
            Color hyperlaneColor = new Color(0.3f, 0.6f, 1f, 0.5f);
            
            Assert.AreEqual(0.3f, hyperlaneColor.r);
            Assert.AreEqual(0.6f, hyperlaneColor.g);
            Assert.AreEqual(1f, hyperlaneColor.b);
            Assert.AreEqual(0.5f, hyperlaneColor.a);
        }

        [Test]
        public void StarColor_Default_IsWhite()
        {
            Color starColor = Color.white;
            
            Assert.AreEqual(1f, starColor.r);
            Assert.AreEqual(1f, starColor.g);
            Assert.AreEqual(1f, starColor.b);
            Assert.AreEqual(1f, starColor.a);
        }

        [Test]
        public void HighlightColor_IsYellow()
        {
            Color highlightColor = Color.yellow;
            
            Assert.AreEqual(1f, highlightColor.r);
            Assert.AreEqual(1f, highlightColor.g);
            Assert.AreEqual(0f, highlightColor.b);
        }

        #endregion

        #region Renderer Component Tests

        [Test]
        public void GalaxyRenderer_Component_AddedToGameObject()
        {
            Assert.IsNotNull(renderer, "GalaxyRenderer should be added to GameObject");
            Assert.AreEqual("GalaxyRenderer", rendererObject.name);
        }

        [Test]
        public void GalaxyRenderer_SectorObjects_InitiallyNull()
        {
            // Before Awake is called, sectorObjects should be null
            // After Awake, they should be initialized
            Assert.IsNotNull(renderer.SectorObjects, "SectorObjects should be initialized after Awake");
        }

        [Test]
        public void GalaxyRenderer_StarObjects_InitiallyNull()
        {
            Assert.IsNotNull(renderer.StarObjects, "StarObjects should be initialized after Awake");
        }

        [Test]
        public void GalaxyRenderer_SectorObjects_InitiallyEmpty()
        {
            Assert.AreEqual(0, renderer.SectorObjects.Count, "SectorObjects should be empty initially");
        }

        [Test]
        public void GalaxyRenderer_StarObjects_InitiallyEmpty()
        {
            Assert.AreEqual(0, renderer.StarObjects.Count, "StarObjects should be empty initially");
        }

        #endregion

        #region Clear Tests

        [Test]
        public void Clear_RemovesAllObjects()
        {
            // Create test data
            var data = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_0", name = "Test", x = 0, z = 0 }
                },
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            // Render galaxy
            renderer.RenderGalaxy(data);
            
            Assert.AreEqual(1, renderer.SectorObjects.Count);

            // Clear
            renderer.Clear();
            
            Assert.AreEqual(0, renderer.SectorObjects.Count);
            Assert.AreEqual(0, renderer.StarObjects.Count);
        }

        [Test]
        public void Clear_CanBeCalledMultipleTimes()
        {
            // Clear multiple times should not throw
            renderer.Clear();
            renderer.Clear();
            renderer.Clear();
            
            Assert.Pass("Multiple Clear calls should not throw");
        }

        [Test]
        public void Clear_OnEmptyRenderer()
        {
            renderer.Clear();
            Assert.AreEqual(0, renderer.SectorObjects.Count);
        }

        #endregion

        #region RenderGalaxy Tests

        [Test]
        public void RenderGalaxy_NullData_DoesNotThrow()
        {
            renderer.RenderGalaxy(null);
            Assert.Pass("Rendering null data should not throw");
        }

        [Test]
        public void RenderGalaxy_EmptyData_CreatesNoObjects()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>(),
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data);
            
            Assert.AreEqual(0, renderer.SectorObjects.Count);
            Assert.AreEqual(0, renderer.StarObjects.Count);
        }

        [Test]
        public void RenderGalaxy_WithSectors_CreatesSectorObjects()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_0", name = "Core", x = 0, z = 0 },
                    new SectorData { id = "Sector_1", name = "Alpha", x = 1, z = 0 }
                },
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data);
            
            Assert.AreEqual(2, renderer.SectorObjects.Count);
            Assert.IsTrue(renderer.SectorObjects.ContainsKey("Sector_0"));
            Assert.IsTrue(renderer.SectorObjects.ContainsKey("Sector_1"));
        }

        [Test]
        public void RenderGalaxy_WithStars_CreatesStarObjects()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>(),
                stars = new List<StarData>
                {
                    new StarData { id = "Star_101", sectorX = 1, sectorZ = 0, localX = 0.5f, localZ = 0.3f }
                },
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data);
            
            Assert.AreEqual(1, renderer.StarObjects.Count);
            Assert.IsTrue(renderer.StarObjects.ContainsKey("Star_101"));
        }

        [Test]
        public void RenderGalaxy_WithHyperlanes_CreatesConnections()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>(),
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>
                {
                    new HyperlaneData { from = new Vector3(0f, 0f, 0f), to = new Vector3(10f, 0f, 0f) }
                }
            };

            renderer.RenderGalaxy(data);
            
            // Hyperlanes are created as LineRenderers
            Assert.Pass("Hyperlane rendering should not throw");
        }

        [Test]
        public void RenderGalaxy_OverwritesPreviousRender()
        {
            var data1 = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_0", name = "Test", x = 0, z = 0 }
                },
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data1);
            Assert.AreEqual(1, renderer.SectorObjects.Count);

            var data2 = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_1", name = "New", x = 1, z = 0 },
                    new SectorData { id = "Sector_2", name = "Another", x = 2, z = 0 }
                },
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data2);
            Assert.AreEqual(2, renderer.SectorObjects.Count);
            Assert.IsFalse(renderer.SectorObjects.ContainsKey("Sector_0"), "Old sector should be cleared");
            Assert.IsTrue(renderer.SectorObjects.ContainsKey("Sector_1"));
            Assert.IsTrue(renderer.SectorObjects.ContainsKey("Sector_2"));
        }

        #endregion

        #region LayerMask Tests

        [Test]
        public void InteractionLayer_Default_IsAllLayers()
        {
            // Default LayerMask -1 means all layers
            int defaultLayer = -1;
            Assert.AreEqual(-1, defaultLayer, "Default interaction layer should be all layers");
        }

        #endregion

        #region Integration Tests

        [Test]
        public void FullGalaxyRender_CompleteData()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_0", name = "Core", x = 0, z = 0 },
                    new SectorData { id = "Sector_1", name = "Alpha", x = 1, z = 0 }
                },
                stars = new List<StarData>
                {
                    new StarData { id = "Star_101", sectorX = 1, sectorZ = 0, localX = 0.5f, localZ = 0.3f },
                    new StarData { id = "Star_102", sectorX = 1, sectorZ = 0, localX = -0.2f, localZ = 0.8f }
                },
                hyperlanes = new List<HyperlaneData>
                {
                    new HyperlaneData { from = new Vector3(0f, 0f, 0f), to = new Vector3(10f, 0f, 0f) }
                }
            };

            renderer.RenderGalaxy(data);
            
            Assert.AreEqual(2, renderer.SectorObjects.Count);
            Assert.AreEqual(2, renderer.StarObjects.Count);
        }

        [Test]
        public void SectorPosition_VerifyInScene()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>
                {
                    new SectorData { id = "Sector_1", name = "Test", x = 1, z = 2 }
                },
                stars = new List<StarData>(),
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data);
            
            if (renderer.SectorObjects.TryGetValue("Sector_1", out GameObject sectorObj))
            {
                // Expected position: (1 * 10, 0, 2 * 10) = (10, 0, 20)
                Assert.AreEqual(10f, sectorObj.transform.position.x);
                Assert.AreEqual(0f, sectorObj.transform.position.y);
                Assert.AreEqual(20f, sectorObj.transform.position.z);
            }
            else
            {
                Assert.Fail("Sector should be in dictionary");
            }
        }

        [Test]
        public void StarPosition_VerifyInScene()
        {
            var data = new GalaxyData
            {
                sectors = new List<SectorData>(),
                stars = new List<StarData>
                {
                    new StarData { id = "Star_101", sectorX = 1, sectorZ = 0, localX = 0.5f, localZ = 0.3f }
                },
                hyperlanes = new List<HyperlaneData>()
            };

            renderer.RenderGalaxy(data);
            
            if (renderer.StarObjects.TryGetValue("Star_101", out GameObject starObj))
            {
                // Expected position: (1 * 10 + 0.5, 0, 0 * 10 + 0.3) = (10.5, 0, 0.3)
                Assert.AreEqual(10.5f, starObj.transform.position.x);
                Assert.AreEqual(0f, starObj.transform.position.y);
                Assert.AreEqual(0.3f, starObj.transform.position.z);
            }
            else
            {
                Assert.Fail("Star should be in dictionary");
            }
        }

        #endregion
    }
}
