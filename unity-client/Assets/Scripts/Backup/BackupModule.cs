using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.Networking;

namespace DoraMCU.Backup
{
    /// <summary>
    /// Manages the game client's backup capabilities: downloading database snapshots,
    /// initializing the local database, and coordinating with the CDC consumer.
    /// </summary>
    public class BackupModule : MonoBehaviour
    {
        [Header("Server Configuration")]
        [SerializeField] private string serverUrl = "http://localhost:8080";
        [SerializeField] private bool enabled = true;

        private LocalDatabase localDatabase;
        private CdcConsumer cdcConsumer;
        private bool snapshotDownloaded = false;
        private bool databaseInitialized = false;

        public bool IsReady => snapshotDownloaded && databaseInitialized && cdcConsumer != null && cdcConsumer.IsConnected;

        void Start()
        {
            if (!enabled)
            {
                Debug.Log("[BackupModule] Backup module disabled");
                return;
            }

            Debug.Log("[BackupModule] Initializing backup module...");
            localDatabase = new LocalDatabase();
            cdcConsumer = new CdcConsumer(serverUrl, localDatabase);

            // Report backup snapshot start to server
            ReportStepStart("backup_snapshot", "Downloading database snapshot");
            StartCoroutine(DownloadSnapshot());
        }

        IEnumerator DownloadSnapshot()
        {
            string snapshotUrl = serverUrl + "/api/v1/game-client/db-snapshot";
            Debug.Log("[BackupModule] Downloading snapshot from: " + snapshotUrl);

            using (UnityWebRequest request = UnityWebRequest.Get(snapshotUrl))
            {
                yield return request.SendWebRequest();

                if (request.result == UnityWebRequest.Result.Success)
                {
                    string snapshotData = request.downloadHandler.text;
                    Debug.Log("[BackupModule] Snapshot downloaded, size: " + snapshotData.Length + " bytes");
                    snapshotDownloaded = true;

                    // Initialize local database with snapshot data
                    ReportStepStart("backup_snapshot", "Initializing local database");
                    localDatabase.ImportSnapshot(snapshotData);
                    databaseInitialized = true;

                    ReportStepComplete("backup_snapshot", "Local database initialized");

                    // Connect to CDC stream
                    ReportStepStart("backup_sync", "Connecting to CDC stream");
                    cdcConsumer.Connect();

                    // Report client registration
                    ReportStepComplete("client_registration", "Game client registered with server");
                }
                else
                {
                    Debug.LogError("[BackupModule] Failed to download snapshot: " + request.error);
                    ReportStepError("backup_snapshot", "Snapshot download failed: " + request.error);
                }
            }
        }

        void ReportStepStart(string key, string message)
        {
            StartCoroutine(PostStep("/api/v1/startup/step/start", key, message));
        }

        void ReportStepComplete(string key, string message)
        {
            StartCoroutine(PostStep("/api/v1/startup/step/complete", key, message));
        }

        void ReportStepError(string key, string message)
        {
            StartCoroutine(PostStep("/api/v1/startup/step/complete", key, "ERROR: " + message));
        }

        IEnumerator PostStep(string endpoint, string key, string message)
        {
            string json = "{\"key\":\"" + key + "\",\"message\":\"" + message + "\"}";
            string url = serverUrl + endpoint;

            using (UnityWebRequest request = new UnityWebRequest(url, "POST"))
            {
                byte[] bodyRaw = System.Text.Encoding.UTF8.GetBytes(json);
                request.uploadHandler = new UploadHandlerRaw(bodyRaw);
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Content-Type", "application/json");
                yield return request.SendWebRequest();

                if (request.result != UnityWebRequest.Result.Success)
                {
                    Debug.LogWarning("[BackupModule] Failed to report step " + key + ": " + request.error);
                }
            }
        }

        void OnDestroy()
        {
            if (cdcConsumer != null)
                cdcConsumer.Disconnect();
        }
    }
}
