using System;
using System.Collections.Generic;
using System.Text.Json;
using UnityEngine;

namespace DoraMCU.Backup
{
    /// <summary>
    /// Abstraction layer for the local database replica.
    /// Stores entity data in memory and applies CDC changes.
    /// In a production build, this would use SQLite or an embedded H2 bridge.
    /// </summary>
    public class LocalDatabase
    {
        private readonly Dictionary<string, Dictionary<string, string>> entities = new();
        private readonly object lockObj = new();

        /// <summary>
        /// Imports a JSON Lines snapshot from the server.
        /// Each line contains {"entity": "TypeName", "data": {...}}.
        /// </summary>
        public void ImportSnapshot(string jsonLines)
        {
            Debug.Log("[LocalDatabase] Importing snapshot...");
            string[] lines = jsonLines.Split(new[] { '\n' }, StringSplitOptions.RemoveEmptyEntries);
            int imported = 0;

            foreach (string line in lines)
            {
                try
                {
                    using (JsonDocument doc = JsonDocument.Parse(line.Trim()))
                    {
                        JsonElement root = doc.RootElement;
                        string entity = root.GetProperty("entity").GetString();
                        JsonElement data = root.GetProperty("data");

                        string id = "unknown";
                        if (data.TryGetProperty("id", out JsonElement idElem))
                        {
                            id = idElem.ToString();
                        }

                        lock (lockObj)
                        {
                            if (!entities.ContainsKey(entity))
                                entities[entity] = new Dictionary<string, string>();

                            entities[entity][id] = data.GetRawText();
                            imported++;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Debug.LogWarning("[LocalDatabase] Failed to import line: " + ex.Message);
                }
            }

            Debug.Log("[LocalDatabase] Imported " + imported + " entities from snapshot");
        }

        /// <summary>
        /// Applies a CDC change event to the local database.
        /// </summary>
        public void ApplyChange(string entity, string id, string operation, string dataJson)
        {
            lock (lockObj)
            {
                switch (operation.ToUpper())
                {
                    case "CREATE":
                    case "UPDATE":
                        if (!entities.ContainsKey(entity))
                            entities[entity] = new Dictionary<string, string>();
                        entities[entity][id] = dataJson;
                        break;

                    case "DELETE":
                        if (entities.ContainsKey(entity))
                            entities[entity].Remove(id);
                        break;

                    default:
                        Debug.LogWarning("[LocalDatabase] Unknown CDC operation: " + operation);
                        break;
                }
            }
        }

        /// <summary>
        /// Retrieves an entity by type and ID.
        /// </summary>
        public string GetEntity(string entity, string id)
        {
            lock (lockObj)
            {
                if (entities.TryGetValue(entity, out var map) && map.TryGetValue(id, out var data))
                    return data;
                return null;
            }
        }

        /// <summary>
        /// Returns all entities of a given type.
        /// </summary>
        public Dictionary<string, string> GetEntities(string entity)
        {
            lock (lockObj)
            {
                if (entities.TryGetValue(entity, out var map))
                    return new Dictionary<string, string>(map);
                return new Dictionary<string, string>();
            }
        }

        /// <summary>
        /// Returns the total number of entities stored.
        /// </summary>
        public int GetTotalEntityCount()
        {
            lock (lockObj)
            {
                int count = 0;
                foreach (var map in entities.Values)
                    count += map.Count;
                return count;
            }
        }
    }
}
