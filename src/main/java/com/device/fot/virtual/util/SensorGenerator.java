package com.device.fot.virtual.util;

import com.device.fot.virtual.model.FoTSensor;
import extended.tatu.wrapper.model.Sensor;
import java.util.List;
import java.util.Random;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;

/**
 *
 * @author Uellington Damasceno
 */
public class SensorGenerator {

    public static List<Sensor> generateSensors(String deviceId, int jitterWindow, int numberOfSensors) {
        int jitterDelay = jitterWindow * numberOfSensors;
        return IntStream.range(0, numberOfSensors)
                .mapToObj(i -> {
                    int initialDelay = new Random().nextInt(jitterDelay);
                    FoTSensor sensor = FoTSensor.randomSensor(deviceId, i, initialDelay);
                    return sensor;
                })
                .collect(toList());
    }
}
