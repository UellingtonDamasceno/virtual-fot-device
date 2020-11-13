package com.device.fot.virtual.model;

import java.util.Date;

public class Data {

    private Device device;
    private Sensor sensor;
    private String value;
    private Date startTime;
    private Date endTime;

    public Data(Device device, Sensor sensor, String value, Date startTime,
            Date endTime) {
        this.device = device;
        this.sensor = sensor;
        this.value = value;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
