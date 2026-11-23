using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Client.Network
{
    /// <summary>
    /// Handles galaxy API calls: galaxy 3D data, AI admin polling.
    /// Extracted from NetworkManager.cs for separation of concerns.
    /// </summary>
    public class GalaxyService
    {
        public event Action<List<AiAdminData>> OnAiAdminsFetched;

        private readonly string serverUrl;
        private readonly Func<string> getJwtToken;
        private readonly Func<IEnumerator, Coroutine> coroutineRunner;
        private readonly Action<float> startPollingCoroutine;

        private string currentGalaxyName;
        private Coroutine pollCoroutine;

        public GalaxyService(string serverUrl, Func<string> getJwtToken,
            Func<IEnumerator, Coroutine> coroutineRunner, Action<float> pollIntervalSetter)
        {
            this.serverUrl = serverUrl;
            this.getJwtToken = getJwtToken;
            this.coroutineRunner = coroutineRunner;
            this.startPollingCoroutine = pollIntervalSetter;
        }

        public void SetCurrentGalaxy(string galaxyName)
        {
            currentGalaxyName = galaxyName;
        }

        public void StartAiAdminPolling(float interval)
        {
            if (pollCoroutine != null || string.IsNullOrEmpty(currentGalaxyName)) return;
            pollCoroutine = coroutineRunner(PollAiAdminsCoroutine(interval));
        }

        public void StopAiAdminPolling()
        {
            if (pollCoroutine != null)
            {
                // Can't stop coroutine from outside MonoBehaviour - tracked by isConnected flag
                pollCoroutine = null;
            }
        }

        private IEnumerator PollAiAdminsCoroutine(float interval)
        {
            while (!string.IsNullOrEmpty(currentGalaxyName))
            {
                FetchAiAdminsForGalaxy(currentGalaxyName, (admins) =>
                {
                    OnAiAdminsFetched?.Invoke(admins);
                });
                yield return new WaitForSeconds(interval);
            }
        }

        public void FetchAiAdminsForGalaxy(string galaxyName, Action<List<AiAdminData>> callback)
        {
            coroutineRunner(FetchAiAdminsCoroutine(galaxyName, callback));
        }

        private IEnumerator FetchAiAdminsCoroutine(string galaxyName, Action<List<AiAdminData>> callback)
        {
            string url = $"{serverUrl}/api/v1/galaxy-3d/galaxy/{Uri.EscapeDataString(galaxyName)}/ai-admins";
            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var response = JsonUtility.FromJson<AiAdminResponse>(request.downloadHandler.text);
                    if (response != null && response.admins != null)
                    {
                        callback?.Invoke(new List<AiAdminData>(response.admins));
                    }
                    else
                    {
                        callback?.Invoke(new List<AiAdminData>());
                    }
                }
                else
                {
                    Debug.LogError($"[GalaxyService] Fetch AI admins failed: {request.error}");
                    callback?.Invoke(new List<AiAdminData>());
                }
            }
        }

        [Serializable]
        public class AiAdminResponse
        {
            public AiAdminData[] admins;
        }
    }
}
