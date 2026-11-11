package com.yisy.modules.doramcu.neuron.dto;

/**
 * Model version comparison for a single region.
 */
public class ModelVersionDto {

    /** Region identifier. */
    private String region;

    /** Model version deployed in this region. */
    private long modelVersion;

    /** Whether this is the primary coordinator's region. */
    private boolean isPrimary;

    /** Whether this region's model version matches the primary. */
    private boolean inSync;

    /** Version drift from primary (0 = in sync, positive = ahead, negative = behind). */
    private long versionDrift;

    public ModelVersionDto() {
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public long getModelVersion() { return modelVersion; }
    public void setModelVersion(long modelVersion) { this.modelVersion = modelVersion; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public boolean isInSync() { return inSync; }
    public void setInSync(boolean inSync) { this.inSync = inSync; }

    public long getVersionDrift() { return versionDrift; }
    public void setVersionDrift(long versionDrift) { this.versionDrift = versionDrift; }
}
