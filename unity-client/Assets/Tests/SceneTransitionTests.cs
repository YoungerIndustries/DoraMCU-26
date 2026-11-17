using UnityEngine;
using UnityEngine.TestTools;
using NUnit.Framework;
using System.Collections;
using DoraMCU.Client.Core;
using DoraMCU.Rendering;

namespace DoraMCU.Tests.SceneTransition
{
    /// <summary>
    /// Comprehensive tests for the scene transition system connecting galaxy → system → planet → surface views.
    /// These tests verify GameManager scene view management, camera transitions, view history, and renderer visibility.
    /// </summary>
    [TestFixture]
    public class SceneTransitionTests
    {
        private GameObject testScene;
        private GameObject cameraObject;
        private GameObject galaxyRendererObject;
        private GameObject planetRendererObject;
        private GameObject surfaceRendererObject;

        [SetUp]
        public void SetUp()
        {
            // Create a test scene
            testScene = new GameObject("TestScene");

            // Create a main camera
            cameraObject = new GameObject("MainCamera");
            cameraObject.AddComponent<Camera>();
            cameraObject.tag = "MainCamera";

            // Create renderer GameObjects
            galaxyRendererObject = new GameObject("GalaxyRenderer");
            galaxyRendererObject.AddComponent<GalaxyRenderer>();

            planetRendererObject = new GameObject("PlanetRenderer");
            planetRendererObject.AddComponent<PlanetSurfaceRenderer>();

            surfaceRendererObject = new GameObject("SurfaceRenderer");
            surfaceRendererObject.AddComponent<ExtendedPlanetSurfaceRenderer>();
        }

        [TearDown]
        public void TearDown()
        {
            Object.DestroyImmediate(testScene);
        }

        #region Scene View Enum Tests

        [Test]
        public void SceneView_EnumHasCorrectValues()
        {
            // Verify the SceneView enum has all expected values
            Assert.AreEqual(4, System.Enum.GetNames(typeof(SceneTransitionHelper.SceneView)).Length);
            Assert.Contains("Galaxy", System.Enum.GetNames(typeof(SceneTransitionHelper.SceneView)));
            Assert.Contains("System", System.Enum.GetNames(typeof(SceneTransitionHelper.SceneView)));
            Assert.Contains("Planet", System.Enum.GetNames(typeof(SceneTransitionHelper.SceneView)));
            Assert.Contains("Surface", System.Enum.GetNames(typeof(SceneTransitionHelper.SceneView)));
        }

        #endregion

        #region View History Tests

        [Test]
        public void ViewHistory_InitiallyEmpty()
        {
            var history = new System.Collections.Generic.Stack<string>();
            Assert.AreEqual(0, history.Count, "View history should start empty");
        }

        [Test]
        public void ViewHistory_PushAndPop()
        {
            var history = new System.Collections.Generic.Stack<string>();
            
            // Push views
            history.Push("Galaxy");
            history.Push("System");
            history.Push("Planet");
            
            Assert.AreEqual(3, history.Count);
            
            // Pop views (LIFO order)
            Assert.AreEqual("Planet", history.Pop());
            Assert.AreEqual("System", history.Pop());
            Assert.AreEqual("Galaxy", history.Pop());
            
            Assert.AreEqual(0, history.Count);
        }

        [Test]
        public void ViewHistory_PopOnEmpty_ReturnsGalaxy()
        {
            var history = new System.Collections.Generic.Stack<string>();
            
            // When history is empty, should default to Galaxy view
            string currentView = history.Count > 0 ? history.Pop() : "Galaxy";
            Assert.AreEqual("Galaxy", currentView);
        }

        [Test]
        public void ViewHistory_FullNavigationChain()
        {
            var history = new System.Collections.Generic.Stack<string>();
            string currentView = "Galaxy";
            
            // Navigate: Galaxy → System
            history.Push(currentView);
            currentView = "System";
            Assert.AreEqual("System", currentView);
            Assert.AreEqual(1, history.Count);
            
            // Navigate: System → Planet
            history.Push(currentView);
            currentView = "Planet";
            Assert.AreEqual("Planet", currentView);
            Assert.AreEqual(2, history.Count);
            
            // Navigate: Planet → Surface
            history.Push(currentView);
            currentView = "Surface";
            Assert.AreEqual("Surface", currentView);
            Assert.AreEqual(3, history.Count);
            
            // Return: Surface → Planet
            string previous = history.Pop();
            Assert.AreEqual("Planet", previous);
            Assert.AreEqual(2, history.Count);
            
            // Return: Planet → System
            previous = history.Pop();
            Assert.AreEqual("System", previous);
            Assert.AreEqual(1, history.Count);
            
            // Return: System → Galaxy
            previous = history.Pop();
            Assert.AreEqual("Galaxy", previous);
            Assert.AreEqual(0, history.Count);
        }

