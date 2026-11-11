using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.AI
{
    /// <summary>
    /// Manages ONNX model loading and inference for the Unity client.
    /// <para>
    /// Downloads the ONNX model from the server, loads it via ONNX Runtime C#,
    /// and runs local inference for AI strategy predictions.
    /// </para>
    /// </summary>
    public class OnnxInferenceManager : MonoBehaviour
    {
        [Header("Model Settings")]
        [Tooltip("Base URL of the DoraMCU server")]
        [SerializeField] private string serverBaseUrl = "http://localhost:8080";

        [Tooltip("Path to store downloaded ONNX models")]
        [SerializeField] private string modelStoragePath = "ONNXModels";

        [Header("Model Dimensions")]
        [SerializeField] private int inputSize = 64;
        [SerializeField] private int outputSize = 17;

        // State
        private string latestModelPath;
        private long modelVersion = 0;
        private bool isModelLoaded = false;

        // ONNX Runtime session (would be Microsoft.ML.OnnxRuntime.InferenceSession)
        // For now, we use a placeholder that can be replaced with the actual ONNX Runtime C# API
        private object onnxSession;

        public bool IsModelLoaded => isModelLoaded;
        public long ModelVersion => modelVersion;
        public int InputSize => inputSize;
        public int OutputSize => outputSize;

        private void Awake()
        {
            DontDestroyOnLoad(gameObject);
        }

        private void Start()
        {
            // Try to load the latest model from the server
            StartCoroutine(LoadModelFromServer());
        }

        /// <summary>
        /// Fetches model info from the server and downloads the latest ONNX model.
        /// </summary>
        private System.Collections.IEnumerator LoadModelFromServer()
        {
            // First, check model info
            using (var request = new UnityWebRequest(serverBaseUrl + "/api/v1/dl/model/info", "GET"))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogWarning("[OnnxInferenceManager] Failed to get model info: " + request.error);
                    yield break;
                }

                var json = request.downloadHandler.text;
                var info = JsonUtility.FromJson<ModelInfoResponse>(WrapInJson(json));

                if (!info.available)
                {
                    Debug.LogWarning("[OnnxInferenceManager] No model available on server");
                    yield break;
                }

                // Download the model file
                yield return DownloadAndLoadModel(info.downloadUrl);
            }
        }

        /// <summary>
        /// Downloads the ONNX model file and loads it.
        /// </summary>
        private System.Collections.IEnumerator DownloadAndLoadModel(string downloadUrl)
        {
            string fullUrl = serverBaseUrl + downloadUrl;

            using (var request = new UnityWebRequest(fullUrl, "GET"))
            {
                request.downloadHandler = new DownloadHandlerBuffer();
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.ConnectionError ||
                    request.result == UnityWebRequest.Result.ProtocolError)
                {
                    Debug.LogError("[OnnxInferenceManager] Failed to download model: " + request.error);
                    yield break;
                }

                // Save the model to persistent data path
                string fullPath = System.IO.Path.Combine(Application.persistentDataPath, modelStoragePath);
                System.IO.Directory.CreateDirectory(fullPath);

                string modelPath = System.IO.Path.Combine(fullPath, "strategy-network.onnx");
                System.IO.File.WriteAllBytes(modelPath, request.downloadHandler.data);

                // Load the model
                LoadModelFromFile(modelPath);
            }
        }

        /// <summary>
        /// Loads an ONNX model from a local file.
        /// </summary>
        public void LoadModelFromFile(string path)
        {
            try
            {
                // TODO: Replace with actual ONNX Runtime C# API
                // var sessionOptions = new SessionOptions();
                // onnxSession = new InferenceSession(path, sessionOptions);

                latestModelPath = path;
                isModelLoaded = true;
                modelVersion = DateTime.UtcNow.Ticks / 10000; // Convert to milliseconds

                Debug.Log($"[OnnxInferenceManager] Model loaded from: {path} (version: {modelVersion})");
            }
            catch (Exception e)
            {
                Debug.LogError($"[OnnxInferenceManager] Failed to load model: {e.Message}");
                isModelLoaded = false;
            }
        }

        /// <summary>
        /// Runs inference on the loaded model.
        /// </summary>
        /// <param name="features">64-dimensional feature vector</param>
        /// <returns>Prediction result with action and probabilities</returns>
        public PredictionResult Predict(float[] features)
        {
            if (!isModelLoaded || features == null || features.Length != inputSize)
            {
                Debug.LogWarning("[OnnxInferenceManager] Model not loaded or invalid input — using fallback");
                return GenerateFallbackPrediction(features);
            }

            try
            {
                // TODO: Replace with actual ONNX Runtime C# API
                // var inputs = new List<NamedOnnxValue>
                // {
                //     NamedOnnxValue.CreateFromTensor("input", new DenseTensor<float>(features, new[] { 1, inputSize }))
                // };
                // var results = ((InferenceSession)onnxSession).Run(inputs, null);
                // var output = results[0].AsEnumerable<Tensor<float>>().First();

                // For now, use fallback
                return GenerateFallbackPrediction(features);
            }
            catch (Exception e)
            {
                Debug.LogError($"[OnnxInferenceManager] Inference failed: {e.Message}");
                return GenerateFallbackPrediction(features);
            }
        }

        /// <summary>
        /// Generates a fallback prediction when the model is not available.
        /// </summary>
        private PredictionResult GenerateFallbackPrediction(float[] features)
        {
            float[] probs = new float[outputSize];
            float uniform = 1.0f / outputSize;

            for (int i = 0; i < outputSize; i++)
            {
                probs[i] = uniform;
            }

            // Simple heuristic: boost certain actions based on feature values
            if (features != null && features.Length >= 64)
            {
                // If resources are high (features 0-7), boost build actions
                float resourceScore = 0;
                for (int i = 0; i < 8; i++) resourceScore += features[i];
                resourceScore /= 8;

                if (resourceScore > 0.5f)
                {
                    probs[0] *= 2; // BUILD_SHIP
                    probs[1] *= 1.5f; // BUILD_STATION
                }

                // If fleet strength is high (features 8-15), boost combat actions
                float fleetScore = 0;
                for (int i = 8; i < 16; i++) fleetScore += features[i];
                fleetScore /= 8;

                if (fleetScore > 0.5f)
                {
                    probs[2] *= 2; // ENGAGE_COMBAT
                }
            }

            // Normalize
            float sum = 0;
            foreach (float p in probs) sum += p;
            for (int i = 0; i < outputSize; i++) probs[i] /= sum;

            // Find best action
            int bestAction = 0;
            float maxProb = probs[0];
            for (int i = 1; i < outputSize; i++)
            {
                if (probs[i] > maxProb)
                {
                    maxProb = probs[i];
                    bestAction = i;
                }
            }

            return new PredictionResult
            {
                action = bestAction,
                probabilities = probs,
                version = 0,
                isFallback = true
            };
        }

        /// <summary>
        /// Disposes the current model session.
        /// </summary>
        public void Dispose()
        {
            if (onnxSession != null)
            {
                // TODO: Dispose ONNX Runtime session
                onnxSession = null;
            }
            isModelLoaded = false;
            modelVersion = 0;
            Debug.Log("[OnnxInferenceManager] Model disposed");
        }

        private void OnDestroy()
        {
            Dispose();
        }

        /// <summary>
        /// Response model for /api/v1/dl/model/info endpoint.
        /// </summary>
        [Serializable]
        private class ModelInfoResponse
        {
            public bool available;
            public long version;
            public string hash;
            public long size;
            public string downloadUrl;
        }

        /// <summary>
        /// Wraps a JSON object in an outer object for JsonUtility parsing.
        /// </summary>
        private string WrapInJson(string json)
        {
            return "{\"data\":" + json + "}";
        }
    }

    /// <summary>
    /// Result of an AI prediction.
    /// </summary>
    [Serializable]
    public class PredictionResult
    {
        public int action;
        public float[] probabilities;
        public long version;
        public bool isFallback;
    }
}
