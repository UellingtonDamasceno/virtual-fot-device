package com.device.fot.virtual.model;

import java.util.List;
import java.util.stream.Collectors;

public class Data<T> {

    private final long timestamp;
    private final String deviceId;
    private final String sensorId;
    private final List<T> values;

    public Data(String deviceId, String sensorId, List<T> values, long timestamp) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.sensorId = sensorId;
        this.values = values;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public String toString() {
        String allValues = this.values
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        
        return new StringBuilder().append(timestamp).append(",")
                .append(deviceId).append(",")
                .append(sensorId).append(",")
                .append(allValues)
                .toString();

    }
}
