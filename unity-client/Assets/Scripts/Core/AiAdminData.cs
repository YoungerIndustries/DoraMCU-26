using System;
using UnityEngine;

namespace DoraMCU.Client.Core
{
    /// <summary>
    /// Data structure for AI admin characters in the game world.
    /// Represents an AI admin with 3D position, role, and workload information.
    /// </summary>
    [Serializable]
    public class AiAdminData
    {
        public long id;
        public string name;
        public string role;
        public string galaxyName;
        public float posX;
        public float posY;
        public float posZ;
        public float homePosX;
        public float homePosY;
        public float homePosZ;
        public string currentOrderId;
        public int activeOrderCount;
        public bool isGalaxyOverseer;
        public string characterType;
        public string empireName;
        public bool hasPosition;

        /// <summary>
        /// Get the 3D position as a Vector3.
        /// </summary>
        public Vector3 Position
        {
            get { return new Vector3(posX, posY, posZ); }
        }

        /// <summary>
        /// Get the home position as a Vector3.
        /// </summary>
        public Vector3 HomePosition
        {
            get { return new Vector3(homePosX, homePosY, homePosZ); }
        }

        /// <summary>
        /// Check if this admin is currently busy with an order.
        /// </summary>
        public bool IsBusy
        {
            get { return currentOrderId != null && currentOrderId.Length > 0; }
        }

        /// <summary>
        /// Get a display string for the admin's status.
        /// </summary>
        public string StatusText
        {
            get { return IsBusy ? "Busy" : "Available"; }
        }
    }
}
