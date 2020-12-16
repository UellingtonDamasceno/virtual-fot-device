package com.device.fot.virtual.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author Uellington Damasceno
 */
public class BrokerSettings {

    private final String uri;
    private final String url;
    private final String port;
    private final String deviceId;
    private final String username;
    private final String password;

    private MqttClient client;
    private int hashCode;

    protected BrokerSettings(String url, String port, String deviceId, String username, String password) {
        this.url = url;
        this.port = port;
        this.deviceId = deviceId;
        this.username = username;
        this.password = password;
        this.uri = new StringBuilder()
                .append(url)
                .append(":")
                .append(port)
                .toString();
        this.hashCode = -1;
    }

    public String getUrl() {
        return url;
    }

    public String getPort() {
        return port;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public MqttClient getClient() throws MqttException {
        return this.client == null
                ? this.client = new MqttClient(this.uri, deviceId.concat("_CLIENT"))
                : this.client;
    }

    public MqttConnectOptions getConnectionOptions() {
        MqttConnectOptions connection = new MqttConnectOptions();
        if (!this.username.isEmpty()) {
            connection.setUserName(this.username);
        }
        if (!this.password.isEmpty()) {
            connection.setPassword(this.password.toCharArray());
        }
        return connection;
    }

    public void disconnectClient() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException ex) {
                Logger.getLogger(BrokerSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public int hashCode() {
        if (this.hashCode != -1) {
            this.hashCode = this.deviceId.hashCode();
            this.hashCode += this.uri.hashCode();
            this.hashCode += this.password.hashCode();
            this.hashCode += this.username.hashCode();
            this.hashCode += this.url.hashCode();
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BrokerSettings)
                && (((BrokerSettings) obj).hashCode() == this.hashCode());
    }

    @Override
    public String toString() {
        return "BrokerSettings{"
                + "\n uri=" + uri
                + "\n url=" + url
                + "\n port=" + port
                + "\n deviceId=" + deviceId
                + "\n username=" + username
                + "\n password=" + password
                + "\n}";
    }

}
