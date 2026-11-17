using UnityEngine;
using DoraMCU.Client.Core;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Safe wrapper around UnityEngine.Input that suppresses InvalidOperationException
    /// thrown by Unity 6000.6 when Input System package is forced active.
    /// </summary>
    public static class InputSafe
    {
        public static Vector3 mousePosition
        {
            get
            {
                try { return UnityEngine.Input.mousePosition; }
                catch { return Vector3.zero; }
            }
        }

        public static Vector2 mouseScrollDelta
        {
            get
            {
                try { return UnityEngine.Input.mouseScrollDelta; }
                catch { return Vector2.zero; }
            }
        }

        public static bool GetKey(string name)
        {
            try { return UnityEngine.Input.GetKey(name); }
            catch { return false; }
        }

        public static bool GetKey(KeyCode key)
        {
            try { return UnityEngine.Input.GetKey(key); }
            catch { return false; }
        }

        public static bool GetKeyDown(string name)
        {
            try { return UnityEngine.Input.GetKeyDown(name); }
            catch { return false; }
        }

        public static bool GetKeyDown(KeyCode key)
        {
            try { return UnityEngine.Input.GetKeyDown(key); }
            catch { return false; }
        }

        public static bool GetKeyUp(KeyCode key)
        {
            try { return UnityEngine.Input.GetKeyUp(key); }
            catch { return false; }
        }

        public static bool GetMouseButton(int button)
        {
            try { return UnityEngine.Input.GetMouseButton(button); }
            catch { return false; }
        }

        public static bool GetMouseButtonDown(int button)
        {
            try { return UnityEngine.Input.GetMouseButtonDown(button); }
            catch { return false; }
        }

        public static bool GetMouseButtonUp(int button)
        {
            try { return UnityEngine.Input.GetMouseButtonUp(button); }
            catch { return false; }
        }

        public static float GetAxis(string name)
        {
            try { return UnityEngine.Input.GetAxis(name); }
            catch { return 0f; }
        }

        public static float GetAxisRaw(string name)
        {
            try { return UnityEngine.Input.GetAxisRaw(name); }
            catch { return 0f; }
        }

        public static bool GetButton(string name)
        {
            try { return UnityEngine.Input.GetButton(name); }
            catch { return false; }
        }

        public static bool GetButtonDown(string name)
        {
            try { return UnityEngine.Input.GetButtonDown(name); }
            catch { return false; }
        }

        public static bool GetButtonUp(string name)
        {
            try { return UnityEngine.Input.GetButtonUp(name); }
            catch { return false; }
        }
    }
}
