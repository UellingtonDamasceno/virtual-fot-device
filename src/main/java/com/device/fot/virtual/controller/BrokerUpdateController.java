package com.device.fot.virtual.controller;

import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.Device;
import com.device.fot.virtual.tatu.TATUMessage;
import com.device.fot.virtual.tatu.TATUMethods;
import com.device.fot.virtual.util.ExtendedTATUWrapper;
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
public class BrokerUpdateController implements MqttCallback, Runnable {

    private Device device;
    private BrokerSettings brokerSettings;
    private Thread timeOutCounter;
    private boolean timeOut;

    public BrokerUpdateController(Device device) {
        this.device = device;
    }

    public void startUpdateBroker(BrokerSettings brokerSettings) {
        MqttClient newSubscriber = null;
        MqttClient newPublisher = null;
        if (!this.device.isUpdating()) {
            try {
                this.device.setIsUpdating(true);
                newSubscriber = brokerSettings.getSubscriber();
                newPublisher = brokerSettings.getPublisher();

                MqttConnectOptions newOptions = brokerSettings.getConnectionOptions();

                newSubscriber.setCallback(this);
                newPublisher.setCallback(this);

                newSubscriber.connect(newOptions);
                newPublisher.connect(newOptions);

                String connectionTopic = ExtendedTATUWrapper.getConnectionTopic();
                String message = ExtendedTATUWrapper.buildConnectMessage(device.getName(), 10.000);

                newSubscriber.subscribe(ExtendedTATUWrapper.getConnectionTopicResponse());
                newPublisher.publish(connectionTopic, new MqttMessage(message.getBytes()));

                this.brokerSettings = brokerSettings;

                this.timeOutCounter = new Thread(this);
                this.timeOutCounter.start();

            } catch (MqttException ex) {
                brokerSettings.disconnectAllClients();
                device.setIsUpdating(false);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        if (!this.timeOut) {
            String message = new String(mqttMessage.getPayload());
            TATUMessage tatuMessage = new TATUMessage(message);
            if (tatuMessage.isResponse() && tatuMessage.getMethod().equals(TATUMethods.CONNACK)) {
                this.timeOutCounter.interrupt();
                JSONObject json = new JSONObject(tatuMessage.getMessageContent());
                if (json.getJSONObject("BODY").getBoolean("CAN_CONNECT")) {
                    this.device.updateBrokerSettings(brokerSettings);
                } else {
                    this.brokerSettings.disconnectAllClients();
                }
            }
        } else {
            this.device.setIsUpdating(false);
            this.brokerSettings.disconnectAllClients();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void run() {
        try {
            this.timeOut = false;
            Thread.sleep(10000L);
            this.timeOut = true;
            if (this.device.isUpdating()) {
                this.device.setIsUpdating(false);
                this.brokerSettings.disconnectAllClients();
            }
        } catch (InterruptedException ex) {
        }
    }

}
