package com.device.fot.virtual.controller;

import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.FoTDevice;
import extended.tatu.wrapper.enums.ExtendedTATUMethods;
import extended.tatu.wrapper.model.TATUMessage;
import extended.tatu.wrapper.util.ExtendedTATUWrapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 *
 * @author Uellington Damasceno
 */
public class BrokerUpdateCallback implements MqttCallback, Runnable {

    private FoTDevice device;
    private BrokerSettings brokerSettings;
    private Thread timeoutCounter;

    public BrokerUpdateCallback(FoTDevice device) {
        this.device = device;
    }

    public void startUpdateBroker(BrokerSettings brokerSettings, double timeout, boolean retryConnect) {
        if (this.device.isUpdating()) {
            return;
        }

        this.device.setIsUpdating(true);
        MqttConnectOptions newOptions = brokerSettings.getConnectionOptions();
        String connectionTopic = ExtendedTATUWrapper.getConnectionTopic();
        String message = ExtendedTATUWrapper.buildConnectMessage(device, timeout);
        this.timeoutCounter = new Thread(this);
        this.timeoutCounter.setName("BROKER/UPDATE/TIMEOUT");

        try {
            MqttClient newClient = brokerSettings.getClient();

            newClient.setCallback(this);

            this.tryConnect(newClient, newOptions, retryConnect);

            newClient.subscribe(ExtendedTATUWrapper.getConnectionTopicResponse());
            newClient.publish(connectionTopic, new MqttMessage(message.getBytes()));

            this.brokerSettings = brokerSettings;

            this.timeoutCounter.start();

        } catch (MqttException ex) {
            brokerSettings.disconnectClient();
            device.setIsUpdating(false);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        TATUMessage tatuMessage = new TATUMessage(message);
        if (tatuMessage.isResponse() && tatuMessage.getMethod().equals(ExtendedTATUMethods.CONNACK)) {
            this.timeoutCounter.interrupt();
            var json = new JSONObject(tatuMessage.getMessageContent());
            if (json.getJSONObject("BODY").getBoolean("CAN_CONNECT")) {
                this.device.updateBrokerSettings(brokerSettings);
                this.brokerSettings.getClient().unsubscribe(ExtendedTATUWrapper.getConnectionTopicResponse());
            } else {
                this.brokerSettings.disconnectClient();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000L);
            if (this.device.isUpdating()) {
                this.device.setIsUpdating(false);
                this.brokerSettings.disconnectClient();
            }
        } catch (InterruptedException ex) {
        }
    }

    public void tryConnect(MqttClient client, MqttConnectOptions options, boolean retryConnect) {
        boolean connected = false;
        do {
            try {
                client.connect(options);
                connected = true;
            } catch (MqttException e) {
            }
        } while (!connected && retryConnect);
    }
}
