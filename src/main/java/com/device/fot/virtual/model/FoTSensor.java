package com.device.fot.virtual.model;

import com.device.fot.virtual.enums.SensorType;
import java.util.LinkedList;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttException;

import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.TATUWrapper;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class FoTSensor extends Sensor implements Runnable {

    private static final Logger logger = Logger.getLogger(LatencyTrackingMqttClient.class.getName());

    private final Integer initialJitterDelay;
    private String deviceId, publishTopic;
    private boolean flow;

    private Thread thread;
    private LatencyTrackingMqttClient publisher;

    private String flowThreadName;
    private Random random;

    private int lastValue;

    private volatile boolean running;

    private FoTSensor(String deviceId, String sensorId, SensorType sensorType, Integer initialJitterDelay) {
        this(deviceId, sensorId, sensorType, 1000, 100, initialJitterDelay);
    }

    public FoTSensor(String deviceId,
            String sensorId,
            SensorType type,
            int publishingTime,
            int collectionTime,
            Integer initialJitterDelay) {

        super(sensorId, type.getName(),
                collectionTime,
                publishingTime,
                type.getMinValue(),
                type.getMaxValue(),
                type.getDelta());

        this.deviceId = deviceId;
        this.publishTopic = TATUWrapper.buildTATUResponseTopic(deviceId);
        this.flow = false;
        this.running = false;
        this.random = new Random();
        this.lastValue = (minValue <= 0 && maxValue <= 0) ? 0 : (maxValue - minValue) + minValue;
        this.flowThreadName = this.buildFlowThreadName(deviceId, id);
        this.initialJitterDelay = initialJitterDelay;
    }

    public String deviceId() {
        return this.deviceId;
    }

    public void setPublisher(LatencyTrackingMqttClient publisher) {
        this.publisher = publisher;
    }

    public boolean isFlow() {
        return this.flow;
    }

    public boolean shouldRestartFlow() {
        return !this.running && this.flow;
    }

    @Override
    public void setPublishingTime(int publishingTime) {
        if (publishingTime >= 1) {
            this.publishingTime = publishingTime;
            return;
        }
        this.stopFlow();
    }

    @Override
    public void setCollectionTime(int collectionTime) {
        if (collectionTime >= 1) {
            this.collectionTime = collectionTime;
            return;
        }
        this.stopFlow();
    }

    public void restartFlow() {
        this.startFlow(this.collectionTime, this.publishingTime);
    }

    public void startFlow(int newFlowCollect, int newFlowPublish) {
        if (newFlowCollect >= 1 && newFlowPublish >= 1) {
            this.collectionTime = newFlowCollect;
            this.publishingTime = newFlowPublish;
            if (thread == null || !thread.isAlive()) {
                System.out.println("Sensor type: " + type + " starting flow.");
                this.thread = Thread.ofVirtual().start(this);
                this.thread.setName(flowThreadName);
            }
            this.flow = true;
            return;
        }
        if (this.running && this.flow) {
            this.stopFlow();
        }
    }

    public void pauseFlow() {
        if (this.thread.isAlive() && this.running) {
            this.running = false;
            this.thread.interrupt();
        }
    }

    public void stopFlow() {
        if (!running || this.thread == null) {
            return;
        }
        this.running = false;
        this.thread.interrupt();
    }

    public boolean isRunnging() {
        return this.running;
    }

    private Data<Integer> getDataFlow() throws InterruptedException {
        var values = new LinkedList<Integer>();
        int tempPublish = this.publishingTime;
        while (tempPublish >= 0) {
            values.add(this.getCurrentValue());
            tempPublish -= this.collectionTime;
            Thread.sleep(this.collectionTime);
        }
        return new Data<>(this.deviceId, this.id, values);
    }

    public Integer getCurrentValue() {
        Integer variation = delta * (random.nextBoolean() ? 1 : -1);
        this.lastValue = Math.min(maxValue, Math.max(minValue, lastValue + variation));
        return lastValue;
    }

    @Override
    public void run() {
        String msg;
        this.flow = true;
        this.running = true;

        try {
            Thread.sleep(random.nextInt(this.initialJitterDelay));
        } catch (InterruptedException ex) {
            System.getLogger(FoTSensor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        while (running) {
            try {
                Data<Integer> data = this.getDataFlow();
                if (publisher != null && running) {
                    msg = TATUWrapper.buildFlowMessageResponse(deviceId, id, publishingTime, collectionTime, data.getValues().toArray());
                    publisher.publishAndTrack(this.publishTopic, this.id, msg);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (MqttException ex) {
                System.err.println("MQTT Exception in sensor " + getId() + ": " + ex.getMessage());
                running = false;
            }
        }
        System.out.println("Sensor " + getId() + " has stopped.");
        this.running = false;
    }

    private String buildFlowThreadName(String deviceId, String id) {
        return new StringBuilder("FLOW/")
                .append(deviceId).append("/")
                .append(id).toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FoTSensor{");
        sb.append("deviceId:").append(deviceId);
        sb.append(", minValue:").append(minValue);
        sb.append(", maxValue:").append(maxValue);
        sb.append(", running:").append(running);
        sb.append('}');
        return sb.toString();
    }

    public static FoTSensor randomSensor(String deviceId, int sensorIdSufix, Integer initialDelay) {
        SensorType type = SensorType.getRandomSensor();
        String sensorId = type.getName() + "_" + sensorIdSufix;
        return new FoTSensor(deviceId, sensorId, type, initialDelay);
    }
}
