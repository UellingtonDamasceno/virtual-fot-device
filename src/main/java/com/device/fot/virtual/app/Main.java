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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import org.json.JSONArray;

/**
 *
 * @author Uellington Damasceno
 */
public class Main {

    public static void main(String[] args) {
        try (InputStream input = Main.class.getResourceAsStream("broker.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties.");
                return;
            }
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

            String timeout = CLI.getTimeout(args)
                    .orElse("10000");

            BrokerSettings brokerSettings = BrokerSettingsBuilder
                    .builder()
                    .setBrokerIp(brokerIp)
                    .setPort(port)
                    .setPassword(password)
                    .setUsername(user)
                    .deviceId(deviceId)
                    .build();

            if (CLI.hasParam("-ps", args)) {
                DataController.getInstance().createAndSetDataFile(deviceId + ".csv");
                DataController.getInstance().start();
                DataController.getInstance().setCanSaveData(true);
            }

            List<Sensor> sensors = readSensors("sensors.json", deviceId)
                    .stream()
                    .map(Sensor.class::cast)
                    .collect(toList());

            FoTDevice device = new FoTDevice(deviceId, sensors);
            BrokerUpdateCallback callback = new BrokerUpdateCallback(device);
            callback.startUpdateBroker(brokerSettings, Long.parseLong(timeout), true);
            
        } catch (IOException ex) {
            System.err.println("Sorry, unable to find sensors.json or not create pesistence file.");
        }
    }

    private static List<FoTSensor> readSensors(String fileName, String deviceName) throws IOException {
        try (var inputStream = Main.class.getResourceAsStream(fileName);
                var inputReader = new InputStreamReader(inputStream);
                var bufferedReader = new BufferedReader(inputReader)) {

            String textFile = bufferedReader.lines().collect(joining());
            JSONArray sensorsArray = new JSONArray(textFile);
            return SensorWrapper.getAllSensors(sensorsArray)
                    .stream()
                    .map(sensor -> new FoTSensor(deviceName, sensor))
                    .collect(toList());
        }
    }

}
