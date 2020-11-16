package com.device.fot.virtual.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.device.fot.virtual.model.Device;
import com.device.fot.virtual.model.Sensor;
import com.device.fot.virtual.model.Data;

public final class TATUWrapper {

    public static String topicBase = "dev/";
    public static String topicResponse = "/RES";

    public static String buildTATUFlowInfoMessage(String sensorId, int collectSeconds, int publishSeconds) {
        return buildTATUFlow(sensorId, collectSeconds, publishSeconds, "INFO ");
    }

    public static String buildTATUFlowValueMessage(String sensorId, int collectSeconds, int publishSeconds) {
        return buildTATUFlow(sensorId, collectSeconds, publishSeconds, "VALUE ");
    }

    private static String buildTATUFlow(String sensorId, int collectSeconds, int publishSeconds, String command){
        return new StringBuilder()
                .append("FLOW ")
                .append(command)
                .append(sensorId)
                .append(" {\"collect\":")
                .append(collectSeconds)
                .append(",\"publish\":")
                .append(publishSeconds)
                .append("}").toString();
    }
    
    public static String buildTATUTopic(String deviceName, String sensorName) {
        return new StringBuilder()
                .append(topicBase)
                .append(deviceName)
                .append("/")
                .append(sensorName)
                .toString();
    }

    public static String buildTATUResponseTopic(String deviceName, String sensorName) {
        return new StringBuilder()
                .append(topicBase)
                .append(deviceName)
                .append("/")
                .append(sensorName)
                .append(topicResponse)
                .toString();
    }

    public static String buildGetMessageResponse(String deviceName, String sensorName, Object value) {
        JSONObject response = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject body = new JSONObject();
        
        header.put("NAME", deviceName);
        body.put(sensorName, value);
        response.put("METHOD", "GET");
        response.put("CODE", "POST");
        response.put("HEADER", header);
        response.put("BODY", body);
        
        return response.toString();
    }

    public static String buildFlowMessageResponse(String deviceName, String sensorName, int publish, int collect, Object[] values) {
        JSONObject response = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject body = new JSONObject();
        JSONObject flow = new JSONObject();

        flow.put("collect", collect);
        flow.put("publish", publish);

        header.put("NAME", deviceName);

        body.put(sensorName, values);
        body.put("FLOW", flow);

        response.put("METHOD", "FLOW");
        response.put("CODE", "POST");
        response.put("HEADER", header);
        response.put("BODY", body);

        return response.toString();
    }

    public static boolean isTATUResponse(String message) {
        String[] splitedMessage = message.split(" ");
        return splitedMessage.length >= 2
                && splitedMessage[1].equals("VALUE")
                && splitedMessage[2].equals("RESPONSE");
    }

    public static boolean isValidTATUMessage(String message) {
        return !(message.isBlank() || message.length() <= 10);
    }

    //{"CODE":"POST","METHOD":"FLOW","HEADER":{"NAME":"ufbaino04"},"BODY":{"temperatureSensor":["36","26"],"FLOW":{"publish":10000,"collect":5000}}}
    public static boolean isValidTATUAnswer(String answer) {
        try {
            JSONObject json = new JSONObject(answer);
            return ((json.get("CODE").toString().contentEquals("POST"))
                    && json.getJSONObject("BODY") != null);
        } catch (org.json.JSONException e) {
            return false;
        }
    }

    public static String getDeviceIdByTATUAnswer(String answer) {
        JSONObject json = new JSONObject(answer);
        return json.getJSONObject("HEADER").getString("NAME");
    }

    public static String getSensorIdByTATUAnswer(String answer) {
        JSONObject json = new JSONObject(answer);
        Iterator<?> keys = json.getJSONObject("BODY").keys();
        String sensorId = keys.next().toString();
        while (sensorId.contentEquals("FLOW")) {
            sensorId = keys.next().toString();
        }
        return sensorId;
    }

    public static List<Data> parseTATUAnswerToListSensorData(String answer, Device device, Sensor sensor, Date baseDate) {
        List<Data> listSensorData = new ArrayList();
        try {
            JSONObject json = new JSONObject(answer);

            JSONArray sensorValues = json
                    .getJSONObject("BODY")
                    .getJSONArray(sensor.getSensorName());

            int collectTime = json
                    .getJSONObject("BODY")
                    .getJSONObject("FLOW")
                    .getInt("collect");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(baseDate);

            for (int i = 0; i < sensorValues.length(); i++) {
                Integer valueInt = sensorValues.getInt(i);
                String value = valueInt.toString();
                Data sensorData = new Data(device, sensor, value, calendar.getTime(), calendar.getTime());
                listSensorData.add(sensorData);
                calendar.add(Calendar.MILLISECOND, collectTime);
            }
        } catch (org.json.JSONException e) {
        }
        return listSensorData;
    }

}
