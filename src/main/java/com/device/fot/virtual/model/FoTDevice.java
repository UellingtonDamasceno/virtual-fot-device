package com.device.fot.virtual.model;

import com.device.fot.virtual.controller.DefaultFlowCallback;
import com.device.fot.virtual.controller.configs.ExperimentConfig;
import extended.tatu.wrapper.model.Device;
import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.TATUWrapper;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Uellington Damasceno
 */
public class FoTDevice extends Device {

    private BrokerSettings brokerSettings;
    private LatencyTrackingMqttClient client;
    private boolean updating;
    private MqttCallback callback;
    private ExperimentConfig config;

    public FoTDevice(String name, List<Sensor> sensors, ExperimentConfig config) {
        super(name, new Random().nextDouble(), new Random().nextDouble(), sensors);
        this.updating = false;
        this.config = config;
    }

    public void restartFlow() {
        this.applayActionAtSensor(sensor -> true, FoTSensor::restartFlow);
    }

    public void stopFlow() {
        this.applayActionAtSensor(FoTSensor::isRunning, FoTSensor::stopFlow);
    }

    public boolean isUpdating() {
        return this.updating;
    }

    public void setIsUpdating(boolean updating) {
        this.updating = updating;
    }

    public void connect(BrokerSettings brokerSettings) throws MqttException {
        String brokerIp = brokerSettings.getUrl();

        this.callback = (callback == null) ? callback = new DefaultFlowCallback(this, brokerIp, config) : callback;

        this.client = brokerSettings.getClient();

        MqttConnectOptions options = brokerSettings.getConnectionOptions();

        this.client.setCallback(callback);
        if (!this.client.isConnected()) {
            this.client.connect(options);
        }

        this.client.subscribe(TATUWrapper.buildTATUTopic(id), 2);
        Consumer<FoTSensor> definePublish = s -> s.setPublisher(client);
        Predicate<FoTSensor> allSensors = s -> true;

        this.applayActionAtSensor(allSensors, definePublish);

        if (this.brokerSettings != null) {
            this.brokerSettings.disconnectClient();
        }
        this.brokerSettings = brokerSettings;
    }

    public void updateBrokerSettings(BrokerSettings newBrokerSettings) throws MqttException {
        BrokerSettings oldBrokerSettings = this.brokerSettings;
        this.stopFlow();
        try {
            this.connect(newBrokerSettings);
        } catch (MqttException ex) {
            if (oldBrokerSettings == null) {
                oldBrokerSettings = newBrokerSettings;
            }
            this.connect(oldBrokerSettings);
        }
        this.restartFlow();
        this.updating = false;
    }

    public void publish(String topic, MqttMessage message) throws MqttException {
        this.client.publish(topic, message);
    }

    private void applayActionAtSensor(Predicate<FoTSensor> filter, Consumer<FoTSensor> action) {
        this.getSensors()
                .stream()
                .filter(FoTSensor.class::isInstance)
                .map(FoTSensor.class::cast)
                .filter(filter)
                .forEach(action);
    }

}
