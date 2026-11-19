package com.yisy.modules.doramcu.dto;

/**
 * DTO for node registration requests from devices joining the brain network.
 */
public class NodeRegistrationRequest {

    private String nodeType;
    private String deviceId;
    private DeviceCapabilities capabilities;
    private NetworkInfo networkInfo;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(DeviceCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }

    public static class DeviceCapabilities {
        private int cpuCores;
        private int ramMb;
        private String gpuTier;

        public int getCpuCores() {
            return cpuCores;
        }

        public void setCpuCores(int cpuCores) {
            this.cpuCores = cpuCores;
        }

        public int getRamMb() {
            return ramMb;
        }

        public void setRamMb(int ramMb) {
            this.ramMb = ramMb;
        }

        public String getGpuTier() {
            return gpuTier;
        }

        public void setGpuTier(String gpuTier) {
            this.gpuTier = gpuTier;
        }
    }

    public static class NetworkInfo {
        private String ipAddress;
        private int port;

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
