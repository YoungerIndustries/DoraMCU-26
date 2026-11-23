package com.yisy.modules.doramcu.gamecore.module;

/**
 * Health status of a dynamically loaded module.
 */
public enum ModuleHealthStatus {

    /** Module is running normally. */
    HEALTHY,

    /** Module is currently being loaded. */
    LOADING,

    /** Module is currently being reloaded. */
    RELOADING,

    /** Module context has crashed or is unresponsive. */
    CRASHED,

    /** Module is disabled by administrator. */
    DISABLED,

    /** Module has been unloaded. */
    UNLOADED
}
