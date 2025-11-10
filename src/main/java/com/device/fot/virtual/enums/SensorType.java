package com.device.fot.virtual.enums;

/**
 *
 * @author Uellington Damasceno
 */
import java.util.List;
import java.util.Random;

public enum SensorType {
    ACCELEROMETER("Accelerometer", "AC", List.of("m/s²", "g"), -50, 50, 2),
    AIR_POLLUTANT_SENSOR("AirPollutantSensor", "AP", List.of("µg/m³", "AQI"), 0, 500, 10),
    AIR_THERMOMETER("AirThermometer", "AT", List.of("°C", "°F", "K"), -40, 60, 1),
    ALCOHOL_LEVEL_SENSOR("AlcoholLevelSensor", "AL", List.of("mg/L", "%BAC"), 0, 400, 10),
    ATMOSPHERIC_PRESSURE_SENSOR("AtmosphericPressureSensor", "PR", List.of("hPa", "bar"), 900, 1100, 2),
    BLOOD_PRESSURE_SENSOR("BloodPressureSensor", "BP", List.of("mmHg"), 60, 180, 5),
    BOARD_THERMOMETER("BoardThermometer", "BT", List.of("°C", "°F", "K"), 0, 100, 2),
    BODY_THERMOMETER("BodyThermometer", "BY", List.of("°C", "°F"), 35, 42, 1),
    CHOLESTEROL_SENSOR("CholesterolSensor", "CH", List.of("mg/dL", "mmol/L"), 100, 400, 10),
    CLOUD_COVER_SENSOR("CloudCoverSensor", "CC", List.of("%", "okta"), 0, 100, 5),
    CONDUCTIVITY_SENSOR("ConductivitySensor", "CO", List.of("µS/cm", "mS/cm"), 0, 2000, 50),
    DEW_POINT_SENSOR("DewPointSensor", "DP", List.of("°C", "°F"), -20, 30, 1),
    DISTANCE_SENSOR("DistanceSensor", "DI", List.of("m", "cm"), 0, 400, 10),
    ECG("ECG", "EC", List.of("mV", "bpm"), 40, 200, 2),
    ELECTRICAL_SENSOR("ElectricalSensor", "EL", List.of("V", "A", "W"), 0, 2500, 100),
    ENERGY_METER("EnergyMeter", "EM", List.of("kWh", "J"), 0, 5000, 100),
    FALL_DETECTOR("FallDetector", "FD", List.of("boolean"), 0, 1, 1),
    FREQUENCY_SENSOR("FrequencySensor", "FR", List.of("Hz", "kHz"), 0, 1000, 10),
    FUEL_LEVEL("FuelLevel", "FL", List.of("%", "L"), 0, 100, 5),
    GPS_SENSOR("GPSSensor", "GP", List.of("latitude", "longitude"), -90, 90, 1),
    GAS_DETECTOR("GasDetector", "GD", List.of("ppm", "%LEL"), 0, 1000, 25),
    GLUCOMETER("Glucometer", "GL", List.of("mg/dL", "mmol/L"), 70, 180, 5),
    GYROSCOPE_SENSOR("GyroscopeSensor", "GY", List.of("rad/s", "°/s"), -2000, 2000, 50),
    HEART_BEAT_SENSOR("HeartBeatSensor", "HB", List.of("bpm"), 40, 200, 1),
    HUMIDITY_SENSOR("HumiditySensor", "HU", List.of("%RH"), 0, 100, 2),
    LIGHT_SENSOR("LightSensor", "LI", List.of("lux", "lm"), 0, 10000, 100),
    MAGNETOMETER("Magnetometer", "MG", List.of("µT", "G"), -100, 100, 5),
    OCCUPANCY_DETECTOR("OccupancyDetector", "OD", List.of("boolean", "count"), 0, 1, 1),
    PH_SENSOR("PHSensor", "PH", List.of("pH"), 0, 14, 1),
    PEDOMETER("Pedometer", "PE", List.of("steps"), 0, 10000, 100),
    PRECIPITATION_SENSOR("PrecipitationSensor", "PC", List.of("mm/h", "in/h"), 0, 50, 1),
    PRESSURE_SENSOR("PressureSensor", "PS", List.of("Pa", "psi", "bar"), 0, 100, 5),
    PROXIMITY_SENSOR("ProximitySensor", "PX", List.of("cm", "boolean"), 0, 200, 5),
    PULSE_OXYMETER("PulseOxymeter", "PO", List.of("%SpO2", "bpm"), 90, 100, 1),
    SOLAR_RADIATION_SENSOR("SolarRadiationSensor", "SR", List.of("W/m²"), 0, 1400, 50),
    SOUND_SENSOR("SoundSensor", "SO", List.of("dB", "dBA"), 30, 120, 5),
    SPEED_SENSOR("SpeedSensor", "SP", List.of("km/h", "mph"), 0, 250, 10),
    SUN_POSITION_DIRECTION_SENSOR("SunPositionDirectionSensor", "SD", List.of("degrees"), 0, 360, 10),
    THERMOMETER("Thermometer", "TH", List.of("°C", "°F", "K"), -20, 100, 1),
    TOUCH_SENSOR("TouchSensor", "TC", List.of("boolean"), 0, 1, 1),
    VOLTAGE_SENSOR("VoltageSensor", "VO", List.of("V", "mV"), 0, 250, 5),
    WEIGHT_SENSOR("WeightSensor", "WE", List.of("kg", "g", "lb"), 0, 200, 1),
    WIND_DIRECTION_SENSOR("WindDirectionSensor", "WD", List.of("degrees"), 0, 360, 10),
    WIND_SPEED_SENSOR("WindSpeedSensor", "WS", List.of("m/s", "km/h"), 0, 150, 5);

    private final String name;
    private final String shortName;
    private final List<String> units;
    private final int minValue;
    private final int maxValue;
    private final int delta;

    private static final Random random = new Random();

    SensorType(String name, String shortName, List<String> units, int minValue, int maxValue, int delta) {
        this.name = name;
        this.shortName = shortName;
        this.units = units;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.delta = delta;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
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
