package com.device.fot.virtual.model;

import com.device.fot.virtual.enums.SensorType;

/**
 *
 * @author Uellington Damasceno
 */
public class NullFoTSensor extends FoTSensor {
    public static NullFoTSensor nullSensor;

    private NullFoTSensor() {
        super("NullDevice", "NullSensor", SensorType.ACCELEROMETER, 0, 0, 0);
    }

    public static synchronized NullFoTSensor getInstance() {
        return (nullSensor == null) ? nullSensor = new NullFoTSensor() : nullSensor;
    }
}
