package com.device.fot.virtual.app;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.json.JSONArray;

import com.device.fot.virtual.controller.BrokerUpdateCallback;
import com.device.fot.virtual.controller.LatencyLogController;
import com.device.fot.virtual.controller.MessageLogController;
import com.device.fot.virtual.controller.configs.DeviceConfig;
import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.BrokerSettingsBuilder;
import com.device.fot.virtual.model.FoTDevice;
import com.device.fot.virtual.model.FoTSensor;
import com.device.fot.virtual.util.CLI;

import extended.tatu.wrapper.model.Sensor;
import extended.tatu.wrapper.util.SensorWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {

            DeviceConfig config = DeviceConfig.load();
            
            String deviceId = CLI.getDeviceId(args)
                    .orElse(config.getDeviceId());

            String brokerIp = CLI.getBrokerIp(args)
                    .orElse(config.getBrokerIp());

            String port = CLI.getPort(args)
                    .orElse(config.getPort());

            String password = CLI.getPassword(args)
                    .orElse(config.getPassword());

            String user = CLI.getUsername(args)
                    .orElse(config.getUsername());

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
            logger.info(brokerSettings.toString());
            if (CLI.hasParam("-ps", args)) {
                MessageLogController.getInstance().createAndUpdateFileName(deviceId + "ml.csv");
                MessageLogController.getInstance().start();
                MessageLogController.getInstance().setCanSaveData(true);
            }
            

            LatencyLogController.getInstance().createAndUpdateFileName(deviceId +"_"+config.getExpNum() +"_ll.csv");
            LatencyLogController.getInstance().start();
            LatencyLogController.getInstance().setCanSaveData(true);


            List<Sensor> sensors = readSensors("sensors.json", deviceId)
                    .stream()
                    .map(Sensor.class::cast)
                    .collect(toList());

            FoTDevice device = new FoTDevice(deviceId, sensors);
            BrokerUpdateCallback callback = new BrokerUpdateCallback(device);
            callback.startUpdateBroker(brokerSettings, Long.parseLong(timeout), true);

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Sorry, unable to find sensors.json or not create pesistence file.");
        }
    }

    private static List<FoTSensor> readSensors(String fileName, String deviceName) throws IOException {
        try (var inputStream = Main.class.getResourceAsStream(fileName); var inputReader = new InputStreamReader(inputStream); var bufferedReader = new BufferedReader(inputReader)) {

            String textFile = bufferedReader.lines().collect(joining());
            JSONArray sensorsArray = new JSONArray(textFile);
            return SensorWrapper.getAllSensors(sensorsArray)
                    .stream()
                    .map(sensor -> new FoTSensor(deviceName, sensor))
                    .collect(toList());
        }
    }
}
