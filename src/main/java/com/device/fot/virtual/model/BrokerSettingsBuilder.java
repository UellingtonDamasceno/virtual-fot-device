package com.device.fot.virtual.model;

/**
 *
 * @author Uellington Damasceno
 */
public class BrokerSettingsBuilder {

    private String serverId;
    private String url;
    private String port;
    private String username;
    private String password;

    public static BrokerSettingsBuilder builder() {
        return new BrokerSettingsBuilder();
    }

    public BrokerSettingsBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public BrokerSettingsBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    public BrokerSettingsBuilder setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public BrokerSettingsBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public BrokerSettingsBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public BrokerSettings build() {
        if (this.serverId == null || this.serverId.isEmpty()) {
            this.serverId = "VIRTUAL_FOT_DEVICE";
        }
        if (this.url == null || this.url.isEmpty()) {
            this.url = "tcp://localhost";
        }
        if (this.port == null || this.port.isEmpty()) {
            this.port = "1883";
        }
        if (this.username == null || this.username.isEmpty()) {
            this.username = "";
        }
        if (this.password == null || this.password.isEmpty()) {
            this.password = "";
        }
        System.out.println("BrokerSettings configured!");
        return new BrokerSettings(url, port, serverId, username, password);
    }

}
