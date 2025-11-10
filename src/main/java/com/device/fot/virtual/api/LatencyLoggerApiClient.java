package com.device.fot.virtual.api;

import com.device.fot.virtual.controller.LatencyApiController;
import com.device.fot.virtual.model.LatencyRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uellington Damasceno
 */
public class LatencyLoggerApiClient {

    private static final Logger logger = Logger.getLogger(LatencyApiController.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient client;
    private final String url;

    public LatencyLoggerApiClient(String url) {
        this.url = url;
        this.client = HttpClient.newHttpClient();
    }

    public void postAllLatencies(ArrayList<LatencyRecord> latencyLines) throws IOException, InterruptedException {
        String jsonArray = objectMapper.writeValueAsString(latencyLines);
        HttpRequest request = this.createRequest(jsonArray);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 300) {
                        logger.log(Level.WARNING, "API retornou um erro. Status: {0}, Body: {1}",
                                new Object[]{response.statusCode(), response.body()});
                    } 
                })
                .exceptionally(ex -> {
                    logger.log(Level.SEVERE, "Falha ao enviar latências para a API", ex);
                    return null; 
                });
    }

    private HttpRequest createRequest(String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(this.url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
