using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.AI
{
    /// <summary>
    /// Full client-side AI pipeline: inference, experience collection, and local training.
    /// <para>
    /// Coordinates with OnnxInferenceManager for predictions and ExperienceBuffer
    /// for collecting (state, action, reward) tuples. Periodically uploads experience
    /// to the server for ensemble training.
    /// </para>
    /// </summary>
    public class ClientAiService : MonoBehaviour
    {
        [Header("References")]
        [SerializeField] private OnnxInferenceManager inferenceManager;
        [SerializeField] private ExperienceBuffer experienceBuffer;
        [SerializeField] private NeuronClientService neuronClientService;

        [Header("Settings")]
        [Tooltip("Base URL of the DoraMCU server")]
        [SerializeField] private string serverBaseUrl = "http://localhost:8080";

        [Tooltip("Use Neuron for AI assistance when available")]
        [SerializeField] private bool useNeuronAi = true;

        [Tooltip("Upload experience batch every N seconds")]
        [SerializeField] private float uploadIntervalSeconds = 300f; // 5 minutes

        [Tooltip("Minimum samples before uploading")]
        [SerializeField] private int minSamplesForUpload = 50;

        // State
        private float uploadTimer;
        private string clientId;
        private long localModelVersion;

        public ExperienceBuffer ExperienceBuffer => experienceBuffer;
        public long LocalModelVersion => localModelVersion;

        private void Awake()
        {
            DontDestroyOnLoad(gameObject);
            clientId = GenerateClientId();
        }

        private void Start()
        {
            if (inferenceManager == null)
            {
                // Try to find in scene
                inferenceManager = FindObjectOfType<OnnxInferenceManager>();
            }

            if (experienceBuffer == null)
            {
                // Create one
                var go = new GameObject("ExperienceBuffer");
                experienceBuffer = go.AddComponent<ExperienceBuffer>();
            }

            if (neuronClientService == null)
            {
                // Try to find in scene
                neuronClientService = FindObjectOfType<NeuronClientService>();
            }

            uploadTimer = 0;
            Debug.Log($"[ClientAiService] Initialized. Client ID: {clientId}, Neuron AI: {useNeuronAi}");
        }

        private void Update()
        {
            uploadTimer += Time.deltaTime;

            // Periodic upload
            if (uploadTimer >= uploadIntervalSeconds && experienceBuffer != null)
            {
                if (experienceBuffer.GetBufferSize() >= minSamplesForUpload)
                {
                    StartCoroutine(UploadExperienceBatch());
                }
                uploadTimer = 0;
            }
        }

        /// <summary>
        /// Runs AI prediction on the current game state.
        /// Uses Neuron AI assistance when available, falls back to local inference.
        /// </summary>
        /// <param name="features">64-dimensional feature vector</param>
        /// <returns>Prediction result</returns>
        public PredictionResult Predict(float[] features)
        {
            // Try Neuron AI assistance first
            if (useNeuronAi && neuronClientService != null && neuronClientService.IsConnected)
            {
                // Start async Neuron request
                StartCoroutine(PredictFromNeuron(features, OnNeuronPredictionComplete));
                return null; // Will be delivered via callback
            }

            // Fall back to local inference
            if (inferenceManager != null)
            {
                return inferenceManager.Predict(features);
            }

            Debug.LogWarning("[ClientAiService] No inference manager — returning null");
            return null;
        }

        /// <summary>
        /// Callback for Neuron AI prediction results.
        /// </summary>
        private void OnNeuronPredictionComplete(NeuronClientService.AiAssistResponseData response)
        {
            if (response != null)
            {
                Debug.Log($"[ClientAiService] Neuron AI: {response.predictedActionName} " +
                         $"(confidence: {response.confidence:F2})");
            }
        }

        /// <summary>
        /// Requests prediction from the Neuron node.
        /// </summary>
        private System.Collections.IEnumerator PredictFromNeuron(float[] features,
            System.Action<NeuronClientService.AiAssistResponseData> callback)
        {
            yield return neuronClientService.QuickPredict(features);
            // Response delivered via OnAiAssistResponse event
        }

        /// <summary>
        /// Records an experience tuple.
        /// </summary>
        public void RecordExperience(float[] state, int action, float reward)
        {
            if (experienceBuffer != null)
            {
                experienceBuffer.Record(state, action, reward);
            }
        }

        /// <summary>
        /// Records an experience from a game event.
        /// </summary>
        public void RecordFromGame(Dictionary<string, object> gameState, string actionName, float reward)
        {
            float[] features = ExtractFeatures(gameState);
            int actionIndex = ActionNameToIndex(actionName);
            RecordExperience(features, actionIndex, reward);
        }

        /// <summary>
        /// Extracts a 64-dimensional feature vector from game state.
        /// </summary>
        public float[] ExtractFeatures(Dictionary<string, object> gameState)
        {
            float[] features = new float[64];

            if (gameState == null) return features;

            // Resources (features 0-7)
            if (gameState.ContainsKey("resources"))
            {
                var resources = gameState["resources"] as Dictionary<string, object>;
                if (resources != null)
                {
                    features[0] = Normalize(SafeFloat(resources, "minerals"), 0, 10000);
                    features[1] = Normalize(SafeFloat(resources, "energy"), 0, 5000);
                    features[2] = Normalize(SafeFloat(resources, "food"), 0, 5000);
                    features[3] = Normalize(SafeFloat(resources, "influence"), 0, 1000);
                }
            }

            // Fleet strength (features 8-15)
            if (gameState.ContainsKey("fleets"))
            {
                var fleets = gameState["fleets"] as List<object>;
                if (fleets != null)
                {
                    features[8] = Normalize((float) fleets.Count, 0, 50);
                }
            }

            // Planet count (features 16-23)
            if (gameState.ContainsKey("planets"))
            {
                var planets = gameState["planets"] as List<object>;
                if (planets != null)
                {
                    features[16] = Normalize((float) planets.Count, 0, 20);
                }
            }

            return features;
        }

        /// <summary>
        /// Uploads the current experience batch to the server or Neuron.
        /// </summary>
        private System.Collections.IEnumerator UploadExperienceBatch()
        {
            if (experienceBuffer == null || experienceBuffer.GetBufferSize() == 0)
            {
                yield break;
            }

            var samples = experienceBuffer.GetBatch();
            if (samples.Count == 0) yield break;

            // Try Neuron first if available
            if (useNeuronAi && neuronClientService != null && neuronClientService.IsConnected)
            {
                yield return neuronClientService.UploadExperience(samples, localModelVersion);
                yield break;
            }

            // Fall back to direct server upload
            var body = new ExperienceBatchRequest
            {
                clientId = clientId,
                clientType = "unity",
                localModelVersion = localModelVersion
            };

            var batchSamples = new ExperienceSampleJson[samples.Count];
            for (int i = 0; i < samples.Count; i++)
            {
                batchSamples[i] = new ExperienceSampleJson
                {
                    state = samples[i].state,
                    action = samples[i].action,
                    reward = samples[i].reward
                };
            }
            body.samples = batchSamples;

            string json = JsonUtility.ToJson(body);

            using (var request = new UnityWebRequest(serverBaseUrl + "/api/v1/dl/experience/aggregate", "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");

                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogWarning($"[ClientAiService] Upload failed: {request.error}");
                    experienceBuffer.ReturnBatch(samples);
                }
                else
                {
                    Debug.Log($"[ClientAiService] Uploaded {samples.Count} experience samples");
                }
            }
        }

        /// <summary>
        /// Updates the local model version after a model sync.
        /// </summary>
        public void SetModelVersion(long version)
        {
            localModelVersion = version;
            Debug.Log($"[ClientAiService] Model version updated to: {version}");
        }

        /// <summary>
        /// Forces an immediate upload of all buffered experience.
        /// </summary>
        public System.Collections.IEnumerator FlushExperience()
        {
            while (experienceBuffer != null && experienceBuffer.GetBufferSize() > 0)
            {
                yield return UploadExperienceBatch();
            }
        }

        /// <summary>
        /// Generates a unique client ID.
        /// </summary>
        private string GenerateClientId()
        {
            return "unity-" + System.DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() +
                   "-" + UnityEngine.Random.value.ToString("X6");
        }

        private float Normalize(float value, float min, float max)
        {
            float clamped = Mathf.Clamp(value, min, max);
            return (clamped - min) / (max - min);
        }

        private float SafeFloat(Dictionary<string, object> dict, string key)
        {
            if (dict == null || !dict.ContainsKey(key)) return 0;
            try
            {
                return Convert.ToSingle(dict[key]);
            }
            catch
            {
                return 0;
            }
        }

        private int ActionNameToIndex(string actionName)
        {
            return actionName switch
            {
                "BUILD_SHIP" => 0,
                "BUILD_STATION" => 1,
                "ENGAGE_COMBAT" => 2,
                "MOVE_FLEET" => 3,
                "EXPAND_COLONY" => 4,
                "RESEARCH_TECH" => 5,
                "TRADE" => 6,
                "OPTIMIZE_MINING" => 7,
                "FORTIFY" => 8,
                "RETREAT" => 9,
                "FLANK_AMBUSH" => 10,
                "WAIT" => 11,
                "DIPLOMACY" => 12,
                "SCOUT" => 13,
                "RAID" => 14,
                "BLOCKADE" => 15,
                "SURRENDER" => 16,
                _ => 11 // Default to WAIT
            };
        }

        [Serializable]
        private class Wrapper
        {
            public object data;
        }

        [Serializable]
        private class ExperienceSampleJson
        {
            public float[] state;
            public int action;
            public float reward;
        }

        [Serializable]
        private class ExperienceBatchRequest
        {
            public string clientId;
            public string clientType;
            public ExperienceSampleJson[] samples;
            public long localModelVersion;
        }
    }
}
