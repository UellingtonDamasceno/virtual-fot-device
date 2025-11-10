package com.device.fot.virtual.model;

import com.device.fot.virtual.controller.configs.ExperimentConfig;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Uellington Damasceno
 */
public class LatencyRecord {

    private String deviceID;

    private Integer experiment;
    private int type;
    private int level;
    private Long latency;
    private String sensorId;
    private String brokerIp;
    private String message;
    
    public LatencyRecord(){
    }
    
    public LatencyRecord(String deviceID,
            String sensorId,
            String brokerIp,
            ExperimentConfig config,
            Long latency,
            String message) {

        this.deviceID = deviceID;
        this.sensorId = sensorId;
        this.brokerIp = brokerIp;
        this.experiment = config.getExpNum();
        this.type = config.getExpType();
        this.level = config.getExpLevel();
        this.latency = latency;
        this.message = message;
    }

    public static LatencyRecord of(String deviceID,
            String sensorId,
            String brokerIp,
            ExperimentConfig config,
            Long latency,
            String message) {
        return new LatencyRecord(deviceID, sensorId, brokerIp, config, latency, message);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExperiment() {
        return experiment;
    }

    public void setExperiment(Integer experiment) {
        this.experiment = experiment;
    }

    public Long getLatency() {
        return latency;
    }

    public void setLatency(Long latency) {
        this.latency = latency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    @Override
    public String toString() {
        long nanos = (this.latency != null) ? this.latency : 0;

        long minutes = TimeUnit.NANOSECONDS.toMinutes(nanos);
        nanos -= TimeUnit.MINUTES.toNanos(minutes);

        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos);
        nanos -= TimeUnit.SECONDS.toNanos(seconds);

        long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
        nanos -= TimeUnit.MILLISECONDS.toNanos(millis);

        String formattedLatency = String.format(
                "%d min, %d s, %d ms, %d ns",
                minutes, seconds, millis, nanos
        );

        return formattedLatency;
    }

    public void reset() {
        this.deviceID = null;
        this.sensorId = null;
        this.brokerIp = null;
        this.experiment = null;
        this.type = 0; 
        this.level = 0; 
        this.latency = null;
        this.message = null;
    }

}
