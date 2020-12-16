package com.device.fot.virtual.app;

import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.BrokerSettingsBuilder;
import com.device.fot.virtual.model.Device;
import com.device.fot.virtual.model.Sensor;
import com.device.fot.virtual.util.CLI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
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
                Properties props = new Properties();
                props.load(input);

                String deviceId = CLI.getDeviceId(args)
                        .orElse(UUID.randomUUID().toString());
                
                String brokerIp = CLI.getBrokerIp(args)
                        .orElse(props.getProperty("brokerId"));
                
                String port = CLI.getPort(args)
                        .orElse(props.getProperty("port"));
               
                String password = CLI.getPassword(args)
                        .orElse(props.getProperty("password"));
                
                String user = CLI.getUsername(args)
                        .orElse(props.getProperty("username"));
                
                BrokerSettings brokerSettings = BrokerSettingsBuilder
                        .builder()
                        .setBrokerIp(brokerIp)
                        .setPort(port)
                        .setPassword(password)
                        .setUsername(user)
                        .deviceId(deviceId)
                        .build();
                
                System.out.println(brokerSettings);
                
                Map<String, Sensor> sensors = readSensors("sensors.json", deviceId);
                Device device = new Device(deviceId, sensors);

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

    private static Map<String, Sensor> readSensors(String fileName, String deviceName) throws IOException {
        try ( InputStream inputStream = Main.class.getResourceAsStream(fileName);  InputStreamReader inputReader = new InputStreamReader(inputStream);  BufferedReader bufferedReader = new BufferedReader(inputReader)) {

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
