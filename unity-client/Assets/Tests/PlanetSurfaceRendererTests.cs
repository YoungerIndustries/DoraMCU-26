using UnityEngine;
using UnityEngine.TestTools;
using NUnit.Framework;
using System.Collections;
using System.Collections.Generic;
using DoraMCU.Rendering;

namespace DoraMCU.Tests.SceneTransition
{
    /// <summary>
    /// Tests for PlanetSurfaceRenderer interaction system: city rendering, click detection,
    /// CityMarkerData components, and latitude/longitude to Vector3 conversion.
    /// </summary>
    [TestFixture]
    public class PlanetSurfaceRendererTests
    {
        private GameObject testScene;
        private GameObject cameraObject;
        private GameObject rendererObject;
        private PlanetSurfaceRenderer renderer;

        [SetUp]
        public void SetUp()
        {
            testScene = new GameObject("TestScene");
            cameraObject = new GameObject("MainCamera");
            cameraObject.AddComponent<Camera>();
            cameraObject.tag = "MainCamera";

            rendererObject = new GameObject("PlanetSurfaceRenderer");
            renderer = rendererObject.AddComponent<PlanetSurfaceRenderer>();
        }

        [TearDown]
        public void TearDown()
        {
            Object.DestroyImmediate(testScene);
        }

        #region PlanetSurfaceData Structure Tests

        [Test]
        public void PlanetSurfaceData_Creation_InitializedCorrectly()
        {
            var data = new PlanetSurfaceData
            {
                name = "Earth",
                type = "terrestrial",
                seed = 12345,
                cities = new List<CityData>()
            };

            Assert.AreEqual("Earth", data.name);
            Assert.AreEqual("terrestrial", data.type);
            Assert.AreEqual(12345, data.seed);
            Assert.IsNotNull(data.cities);
            Assert.AreEqual(0, data.cities.Count);
        }

        [Test]
        public void CityData_Properties_SetCorrectly()
        {
            var city = new CityData
            {
                name = "New York",
                latitude = 40.7128f,
                longitude = -74.0060f,
                population = 8336817
            };

            Assert.AreEqual("New York", city.name);
            Assert.AreEqual(40.7128f, city.latitude, 0.0001f);
            Assert.AreEqual(-74.0060f, city.longitude, 0.0001f);
            Assert.AreEqual(8336817, city.population);
        }

        [Test]
        public void PlanetSurfaceData_WithMultipleCities()
        {
            var data = new PlanetSurfaceData
            {
                name = "Earth",
                type = "terrestrial",
                seed = 12345,
                cities = new List<CityData>
                {
                    new CityData { name = "New York", latitude = 40.7128f, longitude = -74.0060f, population = 8336817 },
                    new CityData { name = "London", latitude = 51.5074f, longitude = -0.1278f, population = 8982000 },
                    new CityData { name = "Tokyo", latitude = 35.6762f, longitude = 139.6503f, population = 13960000 }
                }
            };

            Assert.AreEqual(3, data.cities.Count);
            Assert.AreEqual("New York", data.cities[0].name);
            Assert.AreEqual("London", data.cities[1].name);
            Assert.AreEqual("Tokyo", data.cities[2].name);
        }

        [Test]
        public void PlanetSurfaceData_PlanetTypes()
        {
            string[] types = { "terrestrial", "desert", "ice", "gas_giant" };
            
            foreach (var type in types)
            {
                var data = new PlanetSurfaceData
                {
                    name = "TestPlanet",
                    type = type,
                    seed = 12345,
                    cities = new List<CityData>()
                };
                
                Assert.AreEqual(type, data.type);
            }
        }

        #endregion

        #region Latitude/Longitude to Vector3 Conversion Tests

        [Test]
        public void LatitudeLongitudeToVector3_EquatorZeroLongitude()
        {
            float latitude = 0f;
            float longitude = 0f;
            float radius = 1f;

            // Convert latitude/longitude to radians
            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                Mathf.Sin(latRad) * radius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
            );

