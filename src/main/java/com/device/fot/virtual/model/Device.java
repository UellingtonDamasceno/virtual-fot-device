package com.device.fot.virtual.model;

import com.device.fot.virtual.util.TATUWrapper;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Uellington Damasceno
 */
public class Device {

    private String name;
    private BrokerSettings brokerSettings;
    private Map<String, Sensor> sensors;

    private MqttClient publisher, subscriber;
    private boolean updating;
    private MqttCallback midlleware;

    public Device(String name, Map<String, Sensor> sensors) {
        this.name = name;
        this.sensors = sensors;
        this.updating = false;
    }

    public String getName() {
        return this.name;
    }

    public Sensor getSensorByName(String name) {
        return this.sensors.getOrDefault(name, new NullSensor(this.name));
    }

    public void startFlow() {
        this.sensors.values()
                .stream()
                .filter(Sensor::isFlow)
                .forEach(Sensor::startFlow);
    }

    public void pauseFlow() {
        this.sensors.values()
                .stream()
                .filter(Sensor::isFlow)
                .forEach(Sensor::pauseFlow);
    }

    public void stopFlow() {
        this.sensors.values()
                .stream()
                .filter(Sensor::isRunnging)
                .forEach(Sensor::stopFlow);
    }

    public boolean isUpdating() {
        return this.updating;
    }

    public void setIsUpdating(boolean updating) {
        this.updating = updating;
    }

    public void connect(BrokerSettings brokerSettings) throws MqttException {

        this.subscriber = brokerSettings.getSubscriber();
        this.publisher = brokerSettings.getPublisher();

        MqttConnectOptions options = brokerSettings.getConnectionOptions();
        this.midlleware = (midlleware == null) ? midlleware = new Middleware(this) : midlleware;

        this.subscriber.setCallback(midlleware);
        this.publisher.setCallback(midlleware);

        if (!this.subscriber.isConnected()) {
            this.subscriber.connect(options);
        }
        if (!this.publisher.isConnected()) {
            this.publisher.connect(options);
        }

        this.subscriber.subscribe(TATUWrapper.buildTATUTopic(this.name), 1);
        this.sensors.values().forEach(sensor -> sensor.setPublisher(publisher));

        this.brokerSettings = brokerSettings;
    }

    public void updateBrokerSettings(BrokerSettings newBrokerSettings) throws MqttException {
        BrokerSettings oldBrokerSettings = this.brokerSettings;
        this.pauseFlow();
        this.brokerSettings.disconnectAllClients();
        try {
            this.connect(newBrokerSettings);
        } catch (MqttException ex) {
            this.connect(oldBrokerSettings);
        }
        this.startFlow();
        this.updating = false;
    }

    public void publish(String topic, MqttMessage message) throws MqttException {
        this.publisher.publish(topic, message);
    }
}
