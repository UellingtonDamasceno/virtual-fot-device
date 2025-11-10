package com.device.fot.virtual.controller;

import com.device.fot.virtual.api.LatencyLoggerApiClient;
import com.device.fot.virtual.controller.configs.ExperimentConfig;
import com.device.fot.virtual.model.LatencyRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class LatencyApiController implements Runnable {

    private static final Logger logger = Logger.getLogger(LatencyApiController.class.getName());
    private final LatencyLoggerApiClient apiClient;
    private final LinkedBlockingQueue<LatencyRecord> buffer;
    private boolean running;

    protected Thread thread;
    private final Integer bufferSize;

    public LatencyApiController(LatencyLoggerApiClient apiClient,
            String deviceId,
            String brokerIp,
            ExperimentConfig config) {

        this.apiClient = apiClient;
        this.buffer = new LinkedBlockingQueue<>();
        this.bufferSize = config.getBufferSize();

    }

    public void start() {
        if (this.thread == null || !running) {
            this.thread = Thread.ofVirtual().name("LATENCY_LOGGER_API_WRITER").start(this);
        }
    }

    public void stop() {
        running = false;
    }

    public void putLatencyRecord(LatencyRecord record) {
        this.buffer.add(record);
    }

    private void clearBuffer(List<LatencyRecord> latencyLines) {
        for (LatencyRecord record : latencyLines) {
            LatencyRecordPool.release(record);
        }
        latencyLines.clear();
    }

    @Override
    public void run() {
        running = true;
        var latencyLines = new ArrayList<LatencyRecord>(bufferSize);
        long lastSendTime = System.currentTimeMillis();
        final long SEND_TIMEOUT_MS = 10000; 

        while (running) {
            try {
                LatencyRecord head = buffer.poll(SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (head != null) {
                    latencyLines.add(head);
                    buffer.drainTo(latencyLines, bufferSize - 1);
                }

                long currentTime = System.currentTimeMillis();
                boolean bufferFull = latencyLines.size() >= bufferSize;
                boolean timeoutReached = (currentTime - lastSendTime) >= SEND_TIMEOUT_MS;

                if (!latencyLines.isEmpty() && (bufferFull || timeoutReached)) {

                    apiClient.postAllLatencies(latencyLines);

                    this.clearBuffer(latencyLines);
                    lastSendTime = currentTime;
                }
            } catch (InterruptedException ex) {
                logger.log(Level.INFO, "Thread do LatencyApiController interrompida.");
                Thread.currentThread().interrupt(); 
                this.running = false;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Erro de IO ao enviar latências", ex);
                this.running = false;
            }
        }
        this.clearBuffer(latencyLines);
    }

}
