using System;
using System.Collections.Concurrent;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using UnityEngine;

namespace DoraMCU.Backup
{
    /// <summary>
    /// Connects to the server's CDC WebSocket stream and applies incoming
    /// entity change events to the local database replica.
    /// </summary>
    public class CdcConsumer
    {
        private readonly string serverUrl;
        private readonly LocalDatabase localDatabase;
        private ClientWebSocket webSocket;
        private bool connected = false;
        private CancellationTokenSource cts;
        private long vectorClock = 0;

        public bool IsConnected => connected;

        public CdcConsumer(string serverUrl, LocalDatabase localDatabase)
        {
            this.serverUrl = serverUrl;
            this.localDatabase = localDatabase;
        }

        public void Connect()
        {
            string cdcUrl = serverUrl.Replace("http://", "ws://").Replace("https://", "wss://") + "/ws/cdc";
            Debug.Log("[CdcConsumer] Connecting to CDC stream: " + cdcUrl);

            cts = new CancellationTokenSource();
            Task.Run(() => ConnectAsync(cdcUrl, cts.Token));
        }

        private async Task ConnectAsync(string url, CancellationToken token)
        {
            int retryDelay = 1000;
            while (!token.IsCancellationRequested)
            {
                try
                {
                    webSocket = new ClientWebSocket();
                    await webSocket.ConnectAsync(new Uri(url), token);
                    connected = true;
                    Debug.Log("[CdcConsumer] Connected to CDC stream");

                    // Report CDC sync complete
                    await ReportStepComplete("backup_sync", "CDC stream connected");

                    // Listen for messages
                    while (connected && !token.IsCancellationRequested)
                    {
                        var buffer = new byte[4096];
                        var result = await webSocket.ReceiveAsync(new System.Memory<byte>(buffer), token);
                        string message = Encoding.UTF8.GetString(buffer, 0, result.Count);

                        try
                        {
                            ApplyCdcEvent(message);
                        }
                        catch (Exception ex)
                        {
                            Debug.LogWarning("[CdcConsumer] Failed to apply CDC event: " + ex.Message);
                        }
                    }
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch (Exception ex)
                {
                    connected = false;
                    Debug.LogWarning("[CdcConsumer] CDC connection lost: " + ex.Message + ". Retrying in " + retryDelay + "ms...");
                    await Task.Delay(retryDelay, token);
                    retryDelay = Math.Min(retryDelay * 2, 30000);
                }
            }
        }

        private void ApplyCdcEvent(string json)
        {
            using (JsonDocument doc = JsonDocument.Parse(json))
            {
                JsonElement root = doc.RootElement;
                string entity = root.GetProperty("entity").GetString();
                string id = root.GetProperty("id").GetString();
                string operation = root.GetProperty("operation").GetString();
                JsonElement data = root.GetProperty("data");
                long eventVectorClock = root.GetProperty("vectorClock").GetInt64();

                if (eventVectorClock <= vectorClock)
                    return; // Already applied

                localDatabase.ApplyChange(entity, id, operation, data.GetRawText());
                vectorClock = eventVectorClock;
            }
        }

        private async Task ReportStepComplete(string key, string message)
        {
            string json = "{\"key\":\"" + key + "\",\"message\":\"" + message + "\"}";
            string url = serverUrl + "/api/v1/startup/step/complete";

            try
            {
                using (var handler = new System.Net.Http.HttpClientHandler())
                using (var client = new System.Net.Http.HttpClient(handler))
                {
                    var content = new System.Net.Http.StringContent(json, Encoding.UTF8, "application/json");
                    await client.PostAsync(url, content);
                }
            }
            catch (Exception ex)
            {
                Debug.LogWarning("[CdcConsumer] Failed to report step: " + ex.Message);
            }
        }

        public void Disconnect()
        {
            connected = false;
            cts?.Cancel();
            if (webSocket != null && webSocket.State == WebSocketState.Open)
            {
                webSocket.CloseAsync(WebSocketStatus.NormalClosure, "Disconnecting", CancellationToken.None).Wait();
            }
            Debug.Log("[CdcConsumer] Disconnected from CDC stream");
        }
    }
}
