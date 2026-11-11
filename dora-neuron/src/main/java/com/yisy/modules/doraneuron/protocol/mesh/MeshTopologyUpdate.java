package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;
import java.util.List;

/**
 * Topology update message for P2P mesh network.
 *
 * This message carries the current view of the mesh topology from one
 * neuron to its peers. It includes the list of known peers and their
 * status, allowing each neuron to maintain an up-to-date view of the
 * network topology.
 */
public class MeshTopologyUpdate extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** List of known peers in the mesh. */
    private List<MeshPeerInfo> peers;

    /** Total number of neurons in the mesh (including unknown). */
    private int totalNeurons;

    /** Sequence number for ordering topology updates. */
    private long sequenceNumber;

    /** Flag indicating if this is a request for topology from peers. */
    private boolean isRequest;

    public MeshTopologyUpdate() {
        super("MESH_TOPOLOGY_UPDATE", null);
    }

    public MeshTopologyUpdate(String fromNodeId, List<MeshPeerInfo> peers,
                               int totalNeurons, long sequenceNumber) {
        super("MESH_TOPOLOGY_UPDATE", fromNodeId);
        this.peers = peers;
        this.totalNeurons = totalNeurons;
        this.sequenceNumber = sequenceNumber;
        this.isRequest = false;
    }

    /**
     * Create a request for topology information from peers.
     */
    public static MeshTopologyUpdate createRequest(String fromNodeId) {
        MeshTopologyUpdate update = new MeshTopologyUpdate();
        update.setType("MESH_TOPOLOGY_UPDATE");
        update.setFromNodeId(fromNodeId);
        update.setIsRequest(true);
        update.setMaxHops(2);
        return update;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public List<MeshPeerInfo> getPeers() {
        return peers;
    }

    public void setPeers(List<MeshPeerInfo> peers) {
        this.peers = peers;
    }

    public int getTotalNeurons() {
        return totalNeurons;
    }

    public void setTotalNeurons(int totalNeurons) {
        this.totalNeurons = totalNeurons;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isIsRequest() {
        return isRequest;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    @Override
    public String toString() {
        return "MeshTopologyUpdate{peers=" + (peers != null ? peers.size() : 0) +
                ", totalNeurons=" + totalNeurons + ", seq=" + sequenceNumber +
                ", isRequest=" + isRequest + ", " + super.toString() + '}';
    }
}
