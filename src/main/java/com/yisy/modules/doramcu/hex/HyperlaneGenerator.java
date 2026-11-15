package com.yisy.modules.doramcu.hex;

import java.util.*;
import java.util.PriorityQueue;

/**
 * Generates hyperlane connections between solar systems in a galaxy.
 * <p>
 * Extracted from GalaxyGenerator to separate hyperlane network generation
 * from the rest of the galaxy generation logic.
 * </p>
 * <p>
 * Uses Prim's algorithm with a priority queue for O(n log n) spanning tree
 * construction, replacing the previous O(n²) nearest-neighbor approach.
 * </p>
 */
public class HyperlaneGenerator {

    private final Random random;

    /**
     * Creates a new hyperlane generator.
     *
     * @param random the random instance
     */
    public HyperlaneGenerator(Random random) {
        this.random = random;
    }

    /**
     * Generates hyperlane connections between solar systems in the galaxy.
     * <p>
     * Uses Prim's algorithm with a priority queue for efficient spanning tree
     * construction, then adds random extra connections based on density.
     * </p>
     *
     * @param galaxy the galaxy to add hyperlanes to
     * @param hyperlaneDensity the density multiplier for extra connections
     */
    public void generateHyperlanes(Galaxy galaxy, double hyperlaneDensity) {
        List<SolarSystem> systems = new ArrayList<>(galaxy.getAllSystems().values());
        int n = systems.size();
        if (n < 2) {
            return;
        }

        // Prim's algorithm for minimum spanning tree: O(n log n)
        boolean[] connected = new boolean[n];
        // Priority queue: (distance, fromIndex, toIndex)
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        // Start from the first system
        connected[0] = true;
        int connectedCount = 1;

        // Add edges from system 0 to all unconnected systems
        for (int j = 1; j < n; j++) {
            int dist = Math.abs(systems.get(0).getId() - systems.get(j).getId());
            pq.offer(new int[]{dist, 0, j});
        }

        // Build spanning tree
        while (!pq.isEmpty() && connectedCount < n) {
            int[] edge = pq.poll();
            int dist = edge[0];
            int fromIdx = edge[1];
            int toIdx = edge[2];

            if (connected[toIdx]) {
                continue; // Already connected
            }

            // Add this hyperlane
            galaxy.addHyperlane(systems.get(fromIdx).getId(), systems.get(toIdx).getId(), dist);
            connected[toIdx] = true;
            connectedCount++;

            // Add edges from the newly connected system to all unconnected systems
            for (int j = 0; j < n; j++) {
                if (!connected[j]) {
                    int newDist = Math.abs(systems.get(toIdx).getId() - systems.get(j).getId());
                    pq.offer(new int[]{newDist, toIdx, j});
                }
            }
        }

        // Add additional hyperlanes based on density
        int extraConnections = (int) (n * hyperlaneDensity);
        for (int i = 0; i < extraConnections; i++) {
            int fromIdx = random.nextInt(n);
            int toIdx;
            do {
                toIdx = random.nextInt(n);
            } while (toIdx == fromIdx);

            int distance = Math.abs(systems.get(fromIdx).getId() - systems.get(toIdx).getId());
            galaxy.addHyperlane(systems.get(fromIdx).getId(), systems.get(toIdx).getId(), distance);
        }
    }
}
