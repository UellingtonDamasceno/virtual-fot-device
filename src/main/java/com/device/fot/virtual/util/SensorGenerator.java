package com.device.fot.virtual.util;

import com.device.fot.virtual.model.FoTSensor;
import extended.tatu.wrapper.model.Sensor;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;

/**
 *
 * @author Uellington Damasceno
 */
public class SensorGenerator {

    public static List<Sensor> generateSensors(String deviceId, int numberOfSensors) {
        return IntStream.range(0, numberOfSensors)
                .mapToObj(i -> {
                    return (Sensor) FoTSensor.randomSensor(deviceId, i);
                })
                .collect(toList());
    }
}
