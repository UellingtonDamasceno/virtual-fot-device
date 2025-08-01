package com.device.fot.virtual.model;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author Uellington Damasceno
 */
public class LatencyTrackingMqttClient extends MqttClient {

    public LatencyTrackingMqttClient(String serverURI, String deviceId) throws MqttException {
        super(serverURI, deviceId, new MemoryPersistence());
    }

    public void publishAndTrack(String topic, String sensorId, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(1);
        FlightMessageInfo messageInfo = new FlightMessageInfo(sensorId, message);

        this.aClient.publish(topic, mqttMessage, messageInfo, null);
    }

}
