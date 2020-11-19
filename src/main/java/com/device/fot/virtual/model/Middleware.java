package com.device.fot.virtual.model;

import com.device.fot.virtual.controller.BrokerUpdateController;
import com.device.fot.virtual.tatu.TATUMessage;
import com.device.fot.virtual.util.TATUWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 *
 * @author Uelligton Damasceno
 */
public class Middleware implements MqttCallback {

    private Device device;
    private BrokerUpdateController brokerUpdateController;
    
    public Middleware(Device device) {
        this.device = device;
        this.brokerUpdateController = new BrokerUpdateController(device);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        TATUMessage tatuMessage = new TATUMessage(mqttMessage.getPayload());
        MqttMessage mqttResponse = new MqttMessage();
        String jsonResponse;

        System.out.println("============================");
        System.out.println("MQTT_MESSAGE: " + new String(mqttMessage.getPayload()));
        System.out.println("TOPIC: " + topic);
        System.out.println("MY_MESSAGE: " + tatuMessage);

        switch (tatuMessage.getMethod()) {
            case FLOW:
                Sensor flowSensor = this.device.getSensorByName(tatuMessage.getTarget());
                JSONObject flow = new JSONObject(tatuMessage.getMessageContent());
                flowSensor.startFlow(flow.getInt("collect"), flow.getInt("publish"));
                break;
            case GET:
                Sensor getSensor = this.device.getSensorByName(tatuMessage.getTarget());

                jsonResponse = TATUWrapper.buildGetMessageResponse(device.getName(),
                        getSensor.getSensorName(),
                        getSensor.getCurrentValue());

                mqttResponse.setPayload(jsonResponse.getBytes());
                String publishTopic = TATUWrapper.buildTATUResponseTopic(device.getName(), getSensor.getSensorName());
                this.device.publish(publishTopic, mqttResponse);
                System.out.println("PUBLISH IN TOPIC: " + publishTopic);
                break;
            case SET:
                if (tatuMessage.getTarget().equalsIgnoreCase("brokerMqtt") && !this.device.isUpdating()) {
                    String newMessage = tatuMessage.getMessageContent();

                    JSONObject newBrokerSettingsJson = new JSONObject(newMessage);

                    BrokerSettings newBrokerSettings = BrokerSettingsBuilder.builder()
                            .setServerId(newBrokerSettingsJson.getString("id"))
                            .setUrl(newBrokerSettingsJson.getString("url"))
                            .setPort(newBrokerSettingsJson.getString("port"))
                            .setUsername(newBrokerSettingsJson.getString("user"))
                            .setPassword(newBrokerSettingsJson.getString("password"))
                            .build();

                    this.brokerUpdateController.startUpdateBroker(newBrokerSettings);
                }else{
                    System.out.println("The device is updating: "+ this.device.isUpdating());
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

    @Override
    public void connectionLost(Throwable cause) {
        Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, cause);
    }
}
