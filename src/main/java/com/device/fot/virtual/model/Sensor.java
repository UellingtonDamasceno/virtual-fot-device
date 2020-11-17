package com.device.fot.virtual.model;

import com.device.fot.virtual.util.TATUWrapper;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        this.thread = new Thread(this);
        this.thread.setName("FLOW/" + deviceName + "/" + sensorName);
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
        this.flowPublish = flowPublish;
    }

    public void setFlowCollect(int flowCollect) {
        this.flowCollect = flowCollect;
    }

    public void startFlow() {
        if (!this.thread.isAlive()) {
            this.thread.start();
        }
    }

    public void pauseFlow() {
        this.running = false;
    }

    public void stopFlow() {
        this.running = false;
        this.flow = false;
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
        String topic = TATUWrapper.buildTATUResponseTopic(deviceName, sensorName);
        this.flow = true;
        this.running = true;
        
        while (this.running && this.flow) {
            try {
                msg = TATUWrapper.buildFlowMessageResponse(deviceName, sensorName, flowPublish, flowCollect, this.getDataFlow());
                this.publisher.publish(topic, new MqttMessage(msg.getBytes()));
                Thread.sleep(flowPublish);
            } catch (InterruptedException | MqttException ex) {
                this.running = false;
                Logger.getLogger(Sensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
