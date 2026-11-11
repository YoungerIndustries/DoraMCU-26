using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Interest management system for bandwidth optimization.
    /// Only syncs nearby entities to reduce network traffic.
    /// Uses spatial partitioning to determine which entities
    /// are within the player's interest range.
    /// </summary>
    public class InterestManager
    {
        private readonly Dictionary<long, EntityInterest> entityInterests = new();
        private readonly HashSet<long> inInterestSet = new();
        
        private Vector3 playerPosition;
        private int currentSector;
        private int currentSubSector;

        // Interest ranges
        public float NearInterestRange { get; set; } = 50f;    // Full sync
        public float FarInterestRange { get; set; } = 200f;     // Position only
        public float NoInterestRange { get; set; } = 500f;      // No sync

        // Events
        public event System.Action<long> OnEntityEnteredInterest;
        public event System.Action<long> OnEntityLeftInterest;

        /// <summary>
        /// Update player position for interest calculations.
        /// </summary>
        public void UpdatePlayerPosition(Vector3 position, int sector, int subSector)
        {
            playerPosition = position;
            currentSector = sector;
            currentSubSector = subSector;
        }

        /// <summary>
        /// Check if an entity is within interest range.
        /// </summary>
        public InterestLevel CheckInterest(long entityId, Vector3 entityPosition)
        {
            float distance = Vector3.Distance(playerPosition, entityPosition);

            if (distance <= NearInterestRange)
            {
                return InterestLevel.Near;
            }
            else if (distance <= FarInterestRange)
            {
                return InterestLevel.Far;
            }
            else if (distance <= NoInterestRange)
            {
                return InterestLevel.None;
            }

            return InterestLevel.None;
        }

        /// <summary>
        /// Check if an entity is within interest range using sector coordinates.
        /// </summary>
        public InterestLevel CheckInterest(long entityId, int entitySector, int entitySubSector)
        {
            int sectorDistance = Mathf.Abs(currentSector - entitySector);
            int subSectorDistance = Mathf.Abs(currentSubSector - entitySubSector);

            if (sectorDistance == 0 && subSectorDistance <= 1)
            {
                return InterestLevel.Near;
            }
            else if (sectorDistance <= 1 && subSectorDistance <= 3)
            {
                return InterestLevel.Far;
            }
            else if (sectorDistance <= 2)
            {
                return InterestLevel.None;
            }

            return InterestLevel.None;
        }

        /// <summary>
        /// Update interest for all entities.
        /// Returns entities that entered or left interest range.
        /// </summary>
        public InterestUpdate UpdateAllInterests(List<EntityPosition> entities)
        {
            var update = new InterestUpdate();

            foreach (var entity in entities)
            {
                InterestLevel level = CheckInterest(entity.Id, entity.Position);
                
                var existing = GetOrCreateInterest(entity.Id);
                
                if (existing.Level != level)
                {
                    if (level > InterestLevel.None && existing.Level == InterestLevel.None)
                    {
                        // Entity entered interest
                        update.Entered.Add(entity.Id);
                        inInterestSet.Add(entity.Id);
                        OnEntityEnteredInterest?.Invoke(entity.Id);
                    }
                    else if (level == InterestLevel.None && existing.Level > InterestLevel.None)
                    {
                        // Entity left interest
                        update.Left.Add(entity.Id);
                        inInterestSet.Remove(entity.Id);
                        OnEntityLeftInterest?.Invoke(entity.Id);
                    }

                    existing.Level = level;
                    existing.Position = entity.Position;
                }
            }

            return update;
        }

        /// <summary>
        /// Get entities currently in interest range.
        /// </summary>
        public HashSet<long> GetInInterestEntities()
        {
            return new HashSet<long>(inInterestSet);
        }

        /// <summary>
        /// Check if an entity is in interest range.
        /// </summary>
        public bool IsInInterest(long entityId)
        {
            return inInterestSet.Contains(entityId);
        }

        private EntityInterest GetOrCreateInterest(long entityId)
        {
            if (!entityInterests.ContainsKey(entityId))
            {
                entityInterests[entityId] = new EntityInterest
                {
                    Id = entityId,
                    Level = InterestLevel.None
                };
            }
            return entityInterests[entityId];
        }

        /// <summary>
        /// Interest level for an entity.
        /// </summary>
        public enum InterestLevel
        {
            None = 0,
            Far = 1,
            Near = 2
        }

        /// <summary>
        /// Entity interest tracking.
        /// </summary>
        private class EntityInterest
        {
            public long Id;
            public InterestLevel Level;
            public Vector3 Position;
        }

        /// <summary>
        /// Entity position for interest checking.
        /// </summary>
        public struct EntityPosition
        {
            public long Id;
            public Vector3 Position;
        }

        /// <summary>
        /// Result of interest update.
        /// </summary>
        public class InterestUpdate
        {
            public readonly List<long> Entered = new();
            public readonly List<long> Left = new();
        }
    }
}
