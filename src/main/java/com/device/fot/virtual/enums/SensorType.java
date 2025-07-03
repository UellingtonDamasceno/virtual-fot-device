package com.device.fot.virtual.enums;

/**
 *
 * @author Uellington Damasceno
 */
import java.util.List;
import java.util.Random;

public enum SensorType {
    ACCELEROMETER("Accelerometer", List.of("m/s²", "g"), -50, 50, 2),
    AIR_POLLUTANT_SENSOR("AirPollutantSensor", List.of("µg/m³", "AQI"), 0, 500, 10),
    AIR_THERMOMETER("AirThermometer", List.of("°C", "°F", "K"), -40, 60, 1),
    ALCOHOL_LEVEL_SENSOR("AlcoholLevelSensor", List.of("mg/L", "%BAC"), 0, 400, 10),
    ATMOSPHERIC_PRESSURE_SENSOR("AtmosphericPressureSensor", List.of("hPa", "bar"), 900, 1100, 2),
    BLOOD_PRESSURE_SENSOR("BloodPressureSensor", List.of("mmHg"), 60, 180, 5),
    BOARD_THERMOMETER("BoardThermometer", List.of("°C", "°F", "K"), 0, 100, 2),
    BODY_THERMOMETER("BodyThermometer", List.of("°C", "°F"), 35, 42, 1),
    CHOLESTEROL_SENSOR("CholesterolSensor", List.of("mg/dL", "mmol/L"), 100, 400, 10),
    CLOUD_COVER_SENSOR("CloudCoverSensor", List.of("%", "okta"), 0, 100, 5),
    CONDUCTIVITY_SENSOR("ConductivitySensor", List.of("µS/cm", "mS/cm"), 0, 2000, 50),
    DEW_POINT_SENSOR("DewPointSensor", List.of("°C", "°F"), -20, 30, 1),
    DISTANCE_SENSOR("DistanceSensor", List.of("m", "cm"), 0, 400, 10),
    ECG("ECG", List.of("mV", "bpm"), 40, 200, 2),
    ELECTRICAL_SENSOR("ElectricalSensor", List.of("V", "A", "W"), 0, 2500, 100),
    ENERGY_METER("EnergyMeter", List.of("kWh", "J"), 0, 5000, 100),
    FALL_DETECTOR("FallDetector", List.of("boolean"), 0, 1, 1),
    FREQUENCY_SENSOR("FrequencySensor", List.of("Hz", "kHz"), 0, 1000, 10),
    FUEL_LEVEL("FuelLevel", List.of("%", "L"), 0, 100, 5),
    GPS_SENSOR("GPSSensor", List.of("latitude", "longitude"), -90, 90, 1),
    GAS_DETECTOR("GasDetector", List.of("ppm", "%LEL"), 0, 1000, 25),
    GLUCOMETER("Glucometer", List.of("mg/dL", "mmol/L"), 70, 180, 5),
    GYROSCOPE_SENSOR("GyroscopeSensor", List.of("rad/s", "°/s"), -2000, 2000, 50),
    HEART_BEAT_SENSOR("HeartBeatSensor", List.of("bpm"), 40, 200, 1),
    HUMIDITY_SENSOR("HumiditySensor", List.of("%RH"), 0, 100, 2),
    LIGHT_SENSOR("LightSensor", List.of("lux", "lm"), 0, 10000, 100),
    MAGNETOMETER("Magnetometer", List.of("µT", "G"), -100, 100, 5),
    OCCUPANCY_DETECTOR("OccupancyDetector", List.of("boolean", "count"), 0, 1, 1),
    PH_SENSOR("PHSensor", List.of("pH"), 0, 14, 1),
    PEDOMETER("Pedometer", List.of("steps"), 0, 10000, 100),
    PRECIPITATION_SENSOR("PrecipitationSensor", List.of("mm/h", "in/h"), 0, 50, 1),
    PRESSURE_SENSOR("PressureSensor", List.of("Pa", "psi", "bar"), 0, 100, 5),
    PROXIMITY_SENSOR("ProximitySensor", List.of("cm", "boolean"), 0, 200, 5),
    PULSE_OXYMETER("PulseOxymeter", List.of("%SpO2", "bpm"), 90, 100, 1),
    SOLAR_RADIATION_SENSOR("SolarRadiationSensor", List.of("W/m²"), 0, 1400, 50),
    SOUND_SENSOR("SoundSensor", List.of("dB", "dBA"), 30, 120, 5),
    SPEED_SENSOR("SpeedSensor", List.of("km/h", "mph"), 0, 250, 10),
    SUN_POSITION_DIRECTION_SENSOR("SunPositionDirectionSensor", List.of("degrees"), 0, 360, 10),
    THERMOMETER("Thermometer", List.of("°C", "°F", "K"), -20, 100, 1),
    TOUCH_SENSOR("TouchSensor", List.of("boolean"), 0, 1, 1),
    VOLTAGE_SENSOR("VoltageSensor", List.of("V", "mV"), 0, 250, 5),
    WEIGHT_SENSOR("WeightSensor", List.of("kg", "g", "lb"), 0, 200, 1),
    WIND_DIRECTION_SENSOR("WindDirectionSensor", List.of("degrees"), 0, 360, 10),
    WIND_SPEED_SENSOR("WindSpeedSensor", List.of("m/s", "km/h"), 0, 150, 5);

    private final String name;
    private final List<String> units;
    private final int minValue;
    private final int maxValue;
    private final int delta;

    private static final Random random = new Random();

    SensorType(String name, List<String> units, int minValue, int maxValue, int delta) {
        this.name = name;
        this.units = units;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.delta = delta;
    }

    public String getName() {
        return name;
    }

    public List<String> getUnits() {
        return units;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getDelta() {
        return delta;
    }

    public static SensorType getRandomSensor() {
        SensorType[] values = values();
        return values[random.nextInt(values.length)];
    }

    public static String getRandomSensorName() {
        return SensorType.getRandomSensor().name;
    }
}