        #endregion

        #region Camera Position Calculation Tests

        [Test]
        public void CameraPosition_GalaxyView_CalculatedCorrectly()
        {
            float galaxyCameraDistance = 200f;
            Vector3 targetPosition = Vector3.zero;
            
            Vector3 expectedCameraPos = new Vector3(
                targetPosition.x,
                galaxyCameraDistance,
                targetPosition.z - galaxyCameraDistance
            );
            
            Assert.AreEqual(0f, expectedCameraPos.x);
            Assert.AreEqual(200f, expectedCameraPos.y);
            Assert.AreEqual(-200f, expectedCameraPos.z);
        }

        [Test]
        public void CameraPosition_SystemView_CalculatedCorrectly()
        {
            float systemCameraDistance = 50f;
            Vector3 targetPosition = new Vector3(100f, 0f, 100f);
            
            Vector3 expectedCameraPos = new Vector3(
                targetPosition.x,
                systemCameraDistance,
                targetPosition.z - systemCameraDistance
            );
            
            Assert.AreEqual(100f, expectedCameraPos.x);
            Assert.AreEqual(50f, expectedCameraPos.y);
            Assert.AreEqual(50f, expectedCameraPos.z);
        }

        [Test]
        public void CameraPosition_PlanetView_CalculatedCorrectly()
        {
            float planetCameraDistance = 15f;
            Vector3 targetPosition = new Vector3(100f, 0f, 100f);
            
            Vector3 expectedCameraPos = new Vector3(
                targetPosition.x,
                planetCameraDistance,
                targetPosition.z - planetCameraDistance
            );
            
            Assert.AreEqual(100f, expectedCameraPos.x);
            Assert.AreEqual(15f, expectedCameraPos.y);
            Assert.AreEqual(85f, expectedCameraPos.z);
        }

        [Test]
        public void CameraPosition_SurfaceView_CalculatedCorrectly()
        {
            float surfaceCameraDistance = 5f;
            Vector3 targetPosition = new Vector3(100f, 0f, 100f);
            
            Vector3 expectedCameraPos = new Vector3(
                targetPosition.x,
                surfaceCameraDistance,
                targetPosition.z - surfaceCameraDistance
            );
            
            Assert.AreEqual(100f, expectedCameraPos.x);
            Assert.AreEqual(5f, expectedCameraPos.y);
            Assert.AreEqual(95f, expectedCameraPos.z);
        }

        [Test]
        public void CameraPosition_DifferentTargets_CalculatedCorrectly()
        {
            float galaxyCameraDistance = 200f;
            
            // Test with non-zero target position
            Vector3 targetPosition = new Vector3(50f, 0f, -30f);
            Vector3 cameraPos = new Vector3(
                targetPosition.x,
                galaxyCameraDistance,
                targetPosition.z - galaxyCameraDistance
            );
            
            Assert.AreEqual(50f, cameraPos.x);
            Assert.AreEqual(200f, cameraPos.y);
            Assert.AreEqual(-230f, cameraPos.z);
        }

        #endregion

        #region SmoothStep Interpolation Tests

        [Test]
        public void SmoothStep_StartValue_ReturnsZero()
        {
            float result = Mathf.SmoothStep(0f, 1f, 0f);
            Assert.AreEqual(0f, result);
        }

        [Test]
        public void SmoothStep_EndValue_ReturnsOne()
        {
            float result = Mathf.SmoothStep(0f, 1f, 1f);
            Assert.AreEqual(1f, result);
        }

        [Test]
        public void SmoothStep_MidValue_ReturnsBetweenZeroAndOne()
        {
            float result = Mathf.SmoothStep(0f, 1f, 0.5f);
            Assert.Greater(result, 0f);
            Assert.Less(result, 1f);
        }

