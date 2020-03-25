package com.github.lutzenh.gpgpu;

import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

public class GraphicsDevice {
    private cl_device_id id;
    private cl_platform_id platform_id;
    private String name;
    private String openClVersion;
    private String vendor;
    private String driverVersion;
    private DeviceType type;
    private String platformName;
    private String platformVersion;

    public GraphicsDevice(cl_device_id id, cl_platform_id platform_id, String name, String openClVersion, String vendor, String driverVersion, DeviceType type, String platformName, String platformVersion) {
        this.id = id;
        this.platform_id = platform_id;
        this.name = name;
        this.openClVersion = openClVersion;
        this.vendor = vendor;
        this.driverVersion = driverVersion;
        this.type = type;
        this.platformName = platformName;
        this.platformVersion = platformVersion;
    }

    //region Getters and Setters

    public cl_device_id getId() {
        return id;
    }

    public cl_platform_id getPlatform_id() {
        return platform_id;
    }

    public String getName() {
        return name;
    }

    public String getOpenClVersion() {
        return openClVersion;
    }

    public String getVendor() {
        return vendor;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public DeviceType getType() {
        return type;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    //endregion
}
