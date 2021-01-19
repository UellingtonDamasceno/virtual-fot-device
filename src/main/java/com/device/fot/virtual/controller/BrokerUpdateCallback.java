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
    private boolean timeout;

    public BrokerUpdateCallback(FoTDevice device) {
        this.device = device;
    }

    public void startUpdateBroker(BrokerSettings brokerSettings, double timeout) {
        MqttClient newClient = null;
        if (!this.device.isUpdating()) {
            try {
                this.device.setIsUpdating(true);
                newClient = brokerSettings.getClient();

                MqttConnectOptions newOptions = brokerSettings.getConnectionOptions();

                newClient.setCallback(this);
                newClient.connect(newOptions);
                String connectionTopic = ExtendedTATUWrapper.getConnectionTopic();
                String message = ExtendedTATUWrapper.buildConnectMessage(device, timeout);
                newClient.subscribe(ExtendedTATUWrapper.getConnectionTopicResponse());
                newClient.publish(connectionTopic, new MqttMessage(message.getBytes()));
                this.brokerSettings = brokerSettings;

                this.timeoutCounter = new Thread(this);
                this.timeoutCounter.setName("BROKER/UPDATE/TIMEOUT");
                this.timeoutCounter.start();

            } catch (MqttException ex) {
                brokerSettings.disconnectClient();
                device.setIsUpdating(false);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        if (!this.timeout) {
            String message = new String(mqttMessage.getPayload());
            TATUMessage tatuMessage = new TATUMessage(message);
            if (tatuMessage.isResponse() && tatuMessage.getMethod().equals(ExtendedTATUMethods.CONNACK)) {
                this.timeoutCounter.interrupt();
                JSONObject json = new JSONObject(tatuMessage.getMessageContent());
                if (json.getJSONObject("BODY").getBoolean("CAN_CONNECT")) {
                    this.device.updateBrokerSettings(brokerSettings);
                } else {
                    this.brokerSettings.disconnectClient();
                }
            }
        } else {
            this.device.setIsUpdating(false);
            this.brokerSettings.disconnectClient();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void run() {
        try {
            this.timeout = false;
            Thread.sleep(10000L);
            this.timeout = true;
            if (this.device.isUpdating()) {
                this.device.setIsUpdating(false);
                this.brokerSettings.disconnectClient();
            }
        } catch (InterruptedException ex) {
        }
    }

}
