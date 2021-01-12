package com.device.fot.virtual.model;

import com.device.fot.virtual.controller.DefaultFlowCallback;
import extended.tatu.wrapper.model.Device;
import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.TATUWrapper;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Uellington Damasceno
 */
public class FoTDevice extends Device {

    private BrokerSettings brokerSettings;
    private MqttClient client;
    private boolean updating;
    private MqttCallback midlleware;

    public FoTDevice(String name, List<Sensor> sensors) {
        super(name, new Random().nextDouble(), new Random().nextDouble(), sensors);
        this.updating = false;
    }

    public void startFlow() {
        this.getFoTSensors()
                .stream()
                .filter(FoTSensor::isFlow)
                .forEach(FoTSensor::startFlow);
    }

    public void pauseFlow() {
        this.getFoTSensors()
                .stream()
                .filter(FoTSensor::isFlow)
                .forEach(FoTSensor::pauseFlow);
    }

    public void stopFlow() {
        this.getFoTSensors()
                .stream()
                .filter(FoTSensor::isRunnging)
                .forEach(FoTSensor::stopFlow);
    }

    public boolean isUpdating() {
        return this.updating;
    }

    public void setIsUpdating(boolean updating) {
        this.updating = updating;
    }

    public void connect(BrokerSettings brokerSettings) throws MqttException {

        this.client = brokerSettings.getClient();

        MqttConnectOptions options = brokerSettings.getConnectionOptions();
        this.midlleware = (midlleware == null) ? midlleware = new DefaultFlowCallback(this) : midlleware;

        this.client.setCallback(midlleware);

        if (!this.client.isConnected()) {
            this.client.connect(options);
        }

        this.client.subscribe(TATUWrapper.buildTATUTopic(this.getId()), 1);
        this.getFoTSensors().forEach(sensor -> sensor.setPublisher(client));

        this.brokerSettings = brokerSettings;
    }

    public void updateBrokerSettings(BrokerSettings newBrokerSettings) throws MqttException {
        BrokerSettings oldBrokerSettings = this.brokerSettings;
        this.pauseFlow();
        this.brokerSettings.disconnectClient();
        try {
            this.connect(newBrokerSettings);
        } catch (MqttException ex) {
            this.connect(oldBrokerSettings);
        }
        this.startFlow();
        this.updating = false;
    }

    public void publish(String topic, MqttMessage message) throws MqttException {
        this.client.publish(topic, message);
    }

    private List<FoTSensor> getFoTSensors() {
        return this.getSensors()
                .stream()
                .filter(FoTSensor.class::isInstance)
                .map(FoTSensor.class::cast)
                .collect(Collectors.toList());
    }

}
