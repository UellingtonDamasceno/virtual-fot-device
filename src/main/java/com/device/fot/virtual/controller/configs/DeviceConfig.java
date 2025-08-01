package com.device.fot.virtual.controller.configs;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class DeviceConfig {

    private final String deviceId;
    private final String brokerIp;
    private final String port;
    private final String username;
    private final String password;

    private final String expNum;
    private final Integer sensorNumber;
    private final Integer jitterWindow;

    private static final Logger logger = Logger.getLogger(DeviceConfig.class.getName());

    public DeviceConfig(String deviceId, String brokerIp, String port, String username, String password, String expNum, String sensorNumber, String jitterWindow) {
        this.deviceId = deviceId == null ? UUID.randomUUID().toString() : deviceId;
        this.brokerIp = brokerIp == null ? "localhost" : brokerIp;
        this.port = port == null ? "1883" : port;
        this.username = username == null ? "karaf" : username;
        this.password = password == null ? "karaf" : password;
        this.expNum = expNum == null ? "0" : expNum;
        this.sensorNumber = sensorNumber == null ? 10 : Integer.valueOf(sensorNumber);
        this.jitterWindow = jitterWindow == null ? this.sensorNumber * 5 : Integer.valueOf(jitterWindow);
    }

    public static DeviceConfig load() {
        String deviceId = System.getenv("DEVICE_ID");
        String brokerIp = System.getenv("BROKER_IP");
        String port = System.getenv("PORT");
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        String expNum = System.getenv("EXP_NUM");
        String sensorNumber = System.getenv("SENSOR_NUMBER");
        String jitterWindow = System.getenv("JITTER_WINDOW");

        logger.log(Level.INFO, "Loaded configuration: DeviceId={0}, BrokerIp={1}, Port={2}, Username={3}, SensorNumber={4} JitterWindow={5}",
                new Object[]{
                    deviceId != null ? deviceId : "Default",
                    brokerIp != null ? brokerIp : "Default",
                    port != null ? port : "Default",
                    username != null ? username : "Default",
                    sensorNumber != null ? sensorNumber : "Default",
                    jitterWindow != null ? jitterWindow : "Default"});

        return new DeviceConfig(deviceId, brokerIp, port, username, password, expNum, sensorNumber, jitterWindow);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getExpNum() {
        return expNum;
    }

    public Integer getSensorNumber() {
        return sensorNumber;
    }

    public Integer getJitterWindowMultiplier() {
        return this.jitterWindow;
    }

}
