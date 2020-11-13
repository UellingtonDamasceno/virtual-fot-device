package com.device.fot.virtual.tatu;

import com.device.fot.virtual.util.TATUWrapper;
import java.util.Optional;
import org.json.JSONObject;

/**
 *
 * @author Uellington Damasceno
 */
public class TATUMessage {

    private String message;
    private TATUMethods method;
    private String targetName;
    private Optional<String> content;
    private boolean response;

    public TATUMessage(byte[] payload) {
        this(new String(payload));
    }

    public TATUMessage(String message) {
        this.message = message;
        this.method = TATUMethods.valueOf(message.substring(0, message.indexOf(' ')));
        this.response = TATUWrapper.isTATUResponse(message);
        this.targetName = this.getTarget(message);
        this.content = this.findMessageContent();
    }

    public TATUMethods getMethod() {
        return this.method;
    }

    public String getTarget() {
        return this.targetName;
    }

    public String getMessageContent() {
        return this.content.orElse("");
    }

    public boolean isResponse() {
        return this.response;
    }

    private String getTarget(String msg) {
        return (!this.isResponse())
                ? this.message.split(" ")[2]
                : TATUWrapper.getSensorIdByTATUAnswer(msg.substring(msg.indexOf("{")));
    }

    private Optional<String> findMessageContent() {
        String substring;
        if (this.isResponse()) {
            substring = message.substring(message.indexOf("{"));
            if (TATUWrapper.isValidTATUAnswer(substring)) {
                return Optional.ofNullable(substring);
            }
        } else if (method.equals(TATUMethods.FLOW) || method.equals(TATUMethods.SET)) {
            return Optional.ofNullable(message.substring(message.indexOf("{")));
        }
        return Optional.ofNullable("");
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.accumulate("isResponse", this.response);
        json.accumulate("message", this.message);
        json.accumulate("messageContent", this.content.get());
        json.accumulate("sensor", this.targetName);
        json.accumulate("method", this.method);
        return json.toString();
    }

}
