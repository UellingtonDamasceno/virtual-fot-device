package com.device.fot.virtual.controller;

import com.device.fot.virtual.api.LatencyLoggerApiClient;
import com.device.fot.virtual.controller.configs.ExperimentConfig;
import com.device.fot.virtual.model.LatencyRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Uellington Damasceno
 */
public class LatencyApiController implements Runnable {

    private static final Logger logger = Logger.getLogger(LatencyApiController.class.getName());
    private final LatencyLoggerApiClient apiClient;
    private final Map<Integer, Map.Entry<String, String>> messages;
    private final LinkedBlockingQueue<LatencyRecord> buffer;
    private boolean running;

    protected Thread thread;
    private final String deviceId, brokerIp;
    private final Integer bufferSize, expNum, expType, expLevel;

    public LatencyApiController(LatencyLoggerApiClient apiClient,
            String deviceId,
            String brokerIp,
            ExperimentConfig config) {

        this.apiClient = apiClient;
        this.buffer = new LinkedBlockingQueue<>();
        this.bufferSize = config.getBufferSize();
        this.deviceId = deviceId;
        this.brokerIp = brokerIp;
        this.messages = new ConcurrentHashMap<>();
        this.expNum = config.getExpNum();
        this.expType = config.getExpType();
        this.expLevel = config.getExpLevel();
    }

    public void start() {
        if (this.thread == null || !running) {
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            this.thread.setName("LATENCY_LOGGER_API_WRITER");
            this.thread.start();
        }
    }

    public void stop() {
        running = false;
    }

    public void putMessage(String sensorId, Integer messageId, String message) {
        Map.Entry entry = Map.entry(sensorId, message);
        this.messages.put(messageId, entry);
        logger.log(Level.INFO, "{0} PUT MESSAGE  - MAP COM MENSAGENS SIZE: {1}", new Object[]{messageId, this.messages.size()});
    }

    public void calculateLatancy(int messageId) {
        if (!this.messages.containsKey(messageId)) {
            logger.log(Level.INFO, "N\u00e3o tem mensagem id: {0}", messageId);
            return;
        }
        long currentTimestamp = System.currentTimeMillis();
        
        Map.Entry<String, String> entry = this.messages.remove(messageId);
        logger.log(Level.INFO, "{0} MESSAGE REMOVED - MAP COM MENSAGENS SIZE: {1}", new Object[]{messageId, this.messages.size()});

        String messageContent = entry.getValue();
        String sensorId = entry.getKey();

        long customTimestamp = new JSONObject(messageContent).getJSONObject("HEADER").getLong("TIMESTAMP");

        if (customTimestamp <= 0) {
            logger.log(Level.INFO, "The message{0} don''t have timestamp", messageContent);
            return;
        }

        long latency = currentTimestamp - customTimestamp;
        System.out.println("Latency: " + latency + " messageId: " + messageId + " SensorId: " + sensorId + " message: " + messageContent);

        LatencyRecord record = LatencyRecord.of(deviceId, sensorId, brokerIp, expNum, expType, expLevel, latency, messageContent);
        this.buffer.add(record);
    }

    @Override
    public void run() {
        running = true;
        var latencyLines = new ArrayList<LatencyRecord>(bufferSize);
        while (running) {
            try {
                if (buffer.isEmpty()) {
                    continue;
                }
                latencyLines.add(buffer.take());

                if (latencyLines.size() >= bufferSize) {
                    apiClient.postAllLatencies(latencyLines);
                    logger.log(Level.INFO, "WHEN SEND TO API - MAP COM MENSAGENS SIZE: {0}", this.messages.size());
                    latencyLines.clear();
                }
            } catch (InterruptedException | IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                this.running = false;
            }
        }
    }

}
