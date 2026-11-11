package com.yisy.modules.doraneuron.model.multigame;

/**
 * Status of a registered game profile on this Neuron.
 */
public enum GameStatus {
    /** Game is active and serving AI requests. */
    ACTIVE,
    /** Game model is loading; not yet ready. */
    LOADING,
    /** Game is paused; AI inference disabled. */
    PAUSED,
    /** Game is removed; resources being cleaned up. */
    REMOVED
}