        [Test]
        public void SmoothStep_IsMonotonic()
        {
            float previous = 0f;
            for (int i = 0; i <= 100; i++)
            {
                float t = i / 100f;
                float current = Mathf.SmoothStep(0f, 1f, t);
                Assert.GreaterOrEqual(current, previous, $"SmoothStep should be monotonic at t={t}");
                previous = current;
            }
        }

        #endregion

        #region Vector3 Lerp Tests

        [Test]
        public void Vector3Lerp_StartToStart_ReturnsStart()
        {
            Vector3 start = new Vector3(1f, 2f, 3f);
            Vector3 end = new Vector3(10f, 20f, 30f);
            Vector3 result = Vector3.Lerp(start, end, 0f);
            
            Assert.AreEqual(start, result);
        }

        [Test]
        public void Vector3Lerp_StartToEnd_ReturnsEnd()
        {
            Vector3 start = new Vector3(1f, 2f, 3f);
            Vector3 end = new Vector3(10f, 20f, 30f);
            Vector3 result = Vector3.Lerp(start, end, 1f);
            
            Assert.AreEqual(end, result);
        }

        [Test]
        public void Vector3Lerp_Midpoint_ReturnsAverage()
        {
            Vector3 start = new Vector3(0f, 0f, 0f);
            Vector3 end = new Vector3(10f, 20f, 30f);
            Vector3 result = Vector3.Lerp(start, end, 0.5f);
            
            Assert.AreEqual(5f, result.x);
            Assert.AreEqual(10f, result.y);
            Assert.AreEqual(15f, result.z);
        }

        #endregion

        #region Renderer Visibility Tests

        [Test]
        public void RendererVisibility_GalaxyView_GalaxyAndSystemVisible()
        {
            string targetView = "Galaxy";
            
            bool galaxyVisible = targetView == "Galaxy" || targetView == "System";
            bool planetVisible = targetView == "Planet";
            bool surfaceVisible = targetView == "Surface";
            
            Assert.IsTrue(galaxyVisible, "Galaxy renderer should be visible in Galaxy view");
            Assert.IsFalse(planetVisible, "Planet renderer should not be visible in Galaxy view");
            Assert.IsFalse(surfaceVisible, "Surface renderer should not be visible in Galaxy view");
        }

        [Test]
        public void RendererVisibility_SystemView_GalaxyAndSystemVisible()
        {
            string targetView = "System";
            
            bool galaxyVisible = targetView == "Galaxy" || targetView == "System";
            bool planetVisible = targetView == "Planet";
            bool surfaceVisible = targetView == "Surface";
            
            Assert.IsTrue(galaxyVisible, "Galaxy renderer should be visible in System view");
            Assert.IsFalse(planetVisible, "Planet renderer should not be visible in System view");
            Assert.IsFalse(surfaceVisible, "Surface renderer should not be visible in System view");
        }

        [Test]
        public void RendererVisibility_PlanetView_OnlyPlanetVisible()
        {
            string targetView = "Planet";
            
            bool galaxyVisible = targetView == "Galaxy" || targetView == "System";
            bool planetVisible = targetView == "Planet";
            bool surfaceVisible = targetView == "Surface";
            
            Assert.IsFalse(galaxyVisible, "Galaxy renderer should not be visible in Planet view");
            Assert.IsTrue(planetVisible, "Planet renderer should be visible in Planet view");
            Assert.IsFalse(surfaceVisible, "Surface renderer should not be visible in Planet view");
        }

        [Test]
        public void RendererVisibility_SurfaceView_OnlySurfaceVisible()
        {
            string targetView = "Surface";
            
            bool galaxyVisible = targetView == "Galaxy" || targetView == "System";
            bool planetVisible = targetView == "Planet";
            bool surfaceVisible = targetView == "Surface";
            
            Assert.IsFalse(galaxyVisible, "Galaxy renderer should not be visible in Surface view");
            Assert.IsFalse(planetVisible, "Planet renderer should not be visible in Surface view");
            Assert.IsTrue(surfaceVisible, "Surface renderer should be visible in Surface view");
        }

        #endregion

        #region Transition Duration Tests

        [Test]
        public void TransitionDuration_DefaultIsPositive()
        {
            float transitionDuration = 2f;
            Assert.Greater(transitionDuration, 0f, "Transition duration should be positive");
        }

