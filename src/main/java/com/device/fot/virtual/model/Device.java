package com.device.fot.virtual.model;

import com.device.fot.virtual.tatu.TATUMessage;
import com.device.fot.virtual.util.TATUWrapper;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class Device implements MqttCallback {

    private final String deviceName;
    private BrokerSettings brokerSettings;
    private Map<String, Sensor> sensors;

    private MqttClient publisher, subscriber;

    public Device(String deviceName, Map<String, Sensor> sensors) {
        this.deviceName = deviceName;
        this.sensors = sensors;
    }

    public void connect(BrokerSettings brokerSettings) throws MqttException {
        this.subscriber = brokerSettings.getSubscriber();
        this.publisher = brokerSettings.getPublisher();

        MqttConnectOptions options = brokerSettings.getConnectionOptions();

        this.subscriber.setCallback(this);
        this.publisher.setCallback(this);

        this.subscriber.connect(options);
        this.publisher.connect(options);

        this.subscriber.subscribe(TATUWrapper.buildTATUTopic(this.deviceName, "+"), 1);

        this.brokerSettings = brokerSettings;
        this.sensors.values().forEach(sensor -> sensor.setPublisher(publisher));
    }

    public void updateBrokerSettings(BrokerSettings newBrokerSettings) throws MqttException {
        BrokerSettings oldBrokerSettings = this.brokerSettings;
        try {
            this.disconnect();
            this.connect(newBrokerSettings);
        } catch (MqttException ex) {
            this.connect(oldBrokerSettings);
        }
    }

    public void disconnect() {
        try {
            this.publisher.disconnect();
            this.subscriber.disconnect();
        } catch (MqttException ex) {
            Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Sensor getSensorByName(String name) {
        return this.sensors.getOrDefault(name, new NullSensor(this.deviceName));
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        System.out.println("Alguém desconectou!");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        TATUMessage tatuMessage = new TATUMessage(mqttMessage.getPayload());
        MqttMessage mqttResponse = new MqttMessage();
        String jsonResponse;

        System.out.println("============================");
        System.out.println("MQTT_MESSAGE: " + new String(mqttMessage.getPayload()));
        System.out.println("TOPIC: " + topic);
//        System.out.println("MY_MESSAGE: " + tatuMessage);

        switch (tatuMessage.getMethod()) {
            case FLOW:
                Sensor flowSensor = this.getSensorByName(tatuMessage.getTarget());

                JSONObject flow = new JSONObject(tatuMessage.getMessageContent());
                flowSensor.setFlowCollect(flow.getInt("collect"));
                flowSensor.setFlowPublish(flow.getInt("publish"));

                flowSensor.startFlow();
                break;
            case GET:
                Sensor getSensor = this.getSensorByName(tatuMessage.getTarget());

                jsonResponse = TATUWrapper.buildGetMessageResponse(deviceName,
                        getSensor.getSensorName(),
                        getSensor.getCurrentValue());

                mqttResponse.setPayload(jsonResponse.getBytes());
                String publishTopic = TATUWrapper.buildTATUResponseTopic(deviceName, getSensor.getSensorName());

                this.publisher.publish(publishTopic, mqttResponse);
                System.out.println("PUBLISH IN TOPIC: " + publishTopic);
                break;
            case SET:
                String target = tatuMessage.getTarget();
                if (target.equalsIgnoreCase("brokerMqtt")) {
                    String newMessage = tatuMessage.getMessageContent().replace("\\", "");

                    JSONObject newBrokerSettingsJson = new JSONObject(newMessage);

                    BrokerSettings newBrokerSettings = BrokerSettingsBuilder.builder()
                            .setServerId(newBrokerSettingsJson.getString("id"))
                            .setUrl(newBrokerSettingsJson.getString("url"))
                            .setPort(newBrokerSettingsJson.getString("port"))
                            .setUsername(newBrokerSettingsJson.getString("user"))
                            .setPassword(newBrokerSettingsJson.getString("password"))
                            .build();

                    this.updateBrokerSettings(newBrokerSettings);
                } else {
                    System.out.println("Target false");
                }
                break;
            case EVT:
                break;
            case POST:
                break;
            case INVALID:
                System.out.println("Invalid message!");
                break;
            default:
                throw new AssertionError(tatuMessage.getMethod().name());
        }
        System.out.println("============================");

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {

    }

}
