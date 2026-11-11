package com.yisy.modules.doraneuron.protocol.mesh;

import java.io.Serializable;
import java.util.Map;

/**
 * Message for AI task distribution in the decentralized neural network.
 * 
 * Sent by the Base Node to distribute AI inference tasks to connected neurons.
 */
public class AiTaskMessage extends MeshMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;
    private String taskType;
    private Map<String, Object> input;
    private long timeoutMs;

    public AiTaskMessage() {
        super("AI_TASK", null);
    }

    public AiTaskMessage(String taskId, String taskType, Map<String, Object> input, long timeoutMs) {
        super("AI_TASK", null);
        this.taskId = taskId;
        this.taskType = taskType;
        this.input = input;
        this.timeoutMs = timeoutMs;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