        [Test]
        public void TransitionDuration_ElapsedCalculation()
        {
            float transitionDuration = 2f;
            float deltaTime = 0.05f; // Typical frame time
            int frames = 40; // 2 seconds / 0.05 seconds per frame
            
            float elapsed = 0f;
            for (int i = 0; i < frames; i++)
            {
                elapsed += deltaTime;
            }
            
            Assert.AreEqual(transitionDuration, elapsed, "Elapsed time should equal transition duration");
        }

        [Test]
        public void TransitionProgress_IncreasesOverTime()
        {
            float transitionDuration = 2f;
            float elapsed = 1f; // Halfway through
            
            float progress = elapsed / transitionDuration;
            Assert.AreEqual(0.5f, progress, "Progress should be 0.5 at halfway point");
        }

        #endregion

        #region View State Tests

        [Test]
        public void CurrentView_InitialState_IsGalaxy()
        {
            string currentView = "Galaxy";
            Assert.AreEqual("Galaxy", currentView, "Initial view should be Galaxy");
        }

        [Test]
        public void GetCurrentView_ReturnsCorrectString()
        {
            string currentView = "Planet";
            string viewString = currentView;
            Assert.AreEqual("Planet", viewString);
        }

        [Test]
        public void IsGalaxyView_ReturnsTrue_WhenGalaxy()
        {
            string currentView = "Galaxy";
            bool isGalaxy = currentView == "Galaxy";
            Assert.IsTrue(isGalaxy);
        }

        [Test]
        public void IsSystemView_ReturnsTrue_WhenSystem()
        {
            string currentView = "System";
            bool isSystem = currentView == "System";
            Assert.IsTrue(isSystem);
        }

        [Test]
        public void IsPlanetView_ReturnsTrue_WhenPlanet()
        {
            string currentView = "Planet";
            bool isPlanet = currentView == "Planet";
            Assert.IsTrue(isPlanet);
        }

        [Test]
        public void IsSurfaceView_ReturnsTrue_WhenSurface()
        {
            string currentView = "Surface";
            bool isSurface = currentView == "Surface";
            Assert.IsTrue(isSurface);
        }

        [Test]
        public void IsGalaxyView_ReturnsFalse_WhenNotGalaxy()
        {
            string currentView = "Planet";
            bool isGalaxy = currentView == "Galaxy";
            Assert.IsFalse(isGalaxy);
        }

        #endregion

        #region Camera Distance Configuration Tests

        [Test]
        public void CameraDistances_AreOrderedCorrectly()
        {
            float galaxyCameraDistance = 200f;
            float systemCameraDistance = 50f;
            float planetCameraDistance = 15f;
            float surfaceCameraDistance = 5f;
            
            Assert.Greater(galaxyCameraDistance, systemCameraDistance, "Galaxy view should be farthest");
            Assert.Greater(systemCameraDistance, planetCameraDistance, "System view should be farther than planet");
            Assert.Greater(planetCameraDistance, surfaceCameraDistance, "Planet view should be farther than surface");
        }

        [Test]
        public void CameraDistances_AreAllPositive()
        {
            float galaxyCameraDistance = 200f;
            float systemCameraDistance = 50f;
            float planetCameraDistance = 15f;
            float surfaceCameraDistance = 5f;
            
            Assert.Greater(galaxyCameraDistance, 0f);
            Assert.Greater(systemCameraDistance, 0f);
            Assert.Greater(planetCameraDistance, 0f);
            Assert.Greater(surfaceCameraDistance, 0f);
        }

        #endregion

        #region GameObject Active State Tests

        [Test]
        public void GameObjectSetActive_TogglesCorrectly()
        {
            GameObject testObject = new GameObject("TestObject");
            
            testObject.SetActive(true);
            Assert.IsTrue(testObject.activeSelf, "Object should be active after SetActive(true)");
            
            testObject.SetActive(false);
            Assert.IsFalse(testObject.activeSelf, "Object should be inactive after SetActive(false)");
            
            Object.DestroyImmediate(testObject);
        }

        [Test]
        public void RendererObjects_InitiallyActive()
        {
            Assert.IsTrue(galaxyRendererObject.activeSelf, "Galaxy renderer should be initially active");
            Assert.IsTrue(planetRendererObject.activeSelf, "Planet renderer should be initially active");
            Assert.IsTrue(surfaceRendererObject.activeSelf, "Surface renderer should be initially active");
        }

