package com.device.fot.virtual.model;

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
    private final String serverId;
    private final String username;
    private final String password;
    
    protected BrokerSettings(String url, String port, String serverId, String username, String password) {
        this.url = url;
        this.port = port;
        this.serverId = serverId;
        this.username = username;
        this.password = password;
        this.uri = new StringBuilder()
                .append(url)
                .append(":")
                .append(port)
                .toString();
    }

    public String getUrl() {
        return url;
    }

    public String getPort() {
        return port;
    }

    public String getServerId() {
        return serverId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public MqttClient getSubscriber() throws MqttException{
        return new MqttClient(this.uri, serverId.concat("_SUB"));
    }

    public MqttClient getPublisher() throws MqttException{
        return new MqttClient(this.uri, serverId.concat("_PUB"));
    }
    
    public MqttConnectOptions getConnectionOptions() {
        MqttConnectOptions connection = new MqttConnectOptions();
        if(!this.username.isEmpty()){
            connection.setUserName(this.username);
        }
        if(!this.password.isEmpty()){
            connection.setPassword(this.password.toCharArray());
        }
        return connection;
    }

}
