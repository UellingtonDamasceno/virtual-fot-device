package com.device.fot.virtual.model;

import com.device.fot.virtual.util.TATUWrapper;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author Uellington Damasceno
 */
public class Sensor implements Runnable {

    private final String sensorName;
    private String deviceName;
    private int flowPublish, flowCollect;
    private boolean flow, running;

    public Sensor(String deviceName, String sensorName) {
        this.sensorName = sensorName;
        this.deviceName = deviceName;
        this.flow = false;
        this.running = false;
    }

    public String getSensorName() {
        return this.sensorName;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isFlow() {
        return this.flow;
    }

    public void setFlow(boolean flow) {
        this.flow = flow;
    }

    public void setFlowPublish(int flowPublish) {
        this.flowPublish = flowPublish;
    }

    public void setFlowCollect(int flowCollect) {
        this.flowCollect = flowCollect;
    }

    public Double getCurrentValue() {
        return new Random().nextDouble();
    }

    private Double[] getDataFlow() {
        return new Random()
                .doubles(flowCollect, 0, 100)
                .boxed()
                .collect(Collectors.toList())
                .toArray(new Double[flowCollect]);
    }

    @Override
    public void run() {
        String msg;
        msg = TATUWrapper.buildFlowMessageResponse(deviceName, sensorName, flowPublish, flowCollect, this.getDataFlow());
    }
}
