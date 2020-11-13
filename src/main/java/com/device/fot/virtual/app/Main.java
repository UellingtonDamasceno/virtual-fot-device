package com.device.fot.virtual.app;

import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.BrokerSettingsBuilder;
import com.device.fot.virtual.model.Device;
import com.device.fot.virtual.model.Sensor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;

/**
 *
 * @author Uellington Damasceno
 */
public class Main {

    public static void main(String[] args) {
        try (InputStream input = Main.class.getResourceAsStream("broker.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties.");
            } else {
                Properties properties = new Properties();
                properties.load(input);

                BrokerSettings brokerSettings = BrokerSettingsBuilder
                        .builder()
                        .setUrl(properties.getProperty("url"))
                        .setPort(properties.getProperty("port"))
                        .setPassword(properties.getProperty("password"))
                        .setUsername(properties.getProperty("username"))
                        .setServerId(properties.getProperty("id"))
                        .build();

                Map<String, Sensor> sensors = readSensors("sensors.json", properties.getProperty("id"));
                Device device = new Device(properties.getProperty("id"), sensors);

                try {
                    device.connect(brokerSettings);
                } catch (MqttException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            System.out.println("Sorry, unable to find sensors.json.");
        }
    }

    public static Map<String, Sensor> readSensors(String fileName, String deviceName) throws IOException {
        try (InputStream inputStream = Main.class.getResourceAsStream(fileName);
                InputStreamReader inputReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputReader)) {

            String textFile = bufferedReader.lines().collect(Collectors.joining());
            JSONArray sensorsArray = new JSONArray(textFile);
            return sensorsArray
                    .toList()
                    .stream()
                    .map(Map.class::cast)
                    .map(jsonObject -> jsonObject.get("name"))
                    .map(String.class::cast)
                    .map(sensorName -> new Sensor(deviceName, sensorName))
                    .collect(Collectors.toMap(Sensor::getSensorName, Function.identity()));
        }
    }
}