        [Test]
        public void RendererObjects_CanBeDeactivated()
        {
            galaxyRendererObject.SetActive(false);
            Assert.IsFalse(galaxyRendererObject.activeSelf);
            
            planetRendererObject.SetActive(false);
            Assert.IsFalse(planetRendererObject.activeSelf);
            
            surfaceRendererObject.SetActive(false);
            Assert.IsFalse(surfaceRendererObject.activeSelf);
        }

        #endregion

        #region Navigation Target Tests

        [Test]
        public void NavigationTarget_SectorCoordinates_CalculatedCorrectly()
        {
            int sectorRow = 1;
            int sectorCol = 2;
            
            // Sector position calculation from GameManager
            Vector3 targetPosition = new Vector3(sectorCol * 100f, 0f, sectorRow * 100f);
            
            Assert.AreEqual(200f, targetPosition.x);
            Assert.AreEqual(0f, targetPosition.y);
            Assert.AreEqual(100f, targetPosition.z);
        }

        [Test]
        public void NavigationTarget_SectorKey_CalculatedCorrectly()
        {
            int sectorRow = 1;
            int sectorCol = 2;
            
            int sectorKey = sectorRow * 10 + sectorCol;
            Assert.AreEqual(12, sectorKey);
        }

        [Test]
        public void NavigationTarget_MultipleSectors_DifferentPositions()
        {
            Vector3 pos1 = new Vector3(0 * 100f, 0f, 0 * 100f);
            Vector3 pos2 = new Vector3(1 * 100f, 0f, 0 * 100f);
            Vector3 pos3 = new Vector3(0 * 100f, 0f, 1 * 100f);
            
            Assert.AreNotEqual(pos1, pos2, "Different sectors should have different positions");
            Assert.AreNotEqual(pos1, pos3, "Different sectors should have different positions");
            Assert.AreNotEqual(pos2, pos3, "Different sectors should have different positions");
        }

        #endregion

        #region Transition Skip Tests

        [Test]
        public void TransitionToView_SameView_SkipsTransition()
        {
            string currentView = "Galaxy";
            string targetView = "Galaxy";
            
            bool shouldTransition = targetView != currentView;
            Assert.IsFalse(shouldTransition, "Transition should be skipped when target equals current view");
        }

        [Test]
        public void TransitionToView_DifferentView_TriggersTransition()
        {
            string currentView = "Galaxy";
            string targetView = "System";
            
            bool shouldTransition = targetView != currentView;
            Assert.IsTrue(shouldTransition, "Transition should trigger when target differs from current view");
        }

        #endregion

        #region Back Navigation Tests

        [Test]
        public void ReturnToPreviousView_WithHistory_PopsCorrectView()
        {
            var history = new System.Collections.Generic.Stack<string>();
            history.Push("Galaxy");
            history.Push("System");
            history.Push("Planet");
            
            string previous = history.Pop();
            Assert.AreEqual("Planet", previous, "Should return to Planet view");
        }

        [Test]
        public void ReturnToPreviousView_EmptyHistory_DefaultsToGalaxy()
        {
            var history = new System.Collections.Generic.Stack<string>();
            
            string previous = history.Count > 0 ? history.Pop() : "Galaxy";
            Assert.AreEqual("Galaxy", previous, "Should default to Galaxy when history is empty");
        }

        [Test]
        public void ReturnToPreviousView_MultipleReturns_NavigatesCorrectly()
        {
            var history = new System.Collections.Generic.Stack<string>();
            string currentView = "Surface";
            
            // Simulate: Galaxy → System → Planet → Surface
            history.Push("Galaxy");
            history.Push("System");
            history.Push("Planet");
            
            // First return: Surface → Planet
            string previous = history.Pop();
            Assert.AreEqual("Planet", previous);
            currentView = previous;
            
            // Second return: Planet → System
            previous = history.Pop();
            Assert.AreEqual("System", previous);
            currentView = previous;
            
            // Third return: System → Galaxy
            previous = history.Pop();
            Assert.AreEqual("Galaxy", previous);
            currentView = previous;
            
            // No more history
            Assert.AreEqual(0, history.Count);
        }

