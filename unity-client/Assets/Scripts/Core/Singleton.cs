using UnityEngine;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Singleton base class for managers that should have only one instance.
    /// </summary>
    public class Singleton<T> : MonoBehaviour where T : MonoBehaviour
    {
        private static T _instance;
        public static T Instance => _instance;

        protected virtual void Awake()
        {
            if (_instance != null && _instance != this)
            {
                Destroy(gameObject);
                return;
            }

            _instance = GetComponent<T>();
        }

        public static bool HasInstance => _instance != null;
    }
}
