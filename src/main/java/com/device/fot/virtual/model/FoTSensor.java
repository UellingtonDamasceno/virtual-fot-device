package com.device.fot.virtual.model;

import com.device.fot.virtual.enums.SensorType;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttException;

import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.TATUWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class FoTSensor extends Sensor implements Runnable {

    private static final Logger logger = Logger.getLogger(FoTSensor.class.getName());

    private final Integer initialJitterDelay;
    private String deviceId, publishTopic;

    private Thread thread;
    private LatencyTrackingMqttClient publisher;

    private String flowThreadName;
    private Random random;

    private int lastValue;

    private volatile boolean running, flow;
    private final ArrayList<Integer> valuesBuffer;

    private FoTSensor(String deviceId, String sensorId, SensorType sensorType, Integer initialJitterDelay) {
        this(deviceId, sensorId, sensorType, 1000, 100, initialJitterDelay);
    }

    public FoTSensor(String deviceId,
            String sensorId,
            SensorType type,
            int publishingTime,
            int collectionTime,
            Integer initialJitterDelay) {

        super(sensorId, type.getShortName(),
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
        int capacity = (publishingTime > 0 && collectionTime > 0) ? (publishingTime / collectionTime) + 2 : 16;
        this.valuesBuffer = new ArrayList<>(capacity);
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
        return this.flow && !this.running;
    }

    @Override
    public void setPublishingTime(int publishingTime) {
        if (publishingTime >= 1) {
            this.publishingTime = publishingTime;
            this.ensureBufferCapacity();
            return;
        }
        this.stopFlow();
    }

    @Override
    public void setCollectionTime(int collectionTime) {
        if (collectionTime >= 1) {
            this.collectionTime = collectionTime;
            this.ensureBufferCapacity();
            return;
        }
        this.stopFlow();
    }

    public void restartFlow() {
        this.startFlow(this.collectionTime, this.publishingTime);
    }

    public synchronized void startFlow(int newFlowCollect, int newFlowPublish) {
        if (newFlowCollect < 1 || newFlowPublish < 1) {
            logger.log(Level.WARNING, "Invalid collection/publishing time. Stopping flow for sensor {0}", getId());
            this.stopFlow();
            return;
        }

        this.collectionTime = newFlowCollect;
        this.publishingTime = newFlowPublish;

        this.flow = true;

        if (thread == null || !thread.isAlive()) {
            logger.log(Level.INFO, "Starting flow for sensor {0}...", getId());
            this.ensureBufferCapacity();
            this.thread = Thread.ofVirtual().name(flowThreadName).start(this);
        } else {
            logger.log(Level.INFO, "Flow for sensor {0} is already running. Parameters updated.", getId());
        }
    }

    public synchronized void stopFlow() {
        logger.log(Level.INFO, "Stopping flow for sensor {0}...", getId());
        this.flow = false;

        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    private List<Integer> getDataFlow() throws InterruptedException {
        this.valuesBuffer.clear();
        int tempPublish = this.publishingTime;
        while (tempPublish >= 0) {
            this.valuesBuffer.add(this.getCurrentValue());
            tempPublish -= this.collectionTime;
            Thread.sleep(this.collectionTime);
        }
        return this.valuesBuffer;
    }

    public Integer getCurrentValue() {
        Integer variation = delta * (random.nextBoolean() ? 1 : -1);
        this.lastValue = Math.min(maxValue, Math.max(minValue, lastValue + variation));
        return lastValue;
    }

    private void ensureBufferCapacity() {
        if (this.collectionTime > 0) {
            int requiredCapacity = (this.publishingTime / this.collectionTime) + 2;
            this.valuesBuffer.ensureCapacity(requiredCapacity);
        }
    }

    @Override
    public void run() {
        this.running = true;
        try {
            Thread.sleep(random.nextInt(this.initialJitterDelay));
            while (this.flow) {
                try {
                    List<Integer> data = this.getDataFlow();
                    if (publisher != null && this.flow) {
                        String msg = TATUWrapper.buildFlowMessageResponse(deviceId, id, publishingTime, collectionTime, data.toArray());
                        publisher.publishAndTrack(this.publishTopic, this.id, msg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (MqttException ex) {
                    logger.log(Level.SEVERE, "MQTT Exception in sensor {0}: {1}", new Object[]{getId(), ex.getMessage()});
                    this.flow = false;
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            logger.log(Level.INFO, "Sensor {0} has stopped.", getId());
            this.running = false;
        }
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
        String sensorId = type.getShortName() + sensorIdSufix;
        return new FoTSensor(deviceId, sensorId, type, initialDelay);
    }
}
