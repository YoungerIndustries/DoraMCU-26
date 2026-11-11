using System;
using System.Collections.Generic;
using UnityEngine;

namespace DoraMCU.AI
{
    /// <summary>
    /// Stores (state, action, reward) experience tuples for AI training.
    /// <para>
    /// Buffers experience locally and provides batch access for uploading
    /// to the server for ensemble training.
    /// </para>
    /// </summary>
    [Serializable]
    public class ExperienceSample
    {
        public float[] state;
        public int action;
        public float reward;

        public ExperienceSample(float[] state, int action, float reward)
        {
            this.state = state;
            this.action = action;
            this.reward = reward;
        }
    }

    public class ExperienceBuffer : MonoBehaviour
    {
        [Header("Settings")]
        [Tooltip("Maximum number of samples to buffer")]
        [SerializeField] private int maxBufferSize = 200;

        [Tooltip("Batch size for uploading")]
        [SerializeField] private int batchSize = 50;

        // Buffer
        private Queue<ExperienceSample> buffer;

        public int MaxBufferSize => maxBufferSize;
        public int BatchSize => batchSize;

        private void Awake()
        {
            buffer = new Queue<ExperienceSample>();
        }

        /// <summary>
        /// Records an experience tuple.
        /// </summary>
        public void Record(float[] state, int action, float reward)
        {
            if (state == null || state.Length != 64)
            {
                Debug.LogWarning("[ExperienceBuffer] Invalid state vector: expected 64 elements");
                return;
            }

            var sample = new ExperienceSample(
                (float[]) state.Clone(),
                Mathf.Clamp(action, 0, 16),
                Mathf.Clamp(reward, -1f, 1f)
            );

            buffer.Enqueue(sample);

            // Trim buffer if too large
            while (buffer.Count > maxBufferSize)
            {
                buffer.Dequeue();
            }
        }

        /// <summary>
        /// Gets a batch of samples for uploading.
        /// </summary>
        public List<ExperienceSample> GetBatch()
        {
            var batch = new List<ExperienceSample>();

            while (buffer.Count > 0 && batch.Count < batchSize)
            {
                batch.Add(buffer.Dequeue());
            }

            return batch;
        }

        /// <summary>
        /// Returns a batch back to the buffer (for retry after upload failure).
        /// </summary>
        public void ReturnBatch(List<ExperienceSample> batch)
        {
            // Add back to the front of the queue
            var current = new Queue<ExperienceSample>(buffer);
            buffer = new Queue<ExperienceSample>();

            // Add returned samples first
            foreach (var sample in batch)
            {
                buffer.Enqueue(sample);
            }

            // Then add remaining samples
            foreach (var sample in current)
            {
                if (buffer.Count < maxBufferSize)
                {
                    buffer.Enqueue(sample);
                }
            }
        }

        /// <summary>
        /// Returns the current buffer size.
        /// </summary>
        public int GetBufferSize()
        {
            return buffer.Count;
        }

        /// <summary>
        /// Clears all buffered experience.
        /// </summary>
        public void Clear()
        {
            buffer.Clear();
            Debug.Log("[ExperienceBuffer] Cleared");
        }

        /// <summary>
        /// Gets all samples and clears the buffer.
        /// </summary>
        public List<ExperienceSample> GetAll()
        {
            var all = new List<ExperienceSample>(buffer);
            buffer.Clear();
            return all;
        }
    }
}