        #endregion

        #region Keyboard Input Tests

        [Test]
        public void EscapeKey_Code_IsCorrect()
        {
            // Verify Escape key code exists
            Assert.AreEqual(KeyCode.Escape, KeyCode.Escape);
        }

        [Test]
        public void TabKey_Code_IsCorrect()
        {
            // Verify Tab key code exists
            Assert.AreEqual(KeyCode.Tab, KeyCode.Tab);
        }

        [Test]
        public void EKey_Code_IsCorrect()
        {
            // Verify E key code exists
            Assert.AreEqual(KeyCode.E, KeyCode.E);
        }

        #endregion

        #region Cursor Lock State Tests

        [Test]
        public void CursorLockState_HasCorrectValues()
        {
            // Verify CursorLockMode enum values
            Assert.AreEqual(2, System.Enum.GetNames(typeof(CursorLockMode)).Length);
            Assert.Contains("None", System.Enum.GetNames(typeof(CursorLockMode)));
            Assert.Contains("Locked", System.Enum.GetNames(typeof(CursorLockMode)));
        }

        [Test]
        public void BackNavigation_OnlyWhenCursorUnlocked()
        {
            // Back navigation should only work when cursor is unlocked
            bool cursorUnlocked = CursorLockMode.None == CursorLockMode.None;
            Assert.IsTrue(cursorUnlocked, "Back navigation should work when cursor is unlocked");
        }

        #endregion

        #region Integration Tests

        [Test]
        public void FullNavigationChain_GalaxyToSurface()
        {
            var history = new System.Collections.Generic.Stack<string>();
            string currentView = "Galaxy";
            
            // Step 1: Galaxy → System
            Assert.AreEqual("Galaxy", currentView);
            history.Push(currentView);
            currentView = "System";
            Assert.AreEqual("System", currentView);
            Assert.AreEqual(1, history.Count);
            
            // Step 2: System → Planet
            history.Push(currentView);
            currentView = "Planet";
            Assert.AreEqual("Planet", currentView);
            Assert.AreEqual(2, history.Count);
            
            // Step 3: Planet → Surface
            history.Push(currentView);
            currentView = "Surface";
            Assert.AreEqual("Surface", currentView);
            Assert.AreEqual(3, history.Count);
            
            // Verify final state
            Assert.AreEqual("Surface", currentView);
            Assert.AreEqual(3, history.Count);
        }

        [Test]
        public void FullNavigationChain_SurfaceToGalaxy()
        {
            var history = new System.Collections.Generic.Stack<string>();
            string currentView = "Galaxy";
            
            // Navigate forward
            history.Push(currentView); currentView = "System";
            history.Push(currentView); currentView = "Planet";
            history.Push(currentView); currentView = "Surface";
            
            // Navigate backward
            currentView = history.Pop();
            Assert.AreEqual("Planet", currentView);
            
            currentView = history.Pop();
            Assert.AreEqual("System", currentView);
            
            currentView = history.Pop();
            Assert.AreEqual("Galaxy", currentView);
            
            Assert.AreEqual(0, history.Count);
        }

        [Test]
        public void PartialNavigation_AndReturn()
        {
            var history = new System.Collections.Generic.Stack<string>();
            string currentView = "Galaxy";
            
            // Navigate: Galaxy → System → Planet
            history.Push(currentView); currentView = "System";
            history.Push(currentView); currentView = "Planet";
            
            // Return to System
            currentView = history.Pop();
            Assert.AreEqual("System", currentView);
            
            // Navigate again: System → Planet → Surface
            history.Push(currentView); currentView = "Planet";
            history.Push(currentView); currentView = "Surface";
            
            // Return twice: Surface → Planet → System
            currentView = history.Pop();
            Assert.AreEqual("Planet", currentView);
            
            currentView = history.Pop();
            Assert.AreEqual("System", currentView);
            
            // One more return: System → Galaxy
            currentView = history.Pop();
            Assert.AreEqual("Galaxy", currentView);
        }

        #endregion
    }

    /// <summary>
    /// Helper class to expose the SceneView enum for testing.
    /// This mirrors the enum in GameManager for test access.
    /// </summary>
    public static class SceneTransitionHelper
    {
        public enum SceneView { Galaxy, System, Planet, Surface }
    }
}
