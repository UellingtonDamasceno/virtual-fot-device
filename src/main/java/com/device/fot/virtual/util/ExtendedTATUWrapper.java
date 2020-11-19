package com.device.fot.virtual.util;

import org.json.JSONObject;

/**
 *
 * @author Uellington Damasceno
 */
public final class ExtendedTATUWrapper {

    public static final String TOPIC_BASE = "dev/";
    public static final String TOPIC_RESPONSE = "/RES";

    //"dev/NEW_CONNECTIONS"
    public static String getConnectionTopic() {
        return new StringBuilder()
                .append(TOPIC_BASE)
                .append("NEW_CONNECTIONS")
                .toString();
    }

    //"dev/NEW_CONNECTIONS/RES"
    public static String getConnectionTopicResponse() {
        return getConnectionTopic().concat(TOPIC_RESPONSE);
    }

    //CONNECT VALUE BROKER {"HEADER":{"NAME":String}, "TIME_OUT":Double}
    public static String buildConnectMessage(String deviceName, Double timeOut) {
        JSONObject requestBody = new JSONObject();
        JSONObject header = new JSONObject();

        requestBody.accumulate("TIME_OUT", timeOut);
        header.accumulate("NAME", deviceName);
        requestBody.accumulate("HEADER", header);

        return new StringBuilder()
                .append("CONNECT VALUE BROKER ")
                .append(requestBody.toString())
                .toString();
    }

    //{"CODE":"POST", "METHOD":"CONNACK", "HEADER":{"NAME":String}, "BODY":{"NEW_NAME":String, "CAN_CONNECT":Boolean}}
    public static String buildConnackMessage(String deviceName, String newDeviceName, boolean sucess) {
        JSONObject header = new JSONObject();
        JSONObject body = new JSONObject();
        JSONObject response = new JSONObject();

        header.accumulate("NAME", deviceName);
        body.accumulate("NEW_NAME", newDeviceName);
        body.accumulate("CAN_CONNECT", sucess);

        response.accumulate("METHOD", "CONNECT");
        response.accumulate("CODE", "POST");
        response.accumulate("HEADER", header);
        response.accumulate("BODY", body);

        return response.toString();
    }

}
