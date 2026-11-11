using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.AI
{
    /// <summary>
    /// Connects the Unity client to a Neuron node for AI-assisted gameplay.
    /// <para>
    /// Handles client registration, AI assistance requests, experience upload,
    /// and model version synchronization with the Neuron network.
    /// </para>
    /// </summary>
    public class NeuronClientService : MonoBehaviour
    {
        [Header("Neuron Configuration")]
        [Tooltip("Base URL of the Neuron node")]
        [SerializeField] private string neuronBaseUrl = "http://localhost:8090";

        [Tooltip("How often to send heartbeat (seconds)")]
        [SerializeField] private float heartbeatInterval = 30f;

        [Tooltip("Auto-request AI assistance on game events")]
        [SerializeField] private bool autoAiAssistance = true;

        [Header("Co-Server Promotion")]
        [Tooltip("Enable co-server promotion if main server fails")]
        [SerializeField] private bool coServerPromotionEnabled = true;

        [Tooltip("Heartbeat failures before promotion")]
        [SerializeField] private int maxHeartbeatFailures = 3;

        [Tooltip("Server base URL for health monitoring")]
        [SerializeField] private string serverBaseUrl = "http://localhost:8080";

        // State
        private string clientId;
        private string playerId;
        private bool isConnected;
        private float heartbeatTimer;
        private long serverModelVersion;
        private int predictionCount;
        private int experienceCollected;

        // Co-server promotion state
        private bool isCoServer;
        private int heartbeatFailureCount;
        private bool serverHealthy;
        private float serverHealthCheckTimer;

        // Events
        public event Action<AiAssistResponseData> OnAiAssistResponse;
        public event Action<int> OnExperienceUploaded;
        public event Action<bool> OnConnectionChanged;
        public event Action<bool> OnCoServerPromoted;
        public event Action<bool> OnServerHealthChanged;

        public string ClientId => clientId;
        public bool IsConnected => isConnected;
        public int PredictionCount => predictionCount;
        public int ExperienceCollected => experienceCollected;
        public bool IsCoServer => isCoServer;
        public bool ServerHealthy => serverHealthy;

        private void Awake()
        {
            DontDestroyOnLoad(gameObject);
            clientId = GenerateClientId();
        }

        private void Start()
        {
            StartCoroutine(RegisterWithNeuron());
            serverHealthy = true;
        }

        private void Update()
        {
            if (!isConnected) return;

            heartbeatTimer += Time.deltaTime;
            if (heartbeatTimer >= heartbeatInterval)
            {
                heartbeatTimer = 0;
                StartCoroutine(SendHeartbeat());
            }

            // Server health monitoring for co-server promotion
            if (coServerPromotionEnabled)
            {
                serverHealthCheckTimer += Time.deltaTime;
                if (serverHealthCheckTimer >= 10f)
                {
                    serverHealthCheckTimer = 0;
                    StartCoroutine(CheckServerHealth());
                }
            }
        }

        /// <summary>
        /// Registers this Unity client with the Neuron node.
        /// </summary>
        private IEnumerator RegisterWithNeuron()
        {
            string url = $"{neuronBaseUrl}/api/v1/unity/register";

            var body = new
            {
                clientId = clientId,
                clientType = "unity",
                playerId = playerId ?? "anonymous"
            };

            string json = JsonUtility.ToJson(body);

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogWarning($"[NeuronClientService] Registration failed: {request.error}");
                    isConnected = false;
                    OnConnectionChanged?.Invoke(false);
                }
                else
                {
                    Debug.Log($"[NeuronClientService] Registered with Neuron: {clientId}");
                    isConnected = true;
                    OnConnectionChanged?.Invoke(true);

                    // Check model version
                    StartCoroutine(CheckModelVersion());
                }
            }
        }

        /// <summary>
        /// Sets the player ID after the player logs in.
        /// </summary>
        public void SetPlayerId(string id)
        {
            playerId = id;
            if (isConnected)
            {
                StartCoroutine(RegisterWithNeuron());
            }
        }

        /// <summary>
        /// Requests AI assistance from the Neuron for a game decision.
        /// </summary>
        public IEnumerator RequestAiAssist(float[] features, string context = null, bool useConsensus = false)
        {
            if (!isConnected)
            {
                Debug.LogWarning("[NeuronClientService] Not connected to Neuron");
                yield break;
            }

            string url = $"{neuronBaseUrl}/api/v1/unity/ai/assist";

            var body = new AiAssistRequestBody
            {
                clientId = clientId,
                features = features,
                useConsensus = useConsensus,
                context = context
            };

            string json = JsonUtility.ToJson(body);

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogWarning($"[NeuronClientService] AI assist failed: {request.error}");
                }
                else
                {
                    var response = ParseAiAssistResponse(request.downloadHandler.text);
                    predictionCount++;
                    OnAiAssistResponse?.Invoke(response);
                    Debug.Log($"[NeuronClientService] AI assist: {response.predictedActionName} " +
                             $"(confidence: {response.confidence:F2}, latency: {response.latencyMs}ms)");
                }
            }
        }

        /// <summary>
        /// Quick local prediction without consensus.
        /// </summary>
        public IEnumerator QuickPredict(float[] features)
        {
            if (!isConnected)
            {
                Debug.LogWarning("[NeuronClientService] Not connected to Neuron");
                yield break;
            }

            string url = $"{neuronBaseUrl}/api/v1/unity/ai/predict";

            var body = new AiAssistRequestBody
            {
                clientId = clientId,
                features = features,
                useConsensus = false
            };

            string json = JsonUtility.ToJson(body);

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogWarning($"[NeuronClientService] Quick predict failed: {request.error}");
                }
                else
                {
                    var response = ParseAiAssistResponse(request.downloadHandler.text);
                    predictionCount++;
                    OnAiAssistResponse?.Invoke(response);
                }
            }
        }

        /// <summary>
        /// Uploads experience samples to the Neuron for aggregation.
        /// </summary>
        public IEnumerator UploadExperience(List<ExperienceSample> samples, long localModelVersion)
        {
            if (!isConnected || samples == null || samples.Count == 0)
            {
                yield break;
            }

            string url = $"{neuronBaseUrl}/api/v1/unity/experience/upload";

            var sampleArray = new ExperienceSampleJson[samples.Count];
            for (int i = 0; i < samples.Count; i++)
            {
                sampleArray[i] = new ExperienceSampleJson
                {
                    state = samples[i].state,
                    action = samples[i].action,
                    reward = samples[i].reward
                };
            }

            var batch = new UnityExperienceBatchBody
            {
                clientId = clientId,
                clientType = "unity",
                clientModelVersion = localModelVersion,
                samples = sampleArray
            };

            string json = JsonUtility.ToJson(batch);

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogWarning($"[NeuronClientService] Experience upload failed: {request.error}");
                }
                else
                {
                    experienceCollected += samples.Count;
                    OnExperienceUploaded?.Invoke(samples.Count);
                    Debug.Log($"[NeuronClientService] Uploaded {samples.Count} experience samples");
                }
            }
        }

        /// <summary>
        /// Checks the current model version on the Neuron server.
        /// </summary>
        private IEnumerator CheckModelVersion()
        {
            string url = $"{neuronBaseUrl}/api/v1/unity/model/info";

            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    var info = JsonUtility.FromJson<ModelInfoResponse>(WrapInJson(request.downloadHandler.text));
                    serverModelVersion = info.version;
                    Debug.Log($"[NeuronClientService] Server model version: {serverModelVersion}");
                }
            }
        }

        /// <summary>
        /// Sends a heartbeat to keep the connection alive.
        /// </summary>
        private IEnumerator SendHeartbeat()
        {
            string url = $"{neuronBaseUrl}/api/v1/unity/heartbeat?clientId={clientId}";

            using (var request = new UnityWebRequest(url, "POST"))
            {
                request.uploadHandler = new UploadHandlerBuffer();
                request.downloadHandler = new DownloadHandlerBuffer();

                yield return request.SendWebRequest();
            }
        }

        /// <summary>
        /// Disconnects from the Neuron.
        /// </summary>
        public void Disconnect()
        {
            isConnected = false;
            OnConnectionChanged?.Invoke(false);
            Debug.Log("[NeuronClientService] Disconnected from Neuron");
        }

        /// <summary>
        /// Checks the health of the main server via the startup progress endpoint.
        /// Used for co-server promotion decision making.
        /// </summary>
        private IEnumerator CheckServerHealth()
        {
            string url = $"{serverBaseUrl}/api/v1/startup/progress";

            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                bool wasHealthy = serverHealthy;

                if (request.result == UnityWebRequest.Result.Success)
                {
                    serverHealthy = true;
                    heartbeatFailureCount = 0;
                }
                else
                {
                    heartbeatFailureCount++;
                    Debug.LogWarning($"[NeuronClientService] Server health check failed ({heartbeatFailureCount}/{maxHeartbeatFailures})");

                    if (heartbeatFailureCount >= maxHeartbeatFailures)
                    {
                        serverHealthy = false;
                        Debug.LogError("[NeuronClientService] Server appears to be down - considering co-server promotion");

                        if (!isCoServer)
                        {
                            StartCoroutine(PromoteToCoServer());
                        }
                    }
                }

                if (wasHealthy != serverHealthy)
                {
                    OnServerHealthChanged?.Invoke(serverHealthy);
                }
            }
        }

        /// <summary>
        /// Promotes this client to a co-server if the main server is unavailable.
        /// Uses LeaderElectionService patterns from dora-neuron.
        /// </summary>
        private IEnumerator PromoteToCoServer()
        {
            if (isCoServer)
            {
                Debug.LogWarning("[NeuronClientService] Already promoted to co-server");
                yield break;
            }

            string url = $"{serverBaseUrl}/api/v1/game-client/neuron/co-server/promote";

            var body = new
            {
                clientId = clientId,
                playerId = playerId ?? "anonymous",
                reason = "Main server unavailable"
            };

            string json = JsonUtility.ToJson(body);

            using (var request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    // Server is down - promote locally
                    Debug.LogWarning($"[NeuronClientService] Co-server promotion request failed: {request.error}");
                    Debug.LogWarning("[NeuronClientService] Promoting to co-server locally (server unavailable)");
                    isCoServer = true;
                    OnCoServerPromoted?.Invoke(true);
                    Debug.Log("[NeuronClientService] Promoted to co-server - taking over backup responsibilities");
                }
                else
                {
                    isCoServer = true;
                    OnCoServerPromoted?.Invoke(true);
                    Debug.Log("[NeuronClientService] Successfully promoted to co-server via server");
                }
            }
        }

        /// <summary>
        /// Resigns from co-server role when the main server comes back online.
        /// </summary>
        public IEnumerator ResignCoServer()
        {
            if (!isCoServer) yield break;

            string url = $"{serverBaseUrl}/api/v1/game-client/neuron/co-server/resign";

            using (var request = new UnityWebRequest(url, "POST"))
            {
                request.uploadHandler = new UploadHandlerBuffer();
                request.downloadHandler = new DownloadHandlerBuffer();

                yield return request.SendWebRequest();

                isCoServer = false;
                OnCoServerPromoted?.Invoke(false);
                Debug.Log("[NeuronClientService] Resigned from co-server role");
            }
        }

        /// <summary>
        /// Checks if the server has come back online and resigns co-server role.
        /// </summary>
        private IEnumerator CheckServerRecovery()
        {
            if (!isCoServer) yield break;

            string url = $"{serverBaseUrl}/api/v1/startup/progress";

            using (var request = UnityWebRequest.Get(url))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    Debug.Log("[NeuronClientService] Server recovered - resigning co-server role");
                    StartCoroutine(ResignCoServer());
                }
            }
        }

        private string GenerateClientId()
        {
            return "unity-" + System.DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() +
                   "-" + UnityEngine.Random.value.ToString("X6");
        }

        private AiAssistResponseData ParseAiAssistResponse(string json)
        {
            try
            {
                return JsonUtility.FromJson<AiAssistResponseData>(WrapInJson(json));
            }
            catch (Exception e)
            {
                Debug.LogError($"[NeuronClientService] Failed to parse response: {e.Message}");
                return null;
            }
        }

        private string WrapInJson(string json)
        {
            return "{\"data\":" + json + "}";
        }

        // ── Serializable Data Classes ────────────────────────────────────────

        [Serializable]
        private class AiAssistRequestBody
        {
            public string clientId;
            public float[] features;
            public bool useConsensus;
            public string context;
        }

        [Serializable]
        private class UnityExperienceBatchBody
        {
            public string clientId;
            public string clientType;
            public long clientModelVersion;
            public ExperienceSampleJson[] samples;
        }

        [Serializable]
        private class ExperienceSampleJson
        {
            public float[] state;
            public int action;
            public float reward;
        }

        [Serializable]
        private class ModelInfoWrapper
        {
            public ModelInfoResponse data;
        }

        [Serializable]
        public class ModelInfoResponse
        {
            public bool available;
            public long version;
            public int inputSize;
            public int outputSize;
        }

        [Serializable]
        private class AiAssistResponseWrapper
        {
            public AiAssistResponseData data;
        }

        [Serializable]
        public class AiAssistResponseData
        {
            public int predictedAction;
            public string predictedActionName;
            public float confidence;
            public float[] probabilities;
            public long modelVersion;
            public bool usedConsensus;
            public long latencyMs;
            public string explanation;
        }
    }
}
