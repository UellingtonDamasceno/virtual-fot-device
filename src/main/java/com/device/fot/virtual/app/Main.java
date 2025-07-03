package com.device.fot.virtual.app;

import com.device.fot.virtual.api.LatencyLoggerApiClient;
import java.util.List;

import com.device.fot.virtual.controller.BrokerUpdateCallback;
import com.device.fot.virtual.controller.LatencyApiController;
import com.device.fot.virtual.controller.configs.DeviceConfig;
import com.device.fot.virtual.controller.configs.ExperimentConfig;
import com.device.fot.virtual.model.BrokerSettings;
import com.device.fot.virtual.model.BrokerSettingsBuilder;
import com.device.fot.virtual.model.FoTDevice;
import com.device.fot.virtual.util.CLI;
import com.device.fot.virtual.util.SensorGenerator;

import extended.tatu.wrapper.model.Sensor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

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

        String sensorNumberStr = CLI.getSensorNumber(args)
                .orElse(config.getSensorNumber());

        int numberOfSensors = Integer.parseInt(sensorNumberStr);

        logger.log(Level.INFO, "Number of sensors to generate: {0}", numberOfSensors);

        List<Sensor> sensors = SensorGenerator.generateSensors(deviceId, numberOfSensors);

        ExperimentConfig expConfig = ExperimentConfig.load();
        setupLatencyLoggerApiController(expConfig, deviceId, brokerIp);

        FoTDevice device = new FoTDevice(deviceId, sensors, expConfig);
        BrokerUpdateCallback callback = new BrokerUpdateCallback(device, expConfig);
        callback.startUpdateBroker(brokerSettings, Long.parseLong(timeout), true);

    }

    private static LatencyApiController setupLatencyLoggerApiController(ExperimentConfig config, String deviceId, String brokerIp) {
        logger.log(Level.INFO, config.toString());

        LatencyLoggerApiClient apiClient = new LatencyLoggerApiClient(config.getApiUrl());
        LatencyApiController controller = new LatencyApiController(apiClient, deviceId, brokerIp, config);
        controller.start();
        logger.log(Level.INFO, "Setup Latency Logger API finished.");
        return controller;
    }
}
