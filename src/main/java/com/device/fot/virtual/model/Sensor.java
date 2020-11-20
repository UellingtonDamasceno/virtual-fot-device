package com.device.fot.virtual.model;

import com.device.fot.virtual.util.TATUWrapper;
import java.util.Random;
import java.util.stream.Collectors;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Uellington Damasceno
 */
public class Sensor implements Runnable {

    private final String sensorName;
    private String deviceName;
    private int flowPublish, flowCollect;
    private boolean flow, running;

    private Thread thread;
    private MqttClient publisher;

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

    public void setPublisher(MqttClient publisher) {
        this.publisher = publisher;
    }

    public boolean isFlow() {
        return this.flow;
    }

    public void setFlowPublish(int flowPublish) {
        if (flowPublish >= 1) {
            this.flowPublish = flowPublish;
        } else {
            this.stopFlow();
        }
    }

    public void setFlowCollect(int flowCollect) {
        if (flowCollect >= 1) {
            this.flowCollect = flowCollect;
        } else {
            this.stopFlow();
        }
    }

    public void startFlow() {
        this.startFlow(flowCollect, flowPublish);
    }
    
    public void startFlow(int newFlowCollect, int newFlowPublish) {
        if (newFlowCollect >= 1 && newFlowPublish >= 1) {
            this.flowCollect = newFlowCollect;
            this.flowPublish = newFlowPublish;
            if (thread == null || !thread.isAlive()) {
                this.thread = new Thread(this);
                this.thread.setName("FLOW/" + deviceName + "/" + sensorName);
                this.thread.start();
            }
        } else {
            if (this.running && this.flow) {
                this.stopFlow();
            }
        }
    }

    public void pauseFlow() {
        if (this.thread.isAlive() && this.running) {
            this.running = false;
            this.thread.interrupt();
        }
    }

    public void stopFlow() {
        if (this.thread != null && this.running) {
            this.running = false;
            this.flow = false;
            this.thread.interrupt();
        }
    }

    public boolean isRunnging() {
        return this.running;
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
        String topic = TATUWrapper.buildTATUResponseTopic(deviceName);
        this.flow = true;
        this.running = true;
        while (thread.isAlive() && this.running && this.flow) {
            try {
                msg = TATUWrapper.buildFlowMessageResponse(deviceName, sensorName, flowPublish, flowCollect, this.getDataFlow());
                this.publisher.publish(topic, new MqttMessage(msg.getBytes()));
                Thread.sleep(flowPublish);
            } catch (InterruptedException | MqttException ex) {
                this.running = false;
            }
        }
        this.running = false;
    }
}
