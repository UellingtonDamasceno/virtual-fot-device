package com.device.fot.virtual.app;

import com.device.fot.virtual.controller.BrokerUpdateCallback;
import com.device.fot.virtual.controller.DataController;
import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.BrokerSettingsBuilder;
import com.device.fot.virtual.model.FoTDevice;
import com.device.fot.virtual.model.FoTSensor;
import com.device.fot.virtual.util.CLI;
import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.SensorWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
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
                        .orElse(props.getProperty("brokerIp"));

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

                DataController.getInstance().createAndSetDataFile(deviceId+".csv");
                DataController.getInstance().start();
                System.out.println(brokerSettings);
                List<Sensor> sensors = readSensors("sensors.json", deviceId)
                        .stream()
                        .map(Sensor.class::cast)
                        .collect(Collectors.toList());

                FoTDevice device = new FoTDevice(deviceId, sensors);
                BrokerUpdateCallback callback = new BrokerUpdateCallback(device);
                callback.startUpdateBroker(brokerSettings, 10.0000);
            }
        } catch (IOException ex) {
            System.out.println("Sorry, unable to find sensors.json.");
        }
    }

    private static List<FoTSensor> readSensors(String fileName, String deviceName) throws IOException {
        try (InputStream inputStream = Main.class.getResourceAsStream(fileName); InputStreamReader inputReader = new InputStreamReader(inputStream); BufferedReader bufferedReader = new BufferedReader(inputReader)) {

            String textFile = bufferedReader.lines().collect(Collectors.joining());
            JSONArray sensorsArray = new JSONArray(textFile);
            return SensorWrapper.getAllSensors(sensorsArray)
                    .stream()
                    .map(sensor -> new FoTSensor(deviceName, sensor))
                    .collect(Collectors.toList());

        }
    }

}
