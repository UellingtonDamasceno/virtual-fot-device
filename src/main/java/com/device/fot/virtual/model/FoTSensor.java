package com.device.fot.virtual.model;

import com.device.fot.virtual.controller.DataController;
import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.TATUWrapper;
import static extended.tatu.wrapper.util.TATUWrapper.buildFlowMessageResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Uellington Damasceno
 */
public class FoTSensor extends Sensor implements Runnable {

    private String deviceId;
    private boolean flow, running;

    private Thread thread;
    private MqttClient publisher;

    public FoTSensor(String deviceId, Sensor sensor) {
        this(deviceId,
                sensor.getId(),
                sensor.getType(),
                sensor.getCollectionTime(),
                sensor.getPublishingTime());
    }

    public FoTSensor(String deviceId,
            String sensorName,
            String type,
            int publishingTime,
            int collectionTime) {
        super(sensorName, type, collectionTime, publishingTime);
        this.deviceId = deviceId;
        this.flow = false;
        this.running = false;
    }

    public String deviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setPublisher(MqttClient publisher) {
        this.publisher = publisher;
    }

    public boolean isFlow() {
        return this.flow;
    }

    @Override
    public void setPublishingTime(int publishingTime) {
        if (publishingTime >= 1) {
            this.publishingTime = publishingTime;
        } else {
            this.stopFlow();
        }
    }

    @Override
    public void setCollectionTime(int collectionTime) {
        if (collectionTime >= 1) {
            this.collectionTime = collectionTime;
        } else {
            this.stopFlow();
        }
    }

    public void startFlow() {
        this.startFlow(this.collectionTime, this.publishingTime);
    }

    public void startFlow(int newFlowCollect, int newFlowPublish) {
        if (newFlowCollect >= 1 && newFlowPublish >= 1) {
            this.collectionTime = newFlowCollect;
            this.publishingTime = newFlowPublish;
            if (thread == null || !thread.isAlive()) {
                this.thread = new Thread(this);
                this.thread.setName("FLOW/" + deviceId + "/" + id);
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

    public Integer getCurrentValue() {
        return new Random().nextInt(11) + 30;
    }

    private Data<Integer> getDataFlow() throws InterruptedException {
        Random drawer = new Random();
        int tempPublish = this.publishingTime;
        List<Integer> values = new LinkedList();
        while (tempPublish >= 0) {
            values.add(drawer.nextInt(11) + 30);
            tempPublish -= this.collectionTime;
            Thread.sleep(this.collectionTime);
        }
        return new Data(this.deviceId, this.id, values);
    }

    @Override
    public void run() {
        String msg;
        String topic = TATUWrapper.buildTATUResponseTopic(deviceId);
        this.flow = true;
        this.running = true;
        while (thread.isAlive() && this.running && this.flow) {
            try {
                Data data = this.getDataFlow();
                msg = buildFlowMessageResponse(deviceId, id, publishingTime, collectionTime, data.getValues().toArray());
                this.publisher.publish(topic, new MqttMessage(msg.getBytes()));
                DataController.put(data);
            } catch (InterruptedException | MqttException ex) {
                this.running = false;
            }
        }
        this.running = false;
    }
}