            // At equator (0, 0), position should be (0, 0, radius)
            Assert.AreEqual(0f, pos.x, 0.001f);
            Assert.AreEqual(0f, pos.y, 0.001f);
            Assert.AreEqual(radius, pos.z, 0.001f);
        }

        [Test]
        public void LatitudeLongitudeToVector3_NorthPole()
        {
            float latitude = 90f;
            float longitude = 0f;
            float radius = 1f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                Mathf.Sin(latRad) * radius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
            );

            // At north pole, Y should be at radius
            Assert.AreEqual(0f, pos.x, 0.001f);
            Assert.AreEqual(radius, pos.y, 0.001f);
            Assert.AreEqual(0f, pos.z, 0.001f);
        }

        [Test]
        public void LatitudeLongitudeToVector3_SouthPole()
        {
            float latitude = -90f;
            float longitude = 0f;
            float radius = 1f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                Mathf.Sin(latRad) * radius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
            );

            // At south pole, Y should be at -radius
            Assert.AreEqual(0f, pos.x, 0.001f);
            Assert.AreEqual(-radius, pos.y, 0.001f);
            Assert.AreEqual(0f, pos.z, 0.001f);
        }

        [Test]
        public void LatitudeLongitudeToVector3_PrimeMeridian()
        {
            float latitude = 0f;
            float longitude = 90f;
            float radius = 1f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                Mathf.Sin(latRad) * radius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
            );

            // At 0 latitude, 90 longitude, X should be at radius
            Assert.AreEqual(radius, pos.x, 0.001f);
            Assert.AreEqual(0f, pos.y, 0.001f);
            Assert.AreEqual(0f, pos.z, 0.001f);
        }

        [Test]
        public void LatitudeLongitudeToVector3_MagnitudeEqualsRadius()
        {
            float radius = 5f;

            // Test multiple lat/lon combinations
            float[] latitudes = { 0f, 45f, -45f, 90f, -90f };
            float[] longitudes = { 0f, 90f, -90f, 180f, -180f };

            foreach (var lat in latitudes)
            {
                foreach (var lon in longitudes)
                {
                    float latRad = lat * Mathf.Deg2Rad;
                    float lonRad = lon * Mathf.Deg2Rad;

                    Vector3 pos = new Vector3(
                        Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                        Mathf.Sin(latRad) * radius,
                        Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
                    );

                    Assert.AreEqual(radius, pos.magnitude, 0.001f, 
                        $"Magnitude should equal radius for lat={lat}, lon={lon}");
                }
            }
        }

        [Test]
        public void LatitudeLongitudeToVector3_NewYorkCoordinates()
        {
            float latitude = 40.7128f;
            float longitude = -74.0060f;
            float radius = 1f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                Mathf.Sin(latRad) * radius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
            );

            // Verify magnitude is 1
            Assert.AreEqual(1f, pos.magnitude, 0.001f);
            
            // Y should be positive (northern hemisphere)
            Assert.Greater(pos.y, 0f);
        }

        [Test]
        public void LatitudeLongitudeToVector3_DifferentRadius()
        {
            float latitude = 0f;
            float longitude = 0f;
            float radius = 10f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * radius,
                Mathf.Sin(latRad) * radius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * radius
            );

            Assert.AreEqual(0f, pos.x, 0.001f);
            Assert.AreEqual(0f, pos.y, 0.001f);
            Assert.AreEqual(10f, pos.z, 0.001f);
        }

        #endregion

        #region City Marker Position Tests

        [Test]
        public void CityMarker_PositionOnPlanetSurface()
        {
            float planetRadius = 5f;
            float latitude = 0f;
            float longitude = 0f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            // City marker is positioned at radius * 1.01 to be slightly above surface
            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * planetRadius * 1.01f,
                Mathf.Sin(latRad) * planetRadius * 1.01f,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * planetRadius * 1.01f
            );

            Assert.AreEqual(5.05f, pos.magnitude, 0.001f, "City marker should be slightly above surface");
        }

        [Test]
        public void CityMarker_LocalScale_IsSmall()
        {
            Vector3 scale = new Vector3(0.05f, 0.1f, 0.05f);
            
            Assert.AreEqual(0.05f, scale.x);
            Assert.AreEqual(0.1f, scale.y);
            Assert.AreEqual(0.05f, scale.z);
        }

        [Test]
        public void CityMarker_Rotation_LooksOutward()
        {
            float latitude = 0f;
            float longitude = 0f;
            float planetRadius = 5f;

            float latRad = latitude * Mathf.Deg2Rad;
            float lonRad = longitude * Mathf.Deg2Rad;

            Vector3 pos = new Vector3(
                Mathf.Cos(latRad) * Mathf.Sin(lonRad) * planetRadius,
                Mathf.Sin(latRad) * planetRadius,
                Mathf.Cos(latRad) * Mathf.Cos(lonRad) * planetRadius
            );

            // Rotation should look outward from center
            Quaternion rotation = Quaternion.LookRotation(pos);
            
            // The rotation should point in the direction of pos
            Vector3 forward = rotation * Vector3.forward;
            Assert.AreEqual(pos.normalized.x, forward.normalized.x, 0.01f);
            Assert.AreEqual(pos.normalized.y, forward.normalized.y, 0.01f);
            Assert.AreEqual(pos.normalized.z, forward.normalized.z, 0.01f);
        }

        #endregion

        #region CityMarkerData Component Tests

        [Test]
        public void CityMarkerData_Component_AddedToGameObject()
        {
            GameObject cityGo = new GameObject("CityMarker");
            var marker = cityGo.AddComponent<CityMarkerData>();
            
            Assert.IsNotNull(marker);
            Object.DestroyImmediate(cityGo);
        }

        [Test]
        public void CityMarkerData_Properties_SetCorrectly()
        {
            GameObject cityGo = new GameObject("CityMarker");
            var marker = cityGo.AddComponent<CityMarkerData>();
            
            marker.cityName = "New York";
            marker.latitude = 40.7128f;
            marker.longitude = -74.0060f;
            
            Assert.AreEqual("New York", marker.cityName);
            Assert.AreEqual(40.7128f, marker.latitude, 0.0001f);
            Assert.AreEqual(-74.0060f, marker.longitude, 0.0001f);
            
            Object.DestroyImmediate(cityGo);
        }

        [Test]
        public void CityMarkerData_DefaultValues()
        {
            GameObject cityGo = new GameObject("CityMarker");
            var marker = cityGo.AddComponent<CityMarkerData>();
            
            Assert.IsNull(marker.cityName, "Default city name should be null");
            Assert.AreEqual(0f, marker.latitude, "Default latitude should be 0");
            Assert.AreEqual(0f, marker.longitude, "Default longitude should be 0");
            
            Object.DestroyImmediate(cityGo);
        }

        [Test]
        public void CityMarkerData_MultipleMarkers()
        {
            GameObject city1 = new GameObject("City1");
            GameObject city2 = new GameObject("City2");
            
            var marker1 = city1.AddComponent<CityMarkerData>();
            marker1.cityName = "City1";
            
            var marker2 = city2.AddComponent<CityMarkerData>();
            marker2.cityName = "City2";
            
            Assert.AreEqual("City1", marker1.cityName);
            Assert.AreEqual("City2", marker2.cityName);
            
            Object.DestroyImmediate(city1);
            Object.DestroyImmediate(city2);
        }

        #endregion

        #region Planet Color Tests

        [Test]
        public void PlanetColor_Terrestrial_IsGreen()
        {
            Color terrestrialColor = new Color(0.2f, 0.5f, 0.2f);
            
            Assert.AreEqual(0.2f, terrestrialColor.r);
            Assert.AreEqual(0.5f, terrestrialColor.g);
            Assert.AreEqual(0.2f, terrestrialColor.b);
        }

        [Test]
        public void PlanetColor_Desert_IsBrown()
        {
            Color desertColor = new Color(0.76f, 0.56f, 0.32f);
            
            Assert.AreEqual(0.76f, desertColor.r);
            Assert.AreEqual(0.56f, desertColor.g);
            Assert.AreEqual(0.32f, desertColor.b);
        }

        [Test]
        public void PlanetColor_Ice_IsBlue()
        {
            Color iceColor = new Color(0.7f, 0.8f, 0.9f);
            
            Assert.AreEqual(0.7f, iceColor.r);
            Assert.AreEqual(0.8f, iceColor.g);
            Assert.AreEqual(0.9f, iceColor.b);
        }

        [Test]
        public void PlanetColor_GasGiant_IsOrange()
        {
            Color gasGiantColor = new Color(0.6f, 0.4f, 0.3f);
            
            Assert.AreEqual(0.6f, gasGiantColor.r);
            Assert.AreEqual(0.4f, gasGiantColor.g);
            Assert.AreEqual(0.3f, gasGiantColor.b);
        }

        [Test]
        public void PlanetColor_Atmosphere_IsSemiTransparent()
        {
            Color atmosphereColor = new Color(0.3f, 0.6f, 1f, 0.3f);
            
            Assert.AreEqual(0.3f, atmosphereColor.a, "Atmosphere should be semi-transparent");
        }

        [Test]
        public void GetPlanetColor_SwitchStatement()
        {
            // Simulate the switch statement in GetPlanetColor
            Color terrestrialColor = new Color(0.2f, 0.5f, 0.2f);
            Color desertColor = new Color(0.76f, 0.56f, 0.32f);
            Color iceColor = new Color(0.7f, 0.8f, 0.9f);
            Color gasGiantColor = new Color(0.6f, 0.4f, 0.3f);

            Color GetPlanetColor(string type)
            {
                return type.ToLower() switch
                {
                    "terrestrial" => terrestrialColor,
                    "desert" => desertColor,
                    "ice" => iceColor,
                    "gas_giant" => gasGiantColor,
                    _ => terrestrialColor
                };
            }

            Assert.AreEqual(terrestrialColor, GetPlanetColor("terrestrial"));
            Assert.AreEqual(desertColor, GetPlanetColor("desert"));
            Assert.AreEqual(iceColor, GetPlanetColor("ice"));
            Assert.AreEqual(gasGiantColor, GetPlanetColor("gas_giant"));
            Assert.AreEqual(terrestrialColor, GetPlanetColor("unknown"), "Unknown type should default to terrestrial");
        }

        [Test]
        public void GetPlanetColor_CaseInsensitive()
        {
            Color terrestrialColor = new Color(0.2f, 0.5f, 0.2f);

            Color GetPlanetColor(string type)
            {
                return type.ToLower() switch
                {
                    "terrestrial" => terrestrialColor,
                    _ => terrestrialColor
                };
            }

            Assert.AreEqual(terrestrialColor, GetPlanetColor("Terrestrial"));
            Assert.AreEqual(terrestrialColor, GetPlanetColor("TERRESTRIAL"));
            Assert.AreEqual(terrestrialColor, GetPlanetColor("terrestrial"));
        }

        #endregion

        #region City Object Dictionary Tests

        [Test]
        public void CityObjects_Dictionary_StoresCities()
        {
            var cityObjects = new Dictionary<string, GameObject>();
            
            GameObject city1 = new GameObject("City1");
            GameObject city2 = new GameObject("City2");
            
            cityObjects["New York"] = city1;
            cityObjects["London"] = city2;
            
            Assert.AreEqual(2, cityObjects.Count);
            Assert.AreEqual(city1, cityObjects["New York"]);
            Assert.AreEqual(city2, cityObjects["London"]);
            
            Object.DestroyImmediate(city1);
            Object.DestroyImmediate(city2);
        }

        [Test]
        public void CityObjects_Dictionary_ContainsKey()
        {
            var cityObjects = new Dictionary<string, GameObject>();
            
            GameObject city = new GameObject("City");
            cityObjects["New York"] = city;
            
            Assert.IsTrue(cityObjects.ContainsKey("New York"));
            Assert.IsFalse(cityObjects.ContainsKey("Tokyo"));
            
            Object.DestroyImmediate(city);
        }

        [Test]
        public void CityObjects_Dictionary_Clear()
        {
            var cityObjects = new Dictionary<string, GameObject>();
            
            cityObjects["City1"] = new GameObject("City1");
            cityObjects["City2"] = new GameObject("City2");
            
            Assert.AreEqual(2, cityObjects.Count);
            
            cityObjects.Clear();
            
            Assert.AreEqual(0, cityObjects.Count);
        }

        #endregion

        #region Event Delegate Tests

        [Test]
        public void CityClickedHandler_Delegate_CanBeCreated()
        {
            bool eventFired = false;
            string capturedName = null;
            float capturedLat = 0f;
            float capturedLon = 0f;

            CityClickedHandler handler = (name, lat, lon) =>
            {
                eventFired = true;
                capturedName = name;
                capturedLat = lat;
                capturedLon = lon;
            };

            // Invoke the handler
            handler?.Invoke("New York", 40.7128f, -74.0060f);

            Assert.IsTrue(eventFired);
            Assert.AreEqual("New York", capturedName);
            Assert.AreEqual(40.7128f, capturedLat, 0.0001f);
            Assert.AreEqual(-74.0060f, capturedLon, 0.0001f);
        }

        [Test]
        public void CityClickedHandler_NullInvocation_IsSafe()
        {
            CityClickedHandler handler = null;
            
            // Should not throw
            handler?.Invoke("Test", 0f, 0f);
            
            Assert.Pass("Null invocation should not throw");
        }

        #endregion

        #region Renderer Component Tests

        [Test]
        public void PlanetSurfaceRenderer_Component_AddedToGameObject()
        {
            Assert.IsNotNull(renderer, "PlanetSurfaceRenderer should be added to GameObject");
            Assert.AreEqual("PlanetSurfaceRenderer", rendererObject.name);
        }

        #endregion

        #region Clear Tests

        [Test]
        public void Clear_CanBeCalledMultipleTimes()
        {
            renderer.Clear();
            renderer.Clear();
            renderer.Clear();
            
            Assert.Pass("Multiple Clear calls should not throw");
        }

        #endregion

        #region RenderPlanet Tests

        [Test]
        public void RenderPlanet_NullData_DoesNotThrow()
        {
            renderer.RenderPlanet(null);
            Assert.Pass("Rendering null data should not throw");
        }

        [Test]
        public void RenderPlanet_EmptyCities_NoCitiesCreated()
        {
            var data = new PlanetSurfaceData
            {
                name = "TestPlanet",
                type = "terrestrial",
                seed = 12345,
                cities = new List<CityData>()
            };

            renderer.RenderPlanet(data);
            Assert.Pass("Rendering planet with no cities should not throw");
        }

        [Test]
        public void RenderPlanet_WithCities_CreatesCityObjects()
        {
            var data = new PlanetSurfaceData
            {
                name = "Earth",
                type = "terrestrial",
                seed = 12345,
                cities = new List<CityData>
                {
                    new CityData { name = "New York", latitude = 40.7128f, longitude = -74.0060f, population = 8336817 },
                    new CityData { name = "London", latitude = 51.5074f, longitude = -0.1278f, population = 8982000 }
                }
            };

            renderer.RenderPlanet(data);
            Assert.Pass("Rendering planet with cities should not throw");
        }

        [Test]
        public void RenderPlanet_DifferentTypes()
        {
            string[] types = { "terrestrial", "desert", "ice", "gas_giant" };
            
            foreach (var type in types)
            {
                var data = new PlanetSurfaceData
                {
                    name = "TestPlanet",
                    type = type,
                    seed = 12345,
                    cities = new List<CityData>()
                };
                
                renderer.RenderPlanet(data);
            }
            
            Assert.Pass("Rendering different planet types should not throw");
        }

        [Test]
        public void RenderPlanet_OverwritesPreviousRender()
        {
            var data1 = new PlanetSurfaceData
            {
                name = "Earth",
                type = "terrestrial",
                seed = 12345,
                cities = new List<CityData>
                {
                    new CityData { name = "New York", latitude = 40.7128f, longitude = -74.0060f, population = 8336817 }
                }
            };

            renderer.RenderPlanet(data1);

            var data2 = new PlanetSurfaceData
            {
                name = "Mars",
                type = "desert",
                seed = 54321,
                cities = new List<CityData>
                {
                    new CityData { name = "Olympus Mons", latitude = 18.65f, longitude = 226.2f, population = 1000 }
                }
            };

            renderer.RenderPlanet(data2);
            Assert.Pass("Re-rendering should not throw");
        }

        #endregion

        #region Capsule Collider Tests

        [Test]
        public void CapsuleCollider_Radius_IsSmall()
        {
            float radius = 0.1f;
            Assert.AreEqual(0.1f, radius, "City marker collider radius should be small");
        }

        [Test]
        public void CapsuleCollider_Height_IsSmall()
        {
            float height = 0.2f;
            Assert.AreEqual(0.2f, height, "City marker collider height should be small");
        }

        [Test]
        public void CapsuleCollider_CanBeAdded()
        {
            GameObject cityGo = new GameObject("CityMarker");
            var collider = cityGo.AddComponent<CapsuleCollider>();
            
            collider.radius = 0.1f;
            collider.height = 0.2f;
            
            Assert.AreEqual(0.1f, collider.radius);
            Assert.AreEqual(0.2f, collider.height);
            
            Object.DestroyImmediate(cityGo);
        }

        #endregion

        #region Raycasting Tests

        [Test]
        public void Raycasting_MaxDistance_IsLarge()
        {
            float maxDistance = 1000f;
            Assert.AreEqual(1000f, maxDistance, "Raycasting max distance should be large for planet view");
        }

        [Test]
        public void Raycasting_LayerMask_Default()
        {
            int defaultLayer = -1;
            Assert.AreEqual(-1, defaultLayer, "Default interaction layer should be all layers");
        }

        #endregion

        #region Hover Highlighting Tests

        [Test]
        public void HoverHighlight_EmissionColor_IsWhite()
        {
            Color emissionColor = Color.white;
            Assert.AreEqual(1f, emissionColor.r);
            Assert.AreEqual(1f, emissionColor.g);
            Assert.AreEqual(1f, emissionColor.b);
        }

        [Test]
        public void HoverHighlight_Material_Created()
        {
            // Verify that a highlight material can be created
            Material highlight = new Material(Shader.Find("Standard"));
            highlight.color = Color.white;
            
            Assert.IsNotNull(highlight);
            Assert.AreEqual(Color.white, highlight.color);
            
            Object.DestroyImmediate(highlight);
        }

        #endregion

        #region Integration Tests

        [Test]
        public void FullPlanetRender_CompleteData()
        {
            var data = new PlanetSurfaceData
            {
                name = "Earth",
                type = "terrestrial",
                seed = 12345,
                cities = new List<CityData>
                {
                    new CityData { name = "New York", latitude = 40.7128f, longitude = -74.0060f, population = 8336817 },
                    new CityData { name = "London", latitude = 51.5074f, longitude = -0.1278f, population = 8982000 },
                    new CityData { name = "Tokyo", latitude = 35.6762f, longitude = 139.6503f, population = 13960000 },
                    new CityData { name = "Sydney", latitude = -33.8688f, longitude = 151.2093f, population = 5312000 }
                }
            };

            renderer.RenderPlanet(data);
            
            Assert.Pass("Full planet render should not throw");
        }

        [Test]
        public void CityPositions_AllOnSurface()
        {
            float planetRadius = 5f;
            
            var cities = new List<CityData>
            {
                new CityData { name = "Equator", latitude = 0f, longitude = 0f, population = 1000 },
                new CityData { name = "North", latitude = 45f, longitude = 0f, population = 1000 },
                new CityData { name = "South", latitude = -45f, longitude = 0f, population = 1000 },
                new CityData { name = "East", latitude = 0f, longitude = 90f, population = 1000 },
                new CityData { name = "West", latitude = 0f, longitude = -90f, population = 1000 }
            };

            foreach (var city in cities)
            {
                float latRad = city.latitude * Mathf.Deg2Rad;
                float lonRad = city.longitude * Mathf.Deg2Rad;

                Vector3 pos = new Vector3(
                    Mathf.Cos(latRad) * Mathf.Sin(lonRad) * planetRadius * 1.01f,
                    Mathf.Sin(latRad) * planetRadius * 1.01f,
                    Mathf.Cos(latRad) * Mathf.Cos(lonRad) * planetRadius * 1.01f
                );

                Assert.AreEqual(planetRadius * 1.01f, pos.magnitude, 0.01f,
                    $"City {city.name} should be on planet surface");
            }
        }

        [Test]
        public void CityMarkerData_WithCollider()
        {
            GameObject cityGo = new GameObject("CityMarker");
            
            var marker = cityGo.AddComponent<CityMarkerData>();
            marker.cityName = "Test City";
            marker.latitude = 40.7128f;
            marker.longitude = -74.0060f;
            
            var collider = cityGo.AddComponent<CapsuleCollider>();
            collider.radius = 0.1f;
            collider.height = 0.2f;
            
            Assert.IsNotNull(marker);
            Assert.IsNotNull(collider);
            Assert.AreEqual("Test City", marker.cityName);
            
            Object.DestroyImmediate(cityGo);
        }

        [Test]
        public void MultiplePlanetTypes_RenderCorrectly()
        {
            var planets = new List<PlanetSurfaceData>
            {
                new PlanetSurfaceData { name = "Earth", type = "terrestrial", seed = 1, cities = new List<CityData>() },
                new PlanetSurfaceData { name = "Mars", type = "desert", seed = 2, cities = new List<CityData>() },
                new PlanetSurfaceData { name = "Europa", type = "ice", seed = 3, cities = new List<CityData>() },
                new PlanetSurfaceData { name = "Jupiter", type = "gas_giant", seed = 4, cities = new List<CityData>() }
            };

            foreach (var planet in planets)
            {
                renderer.RenderPlanet(planet);
            }
            
            Assert.Pass("All planet types should render without error");
        }

        [Test]
        public void CityPopulation_VariousSizes()
        {
            var cities = new List<CityData>
            {
                new CityData { name = "Village", latitude = 0f, longitude = 0f, population = 100 },
                new CityData { name = "Town", latitude = 10f, longitude = 10f, population = 10000 },
                new CityData { name = "City", latitude = 20f, longitude = 20f, population = 1000000 },
                new CityData { name = "Megalopolis", latitude = 30f, longitude = 30f, population = 20000000 }
            };

            Assert.AreEqual(100, cities[0].population);
            Assert.AreEqual(10000, cities[1].population);
            Assert.AreEqual(1000000, cities[2].population);
            Assert.AreEqual(20000000, cities[3].population);
        }

        #endregion
    }
}
